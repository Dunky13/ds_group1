package distributed.systems.core;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import distributed.systems.core.Message;
import distributed.systems.das.GameState;
import distributed.systems.executors.ServerExecutor;


public class ReceiverThread implements Runnable 
{
	private BlockingQueue<Message> executableQueue;
	private BlockingQueue<Message> unDeliverablesQueue;
	private BlockingQueue<Message> receivedMsgQueue;
	private Iterator<Message> delIt;
	private ServerClock LC;
	private Message msg;
	private ProposedTimestamps proposedTimestamps;
	private ServerExecutor se;
	
	public ReceiverThread(ServerExecutor inSe,BlockingQueue<Message> executableQ, BlockingQueue<Message> unDeliverablesQ, BlockingQueue<Message> receivedMsgQ, ServerClock inLC, ProposedTimestamps inPt)
	{
		executableQueue = executableQ;
		unDeliverablesQueue = unDeliverablesQ;
		delIt = unDeliverablesQueue.iterator();
		receivedMsgQueue = receivedMsgQ;
		LC = inLC;
		proposedTimestamps = inPt;
		se=inSe;
		msg = null;
	}
	
	
	public void run()
	{	
		while(GameState.getRunningState()) {
			try {
				msg = receivedMsgQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			switch((Integer) msg.get("type"))
			{
				case Constants.ORIGINAL_MSG:
					procOriginatorMsg(msg);
					break;
				case Constants.PROPOSED_TIMESTAMP_MSG:
					procProposedLcMsg(msg);
					break;
				case Constants.FINAL_TIMESTAMP_MSG:
					procMaxLcMsg(msg, delIt);
					break;
			}
		}
	}
	
	
	/**
	 * Process an Original message that arrived at another server first.
	 * That server appends type=1 to the original msg and 'multicasts' it to everyone else.
	 * This method is the one that receives and processes that message.
	 * @param msg
	 */
	public void procOriginatorMsg(Message msg) {
		//int origLc = (Integer)msg.get("LC"); //get originators clock
		//int localLc = LC.getClockValue(); // get local clock
		//if(origLc > localLc) LC.advanceClock(origLc); //compare and advance if needed
		int localLc = LC.getClockValue(); //get the new LC value
		msg.put("proposedLC", localLc);
		msg.put("type", Constants.PROPOSED_TIMESTAMP_MSG); //turn into proposedLC type
		unDeliverablesQueue.add(msg);
		sendLocalLcToOriginator(msg);
	}
	
	/**
	 * Use the server Id to send back the local proposed timestamp.
	 * Called by procOriginatorMsg()
	 * @param msg
	 */
	public void sendLocalLcToOriginator(Message msg){
		int outgoingServerId = (Integer) msg.get("serverID");
		ServerAndPorts sp = Constants.SERVER_PORT[outgoingServerId];
		se.sendMessageToOne(sp, msg);		
	}
	
	
	
	/**
	 * Process the proposed timestamps that everyone sent (used by originator)..
	 * @param msg
	 */
	public void procProposedLcMsg(Message msg) {
		int serverID = (Integer) msg.get("serverID");
		int proposedLC = (Integer) msg.get("proposedLC");
		proposedTimestamps.setLocalClock(serverID, proposedLC);
	}
	
	/**
	 * Process Max Logical Clock Message.
	 * Actions that take place when a MAX LC msg is sent.
	 * 1)Find msg in the Undeliverables Q using the msg id of the incoming msg.
	 * 2)Remove it from Undel Q and add the incoming msg to the execution Q
	 * after striping it of unused KeyVals.
	 * @param msg
	 * @param it
	 */
	@SuppressWarnings("rawtypes")
	public void procMaxLcMsg (Message msg, Iterator it) {
		int msgId = (Integer)msg.get("id");
		int maxLC = (Integer)msg.get("MaxLC");
		for(Iterator i=it; it.hasNext();) {//untested
			Message m = (Message) i.next();
			if((Integer)m.get("id")==msgId) {
				it.remove(); //Remove from undeliverables
				break;
			}
		}
		msg.put("LC",msg.get("MaxLC")); //Replace LC with maxLC
		msg.removeMsgKeyVal("type");
		msg.removeMsgKeyVal("MaxLC");
		msg.removeMsgKeyVal("serverID");
		msg.removeMsgKeyVal("proposedLC");
		msg.put("isDeliverable", 1);
		executableQueue.add(msg); //Insert into execution Q
	}
	

	
	
	
	
}
