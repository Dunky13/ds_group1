package distributed.systems.core;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ClientAndPort
{
	private String hostname;
	private int port_one;

	ClientAndPort(String hostname, int port_one)
	{
		this.hostname = hostname;
		this.port_one = port_one;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname()
	{
		return hostname;
	}

	public int getPort()
	{
		return port_one;
	}

	public SocketAddress getSocketAddress()
	{
		return new InetSocketAddress(this.getHostname(), this.getPort());
	}
}
