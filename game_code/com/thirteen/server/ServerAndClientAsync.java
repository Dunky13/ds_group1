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




public class ServerAndClientAsync {

  private static void spawnClient() {
    try {
    AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
    System.out.print("\u001B[33m" + "Please enter a  message  (Bye  to quit): " + "\u001B[0m");
    String msg = (new BufferedReader(new InputStreamReader(System.in))).readLine();
    System.out.print("\u001B[33m" + "Please enter a  port to send the message to: " + "\u001B[0m");
    int port = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
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
    //String msg = "Hello";
    byte[] data = msg.getBytes(cs);
    attach.buffer.put(data);
    attach.buffer.flip();
    ReadWriteHandlerClient readWriteHandler = new ReadWriteHandlerClient();
    channel.write(attach.buffer, attach, readWriteHandler);
    // new client thread that deals with the sending message part
    Thread clientThread = new Thread();
    attach.mainThread = clientThread;
    attach.mainThread.join();
    } catch(Exception e) {
      
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
  
  public static void main(String[] args) throws Exception {        
    if(args.length != 1) {
      System.out.println("missing server port argument");
      System.exit(1);
    }
    spawnClient();
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
      int port = 0;
      System.out.println("\u001B[33m" + "Server Responded: "+ msg + "" + "\u001B[0m");
      try {
        // ask the user for another message to send to server
        msg = this.getTextFromUser();
        port = this.getPortFromUser();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (msg.equalsIgnoreCase("bye")) {
        attach.mainThread.interrupt();
        return;
      }
      attach.buffer.clear();
      byte[] data = msg.getBytes(cs);
      attach.buffer.put(data);
      attach.buffer.flip();
      attach.isRead = false; // It is a write
      attach.channel.write(attach.buffer, attach, this);
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
  private String getTextFromUser() throws Exception {
    System.out.print("\u001B[33m" + "Please enter a  message  (Bye  to quit): " + "\u001B[0m");
    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
    String msg = consoleReader.readLine();
    return msg;
  }
  private int getPortFromUser() throws Exception {
    System.out.print("\u001B[33m" + "Please enter a  port to send the message to: " + "\u001B[0m");
    return Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
  }
}
