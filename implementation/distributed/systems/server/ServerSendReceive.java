package distributed.systems.server;

import java.io.IOException;
import java.net.InetSocketAddress;
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

  public ServerSendReceive(ServerExecutor serverExecutor) {
    this.serverExecutor = serverExecutor;

    this.serverAndPorts = this.serverExecutor.getServerPortData();
    final ServerSendReceive that = this;
    new Thread(new Runnable() {			
        public void run() {
          that.findServers();
        }
      }).start();

    //		new Thread(() -> {
    //			this.askForInput();
    //		}).start();

    // spawining the actual server
     
      new Thread(new Runnable() {			
          public void run() {
            that.spawnServer();
          }
        }).start();
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
              serverExecutor.serversConnected();
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

  private void spawnServer() {
    try {
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
    SocketAddress sAddr = this.serverAndPorts.getSocketAddress();
    server.bind(sAddr);
    System.out.format("\u001B[35m" + "Server is listening at %s%n" + "\u001B[0m", sAddr);
    Attachment attach2 = new Attachment();
    attach2.server = server;
    server.accept(attach2, new ConnectionHandler(serverExecutor));
    Thread.currentThread().join();
    } catch (Exception e) {
    }
  }

  // Send a message to all the others servers
  public void sendToAll(Message msg)
  {
    try
    {
      System.out.println("sendToAll");
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
	  System.out.println("sendToOne");
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

  public static void sendMoveToServer(int port, Message msg){
	try {  
    AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
    SocketAddress serverAddr = new InetSocketAddress("localhost", port);
    Future<Void> result = channel.connect(serverAddr);
    result.get();
    System.out.println("Connected");
    Attachment attach = new Attachment();
    attach.channel = channel;
    byte[] data = msg.serialize();
    attach.buffer = ByteBuffer.allocate(2048);
    attach.isRead = false;
    attach.buffer.put(data);
    attach.buffer.flip();
    ReadWriteHandlerClient readWriteHandler = new ReadWriteHandlerClient();
    attach.channel.write(attach.buffer, attach, readWriteHandler);
	}catch (Exception e) {
    	e.printStackTrace();
    }
  }
}
