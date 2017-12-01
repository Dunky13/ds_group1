package distributed.systems.core;

import java.util.concurrent.BlockingQueue;

import distributed.systems.core.Message;
import distributed.systems.das.GameState;

//Start the thread like this: thread = new Thread(new SenderThread);
public class SenderThread implements Runnable 
{
	private BlockingQueue<Message> processQueue;
	private BlockingQueue<Message> unDeliverablesQueue;
	private ServerClock LC;
	private ProposedTimestamps proposedTimestamps;
	private Message msg;
	
	public SenderThread(BlockingQueue<Message> processQ, BlockingQueue<Message> undeliverableQ, ServerClock inLC,ProposedTimestamps inPt)
	{
		processQueue = processQ;
		unDeliverablesQueue = undeliverableQ;
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
			msg.put("LC", LC.getClockValue());
			sendMessageToOtherServers(msg, Constants.ORIGINAL_MSG);
		
			// move message from processQueue to Undeliverables Queue
			moveLocalMsgToUndeliverables(msg);
		
			// spin on boolean value in proposedTimestamps or ad timeout value for fault-tolerance
			while (!proposedTimestamps.receivedAllClocks) {
			// wait for all clock values
			}
			int maxTimestamp = computeMax(proposedTimestamps.localClocks);// compute max and broadcast
			proposedTimestamps.reset();
		
			msg.put("maxLC", maxTimestamp);
			sendMessageToOtherServers(msg, Constants.FINAL_TIMESTAMP_MSG);
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
		msg.put("type", type);
		//TO-DO: Multicast	
		
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
	
	
	
}

