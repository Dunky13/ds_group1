import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.ArrayList;


public class ServerAndClientAsync {
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
          attach.id = port; // change this to port
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

  // Send a message to all the others servers
  private static void sendToAll(String msg) {      
      try {        
        for (Attachment attach : serverList) {
          attach.buffer = ByteBuffer.allocate(2048);
          attach.isRead = false;
          Charset cs = Charset.forName("UTF-8");
          byte[] data = msg.getBytes(cs);
          attach.buffer.put(data);
          attach.buffer.flip();
          ReadWriteHandlerClient readWriteHandler = new ReadWriteHandlerClient();
          attach.channel.write(attach.buffer, attach, readWriteHandler);
        }
      } catch (Exception e) {
      }
  }

  // Send a message to a specific server identified with a port
  private static void sendToOne(String msg, int port) {      
    try {        
      for (Attachment attach : serverList) {
        if (attach.id == port) {
          attach.buffer = ByteBuffer.allocate(2048);
          attach.isRead = false;
          Charset cs = Charset.forName("UTF-8");
          byte[] data = msg.getBytes(cs);
          attach.buffer.put(data);
          attach.buffer.flip();
          ReadWriteHandlerClient readWriteHandler = new ReadWriteHandlerClient();
          attach.channel.write(attach.buffer, attach, readWriteHandler);
        }
      }
    } catch (Exception e) {
    }
  }

  private static void askForInput() {
    while (true) {
      try {
      System.out.print("\u001B[32m" + "enter a message: " + "\u001B[0m");
      String msg = (new BufferedReader(new InputStreamReader(System.in))).readLine();
      System.out.print("\u001B[32m" + "enter 0 for send to All, port number otherwise: " + "\u001B[0m");
      int port = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
      if (port == 0) {
        sendToAll(msg);
      } else {
        sendToOne(msg, port);
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
    
    new Thread(() -> {
        findServers(Integer.parseInt(args[0]));
    }).start();

    new Thread(() -> {
        askForInput();
    }).start();
    
    
    // spawining the actual server
    spawnServer(Integer.parseInt(args[0]));    
  }
}

class ConnectionHandler implements
    CompletionHandler<AsynchronousSocketChannel, Attachment> {
  @Override
  public void completed(AsynchronousSocketChannel client, Attachment attach) {
    try {
      SocketAddress clientAddr = client.getRemoteAddress();
      System.out.format("\u001B[35m" + "Accepted a  connection from  %s%n" + "\u001B[0m", clientAddr);
      System.out.format("\u001B[35m" + "Accepted a  connection with id  %s%n" + "\u001B[0m", attach.id);
      attach.server.accept(attach, this);
      ReadWriteHandlerServer rwHandler = new ReadWriteHandlerServer();
      Attachment newAttach = new Attachment();
      newAttach.server = attach.server;
      newAttach.channel = client;
      newAttach.buffer = ByteBuffer.allocate(2048);
      newAttach.isRead = true;
      newAttach.clientAddr = clientAddr;
      client.read(newAttach.buffer, newAttach, rwHandler);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void failed(Throwable e, Attachment attach) {
    System.out.println("\u001B[35m" + "Failed to accept a  connection." + "\u001B[0m");
    e.printStackTrace();
  }
}
// Class that handles the messages with the client
class ReadWriteHandlerServer implements CompletionHandler<Integer, Attachment> {
  @Override
  public void completed(Integer result, Attachment attach) {
    if (result == -1) {
      try {
        attach.channel.close();
        System.out.format("\u001B[35m" + "Stopped listening to the client %s%n" + "\u001B[0m",
                          attach.clientAddr);
        System.out.format("\u001B[35m" + "With id %s%n" + "\u001B[0m",
                          attach.id);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      return;
    }
    if (attach.isRead) {
      // decoding the client message
      attach.buffer.flip();
      int limits = attach.buffer.limit();
      byte bytes[] = new byte[limits];
      attach.buffer.get(bytes, 0, limits);
      Charset cs = Charset.forName("UTF-8");
      String msg = new String(bytes, cs);
      System.out.format("\u001B[35m" + "Client at  %s  says: %s%n" + "\u001B[0m", attach.clientAddr,
          msg);
      attach.isRead = false; // to rm
      attach.buffer.rewind();      
      // Mirror: send back the received message to client
      attach.channel.write(attach.buffer, attach, this);
      attach.isRead = true;
      attach.buffer.clear();
      attach.channel.read(attach.buffer, attach, this);
    }
  }

  @Override
  public void failed(Throwable e, Attachment attach) {
    e.printStackTrace();
  }
}

// class that handles the messages with the server
class ReadWriteHandlerClient implements CompletionHandler<Integer, Attachment> {
  @Override
  public void completed(Integer result, Attachment attach) {
    if (attach.isRead) {
      // decode server answer. Useful to receive ACK
      attach.buffer.flip();
      Charset cs = Charset.forName("UTF-8");
      int limits = attach.buffer.limit();
      byte bytes[] = new byte[limits];
      attach.buffer.get(bytes, 0, limits);
      String msg = new String(bytes, cs);
      //int port = 0;
      System.out.println("\u001B[33m" + "Server Responded: "+ msg + "" + "\u001B[0m");
    } else {
      attach.isRead = true;
      attach.buffer.clear();
      attach.channel.read(attach.buffer, attach, this);
    }
  }
  @Override
  public void failed(Throwable e, Attachment attach) {
    e.printStackTrace();
  }
}
