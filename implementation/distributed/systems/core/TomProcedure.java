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
import java.io.IOException;
import java.net.MulticastSocket;

import distributed.systems.core.*;
import distributed.systems.core.logger.Logger;

//Types of messages
//Originator (new) message = 1
//Proposed LC message = 2
//Final LC message = 3

public class TomProcedure{
	private final int timeout = 1000;
	Logger logger;
	private String[][] listOfServersAndPorts;
	private String localServerId;
	
	//1)Insertion in this queue signifies the start of the Tom process for that message.
	private static LinkedBlockingQueue <Message> processQueue;
	
	//2)Intermediate queue for undeliverable messages
	private static LinkedBlockingQueue <Message> unDeliverablesQueue;
	
	//3)Final Q in process. Messages in this Q are ready to be executed.
	public PriorityBlockingQueue <Message> executionQueue;
	
	private MulticastSocket m;
	
	//Default Constructor
	public TomProcedure(String serverId){
		processQueue = new LinkedBlockingQueue<Message>();
		unDeliverablesQueue = new  LinkedBlockingQueue<Message>();
		LCpriority lcPriority = new LCpriority();
		executionQueue = new PriorityBlockingQueue<Message>(1000,lcPriority);
		logger = new Logger();
		localServerId = serverId;
		try {
			m = new MulticastSocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//m.
		
	}
	
	//QUEUE METHODS
	
	/**
	 * The whole TOM process packed into a single method.
	 * No return type 
	 * @param msg
	 * @param LC
	 */
	public void submitMsgToTOM(Message msg, int LC) {
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
	
	
	/**
	 * Returns the first available (executable) action
	 * @return msg
	 */
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
	 * Returns the maximum value from the provided input.
	 * Use for deciding which LC value to use for a given message,
	 * after voting occurs between the servers.
	 * @param tss
	 * @return
	 */
	public int computeMax(int...tss) {
		int result=0;
		
		for (int ts : tss) {
			if (ts > result)result=ts;
		}
		return result;
	}
	
/**
 * Custom comparator for insertionsort in the execution queue	
 * @author vasilis
 */
class LCpriority implements Comparator<Message>{
	
	public int compare(Message m1, Message m2) {
		
		return Integer.compare((Integer)m1.get("LC"), (Integer)m2.get("LC"));
	}
	
}
	
	
	
	
}



