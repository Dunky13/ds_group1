package distributed.systems.core.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import distributed.systems.core.Message;

public class Logger
{

	private String filename;
	private String logFolderName;
	private String fullFilePath;

	private BlockingQueue<ArrayList<String>> toPrint;
	private Thread logThread;
	private boolean logRunning;

	public Logger()
	{
		//IMPORTANT: Change the paths to the log file!
		String remove = "file:";
		filename = "DAS_Game_log.txt";
		logFolderName = getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		logFolderName += "distributed.systems/Logs"; //Change "logger" to package path
		logFolderName = logFolderName.replace(remove, "");
		fullFilePath = logFolderName + "/" + filename;
		logRunning = true;
		logThread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					File logfile = new File(fullFilePath);
					FileWriter fw = new FileWriter(logfile, true);
					BufferedWriter bw = new BufferedWriter(fw);
					ArrayList<String> texts;
					while (logRunning)
					{
						try
						{
							texts = toPrint.take();
							for (String text : texts)
							{
								bw.write(text);
								bw.newLine();
							}
							bw.flush();
						}
						catch (InterruptedException e)
						{
						}
						catch (IOException e)
						{

						}

					}
					bw.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		logThread.start();
	}

	public void close() throws InterruptedException
	{
		toPrint.notifyAll();
		logRunning = false;
		logThread.notifyAll();
		logThread.interrupt();
		logThread.join();
	}

	/**
	 * Call once, when the server is started. Could use log versioning but lets
	 * work with a single log file for simplicity.
	 */
	public void createLogFile()
	{
		try
		{
			System.out.println("Creating Dir:" + logFolderName);
			File dir = new File(logFolderName);
			dir.mkdir();
			System.out.println("Creating File:" + fullFilePath);
			File logfile = new File(fullFilePath);
			if (!logfile.exists())
			{
				logfile.createNewFile();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Use when you want to write text to the log file, like Connected to server
	 * x, etc..
	 * 
	 * @param text
	 */
	//	public void logText(String text) {
	//		
	//	}

	/**
	 * Pass any message for printing to the log file.
	 * 
	 * @param msg
	 */
	public void logMessage(Message msg)
	{
		Iterator<?> entries = msg.getIterator();
		ArrayList<String> texts = new ArrayList<String>();
		texts.add("[M] Message printing");
		while (entries.hasNext())
		{
			Map.Entry entry = (Map.Entry)entries.next();
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			texts.add("Key = " + key + ",  Value = " + value);
		}
		texts.add("[M] End of Message printing");
		toPrint.add(texts);
	}

	/**
	 * Used to log game events such as heal, attack, and move
	 * 
	 * @param unitId
	 * @param x
	 * @param y
	 * @param target_x
	 * @param target_y
	 * @param action
	 */
	public void logActionEvent(int unitId, int x, int y, int target_x, int target_y, String action)
	{
		ArrayList<String> texts = new ArrayList<String>();
		texts.add("[AE] Action Event printing");
		texts.add(
			"Unit with id = "
				+ unitId
				+ " at location ["
				+ x
				+ "],["
				+ y
				+ "] executed action: "
				+ action
				+ " on target located at ["
				+ target_x
				+ "],["
				+ target_y
				+ "].");
		texts.add("[AE] End of Action Event printing");
		toPrint.add(texts);
	}

	/**
	 * Use to log other events such as spawn disconnect, etc..
	 * 
	 * @param unidId
	 * @param x
	 * @param y
	 * @param event
	 */
	public void logOtherEvent(int unitId, int x, int y, String event)
	{
		ArrayList<String> texts = new ArrayList<String>();
		texts.add("[OE] Other Event printing");
		texts.add("Unit with id = " + unitId + " " + event + " at/from location[" + x + "],[" + y + "] ");
		texts.add("[OE] End of Other Event printing");
		toPrint.add(texts);
	}

}
