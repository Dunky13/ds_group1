package distributed.systems.core;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import distributed.systems.core.Message;
import distributed.systems.das.GameState;

//Start the thread like this: thread = new Thread(new SenderThread);
public class SenderThread implements Runnable 
{
	private BlockingQueue<Message> processQueue;
	private BlockingQueue<Message> unDeliverablesQueue;
	private BlockingQueue<Message> executionQueue;
	private ServerClock LC;
	private ProposedTimestamps proposedTimestamps;
	private Message msg;
	
	public SenderThread(BlockingQueue<Message> processQ, BlockingQueue<Message> undeliverableQ,  BlockingQueue<Message> executionQ, ServerClock inLC, ProposedTimestamps inPt)
	{
		processQueue = processQ;
		unDeliverablesQueue = undeliverableQ;
		executionQueue = executionQ;
		LC = inLC;
		proposedTimestamps=inPt;
		msg = null;
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
			}
			int maxTimestamp = computeMax(proposedTimestamps.localClocks);// compute max and broadcast
			proposedTimestamps.reset();
		
			msg.put("maxLC", maxTimestamp);
			sendMessageToOtherServers(msg, Constants.FINAL_TIMESTAMP_MSG);
			Iterator iter = unDeliverablesQueue.iterator();
			moveLocalMsgToExecutable(msg,iter);
		}
	}
	
	
	/**
	 * Call after 'Multcasting' the Originator message
	 * @param msg
	 */
	public void moveLocalMsgToUndeliverables(Message msg) {
		msg.put("isDeliverable", 0);
		msg.put("LC", LC.getClockValue());
		unDeliverablesQueue.add(msg);
	}
	
	
	
	private void sendMessageToOtherServers(Message msg, int type) {
		msg.put("type", type); //type 1 for originator messages.
		//TO-DO: Multicast
		
	}	
	
	public void moveLocalMsgToExecutable(Message msg, Iterator it) {
		int msgId = (Integer)msg.get("id");
		int maxLC = (Integer)msg.get("MaxLC");
		for(Iterator i=it; it.hasNext();) {//untested
			Message m = (Message) i.next();
			if((Integer)m.get("id")==msgId) {
				it.remove();
				break;
			}
		}
		//Remove from undeliverables
		msg.put("LC",msg.get("MaxLC")); //Replace LC with maxLC
		msg.removeMsgKeyVal("type");
		msg.removeMsgKeyVal("MaxLC");
		msg.removeMsgKeyVal("serverID");
		msg.removeMsgKeyVal("proposedLC");
		msg.put("isDeliverable", 1);
		executionQueue.add(msg); //Insert into execution Q
	}
	
	/** Return 
	
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
	
	
	
}

