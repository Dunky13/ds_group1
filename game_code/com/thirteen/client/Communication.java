package com.thirteen.client;

import java.util.HashMap;
import java.util.Map;

import com.thirteen.shared.Message;
import com.thirteen.shared.core.IMessageReceivedHandler;
import com.thirteen.shared.core.LocalSocket;
import com.thirteen.shared.core.Socket;
import com.thirteen.shared.core.SynchronizedSocket;
import com.thirteen.shared.core.exception.AlreadyAssignedIDException;

public class Communication implements IMessageReceivedHandler {
	// The communication socket between this client and the board
	protected Socket clientSocket;
	
	// Map messages from their ids
	private Map<Integer, Message> messageList;
	
	
	Communication() {
		//TODO: Remove unitID
		int unitID = 0;
		
		Socket localSocket = new LocalSocket();
		
		messageList = new HashMap<Integer, Message>();
		
		// Create a new socket
		clientSocket = new SynchronizedSocket(localSocket);
	
		try {
			// Try to register the socket
			clientSocket.register("D" + unitID);
		}
		catch (AlreadyAssignedIDException e) {
			System.err.println("Socket \"D" + unitID + "\" was already registered.");
		}
	
		clientSocket.addMessageReceivedHandler(this);
		
		//TODO: Add this at appropiate location
		clientSocket.unRegister();
	}
	
	public void onMessageReceived(Message message) {
		messageList.put((Integer)message.get("id"), message);
	}
}
