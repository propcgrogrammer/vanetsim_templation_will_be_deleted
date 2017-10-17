package vanetsim.scenario;

import java.awt.Color;
import java.text.ParseException;
import java.util.ArrayDeque;

import vanetsim.routing.WayPoint;

public final class MyVehicle extends Vehicle {

	public MyVehicle(ArrayDeque<WayPoint> destinations, int vehicleLength, int maxSpeed, int maxCommDist,
			boolean wiFiEnabled, boolean emergencyVehicle, int brakingRate, int accelerationRate, int timeDistance,
			int politeness, Color color) throws ParseException {
		super(destinations, vehicleLength, maxSpeed, maxCommDist, wiFiEnabled, emergencyVehicle, brakingRate, accelerationRate,
				timeDistance, politeness, color);
		// TODO Auto-generated constructor stub
	}
	
	

}
