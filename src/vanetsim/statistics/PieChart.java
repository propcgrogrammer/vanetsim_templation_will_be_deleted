package vanetsim.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class PieChart  extends ApplicationFrame{

	
	private static boolean canShow = true;
	
	
	private JFreeChart pieChart = null;
	
//	private Map<String, Statistics> resultMap = new HashMap<String, Statistics>();
	
//	private Integer curSpeed = 0;
	
	
	public PieChart(String applicationTitle)
	{   
		super(applicationTitle);
		
	}
	
	
	private PieDataset  createDataset(ArrayList<StatisticsData> stat )
	{
		DefaultPieDataset   dataset = new DefaultPieDataset  ( );
		
		dataset.setValue( "Waiting Red Light Time" , new Double( stat.get(0).getWaitingTime() ) );
		dataset.setValue( "Blocking Time" , new Double( stat.get(0).getBlockingTime() ) );
		dataset.setValue( "Moving Time" , new Double( stat.get(0).getTravelTime() ) );  
		
//		dataset.setValue( "Moving Time" , new Double( stat.get(0).getTravelTime() ) );  
//		dataset.setValue( "Waiting Red Light Time" , new Double( stat.get(0).getWaitingTime() ) );
//		dataset.setValue( "Blocking Time" , new Double( stat.get(0).getBlockingTime() ) );
	     
	      
//	      for(Map.Entry<String, Statistics> entry : resultMap.entrySet())
//	      {
//	    	  dataset.addValue( Integer.parseInt(entry.getValue().toString()) , "velocity" ,  entry.getKey());
//	      }
	     
	      return dataset;
	}
	public void createResultImage(int width, int height)
	{
		if(pieChart != null)
		{
			
		    try {
		    	String imgName = new java.util.Date().toString() + "_pieChart.jpeg";
		    	File pieChartFile = new File( imgName ); 
				ChartUtilities.saveChartAsPNG(pieChartFile ,pieChart, width ,height);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    return;
		}
		return;
	}
	
	public void createResultFrame(String chartTitle, ArrayList<StatisticsData> stat)
	{
		
		if(canShow)
		{
			        pieChart = ChartFactory.createPieChart(
			         chartTitle,
			         createDataset(stat),
			         true,true,true);
			         
			      ChartPanel chartPanel = new ChartPanel( pieChart );
			      chartPanel.setPreferredSize( new java.awt.Dimension( 1260 , 967 ) );
			      setContentPane( chartPanel );
			
	
				      this.pack( );
				      RefineryUtilities.centerFrameOnScreen( this );
				      this.setVisible( true );
				      
				      canShow = false;
				      return;
		}
		return;
		
	}

	
	
}
