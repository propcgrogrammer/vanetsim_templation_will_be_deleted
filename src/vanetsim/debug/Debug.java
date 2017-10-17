package vanetsim.debug;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Debug {
	
	private static final Debug INSTANCE = new Debug();
	private static String FILENAME = "";
	private static final int SECTION = 40;
	public final static boolean ISLOGGED = false;
	
//	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
	
	public Debug(){}
	public static Debug getInstance(){ return INSTANCE; }
	public static void whereru(String class_name, boolean isWrite)
	{
		System.out.println(".");
		System.out.println("====================================================");
		System.out.println("===       at class  :   " + class_name + "      ===");
		System.out.println("====================================================");
		
		if(isWrite)
		{
			FileWriter fw;
			
			try {
				
				if(FILENAME.equals("") || FILENAME == "")
				{
					
				    FILENAME = "log_" + new java.util.Date().getTime() + ".txt";
				}
				
				fw = new FileWriter(FILENAME,true);
				fw.write(".\r\n");
				fw.write("====================================================\r\n");
				fw.write("===       at class  :   " + class_name + "      ===\r\n");
				fw.write("====================================================\r\n");
				
				fw.flush();
				fw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	public static void detailedInfo(String info, boolean isWrite)
	{
		System.out.println(".");
		System.out.println("++++++++++++ (Detailed Info) +++++++++++");
		
		if(info == null || info.equals("") || info == "")  return;
		
		int length = info.length();
		int p = (length / SECTION) + 1;
		int q = length % SECTION;
		
	//	System.out.println("p =>" + p + " : q =>" + q);
		
		for(int i = 0 ; i < p ; i++)
		{
			
			if(i == p-1) System.out.println("> " + info.substring((i*SECTION),(i*SECTION)+q));
			else System.out.println("> " + info.substring((i*SECTION), ((i+1)*SECTION)-1));

//			sb.append("+");
//			if(i == p-1)   sb.append(info.substring((i*SECTION),(i*SECTION)+q));
//			else   sb.append(info.substring((i*SECTION), ((i+1)*SECTION)-1));
//			sb.append("\n");
		}
		
		System.out.println("+++++++++++++++++++++++++++++++++++++++++");
		
		if(isWrite)
		{
			FileWriter fw;
			
			try {
				
				if(FILENAME.equals("") || FILENAME == "")
				{
					
				    FILENAME = "log_" + new java.util.Date().getTime() + ".txt";
				}
				
				fw = new FileWriter(FILENAME,true);
				fw.write(".\r\n");
				fw.write("++++++++++++ (Detailed Info) +++++++++++++++++\r\n");
				
				for(int i = 0 ; i < p ; i++)
				{	
					if(i == p-1) fw.write("> " + info.substring((i*SECTION),(i*SECTION)+q) + "\r\n");
					else fw.write("> " + info.substring((i*SECTION), ((i+1)*SECTION)-1) + "\r\n");
				}
				
				fw.write("++++++++++++++++++++++++++++++++++++++++++++++++\r\n");
				
				fw.flush();
				fw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	public static void callFunctionInfo(String class_name, String function_name, boolean isWrite) 
	{
		System.out.println(".");
		System.out.println("+--------------------- ( function info )-------------------------+");
		System.out.println("|                  class       |               function          | ");
		System.out.println("+-----------------------------------------------------------------+");
		System.out.println("|   " + class_name + "        |       " + function_name + "  |     ");
		System.out.println("+-----------------------------------------------------------------+");
		if(isWrite)
		{
			FileWriter fw;
			
			try {
				
				if(FILENAME.equals("") || FILENAME == "")
				{
					
				    FILENAME = "log_" + new java.util.Date().getTime() + ".txt";
				}
				
				fw = new FileWriter(FILENAME,true);
				fw.write(".\r\n");
				fw.write("+--------------------- ( function info )-------------------------+\r\n");
				fw.write("|                  class       |               function          |  \r\n");
				fw.write("+-----------------------------------------------------------------+\r\n");
				fw.write("|   " + class_name + "        |       " + function_name + "  |     \r\n");
				fw.write("+-----------------------------------------------------------------+\r\n");
		    
				fw.flush();
				fw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	public static void ThreadInfo(Thread thread, boolean isWrite) 
	{
		
		System.out.println(".");
		System.out.println("<<<<<<<<<<<<<<<<<<<<<  thread info >>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("<                  class        |               name          |         state         > ");
		System.out.println("<----------------------------------------------------------------->");
		System.out.println("<   " + thread.getClass().getName() + "        |       " + thread.getName() + "               |        "+ thread.getState().toString() + "  >     ");
		System.out.println("<----------------------------------------------------------------->");
		
		
		
		if(isWrite)
		{
			FileWriter fw;
			
			try {
				
				if(FILENAME.equals("") || FILENAME == "")
				{
					
				    FILENAME = "log_" + new java.util.Date().getTime() + ".txt";
				}
				
				fw = new FileWriter(FILENAME,true);
				fw.write(".\r\n");
				fw.write("<<<<<<<<<<<<<<<<<<<<<  thread info >>>>>>>>>>>>>>>>>>>>>>>>\r\n");
				fw.write("<                  class        |               name          |         state         > \r\n");
				fw.write("<-----------------------------------------------------------------> \r\n");
				fw.write("|   " + thread.getClass().getName() + "        |       " + thread.getName() +"        |        " + thread.getState().toString() +"  |     \r\n");
				fw.write("<-----------------------------------------------------------------> \r\n");
		    
				fw.flush();
				fw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	public static void debugInfo(String title, String content, boolean isWrite) 
	{
		System.out.println(".");
		System.out.println("+--------------------- ( variable info )--------------------------+");
		System.out.println("|                  variable       |               value          | ");
		System.out.println("+-----------------------------------------------------------------+");
		System.out.println("|   " + title + "        |       " + content + "  |     ");
		System.out.println("+-----------------------------------------------------------------+");
		
		if(isWrite)
		{
			try {
				
				if(FILENAME.equals("") || FILENAME == "")
				{
					
				    FILENAME = "log_" + new java.util.Date().getTime() + ".txt";
				}
				
				
				FileWriter fw;
				
				fw = new FileWriter(FILENAME,true);
				fw.write(".\r\n");
				fw.write("+--------------------- ( variable info )--------------------------+\r\n");
			    fw.write("|                  variable       |               value          | \r\n");
			    fw.write("+-----------------------------------------------------------------+\r\n");
			    fw.write("|   " + title + "        |       " + content + "  |     \r\n");
			    fw.write("+-----------------------------------------------------------------+\r\n");
			    
		        fw.flush();
			    fw.close();
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		}
		
	}
	public static void debugInfo(HashMap<String,String> map, boolean isWrite) 
	{
		
		FileWriter fw;
		try {
			
			if(FILENAME.equals("") || FILENAME == "")
			{
				Date d = new Date(1);
			    FILENAME = "log_" + new java.util.Date().getTime() + ".txt";
			}
			
			fw = new FileWriter(FILENAME,true);
			System.out.println(".");
			System.out.println("+--------------------- ( variable info )--------------------------+");
			System.out.println("|                  variable       |               value          | ");
			System.out.println("+-----------------------------------------------------------------+");
		    if(isWrite)
		    {
		    	fw.write(".\r\n");
		    	fw.write("+--------------------- ( variable info )--------------------------+\r\n");
		    	fw.write("|                  variable       |               value          | \r\n");
		    	fw.write("+-----------------------------------------------------------------+\r\n");
		    }
		    
		
		    for(Object key : map.keySet())
		    {
			  String k = key.toString();
			  String v = (map.get(key)).toString();
			
			  if(isWrite) fw.write("|   " + k + "        |       " + v + "  |     \r\n");
			
			  System.out.println("|   " + k + "        |       " + v + "  |     ");
		     }
		
		   System.out.println("+-----------------------------------------------------------------+");
		   if(isWrite)  fw.write("+-----------------------------------------------------------------+\r\n");
		   fw.flush();
		   fw.close();
		
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	

}
