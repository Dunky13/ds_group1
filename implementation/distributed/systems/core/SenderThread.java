package distributed.systems.core;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import distributed.systems.core.Message;
import distributed.systems.core.logger.Logger;
import distributed.systems.das.GameState;
import distributed.systems.executors.ServerExecutor;

//Start the thread like this: thread = new Thread(new SenderThread);
public class SenderThread implements Runnable 
{
	private BlockingQueue<Message> processQueue;
	private BlockingQueue<Message> unDeliverablesQueue;
	private BlockingQueue<Message> executionQueue;
	private ServerClock LC;
	private ProposedTimestamps proposedTimestamps;
	private Message msg;
	public ServerExecutor se;
	private Logger logger;
	
	public SenderThread(ServerExecutor inSe, BlockingQueue<Message> processQ, BlockingQueue<Message> undeliverableQ,  BlockingQueue<Message> executionQ, ServerClock inLC, ProposedTimestamps inPt)
	{
		se = inSe;
		processQueue = processQ;
		unDeliverablesQueue = undeliverableQ;
		executionQueue = executionQ;
		LC = inLC;
		proposedTimestamps=inPt;
		msg = null;
		logger = new Logger(se.getServerPortData().getID());
		if (GameState.getAmIaLogger())logger.logText("Sender thread constructor called");
		System.out.println("Sender thread constructor called");
	}
	
	
	
	public void run( ) {
		while(GameState.getRunningState()) {
			//dequeue item from processqueue
			try {
				msg = processQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			// Send msg to other server
			//msg.put("LC", LC.getClockValue()); already done in submitMsgToTom
			sendMessageToOtherServers(msg, Constants.ORIGINAL_MSG);
		
			// move message from processQueue to Undeliverables Queue on local srv
			moveLocalMsgToUndeliverables(msg);
		
			// spin on boolean value in proposedTimestamps or ad timeout value for fault-tolerance
			while (!proposedTimestamps.receivedAllClocks) {
			// wait for all clock values
				//System.out.println("Waiting on Clock val...");
				
			}
			int maxTimestamp = computeMax(proposedTimestamps.localClocks);// compute max and broadcast
			proposedTimestamps.reset();
		
			msg.put("maxLC", maxTimestamp);
			sendMessageToOtherServers(msg, Constants.FINAL_TIMESTAMP_MSG);
			Iterator iter = unDeliverablesQueue.iterator();
			moveLocalMsgToExecutable(msg,iter);
			if (GameState.getAmIaLogger())logger.logText("moveLocalMsgToExecutable called from thread loop");
			System.out.println("moveLocalMsgToExecutable called from thread loop");
			if (GameState.getAmIaLogger())logger.logMessage(msg);
		}
	}
	
	
	/**
	 * Call after 'Multcasting' the Originator message
	 * @param msg
	 */
	public synchronized void moveLocalMsgToUndeliverables(Message msg) {
		if (GameState.getAmIaLogger())logger.logText("moveLocalMsgToUndeliverables called");
		System.out.println("moveLocalMsgToUndeliverables called");
		if (GameState.getAmIaLogger())logger.logMessage(msg);
		msg.put("isDeliverable", 0);
		msg.put("LC", LC.getClockValue());
		proposedTimestamps.setLocalClock(msg.getInt("serverID"), LC.getClockValue());
		unDeliverablesQueue.add(msg);
	}
	
	
	
	private synchronized void sendMessageToOtherServers(Message msg, int type) {
		if (GameState.getAmIaLogger())logger.logText("sendMessageToOtherServers called");
		System.out.println("sendMessageToOtherServers called");
		msg.put("type", type); //type 1 for originator messages.
		if (GameState.getAmIaLogger())logger.logMessage(msg);
		//TO-DO: Multicast
		se.sendMessageToMany(msg);
		System.out.println("se returned");
	}	
	
	public synchronized void moveLocalMsgToExecutable(Message msg, Iterator it) {
		if (GameState.getAmIaLogger())logger.logText("moveLocalMsgToExecutable called");
		System.out.println("moveLocalMsgToExecutable called");
		if (GameState.getAmIaLogger())logger.logMessage(msg);
		int msgId = msg.getInt("id");
		int maxLC = msg.getInt("maxLC");
		for(Iterator i=it; it.hasNext();) {//untested
			Message m = (Message) i.next();
			if(m.getInt("id")==msgId) {
				it.remove();
				break;
			}
		}
		//Remove from undeliverables
		msg.put("LC",msg.getInt("maxLC")); //Replace LC with maxLC
		msg.removeMsgKeyVal("type");
		msg.removeMsgKeyVal("maxLC");
		msg.removeMsgKeyVal("serverID");
		msg.removeMsgKeyVal("proposedLC");
		msg.put("isDeliverable", 1);
		executionQueue.add(msg); //Insert into execution Q
		System.out.println("End of moveLocalMsgToExecutable");
	}
	
	/** Return 
	
	/**
	 * Returns the maximum value from the provided input.
	 * Use for deciding which LC value to use for a given message,
	 * after voting occurs between the servers.
	 * @param tss
	 * @return
	 */
	public synchronized int computeMax(int...tss) {
		System.out.println("Compute Max called!");
		int result=0;
		
		for (int ts : tss) {
			if (ts > result)result=ts;
		}
		return result;
	}
	
	
	
}

