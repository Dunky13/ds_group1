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
    this.unit.setClientExecutor(this);
  }

  public void listenToServer() {
    // When message is received
     // move to readWriteHandlerClientReceiver.java
    try {
      AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
      SocketAddress sAddr = this.clientAndPort.getSocketAddress();
      server.bind(sAddr);
      System.out.format("\u001B[33m" + "CLient receiver is listening at %s%n" + "\u001B[0m", sAddr);
      Attachment attach2 = new Attachment();
      attach2.server = server;
      server.accept(attach2, new ConnectionHandlerClient(this));
      Thread.currentThread().join();
    } catch (Exception e) {
    }
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

  public Unit getUnit() {
    return this.unit;
  }
	
}
