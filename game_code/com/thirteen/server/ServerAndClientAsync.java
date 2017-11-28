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

  private static void spawnClient() throws Exception {
    AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
    SocketAddress serverAddr = new InetSocketAddress("localhost", 8989);
    Future<Void> result = channel.connect(serverAddr);
    result.get();
    System.out.println("Connected");
    Attachment attach = new Attachment();
    attach.channel = channel;
    attach.buffer = ByteBuffer.allocate(2048);
    attach.isRead = false;
    // attach.mainThread = Thread.currentThread();
    Charset cs = Charset.forName("UTF-8");
    String msg = "Hello";
    byte[] data = msg.getBytes(cs);
    attach.buffer.put(data);
    attach.buffer.flip();
    ReadWriteHandlerClient readWriteHandler = new ReadWriteHandlerClient();
    channel.write(attach.buffer, attach, readWriteHandler);
    // new client thread that deals with the sending message part
    Thread clientThread = new Thread();
    attach.mainThread = clientThread;
    attach.mainThread.join();
  }

  private static void spawnServer() throws Exception {
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
    String host = "localhost";
    int port = 8990;
    InetSocketAddress sAddr = new InetSocketAddress(host, port);
    server.bind(sAddr);
    System.out.format("Server is listening at %s%n", sAddr);
    Attachment attach2 = new Attachment();
    attach2.server = server;
    server.accept(attach2, new ConnectionHandler());
    Thread.currentThread().join();
  }
  
  public static void main(String[] args) throws Exception {    
    spawnClient();
    spawnServer();    
  }
}

class ConnectionHandler implements
    CompletionHandler<AsynchronousSocketChannel, Attachment> {
  @Override
  public void completed(AsynchronousSocketChannel client, Attachment attach) {
    try {
      SocketAddress clientAddr = client.getRemoteAddress();
      System.out.format("Accepted a  connection from  %s%n", clientAddr);
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
    System.out.println("Failed to accept a  connection.");
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
        System.out.println("Stopped listening to the client %s%n",
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
      System.out.format("Client at  %s  says: %s%n", attach.clientAddr,
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
      System.out.println("Server Responded: "+ msg);
      try {
        // ask the user for another message to send to server
        msg = this.getTextFromUser();
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
  private String getTextFromUser() throws Exception{
    System.out.print("Please enter a  message  (Bye  to quit): ");
    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
    String msg = consoleReader.readLine();
    return msg;
  }
}
