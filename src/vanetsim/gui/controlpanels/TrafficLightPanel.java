package vanetsim.gui.controlpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JPanel;

import vanetsim.map.TrafficLight;

public class TrafficLightPanel extends JPanel{
	
	private JPanel leftSignal = null;
	private Color[] color = new Color[]{Color.green,Color.yellow,Color.red};
	private Color[] drawColor = new Color[]{Color.white,Color.white,Color.white};
	
	private JPanel[] drawPanel = null;
	private JPanel draw = null;
	
	
	private JPanel centerInfo = null;
	
	public TrafficLightPanel()
	{
		this.setLayout(new BorderLayout());
		leftSignal = new JPanel(new GridLayout(0, 1));
		this.add(leftSignal, BorderLayout.CENTER);
		this.setVisible(true);
	}
	public void setTrafficLights(TrafficLight[] trafficLight)
	{
		int size = trafficLight.length;
		drawPanel = new JPanel[size];
		for(int i=0;i<drawPanel.length;i++)
		{
			drawPanel[i] = new JPanel()
			{
	            public void paint(Graphics g)
	            {    
	                g.setColor(drawColor[0]);
	                g.fillOval(50,60,100,100);
	                g.setColor(drawColor[1]);
	                g.fillOval(200,60,100,100);
	                g.setColor(drawColor[2]);
	                g.fillOval(350,60,100,100);
	              }
	        };
	        drawPanel[i].setLayout(null);
	        leftSignal.add(drawPanel[i]);
	        repaint();
		}
		
	}
	public void setTrafficLight(TrafficLight trafficLight)
	{
		
		draw = new JPanel()
		{
            public void paint(Graphics g)
            {    
                g.setColor(drawColor[0]);
                g.fillOval(50,60,100,100);
                g.setColor(drawColor[1]);
                g.fillOval(200,60,100,100);
                g.setColor(drawColor[2]);
                g.fillOval(350,60,100,100);
              }
        };

        draw.setLayout(null);
        leftSignal.add(draw);
        repaint();
		
	}

}
