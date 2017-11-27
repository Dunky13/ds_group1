package com.thirteen.shared.communication;

public class ServerLocation {

	private String address;
	private int port;

	public ServerLocation(String address, int port) {
		this.address = address;
		this.port = port;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
}
