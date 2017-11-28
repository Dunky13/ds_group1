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


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import distributed.systems.core.*;
import distributed.systems.core.logger.Logger;


//Have this class extend Thread??
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
	public LinkedBlockingQueue <Message> executionQueue;
	
	//Default Constructor
	public TomProcedure(String serverId){
		processQueue = new LinkedBlockingQueue<Message>();
		unDeliverablesQueue = new  LinkedBlockingQueue<Message>();
		executionQueue = new LinkedBlockingQueue<Message>();
		logger = new Logger();
		localServerId = serverId;
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
		//Step 2: Everyone stores the message as undeliverable
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
	
//	
//	/**
//	 * Call on msg receive by a client, local Logical Clock should be adjusted
//	 * before passing it here.
//	 * @param msg
//	 * @param LC
//	 */
//	public void insertInProcessQ(Message msg, int LC) {
//		msg.put("LC", LC);
//		msg.put("serverID", localServerId);
//		try {
//			processQueue.put(msg);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * 
//	 */
//	public void processProcessQueueMessage(Message msg) {
//		if(!processQueue.isEmpty()) {
//			//Step 1: Multicast the timestamped message
//			//Step 2: Wait for other processes to send their proposed timestamps
//			//processQueue.remove();
//			insertInUnDeliverablesQueue(msg);
//			
//		}else {
//			//zzzzzzz...
//			System.out.println("Process Q was empty on processProcessQueueMessage call..");
//		}
//		
//	}
//	
//	/**
//	 * 
//	 * @param msg
//	 * @throws InterruptedException 
//	 */
//	public void insertInUnDeliverablesQueue(Message msg){
//		msg.put("isDeliverable", 0);
//		try {
//			unDeliverablesQueue.put(msg);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	
//	/**
//	 * 
//	 */
//	public void processUnDeliverablesQueueMessage(Message msg) {
//		if(!unDeliverablesQueue.isEmpty()) {
//			
//			
//			//Before exiting mark the msg and add it to the exec Q
//			msg.put("isDeliverable", 1);
//			insertInExcecutionQueue(msg);
//		}else {
//			//zzzzzzz...
//		}	
//	}
//	
//	/**
//	 * 
//	 * @param msg
//	 */
//	public void insertInExcecutionQueue(Message msg) {
//		if ((Integer) msg.get("isDeliverable")==1) {
//			try {
//				executionQueue.put(msg);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}else {
//			System.out.println("insertInExcecutionQueue called with unDeliverable msg");
//		}
//	}
//	
//	/**
//	 * Takes the first Message in the queue, pops it and returns it
//	 * back so that it can be applied on the Battlefield. Can return
//	 * null if called while the Q is empty.
//	 */
//	public Message processExcecutionQueueMessage(Message msg) {
//		if(!executionQueue.isEmpty()) {
//			Message tempMsg = executionQueue.remove();
//			return tempMsg;
//		}else {
//			//zzzzzz
//			System.out.println("processExcecutionQueueMessage called while Q was empty");
//			return null;
//		}
//	}
	
	
	//Helper methods
	/**
	 * Computes and returns the max timestamp between the input params
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
	
	
	
	
	
}



