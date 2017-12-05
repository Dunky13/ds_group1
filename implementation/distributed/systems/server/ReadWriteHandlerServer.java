package distributed.systems.server;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;

// Class that handles the messages with the client
class ReadWriteHandlerServer implements CompletionHandler<Integer, Attachment>
{
	@Override
	public void completed(Integer result, Attachment attach)
	{
		if (result == -1)
		{
			try
			{
				attach.channel.close();
				System.out.format("\u001B[35m" + "Stopped listening to the client %s%n" + "\u001B[0m", attach.clientAddr);
				System.out.format("\u001B[35m" + "With id %s%n" + "\u001B[0m", attach.id);
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			return;
		}
		if (attach.isRead)
		{
			// decoding the client message
			attach.buffer.flip();
			int limits = attach.buffer.limit();
			byte bytes[] = new byte[limits];
			attach.buffer.get(bytes, 0, limits);
			Charset cs = Charset.forName("UTF-8");
			String msg = new String(bytes, cs);
			System.out.format("\u001B[35m" + "Client at  %s  says: %s%n" + "\u001B[0m", attach.clientAddr, msg);
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
	public void failed(Throwable e, Attachment attach)
	{
		e.printStackTrace();
	}
}