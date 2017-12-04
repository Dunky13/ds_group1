package distributed.systems.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.ArrayList;


public class ServerSendReceive {
	//  public static volatile String spinValue  = "";
	private static final int[] ports = {8989, 8990, 8991, 8992, 8993};
	private static ArrayList<Attachment> serverList = new ArrayList<Attachment>();
	private static ArrayList<Integer> aliveServers = new ArrayList<Integer>();


	private static void findServers(int myport) {
		while(true) {
			try {
				for (int port : ports) {
					if (!aliveServers.contains(port) && port != myport) {
						AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
						SocketAddress serverAddr = new InetSocketAddress("localhost", port);
						Future<Void> result = channel.connect(serverAddr);
						result.get();
						System.out.println("Connected");
						SocketAddress clientAddr = channel.getRemoteAddress();
						System.out.format("\u001B[35m" + "sdfvfvdf Accepted a  connection from  %s%n" + "\u001B[0m", clientAddr);
						Attachment attach = new Attachment();
						attach.channel = channel;
						//attach.id = port;
						System.out.println("The id: "+attach.id);
						serverList.add(attach);
						aliveServers.add(port);
						if (aliveServers.size() == (ports.length - 1)) {
							System.out.println("exiting");
							return;
						}
					}
				}
			} catch (Exception e) {
			}      
		}    
	}


	private static void spawnClient(int port) {
		while(true) {
			try {
				System.out.println("Dio porco in the spawn client");
				AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
				String msg = "First connection message";
				SocketAddress serverAddr = new InetSocketAddress("localhost", port);
				Future<Void> result = channel.connect(serverAddr);
				result.get();
				System.out.println("Connected");
				Attachment attach = new Attachment();
				attach.channel = channel;
				attach.buffer = ByteBuffer.allocate(2048);
				attach.isRead = false;
				// attach.mainThread = Thread.currentThread();
				Charset cs = Charset.forName("UTF-8");
				// String msg = "Hello";
				byte[] data = msg.getBytes(cs);
				attach.buffer.put(data);
				attach.buffer.flip();
				ReadWriteHandlerClient readWriteHandler = new ReadWriteHandlerClient();
				channel.write(attach.buffer, attach, readWriteHandler);
				attach.mainThread = Thread.currentThread();
				attach.mainThread.join();
			} catch(Exception e) {
				//System.out.println("Server on port "+port+ " not found .. retrying");
				try {
					// sleeping before trying again
					Thread.currentThread().sleep(4000);
				} catch (InterruptedException iex) {

				}
			}
		}
	}

	private static void spawnServer(int p) throws Exception {
		AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
		String host = "localhost";
		int port = p;
		InetSocketAddress sAddr = new InetSocketAddress(host, port);
		server.bind(sAddr);
		System.out.format("\u001B[35m" + "Server is listening at %s%n" + "\u001B[0m", sAddr);
		Attachment attach2 = new Attachment();
		attach2.server = server;
		server.accept(attach2, new ConnectionHandler());
		Thread.currentThread().join();
	}

	// This method will be called with a thred assigned to it whenever a new game move is issued.
	private static void modifySharedVairable() {
		while (true) {
			System.out.print("\u001B[32m" + "enter a string in the shared variable: " + "\u001B[0m");
			try {
				String msg = (new BufferedReader(new InputStreamReader(System.in))).readLine();
				//spinValue = msg;
				for (Attachment attach : serverList) {
					attach.buffer = ByteBuffer.allocate(2048);
					attach.isRead = false;
					// attach.mainThread = Thread.currentThread();
					Charset cs = Charset.forName("UTF-8");
					// String msg = "Hello";
					byte[] data = msg.getBytes(cs);
					attach.buffer.put(data);
					attach.buffer.flip();
					ReadWriteHandlerClient readWriteHandler = new ReadWriteHandlerClient();
					attach.channel.write(attach.buffer, attach, readWriteHandler);
					//          channel.write(attach.buffer, attach, readWriteHandler);
				}
			} catch (Exception e) {
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// arguments checking
		if(args.length != 1) {
			System.out.println("missing server port argument");
			System.exit(1);
		}
		// Binding a thread to each server
		// for (int i : ports) {
		//   if (i != Integer.parseInt(args[0])) {
		//     new Thread(() -> {
		//         spawnClient(i);
		//     }).start();
		//   }
		// }
		new Thread(() -> {
			findServers(Integer.parseInt(args[0]));
		}).start();
		// ask user for share variable update value
		new Thread(() -> {
			modifySharedVairable();
		}).start();
		// spawining the actual server
		spawnServer(Integer.parseInt(args[0]));    
	}
}