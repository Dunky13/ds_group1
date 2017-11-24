package com.thirteen.shared.core;

import com.thirteen.shared.BattleField;
import com.thirteen.shared.Message;
import com.thirteen.shared.core.exception.AlreadyAssignedIDException;
import com.thirteen.shared.core.exception.IDNotAssignedException;
import com.thirteen.shared.units.Unit;

public class Socket {
	//Should we code this as the base sockect class and then derive multicast and publish-subscribe from it?
	
	
	public void register(String string) throws AlreadyAssignedIDException{
		// TODO Auto-generated method stub
		
	}


	public void sendMessage(Message damageMessage, String string) throws IDNotAssignedException {
		// TODO Auto-generated method stub
		
	}

	public void unRegister() {
		// TODO Auto-generated method stub
		
	}

	public void addMessageReceivedHandler(Unit unit) {
		// TODO Auto-generated method stub
		
	}
	public void addMessageReceivedHandler(BattleField battleField) {
		// TODO Auto-generated method stub
		
	}

}