package distributed.systems.core;
/**
 * UNFINISHED- Meant as a building block.
 * Class representation of the whole TOM procedure.
 * The idea is to enforce consistency and introduce fault-tolerance
 * between replicated state machines (game instances) by
 * implementing Total-ordered multicasting (Skeen's algorithm
 * +Lamport's Logical clocks).
 * Contains procedure steps from message receive by the server,
 * up to the insertion and execution of the 'action'.
 * 
 * Clock should not be instantiated in this class, only passed as value.
 * Therefore adjusting the clock should not occur here.
 * 
 * @author Vasilis Gkanasoulis 
 */


import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.util.concurrent.*;

import distributed.systems.core.*;
import distributed.systems.core.logger.Logger;

//Types of messages wrt to threads
//Originator (new) message = 1
//Proposed LC message = 2
//Final LC message = 3

public class TomProcedure{
	private final int timeout = 1000;
	Logger logger;
	private String[][] listOfServersAndPorts;
	private String localServerId;
	private int mcPort;
	private String mcIP = null;
	private MulticastSocket mcs=null;
	private InetAddress mcIpAddress=null;
	private ExecutorService service;
	private final int THREAD_POOL_SIZE = 10; //just an arbitrary number so far
	
	//1)Insertion in this queue signifies the start of the Tom process for that message.
	public static LinkedBlockingQueue <Message> processQueue;
	
	//2)Intermediate queue for undeliverable messages
	private static LinkedBlockingQueue <Message> unDeliverablesQueue;
	
	//3)Final Q in process. Messages in this Q are ready to be executed.
	public PriorityBlockingQueue <Message> executionQueue;
	
	//4) Queue for the thread pool that processes all incoming messages
	//Differentiation is done based on message type
	public LinkedBlockingQueue<Message> receivedMsgQueue;
		
	
	//Default Constructor
	public TomProcedure(String serverId, String multiCastIP, int multiCastPort){
		processQueue = new LinkedBlockingQueue<Message>();
		unDeliverablesQueue = new  LinkedBlockingQueue<Message>();
		LCpriority lcPriority = new LCpriority();
		executionQueue = new PriorityBlockingQueue<Message>(1000,lcPriority);
		service = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		logger = new Logger();
		localServerId = serverId;
		mcIP = multiCastIP;
		mcPort = multiCastPort;
		try {
			mcIpAddress = InetAddress.getByName(mcIP);
			mcs = new MulticastSocket(mcPort);
			System.out.println("Multicast member running at: " + mcs.getLocalSocketAddress() + " .Attempting to join Multicast group...");
			logger.logText("Multicast member running at: " + mcs.getLocalSocketAddress() + " .Attempting to join Multicast group...");
			mcs.joinGroup(mcIpAddress);
			
			//Test code for mcs send.
			//Move to a TOM thread after testing.
			Message tstMsg = new Message();
			tstMsg.put("TestId", 1);
			tstMsg.put("TestAction", "spawn");
			tstMsg.put("Test", "Hello socket");
			logger.logText("Will attempt to send the following message:");
			logger.logMessage(tstMsg);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(baos);
		    oos.writeObject(tstMsg);
		    oos.close();
		    //System.out.println("Message Data size: " + baos.size());
			byte[] buf = baos.toByteArray();
			DatagramPacket pckt = new DatagramPacket(buf,buf.length);
			mcs.send(pckt);
			//Test code for the receiving side.
			
			byte[] recvbuf = new byte[1000];
			DatagramPacket r_pckt = new DatagramPacket(recvbuf,recvbuf.length);
			mcs.receive(r_pckt);
			ByteArrayInputStream r_baos = new ByteArrayInputStream(recvbuf);
		    ObjectInputStream r_oos = new ObjectInputStream(r_baos);
			Message r_msg = (Message)r_oos.readObject();
			r_oos.close();
			logger.logText("Attempting to read received message:");
		    logger.logMessage(r_msg);
			//End of test code
			
		} catch (IOException e) {
			// Handle
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// thrown by r_oos.readObject();
			e.printStackTrace();
		}
		
		
	}
	
	//QUEUE METHODS
	
	/**
	 * Message submital to TOM proc is decoupled from the process itself.
	 * Fire-and-forget style.
	 * @param msg
	 * @param LC
	 */
	public synchronized void submitMsgToTOM(Message msg, int LC) {
		msg.put("LC", LC);
		msg.put("serverID", localServerId);	
		processQueue.add(msg);
		//Should return and have a separate thread take over the rest of the process
		//otherwise this blocks.
		//Step 1: Multicast the timestamped message
		//Step 2: Upon receival everyone stores the message as undeliverable  (including the originator)
		//Step 3: Wait for other processes to send their proposed timestamps
		//Step 4: Gather the  timestamps values and figure out the MAX 
		//Step 5: Multicast the MAX timestamp so that everyone can append it to the message
		//Step 6: Mark the message as deliverable on each server, and pass it to the execution queue			
	}
	
	
	
	public void startTomProc() {
		
	}
	
	
	/**
	 * To be called by threads in the receiver thread pool
	 * @return
	 */
	public Message deQueueMsgForThreadPool() {
		Message rtnMsg;
		rtnMsg = receivedMsgQueue.remove();
		logger.logMessage(rtnMsg);
		return rtnMsg;
	}
	
//	public void moveToLocalUndeliverables(Message msg, int localLC) {
//		msg.put("isDeliverable", 0);
//		msg.put("LC", localLC);
//		unDeliverablesQueue.add(msg);
//		
//	}
	
	
	/**
	 * Returns the first available (executable) action
	 * @return msg
	 */
	// this can be done by a separate threads who continuously pulls the head of the execution queue
	public Message getExecutableMsgFromTOM() {
		Message msg;
		msg = executionQueue.remove(); //needs testing
		logger.logMessage(msg);
		return msg;
	}
	
	/**
	 * Method for polling the execution queue for available actions.
	 * @return
	 */
	public boolean isMessageAvailable() {
		if(!executionQueue.isEmpty() && ((Integer)executionQueue.peek().get("isDeliverable"))==1) {
			return true;
		}else {
			return false;
		}	
	}
	
	

	
/**
 * Custom comparator class for insertionsort in the execution queue.
 * Criteria is the Logical Clock value of the message.	
 * @author vasilis
 */
class LCpriority implements Comparator<Message>{
	
	public int compare(Message m1, Message m2) {
		
		return Integer.compare((Integer)m1.get("LC"), (Integer)m2.get("LC"));
	}
	
}
	
	
		
}

