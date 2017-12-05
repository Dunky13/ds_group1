package distributed.systems.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.Future;
import distributed.systems.core.Constants;
import distributed.systems.core.Message;
import distributed.systems.core.ServerAndPorts;
import distributed.systems.executors.ServerExecutor;

public class ServerSendReceive
{
	private static ArrayList<Attachment> serverList = new ArrayList<Attachment>();
	private static ArrayList<ServerAndPorts> aliveServers = new ArrayList<ServerAndPorts>();
	private ServerExecutor serverExecutor;
	private ServerAndPorts serverAndPorts;

	public ServerSendReceive(ServerExecutor serverExecutor)
	{
		this.serverExecutor = serverExecutor;

		this.serverAndPorts = this.serverExecutor.getServerPortData();
		final ServerSendReceive that = this;
		new Thread(new Runnable()
		{

			
			public void run()
			{
				that.findServers();
			}

		}).start();

		//		new Thread(() -> {
		//			this.askForInput();
		//		}).start();

		// spawining the actual server

		try
		{
			this.spawnServer();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public void receivedMessage()
	{
		serverExecutor.receiveMessage(null);
	}

	private void findServers()
	{
		while (true)
		{
			try
			{
				for (ServerAndPorts sap : Constants.SERVER_PORT)
				{
					if (sap == this.serverAndPorts)
						continue;
					if (!aliveServers.contains(sap))
					{
						AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
						SocketAddress serverAddr = sap.getSocketAddress();
						Future<Void> result = channel.connect(serverAddr);
						result.get();
						System.out.println("Connected");
						SocketAddress clientAddr = channel.getRemoteAddress();
						System.out.format("\u001B[35m" + "Accepted a  connection from  %s%n" + "\u001B[0m", clientAddr);
						Attachment attach = new Attachment();
						attach.channel = channel;
						attach.id = sap.getPort();
						System.out.println("The id: " + attach.id);
						serverList.add(attach);
						aliveServers.add(sap);
						if (aliveServers.size() == (Constants.SERVER_PORT.length - 1))
						{
							System.out.println("exiting");
							return;
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
	}

	private void spawnServer() throws IOException, InterruptedException
	{
		AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
		SocketAddress sAddr = this.serverAndPorts.getSocketAddress();
		server.bind(sAddr);
		System.out.format("\u001B[35m" + "Server is listening at %s%n" + "\u001B[0m", sAddr);
		Attachment attach2 = new Attachment();
		attach2.server = server;
		server.accept(attach2, new ConnectionHandler(serverExecutor));
		Thread.currentThread().join();
	}

	// Send a message to all the others servers
	public void sendToAll(Message msg)
	{
		try
		{
			byte[] data = msg.serialize();
			for (Attachment attach : serverList)
			{
				attach.buffer = ByteBuffer.allocate(2048);
				attach.isRead = false;
				attach.buffer.put(data);
				attach.buffer.flip();
				ReadWriteHandlerClient readWriteHandler = new ReadWriteHandlerClient();
				attach.channel.write(attach.buffer, attach, readWriteHandler);
			}
		}
		catch (IOException e)
		{
		}
	}

	// Send a message to a specific server identified with a port
	public void sendToOne(ServerAndPorts sp, Message msg)
	{
		for (Attachment attach : serverList)
		{
			if (attach.id == sp.getPort())
			{
				try
				{
					byte[] data = msg.serialize();
					attach.buffer = ByteBuffer.allocate(2048);
					attach.isRead = false;
					attach.buffer.put(data);
					attach.buffer.flip();
					ReadWriteHandlerClient readWriteHandler = new ReadWriteHandlerClient();
					attach.channel.write(attach.buffer, attach, readWriteHandler);
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	//	private static void askForInput()
	//	{
	//		while (true)
	//		{
	//			try
	//			{
	//				System.out.print("\u001B[32m" + "enter a message: " + "\u001B[0m");
	//				String msg = (new BufferedReader(new InputStreamReader(System.in))).readLine();
	//				System.out.print("\u001B[32m" + "enter 0 for send to All, port number otherwise: " + "\u001B[0m");
	//				int port = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
	//				if (port == 0)
	//				{
	//					sendToAll(msg);
	//				}
	//				else
	//				{
	//					sendToOne(msg, port);
	//				}
	//			}
	//			catch (Exception e)
	//			{
	//			}
	//		}
	//	}

	//	public static void main(String[] args) throws Exception
	//	{
	//		// arguments checking
	//		if (args.length != 1)
	//		{
	//			System.out.println("missing server port argument");
	//			System.exit(1);
	//		}
	//
	//		
	//	}
}
