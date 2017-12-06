package distributed.systems.core;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ServerAndPorts
{
	private String hostname;
	private int tom_port;
	private int receiver_port;
	private int multicast_port;
	private int publish_port;
	private int id;

	ServerAndPorts(int id, String hostname, int tom_port, int receiver_port, int multicast_port, int publish_port)
	{
		this.id = id;
		this.hostname = hostname;
		this.tom_port = tom_port;
		this.receiver_port = receiver_port;
		this.multicast_port = multicast_port;
		this.publish_port = publish_port;

	}

	/**
	 * @return the id
	 */
	public int getID()
	{
		return id;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname()
	{
		return hostname;
	}

	/**
	 * @return the tom_port
	 */
	public int getTom_port()
	{
		return tom_port;
	}

	/**
	 * @return the receiver_port
	 */
	public int getReceiver_port()
	{
		return receiver_port;
	}

	/**
	 * @return the multicast_port
	 */
	public int getMulticast_port()
	{
		return multicast_port;
	}

	/**
	 * @return the publish_port
	 */
	public int getPublish_port()
	{
		return publish_port;
	}

	public int getServerToServerReceivePort()
	{
		return publish_port;
	}

	public int getPort()
	{
		return multicast_port;
	}

	public int getServerToClientPort()
	{
		return receiver_port;
	}

	public int getClientToServerPort()
	{
		return tom_port;
	}

	public SocketAddress getSocketAddress()
	{
		return new InetSocketAddress(this.getHostname(), this.getPort());
	}
}
