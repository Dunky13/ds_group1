package distributed.systems.server;

import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;

// class that handles the messages with the server
class ReadWriteHandlerClient implements CompletionHandler<Integer, Attachment>
{
	public void completed(Integer result, Attachment attach)
	{
		if (attach.isRead)
		{
			// decode server answer. Useful to receive ACK
			attach.buffer.flip();
			Charset cs = Charset.forName("UTF-8");
			int limits = attach.buffer.limit();
			byte bytes[] = new byte[limits];
			attach.buffer.get(bytes, 0, limits);
			String msg = new String(bytes, cs);
			//int port = 0;
			System.out.println("\u001B[33m" + "Server Responded: " + msg + "" + "\u001B[0m");
		}
		else
		{
			attach.isRead = true;
			attach.buffer.clear();
			attach.channel.read(attach.buffer, attach, this);
		}
	}

	public void failed(Throwable e, Attachment attach)
	{
		e.printStackTrace();
	}
}