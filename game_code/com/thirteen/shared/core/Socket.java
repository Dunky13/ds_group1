package com.thirteen.shared.core;

import com.thirteen.client.Communication;
import com.thirteen.shared.GameField;
import com.thirteen.shared.Message;
import com.thirteen.shared.core.exception.AlreadyAssignedIDException;
import com.thirteen.shared.core.exception.IDNotAssignedException;

public class Socket
{
	//Should we code this as the base sockect class and then derive multicast and publish-subscribe from it?

	public void register(String string) throws AlreadyAssignedIDException
	{
		// TODO Auto-generated method stub

	}

	public void sendMessage(Message damageMessage, String string) throws IDNotAssignedException
	{
		// TODO Auto-generated method stub

	}

	public void unRegister()
	{
		// TODO Auto-generated method stub

	}

	public void addMessageReceivedHandler(Communication clientMessaging)
	{
		// TODO Auto-generated method stub

	}

	public void addMessageReceivedHandler(GameField battleField)
	{
		// TODO Auto-generated method stub

	}

}