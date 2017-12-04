package distributed.systems.core;

public final class Constants{
	
	public static final String HOSTNAME="localhost";
	
	public static final int NUMBER_OF_SERVERS = 5;
	
	public static int[] TOM_PORTS = {2000,2001,2002,2003,2004};
	
	public static int[] RECEIVER_PORTS = {3000,3001,3002,3003,3004};
	
	public static int[] MULTICAST_PORTS = {4000,4001,4002,4003,4004};
	
	public static int[] PUBLISH_PORTS = {5000,5001,5002,5003,5004};
	
	public static final int ORIGINAL_MSG = 1;
	public static final int PROPOSED_TIMESTAMP_MSG = 2;
	public static final int FINAL_TIMESTAMP_MSG = 3;	
}
