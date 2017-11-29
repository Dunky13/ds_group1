package distributed.systems.core;

public class ProposedTimestamps {

	public volatile boolean receivedAllClocks;
	public final int[] localClocks;
	
	public ProposedTimestamps() {
		receivedAllClocks = false;
		localClocks = new int[5]; // ideally a constant 
		initLocalClocks();
	}
	
	private void initLocalClocks() {
		for (int i = 0; i < localClocks.length; i++) {
			localClocks[i] = 0;
		}
	}
	
	/**
	 * 
	 * @param serverID, value between 1-5
	 * @param LC
	 */
	public void setLocalClock(int serverID, int LC) {
		if (!receivedAllClocks) { // just an extra check
			localClocks[serverID-1] = LC;
			
			boolean flip = true;
			for (int i = 0; i < localClocks.length; i++) {
				if (localClocks[i] == 0) {
					flip = false;
					break;
				}
			}
			
			if (flip) {
				receivedAllClocks = true;
			}
		}
	}
	
	public void reset() {
		receivedAllClocks = false;
		initLocalClocks();
	}
}
