package distributed.systems.core;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ServerAndPorts
{
	private String hostname;
	private int port_three;
	private int port_two;
	private int port_one;
	private int port_four;
	private int id;

	ServerAndPorts(int id, String hostname, int port_one, int port_two, int port_three, int port_four)
	{
		this.id = id;
		this.hostname = hostname;
		this.port_one = port_one;
		this.port_two = port_two;
		this.port_three = port_three;
		this.port_four = port_four;

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
		return port_three;
	}

	/**
	 * @return the receiver_port
	 */
	public int getReceiver_port()
	{
		return port_two;
	}

	/**
	 * @return the multicast_port
	 */
	public int getMulticast_port()
	{
		return port_one;
	}

	/**
	 * @return the publish_port
	 */
	public int getPublish_port()
	{
		return port_four;
	}

	public int getServerToServerReceivePort()
	{
		return port_four;
	}

	public int getPort()
	{
		return port_one;
	}

	public int getServerToClientPort()
	{
		return port_two;
	}

	public int getClientToServerPort()
	{
		return port_three;
	}

	public SocketAddress getSocketAddress()
	{
		return new InetSocketAddress(this.getHostname(), this.getPort());
	}

	public int getReceivePort() {
		
		return port_two;
	}
	
	public String toString() {
		return "ID: " + this.id +" - Host: " + this.getHostname() + " - P1: " + this.port_one + " - P2: " + this.port_two;
	}
}
