package distributed.systems.executors;

import distributed.systems.core.Message;
import distributed.systems.das.units.Unit;


public class ClientExecutor extends Thread {

	private Unit unit;
	private int port;

	public ClientExecutor(Unit u, int port) {
		this.unit = u;
		this.port = port;
	}
	
	public void listenToServer() {
		// When message is received
		Message message = null;
		unit.onMessageReceived(message);
	}
}
