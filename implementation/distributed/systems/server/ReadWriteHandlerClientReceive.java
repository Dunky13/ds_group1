package distributed.systems.server;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import distributed.systems.core.Message;
import distributed.systems.executors.ClientExecutor;

// Class that handles the messages with the client
public class ReadWriteHandlerClientReceive implements CompletionHandler<Integer, Attachment> {
	private ClientExecutor clientExecutor;

	public ReadWriteHandlerClientReceive(ClientExecutor clientExecutor) {
		this.clientExecutor = clientExecutor;    
	}

	public void completed(Integer result, Attachment attach) {
		if (result == -1) {
			try {
				attach.channel.close();
				System.out.format("\u001B[33m" + "Stopped listening to the server %s%n" + "\u001B[0m", attach.clientAddr);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
			return;
		}
		if (attach.isRead) {
			attach.buffer.flip();
			int limits = attach.buffer.limit();
			byte bytes[] = new byte[limits];
			attach.buffer.get(bytes, 0, limits);
			Message msg;
			try {
				msg = Message.deserialze(bytes);
				clientExecutor.getUnit().onMessageReceived(msg);
				attach.isRead = false; // to rm
				attach.buffer.rewind();
			}
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
	public void failed(Throwable e, Attachment attach) {
		e.printStackTrace();
	}
}
