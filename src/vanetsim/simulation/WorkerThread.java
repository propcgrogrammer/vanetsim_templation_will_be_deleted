package vanetsim.simulation;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import vanetsim.ErrorLog;
import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.localization.Messages;
import vanetsim.map.JunctionQueue;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.Street;
import vanetsim.scenario.Vehicle;
import vanetsim.scenario.RSU;

/**
 * This thread is meant to run parallel with multiple others to gain advantage of multiple CPUs.
 * All simulation tasks are initiated from this class!
 */
public final class WorkerThread extends Thread {
	
	/** An array holding all regions this thread is working on. */
	private final Region[] ourRegions_;
	
	/** The available time in milliseconds to render in one step. This also determines how far a car moves in one time tick. */
	private final int timePerStep_;
	
	/** The changed regions that need to be updated before doing the next step. */
	private final LinkedHashSet<Integer> changedRegions_ = new LinkedHashSet<Integer>(16);
	
	/** The <code>CyclicBarrier</code> called to schedule start of new work. */
	private CyclicBarrier barrierStart_;

	/** The <code>CyclicBarrier</code> called during the work steps. */
	private CyclicBarrier barrierDuringWork_;

	/** The <code>CyclicBarrier</code> called after performing all tasks. */
	private CyclicBarrier barrierFinish_;
	
	
	/**
	 * The main constructor for the worker thread. Don't use any other constructor inherited from the
	 * Thread class as all parameters are essential!
	 * 
	 * @param ourRegions the regions which are assigned to us.
	 * @param timePerStep the time in milliseconds for one step
	 */
	public WorkerThread(Region[] ourRegions, int timePerStep){
		
		Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
		Debug.callFunctionInfo(this.getClass().getName(), "WorkerThread(Region[] ourRegions, int timePerStep)", Debug.ISLOGGED);
		
		setName("Worker startX:" + ourRegions[0].getX() + " startY:" + + ourRegions[0].getY()); //$NON-NLS-1$ //$NON-NLS-2$
		Debug.ThreadInfo(this,Debug.ISLOGGED);
		ourRegions_ = ourRegions;
		timePerStep_ = timePerStep;
		ErrorLog.log(Messages.getString("WorkerThread.workerCreated") + ourRegions_.length + Messages.getString("WorkerThread.regions"), 1, this.getName(), "Worker constructor", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Set <code>CyclicBarriers</code> for thread synchronization.
	 * 
	 * @param barrierStart 				the barrier for starting
	 * @param barrierDuringWork	the barrier after adjusting the speed
	 * @param barrierFinish				the barrier after completing all tasks
	 */
	public void setBarriers(CyclicBarrier barrierStart, CyclicBarrier barrierDuringWork, CyclicBarrier barrierFinish){
		Debug.callFunctionInfo(this.getClass().getName(), "setBarriers(CyclicBarrier barrierStart, CyclicBarrier barrierDuringWork, CyclicBarrier barrierFinish)", Debug.ISLOGGED);
		barrierStart_ = barrierStart;
		barrierDuringWork_ = barrierDuringWork;
		barrierFinish_ = barrierFinish;
	}

	/**
	 * Adds a region to the list of changed region (so that it gets updated).
	 * 
	 * @param i	the number of the region in this thread
	 */
	public void addChangedRegion(int i){
		synchronized(changedRegions_){
			changedRegions_.add(Integer.valueOf(i));
		}
	}

	/**
	 * The main method. All simulation is initiated from here!
	 */
	public void run() {
		int i, j, length;
		int ourRegionsLength = ourRegions_.length;
		// An array copy of the vehicles instead of an ArrayList or something else is used because of three reasons:
		// 1. While iterating through all regions, a ConcurrentModificationExceptions is thrown when a vehicle gets into a new region.
		// 2. As we need the array anyways because of (1), we can use it cached for the other actions as well!
		// 3. Converting an ArrayList to an array is quite fast - it's basically a System.arraycopy()-operation which is made
		//    through a very fast system-memcpy(). The overhead for array construction is by far less than the overhead
		//    caused by working with iterators (if there are lots of vehicles)!
		Vehicle[][] vehicles = new Vehicle[ourRegionsLength][];
		Vehicle[] vehicleSubarray;	// it is better to cache lookups in the double-array.
		Vehicle vehicle;
		
		long tmpTimePassed = 999999999;
		long tmpTimePassedSaved = 99999999;
		int silentPeriodDuration = Vehicle.getTIME_OF_SILENT_PERIODS();
		int silentPeriodFrequency = Vehicle.getTIME_BETWEEN_SILENT_PERIODS();
		
		RSU[][] rsus = new RSU[ourRegionsLength][];
		RSU[] rsuSubarray;	// it is better to cache lookups in the double-array.
		RSU rsu;
		
		Iterator<Integer> changedRegionIterator;
		int tmp;

		for(i = 0; i < ourRegionsLength; ++i){
			ourRegions_[i].createBacklink(this, i);
			ourRegions_[i].calculateJunctions();	//recalculate because user might have edited map after loading
			vehicles[i] = ourRegions_[i].getVehicleArray();
			
			rsus[i] = ourRegions_[i].getRSUs();
		}

		boolean communicationEnabled = Vehicle.getCommunicationEnabled();
		boolean beaconsEnabled = Vehicle.getBeaconsEnabled();
		boolean recyclingEnabled = Vehicle.getRecyclingEnabled();

		//sleep if no barriers have been set yet
		while (barrierStart_ == null || barrierDuringWork_ == null || barrierFinish_ == null){
			try{
				sleep(50);
			} catch (Exception e){}
		}

		// the try/catch-expressions are done in a way that the least possible amount is needed while still assuring some fail-safety.
		// To debug problems or new functions, it is recommended to move the "try"s down so that they are just around the "wait()"-calls!
		while(true){
			// ================================= 
			// Step 1: Update changed regions with new vehicle arrays
			// ================================= 
			if(changedRegions_.size() > 0){
				changedRegionIterator = changedRegions_.iterator();
				while(changedRegionIterator.hasNext()){
					tmp = changedRegionIterator.next().intValue();
					vehicles[tmp] = ourRegions_[tmp].getVehicleArray();
				}
				changedRegions_.clear();
			}
			// ================================= 
			// Step 2: Wait for SimulationMaster to start
			// ================================= 		
			try{
				barrierStart_.await();
			} catch (InterruptedException e){	// master wants us to stop!
				break;
			} catch (BrokenBarrierException e){	// master wants us to stop!
				break;
			} catch (Exception e){}

			// ================================= 
			// Step 3: Adjust speed, do message cleanup and create jam messages
			// ================================= 
			try{
				//vehicles: adjustSpeed() & getCurStreet() && addVehicleQueue
				/*
				  update getCurStreet() & addVehicleQueue() on 2017/10/17
				 */
				for(i = 0; i < ourRegionsLength; ++i){
					vehicleSubarray = vehicles[i];
					length = vehicleSubarray.length;
					for(j = 0; j < length; ++j){

						Vehicle veh = vehicleSubarray[j];

						veh.adjustSpeed(timePerStep_);
						String vehicleID = veh.getHexID();
						Street street = veh.getCurStreet();
						String streetName = street.getName();

						for(Vehicle v : street.getQueue_().getVehicles())
						{
							if(!v.equals(veh))  street.getQueue_().addVehicle(veh);
						}


						System.out.println("車輛資訊 ＋ID :"+vehicleID+" || +所在街道 ："+streetName
						+ " || 車輛數 ："+ street.getQueue_().size());


						//System.out.println("道路資訊 ＋名稱 :"+streetName+" || +車輛數 ："+street.getQueue_().size());


					}
				}
				
				//rsus: cleanup old messages
				for(i = 0; i < ourRegionsLength; ++i){
					rsuSubarray = rsus[i];
					length = rsuSubarray.length;
					for(j = 0; j < length; ++j){
						rsuSubarray[j].cleanup(timePerStep_);
					}
				}
			
				// Wait for all concurrent threads to synchronize				
				barrierDuringWork_.await();
			} catch (BrokenBarrierException e){	//don't try to "repair" if barrier is broken
			} catch (Exception e){
				try{
					barrierDuringWork_.await();
				}catch (Exception e2){}
			}
			
			// ================================= 
			// Step 4: Send messages.
			// ================================= 
			if(communicationEnabled){
				try{
					//vehicles send messages
					
					for(i = 0; i < ourRegionsLength; ++i){
						vehicleSubarray = vehicles[i];
						length = vehicleSubarray.length;
						for(j = 0; j < length; ++j){
							vehicle = vehicleSubarray[j];
							if(vehicle.isActive() && vehicle.isWiFiEnabled() && vehicle.getCommunicationCountdown() < 1){
								vehicle.sendMessages();
							}
						}
					}
					
					//rsus: send messages
					for(i = 0; i < ourRegionsLength; ++i){
						rsuSubarray = rsus[i];
						length = rsuSubarray.length;
						for(j = 0; j < length; ++j){
							rsu = rsuSubarray[j];
							if(rsu.getCommunicationCountdown() < 1 && !rsu.isEncrypted_()){
								rsuSubarray[j].sendMessages();	
							}
						}
					}
					
					// Wait for all concurrent threads to synchronize


					barrierDuringWork_.await();
				} catch (BrokenBarrierException e){	//don't try to "repair" if barrier is broken
				} catch (Exception e){
					try{
						barrierDuringWork_.await();
					}catch (Exception e2){}
				}
			}			

			// ================================= 
			// Step 5a:  Send beacons. Beacons are sent here so that they are not considered in the current step yet!
			//          Putting this in the movement step is not possible!
			// ================================= 
			if(communicationEnabled && beaconsEnabled){
				try{ 
				//handle silent periods
				
				if(Vehicle.isSilentPeriodsOn()){
					tmpTimePassed = Renderer.getInstance().getTimePassed();
					if(tmpTimePassed > silentPeriodFrequency && tmpTimePassed%(silentPeriodDuration + silentPeriodFrequency) < 240){
						tmpTimePassedSaved = tmpTimePassed;
						Vehicle.setSilent_period(true);
					}
					else if(Vehicle.isSilent_period() && tmpTimePassed > (tmpTimePassedSaved + silentPeriodDuration)) Vehicle.setSilent_period(false);
				}

					//vehicles: send beacons
					for(i = 0; i < ourRegionsLength; ++i){
						vehicleSubarray = vehicles[i];
						length = vehicleSubarray.length;
						for(j = 0; j < length; ++j){
							vehicle = vehicleSubarray[j];
							if(vehicle.isActive() && vehicle.isWiFiEnabled() && vehicle.getBeaconCountdown() < 1 && !vehicle.isInMixZone()){
								vehicle.sendBeacons();
							}
							if(vehicle.isActive() && vehicle.isWiFiEnabled() && vehicle.getBeaconCountdown() < 1 && vehicle.isInMixZone() && vehicle.getCurMixNode_() != null && vehicle.getCurMixNode_().getEncryptedRSU_() != null){
								vehicle.sendEncryptedBeacons();
							}
						}
					}

					//rsu: send beacons
					for(i = 0; i < ourRegionsLength; ++i){
						rsuSubarray = rsus[i];
						length = rsuSubarray.length;
						for(j = 0; j < length; ++j){
							rsu = rsuSubarray[j];
							if(rsu.getBeaconCountdown() < 1 && !rsu.isEncrypted_()) rsu.sendBeacons();
							if(rsu.getBeaconCountdown() < 1 && rsu.isEncrypted_()) rsu.sendEncryptedBeacons();
						}
					}
				
					// Wait for all concurrent threads to synchronize
					barrierDuringWork_.await();
				} catch (BrokenBarrierException e){	//don't try to "repair" if barrier is broken
				} catch (Exception e){
					try{
						barrierDuringWork_.await();
					}catch (Exception e2){}
				}
			}

			// ================================= 
			// Step 5b: Move attacker
			// ================================= 
			
		
				if(Renderer.getInstance().getAttackerVehicle() != null) Renderer.getInstance().getAttackerVehicle().moveAttacker();
				
			// ================================= 
			// Step 6: Move all vehicles one step further
			// ================================= 
			try{
				for(i = 0; i < ourRegionsLength; ++i){
					vehicleSubarray = vehicles[i];
					length = vehicleSubarray.length;
					for(j = 0; j < length; ++j){
						if(vehicleSubarray[j].isActive()) vehicleSubarray[j].move(timePerStep_);
						else if(recyclingEnabled && vehicleSubarray[j].getMayBeRecycled()) vehicleSubarray[j].reset();
					}
				}
				
				// Wait for all concurrent threads to synchronize	
				barrierFinish_.await();
			} catch (BrokenBarrierException e){	//don't try to "repair" if barrier is broken
			} catch (Exception e){
				try{
					barrierFinish_.await();	//need to wait again...
				}catch (Exception e2){}
			}		

			
			// ================================= 
			// Step 7: Check the states of all traffic lights and change if necessary
			// ================================= 
			
				Node[] tmpNodes = null;
				for(i = 0; i < ourRegions_.length; i++){
					tmpNodes = ourRegions_[i].getNodes();
					for(j = 0; j < tmpNodes.length; j++){
						if(tmpNodes[j].isHasTrafficSignal_() && tmpNodes[j].getJunction() != null && tmpNodes[j].getJunction().getNode().getTrafficLight_() != null){	
							tmpNodes[j].getJunction().getNode().getTrafficLight_().changePhases(timePerStep_);						
						}
					}
				}
			


			
			
			
		}
		// remove the backlinks from the regions so that garbage collection can really remove everything
		for(i = 0; i < ourRegionsLength; ++i){
			ourRegions_[i].createBacklink(null, -1);
		}
		
		ErrorLog.log(Messages.getString("WorkerThread.workerExited"), 1, this.getName(), "run", null); //$NON-NLS-1$ //$NON-NLS-2$
	}
}