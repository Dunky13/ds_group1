package com.thirteen.server;
import java.io.IOException;
/**
 * Implementation of MultiCast sockets for server-to-server communication.
 */
import java.net.MulticastSocket;
//import java.net.*;


public class ServerSocket {
	
	public String localAddress;
	public int totalNumberOfServers;
	public String[] serverAddressList;
	public int portNumber;
	public int ttl; //Time-to-live (cast to byte before sending)
	MulticastSocket mcs;
	
	
	
	public ServerSocket(String lAddress, int numServers, String[] SAL, int port, int in_ttl) {
		localAddress=lAddress;
		totalNumberOfServers=numServers;
		portNumber=port;
		ttl=in_ttl;
		for (int i=0; i<SAL.length; i++) {
			serverAddressList[i] = SAL[i];
		}
		try {
			mcs = new MulticastSocket(portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	public void setUpMulticastSocket() {
//		try {
//			mcs = new MulticastSocket();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	
	
	
	
	
	
}
