package distributed.systems.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;

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
			try {
				// ask the user for another message to send to server
				//msg = this.getTextFromUser();
				//port = this.getPortFromUser();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// if (msg.equalsIgnoreCase("bye")) {
			//   attach.mainThread.interrupt();
			//   return;
			// }
			// String prevValue = ServerAndClientAsync.spinValue;
			// while (prevValue.equals(ServerAndClientAsync.spinValue)) {
			//   // busy wait
			// }
			// msg = ServerAndClientAsync.spinValue;
			// attach.buffer.clear();
			// byte[] data = msg.getBytes(cs);
			// attach.buffer.put(data);
			// attach.buffer.flip();
			// attach.isRead = false; // It is a write
			// attach.channel.write(attach.buffer, attach, this);
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