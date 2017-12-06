package distributed.systems.core;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import distributed.systems.core.Message;
import distributed.systems.core.logger.Logger;
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
	private Logger logger;
	
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
		logger = new Logger(se.getServerPortData().getID());
		if (GameState.getAmIaLogger())logger.logText("Receiver thread constructor called");
		System.out.println("Receiver thread constructor called");
	}
	
	
	public void run()
	{	
		while(GameState.getRunningState()) {
			System.out.println("Receiver Looping");
			try {
				msg = receivedMsgQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			switch(msg.getInt("type"))
			{
				case Constants.ORIGINAL_MSG:
					procOriginatorMsg(msg);
					System.out.println("Received Originator msg");
					break;
				case Constants.PROPOSED_TIMESTAMP_MSG:
					System.out.println("Received pts msg");
					procProposedLcMsg(msg);
					break;
				case Constants.FINAL_TIMESTAMP_MSG:
					System.out.println("Received final msg");
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
	public synchronized void procOriginatorMsg(Message msg) {
		if (GameState.getAmIaLogger())logger.logText("procOriginatorMsg called");
		System.out.println("procOriginatorMsg called");
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
	public synchronized void sendLocalLcToOriginator(Message msg){
		if (GameState.getAmIaLogger())logger.logText("sendLocalLcToOriginator called");
		System.out.println("sendLocalLcToOriginator called");
		//int outgoingServerId = Integer.parseInt( (String)msg.get("serverID"));
		int outgoingServerId = msg.getInt("serverID");
		msg.put("serverID", se.getServerPortData().getID());
		ServerAndPorts sp = Constants.SERVER_PORT[outgoingServerId];
		System.out.println("Destination srv ID " + Constants.SERVER_PORT[outgoingServerId].toString());
		se.sendMessageToOne(sp, msg);		
	}
	
	
	
	/**
	 * Process the proposed timestamps that everyone sent (used by originator)..
	 * @param msg
	 */
	public synchronized void procProposedLcMsg(Message msg) {
		if (GameState.getAmIaLogger())logger.logText("procProposedLcMsg called");
		System.out.println("procProposedLcMsg called");
		//int serverID = Integer.parseInt( (String) msg.get("serverID"));
		//int proposedLC = Integer.parseInt( (String) msg.get("proposedLC"));
		int serverID = msg.getInt("serverID");
		int proposedLC = msg.getInt("proposedLC");
		System.out.println("procProposedLcMsg id: " + serverID + " in_LC:" +proposedLC);
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
	public synchronized void procMaxLcMsg (Message msg, Iterator it) {
		if (GameState.getAmIaLogger())logger.logText("procMaxLcMsg called");
		System.out.println("procMaxLcMsg called");
		int msgId = msg.getInt("id");
		int maxLC = msg.getInt("maxLC");
		for(Iterator i=it; it.hasNext();) {//untested
			Message m = (Message) i.next();
			if(m.getInt("id")==msgId) {
				it.remove(); //Remove from undeliverables
				break;
			}
		}
		msg.put("LC",msg.getInt("maxLC")); //Replace LC with maxLC
		msg.removeMsgKeyVal("type");
		msg.removeMsgKeyVal("maxLC");
		msg.removeMsgKeyVal("serverID");
		msg.removeMsgKeyVal("proposedLC");
		msg.put("isDeliverable", 1);
		executableQueue.add(msg); //Insert into execution Q
	}
	

	
	
	
	
}
