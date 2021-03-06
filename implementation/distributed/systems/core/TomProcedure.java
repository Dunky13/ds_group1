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
//Step 1: Multicast the timestamped message

//Step 2: Upon receival everyone stores the message as undeliverable  (including the originator)
//Step 3: Wait for other processes to send their proposed timestamps
//Step 4: Gather the  timestamps values and figure out the MAX 
//Step 5: Multicast the MAX timestamp so that everyone can append it to the message
//Step 6: Mark the message as deliverable on each server, and pass it to the execution queue	

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import distributed.systems.core.logger.Logger;
import distributed.systems.das.GameState;
import distributed.systems.executors.ServerExecutor;

//Types of messages wrt to threads
//Originator (new) message = 1
//Proposed LC message = 2
//Final LC message = 3

public class TomProcedure
{
	private final int timeout = 1000;
	private final int INITIAL_CAPACITY = 1000;
	private Logger logger;
	private ServerClock LC;
	private ProposedTimestamps PT;
	private String localServerId;

	private ExecutorService service;
	private Thread tomSenderThread;
	private Thread tomReceiverThread;
	public ServerExecutor se;

	//1)Insertion in this queue signifies the start of the Tom process for that message.
	public static LinkedBlockingQueue<Message> processQueue;

	//2)Intermediate queue for undeliverable messages
	private static LinkedBlockingQueue<Message> unDeliverablesQueue;

	//3)Final Q in process. Messages in this Q are ready to be executed.
	public PriorityBlockingQueue<Message> executionQueue;

	//4) Queue for the thread pool that processes all incoming messages
	//Differentiation is done based on message type
	public LinkedBlockingQueue<Message> srvMsgQueue;

	//Default Constructor
	public TomProcedure(ServerExecutor inse, ServerClock inLC, LinkedBlockingQueue<Message> inRecvMsgs )
	{
		processQueue = new LinkedBlockingQueue<Message>();
		unDeliverablesQueue = new LinkedBlockingQueue<Message>();
		LCpriority lcPriority = new LCpriority();
		executionQueue = new PriorityBlockingQueue<Message>(INITIAL_CAPACITY, lcPriority);
		srvMsgQueue = inRecvMsgs; // Use it in receiveThread
		
		se=inse;
		localServerId = se.getServerPortData().getID() + "";
		logger = new Logger(Integer.parseInt(localServerId));
		LC = inLC;
		PT = new ProposedTimestamps();
		if (GameState.getAmIaLogger())logger.logText("TOM Initialized. Starting Threads");
		System.out.println("TOM Initialized. Starting Threads");
		tomSenderThread = new Thread(new SenderThread(se,processQueue, unDeliverablesQueue, executionQueue, LC, PT));
		tomSenderThread.start();
		//tomReceiverThread = new Thread(new ReceiverThread(se,executionQueue, unDeliverablesQueue, srvMsgQueue, LC, PT));
		//tomReceiverThread.start();
		//Simplify for now with just a single receiver thread
		service = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
		for (int i = 0; i < Constants.THREAD_POOL_SIZE; i++) {
			service.submit(new ReceiverThread(se,executionQueue, unDeliverablesQueue,srvMsgQueue,LC,PT));
		}	
		
	}

	//QUEUE METHODS
	/**
	 * Message submission to TOM proc is decoupled from the process itself.
	 * Fire-and-forget style.
	 * 
	 * @param msg
	 * @param LC
	 */
	public synchronized void submitMsgToTOM(Message msg)
	{
		if (GameState.getAmIaLogger())logger.logText("Message submitted to TOM");
		if (GameState.getAmIaLogger())logger.logMessage(msg);
		msg.put("LC", LC.getClockValue());
		msg.put("serverID", localServerId);
		processQueue.add(msg);
	}

	public void startTomProc()
	{
		//ST.run();
	}
//
//	public void close()
//	{
//		try
//		{
//			this.logger.close();
//		}
//		catch (InterruptedException e)
//		{
//		}
//	}

	/**
	 * Receiver thread code
	 */
	//	public void run() {
	//		try {
	//			for(;;) {
	//				service.execute();
	//			}
	//			
	//		}catch (IOException ex) {
	//			service.shutdown();
	//		}	
	//	}

	/**
	 * To be called by threads in the receiver thread pool. May not be used!
	 * 
	 * @return
	 */
	//	public Message deQueueMsgForThreadPool() {
	//		Message rtnMsg;
	//		//rtnMsg = receivedMsgQueue.remove();
	//		logger.logMessage(rtnMsg);
	//		return rtnMsg;
	//	}

	/**
	 * Returns the first available (executable) action
	 * 
	 * @return msg
	 */
	// this can be done by a separate threads who continuously pulls the head of the execution queue
	public synchronized Message getExecutableMsgFromTOM()
	{
		if (GameState.getAmIaLogger())logger.logText("Attempting to retrieve executable msg from TOM");
		System.out.println("Attempting to retrieve executable msg from TOM");
		Message msg;
		msg = executionQueue.remove(); //needs testing
		if (GameState.getAmIaLogger())logger.logMessage(msg);
		return msg;
	}

	/**
	 * Method for polling the execution queue for available actions.
	 * 
	 * @return
	 */
	public boolean isMessageAvailable()
	{
		System.out.println("isMessageAvailable called");
		if (!executionQueue.isEmpty() && (executionQueue.peek().getInt("isDeliverable")) == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Custom comparator class for insertionsort in the execution queue.
	 * Criteria is the Logical Clock value of the message.
	 * 
	 * @author vasilis
	 */
	class LCpriority implements Comparator<Message>
	{

		public int compare(Message m1, Message m2)
		{
			return Integer.compare(m1.getInt("LC"), m2.getInt("LC"));
		}
	}

}
