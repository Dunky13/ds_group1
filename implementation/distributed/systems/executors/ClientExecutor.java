package distributed.systems.executors;

import distributed.systems.core.Message;
import distributed.systems.das.units.Unit;


public class ClientExecutor extends Thread {

	private Unit unit;

	public ClientExecutor(Unit u) {
		this.unit = u;
	}
	
	public void listenToServer() {
		// When message is received
		Message message = null;
		unit.onMessageReceived(message);
	}
}
