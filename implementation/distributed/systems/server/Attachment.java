package distributed.systems.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import distributed.systems.core.ServerAndPorts;

import java.nio.ByteBuffer;
import java.net.SocketAddress;

public class Attachment {
	public AsynchronousServerSocketChannel server;
	Thread mainThread;
	public AsynchronousSocketChannel channel;
	ByteBuffer buffer;
	SocketAddress clientAddr;
	boolean isRead;
	int id;
	public ServerAndPorts sp;
	
	public Attachment() {
		
	}
	
	public Attachment(ServerAndPorts sp) {
		this.sp = sp;
	}
	
	public String toString() {
		return "Attachment ID: " + this.id + " SP: " + this.sp.toString();
	}
}