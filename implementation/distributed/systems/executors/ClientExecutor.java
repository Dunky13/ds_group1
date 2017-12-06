package distributed.systems.executors;

import distributed.systems.core.ClientAndPort;
import distributed.systems.core.Message;
import distributed.systems.das.units.Player;
import distributed.systems.das.units.Unit;


public class ClientExecutor extends Thread {

	private Unit unit;
	private ClientAndPort clientAndPort;

	public ClientExecutor(Unit u, ClientAndPort port) {
		this.unit = u;
		this.clientAndPort = port;
	}

	public void listenToServer() {
		// When message is received
		Message message = null;
		unit.onMessageReceived(message);
	}
	
	public int getPort() {
		return this.clientAndPort.getPort();
	}
	
	public ClientAndPort getClientPort() {
		return this.clientAndPort;
	}
	
	
	public int getUnitId() {
		return unit.getUnitID();
	}
	
}
