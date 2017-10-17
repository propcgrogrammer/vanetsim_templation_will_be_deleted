package vanetsim.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


public class Statistics extends ApplicationFrame{

	
	private static boolean canShow = true;
	private Map<String, Integer> resultMap = new HashMap<String, Integer>();
	
	private JFreeChart lineChart = null;
	
//	private Map<String, Statistics> resultMap = new HashMap<String, Statistics>();
	
//	private Integer curSpeed = 0;
	
	
	public Statistics(String applicationTitle)
	{   
		super(applicationTitle);
		resultMap.clear();
	}
	
	
	private DefaultCategoryDataset createDataset(ArrayList<StatisticsData> stat )
	{
	      DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
	      
	      for(StatisticsData s : stat)   dataset.addValue( s.getSpeed() , "velocity" ,  s.getTime());
	      
//	      for(Map.Entry<String, Statistics> entry : resultMap.entrySet())
//	      {
//	    	  dataset.addValue( Integer.parseInt(entry.getValue().toString()) , "velocity" ,  entry.getKey());
//	      }
	     
	      return dataset;
	}
	public void createResultImage(int width, int height)
	{
		if(lineChart != null)
		{
			
		    try {
		    	String imgName = new java.util.Date().toString() + "_lineChart.jpeg";
		    	File lineChartFile = new File( imgName ); 
				ChartUtilities.saveChartAsPNG(lineChartFile ,lineChart, width ,height);
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
			        lineChart = ChartFactory.createLineChart(
			         chartTitle,
			         "time","velocity",
			         createDataset(stat),
			         PlotOrientation.VERTICAL,
			         true,true,true);
			         
			      ChartPanel chartPanel = new ChartPanel( lineChart );
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
