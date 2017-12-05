package distributed.systems.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Attachment> {
  private ServerExecutor serverExecutor;

  public ConnectionHandler(ServerExecutor serverExecutor) {
    this.serverExecutor = serverExecutor;
  }
  
  @Override
  public void completed(AsynchronousSocketChannel client, Attachment attach) {
    try {
      SocketAddress clientAddr = client.getRemoteAddress();
      System.out.format("\u001B[35m" + "Accepted a  connection from  %s%n" + "\u001B[0m", clientAddr);
      System.out.format("\u001B[35m" + "Accepted a  connection with id  %s%n" + "\u001B[0m", attach.id);
      attach.server.accept(attach, this);
      ReadWriteHandlerServer rwHandler = new ReadWriteHandlerServer(serverExecutor);
      Attachment newAttach = new Attachment();
      newAttach.server = attach.server;
      newAttach.channel = client;
      newAttach.buffer = ByteBuffer.allocate(2048);
      newAttach.isRead = true;
      newAttach.clientAddr = clientAddr;
      client.read(newAttach.buffer, newAttach, rwHandler);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void failed(Throwable e, Attachment attach) {
    System.out.println("\u001B[35m" + "Failed to accept a  connection." + "\u001B[0m");
    e.printStackTrace();
  }
}
