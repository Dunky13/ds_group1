package com.thirteen.server;

import com.thirteen.shared.communication.Message;
import java.io.*;
import java.util.Iterator;
import java.util.Map;


public class Logger {
	
	private String filename;
	private String logFolderName;
	private String fullFilePath;
	
	public Logger() {
		//IMPORTANT: Change the paths to the log file!
		String remove ="file:";
		filename="DAS_Game_log.txt";
		logFolderName =  getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		logFolderName += "logger/Logs"; //Change "logger" to package path
		logFolderName = logFolderName.replace(remove, "");
		fullFilePath= logFolderName + "/" + filename;
	}
	
	
	/**
	 * Call once, when the server is started. Could use log versioning
	 * but lets work with a single log file for simplicity.
	 */
	public void createLogFile() {
		try {
			//System.out.println("Creating Dir:" + logFolderName);
			File dir = new File(logFolderName);
			dir.mkdir();
			//System.out.println("Creating File:" + fullFilePath);
			File logfile = new File(fullFilePath);
			if (!logfile.exists()) {	
				logfile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}
	
	
	/**
	 * Use when you want to write text to the log file, like
	 * Connected to server x, etc..
	 * @param text 
	 */
	public void logText(String text) {
		try {
			File logfile = new File(fullFilePath);
			FileWriter fw = new FileWriter(logfile,true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(text);
			bw.newLine();
			bw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	/**
	 * Pass any message for printing to the log file.
	 * @param msg
	 */
	public void logMessage(Message msg) {
		Iterator entries = msg.getIterator();
		logText("[M] Message printing");
		while(entries.hasNext()) {
		    Map.Entry entry = (Map.Entry) entries.next();
		    String key = entry.getKey().toString();
		    String value = entry.getValue().toString();
			logText("Key = " + key + ",  Value = " + value );
		}
		logText("[M] End of Message printing");	
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
	public void logActionEvent(int unitId, int x, int y , int target_x, int target_y, String action) {
		logText("[AE] Action Event printing");
		logText("Unit with id = " + unitId + " at location [" + x + "],[" + y + "] executed action: " + action + " on target located at ["+target_x+"],["+target_y+"].");
		logText("[AE] End of Action Event printing");
		
	}
	
	/**
	 * Use to log other events such as spawn disconnect, etc..
	 * @param unidId
	 * @param x
	 * @param y
	 * @param event
	 */
	public void logOtherEvent(int unitId, int x, int y, String event) {
		logText("[OE] Other Event printing");
		logText ("Unit with id = " + unitId + " " + event + " at/from location[" + x + "],[" + y + "] ");
		logText("[OE] End of Other Event printing");
	}
	
	
}
