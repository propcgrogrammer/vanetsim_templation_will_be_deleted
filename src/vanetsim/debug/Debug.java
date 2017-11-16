package vanetsim.debug;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Debug {


	/**
	 * /////////////////////////////////////
	 * //      instance variable
	 * /////////////////////////////////////
	 */

	/**
	 * 取得Debug物件實體
	 */
	private static final Debug INSTANCE = new Debug();

	/**
	 * 儲存的檔案名稱（名稱格式採用java.util.Date().getTime()）
	 */
	private static String FILENAME = "";

	/**
	 * 每SECTION個字元進行換行
	 */
	private static final int SECTION = 40;

	/**
	 * if ISLOGGED = true  該筆紀錄寫入檔案 反之
	 */
	public final static boolean ISLOGGED = true;

	/**
	 * if IS_SHOW_IN_CONSOLE = true  該筆紀錄顯示於console log 反之 (預設為顯示)
	 */
	public final static boolean IS_SHOW_IN_CONSOLE = true;

//	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");


	/**
	 * /////////////////////////////////////
	 * //      method
	 * /////////////////////////////////////
	 */

	public static Debug getInstance(){ return INSTANCE; }

	/**
	 * 檢測目前程式執行的位置
	 * @param class_name 類別名稱
	 * @param isWrite 是否進行檔案寫入
	 */
	public static void whereru(String class_name, boolean isWrite)
	{
		if(IS_SHOW_IN_CONSOLE)
		{
			System.out.println(".");
			System.out.println("====================================================");
			System.out.println("===       at class  :   " + class_name + "      ===");
			System.out.println("====================================================");
		}

		if(isWrite)
		{
			FileWriter fw;

			try {

				if(FILENAME.equals("") || FILENAME == "")
					FILENAME = "log_" + new java.util.Date().getTime() + ".txt";

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

	/**
	 * 獲得詳細資訊
	 * @param info 顯示的資訊內容
	 * @param isWrite 是否進行檔案寫入
	 */
	public static void detailedInfo(String info, boolean isWrite)
	{

		if (info == null || info.equals("") || info == "") return;

		int length = info.length();
		int p = (length / SECTION) + 1;
		int q = length % SECTION;

		if(IS_SHOW_IN_CONSOLE) {
			System.out.println(".");
			System.out.println("++++++++++++ (Detailed Info) +++++++++++");

			//	System.out.println("p =>" + p + " : q =>" + q);

			for (int i = 0; i < p; i++) {

				if (i == p - 1) System.out.println("> " + info.substring((i * SECTION), (i * SECTION) + q));
				else System.out.println("> " + info.substring((i * SECTION), ((i + 1) * SECTION) - 1));

			}

			System.out.println("+++++++++++++++++++++++++++++++++++++++++");
		}

		if(isWrite)
		{
			FileWriter fw;

			try {

				if(FILENAME.equals("") || FILENAME == "")
					FILENAME = "log_" + new java.util.Date().getTime() + ".txt";


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

	/**
	 * 呼叫函式的資訊
	 * @param class_name 類別名稱
	 * @param function_name 函式名稱
	 * @param isWrite 是否進行檔案寫入
	 */
	public static void callFunctionInfo(String class_name, String function_name, boolean isWrite)
	{
		if(IS_SHOW_IN_CONSOLE) {
			System.out.println(".");
			System.out.println("+--------------------- ( function info )-------------------------+");
			System.out.println("|                  class       |               function          | ");
			System.out.println("+-----------------------------------------------------------------+");
			System.out.println("|   " + class_name + "        |       " + function_name + "  |     ");
			System.out.println("+-----------------------------------------------------------------+");
		}
		if(isWrite)
		{
			FileWriter fw;

			try {

				if(FILENAME.equals("") || FILENAME == "")
					FILENAME = "log_" + new java.util.Date().getTime() + ".txt";


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

	/**
	 * 執行緒資訊
	 * @param thread 執行緒物件
	 * @param isWrite 是否進行檔案寫入
	 */
	public static void ThreadInfo(Thread thread, boolean isWrite)
	{
		if(IS_SHOW_IN_CONSOLE) {
			System.out.println(".");
			System.out.println("<<<<<<<<<<<<<<<<<<<<<  thread info >>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println("<                  class        |               name          |         state         > ");
			System.out.println("<----------------------------------------------------------------->");
			System.out.println("<   " + thread.getClass().getName() + "        |       " + thread.getName() + "               |        " + thread.getState().toString() + "  >     ");
			System.out.println("<----------------------------------------------------------------->");
		}


		if(isWrite)
		{
			FileWriter fw;

			try {

				if(FILENAME.equals("") || FILENAME == "")
					FILENAME = "log_" + new java.util.Date().getTime() + ".txt";


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

	/**
	 * 變數監看函式（除錯用），使用於單一變數
	 * @param title 變數名稱/標題
	 * @param content 變數內容
	 * @param isWrite 是否寫入檔案
	 */
	public static void debugInfo(String title, String content, boolean isWrite)
	{
		if(IS_SHOW_IN_CONSOLE) {
			System.out.println(".");
			System.out.println("+--------------------- ( variable info )--------------------------+");
			System.out.println("|                  variable       |               value          | ");
			System.out.println("+-----------------------------------------------------------------+");
			System.out.println("|   " + title + "        |       " + content + "  |     ");
			System.out.println("+-----------------------------------------------------------------+");
		}

		if(isWrite)
		{
			try {

				if(FILENAME.equals("") || FILENAME == "")
					FILENAME = "log_" + new java.util.Date().getTime() + ".txt";



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

	/**
	 * 變數監看函式（除錯用），使用於大量變數
	 * @param map 大量變數儲存於HashMap中
	 * @param isWrite 是否進行檔案寫入
	 */
	public static void debugInfo(HashMap<String,String> map, boolean isWrite)
	{


		if(isWrite) {

			try {
				FileWriter fw;

				if (FILENAME.equals("") || FILENAME == "")
					FILENAME = "log_" + new java.util.Date().getTime() + ".txt";


				fw = new FileWriter(FILENAME, true);
				fw.write(".\r\n");
				fw.write("+--------------------- ( variable info )--------------------------+\r\n");
				fw.write("|                  variable       |               value          | \r\n");
				fw.write("+-----------------------------------------------------------------+\r\n");

				for(Object key : map.keySet())
				{
					String k = key.toString();
					String v = (map.get(key)).toString();

					fw.write("|   " + k + "        |       " + v + "  |     \r\n");

				}

				fw.write("+-----------------------------------------------------------------+\r\n");
				fw.flush();
				fw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(IS_SHOW_IN_CONSOLE) {

			System.out.println(".");
			System.out.println("+--------------------- ( variable info )--------------------------+");
			System.out.println("|                  variable       |               value          | ");
			System.out.println("+-----------------------------------------------------------------+");


			for (Object key : map.keySet()) {
				String k = key.toString();
				String v = (map.get(key)).toString();


				System.out.println("|   " + k + "        |       " + v + "  |     ");
			}

			System.out.println("+-----------------------------------------------------------------+");

		}


	}

}
