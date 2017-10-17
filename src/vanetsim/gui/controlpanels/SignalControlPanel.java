package vanetsim.gui.controlpanels;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SignalControlPanel extends JPanel{
	
	private Color[] drawColor = new Color[]{Color.white,Color.white,Color.white};
//	private static final SignalControlPanel SignalControlPanelInstance = new SignalControlPanel()
//	{
//        public void paint(Graphics g)
//        {    
//            g.setColor(drawColor[0]);
//            g.fillOval(0,0,10,10);
//            g.setColor(drawColor[1]);
//            g.fillOval(20,0,10,10);
//            g.setColor(drawColor[2]);
//            g.fillOval(40,0,10,10);
//          }
//    };
    
	public SignalControlPanel()
	{
		
	}
	
	public SignalControlPanel getSignalControlPanel(int state)
	{
		setColor(state);
		
		return new SignalControlPanel()
		{
	        public void paint(Graphics g)
	        {    
	            g.setColor(drawColor[0]);
	            g.fillOval(0,0,20,20);
	            g.setColor(drawColor[1]);
	            g.fillOval(30,0,20,20);
	            g.setColor(drawColor[2]);
	            g.fillOval(60,0,20,20);
	          }
	    };
	    
	}
	
	public SignalControlPanel getSignalControlPanel()
	{
		return new SignalControlPanel()
		{
	        public void paint(Graphics g)
	        {    
	            g.setColor(drawColor[0]);
	            g.fillOval(0,0,10,10);
	            g.setColor(drawColor[1]);
	            g.fillOval(20,0,10,10);
	            g.setColor(drawColor[2]);
	            g.fillOval(40,0,10,10);
	          }
	    };
	    
	}
	
	public SignalControlPanel[] getSignalControlPanelArray(int size)
	{
		SignalControlPanel[] tmp = new SignalControlPanel[size];
		
		
		for(SignalControlPanel s : tmp)
		{
			s = new SignalControlPanel()
			{
		        public void paint(Graphics g)
		        {    
		            g.setColor(drawColor[0]);
		            g.fillOval(0,0,10,10);
		            g.setColor(drawColor[1]);
		            g.fillOval(20,0,10,10);
		            g.setColor(drawColor[2]);
		            g.fillOval(40,0,10,10);
		          }
		    };
		}
		return tmp;
	}
	
	public void setColor(int state)
	{
		if(state == 0) this.drawColor = new Color[]{Color.green,Color.white,Color.white};
		else if(state == 1) this.drawColor = new Color[]{Color.white,Color.yellow,Color.white};
		else if(state == 2) this.drawColor = new Color[]{Color.white,Color.white,Color.red};
		
	}

}
