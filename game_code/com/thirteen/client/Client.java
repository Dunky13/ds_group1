/**
 * 
 */
package com.thirteen.client;

import com.thirteen.shared.GameField;
import com.thirteen.shared.communication.Message;
import com.thirteen.shared.units.Dragon;
import com.thirteen.shared.units.Player;
import com.thirteen.shared.units.base.Unit;
import com.thirteen.shared.units.base.UnitType;

/**
 * Class that handles communication with Server for specific Unit (Player or
 * Dragon)
 * 
 * @author mwent
 *
 */
public class Client implements Runnable
{
	private Unit unit;
	private UnitType unitType;

	Client(UnitType ut)
	{
		this.unitType = ut;
	}

	private void connect()
	{
		//TODO: Server Comms

	}

	public void sendMessage(Message m)
	{
		//TODO: Server Comms

	}

	public void getMessage(Message m)
	{
		//TODO: Server Comms

	}

	public GameField retrieveBattleField()
	{
		//TODO: Server Comms
		return null;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		this.connect();
		if (this.unitType == UnitType.player)
			this.unit = new Player(this);
		else if (this.unitType == UnitType.dragon)
			this.unit = new Dragon(this);
		if (this.unit == null || !this.unit.running())
			this.unit.stopRunnerThread();
	}
}
