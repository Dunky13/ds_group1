import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.ByteBuffer;
import java.net.SocketAddress;

class Attachment {
  AsynchronousServerSocketChannel server;
  Thread mainThread;
  AsynchronousSocketChannel channel;
  ByteBuffer buffer;
  SocketAddress clientAddr;
  boolean isRead;
  int id;
}
