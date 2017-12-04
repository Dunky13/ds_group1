package distributed.systems.executors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

import distributed.systems.core.Constants;
import distributed.systems.core.Message;
import distributed.systems.core.ServerAndPorts;
import distributed.systems.core.Socket;
import distributed.systems.das.BattleField;
import distributed.systems.server.Attachment;
import distributed.systems.server.ServerSendReceive;

public class ServerExecutor {

	private ServerAndPorts sp;
	private SocketAddress clientToServerAddr;
	private SocketAddress serverToServerAddr;
	private ServerSendReceive serverSendReceive;
	private BattleField b;

	ServerExecutor(int serverID) {
		sp = Constants.SERVER_PORT[serverID];
		b = BattleField.getBattleField();
		b.init(this);
		serverSendReceive = new ServerSendReceive(this);
	}
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("missing server port argument");
			System.exit(1);
		}
		ServerExecutor se = new ServerExecutor(Integer.parseInt(args[0]));
	}
	
	public void sendMessageToMany(Message msg) {
		serverSendReceive.sendToAll(msg);
	}

	public void sendMessageToOne(ServerAndPorts sp, Message msg) {
		serverSendReceive.sendToOne(sp, msg);
	}
	
	public void receiveMessage(Message msg) {
		b.receivedClientMessage(msg);
	}
	
	public void receiveServerMessage(Message msg) {
		b.receivedServerMessage(msg);
	}

}
