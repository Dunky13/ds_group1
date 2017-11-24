/**
 * 
 */
package com.thirteen.client;

import java.util.ArrayList;
import com.thirteen.shared.Const;
import com.thirteen.shared.GameField;
import com.thirteen.shared.Message;
import com.thirteen.shared.units.Dragon;
import com.thirteen.shared.units.Player;
import com.thirteen.shared.units.base.Unit;
import com.thirteen.shared.units.base.UnitType;

/**
 * @author mwent
 *
 */
public class Client implements Runnable
{
	public static final int MIN_PLAYER_COUNT = Const.MIN_PLAYER_COUNT / Const.NUMBER_OF_SERVERS;
	public static final int MAX_PLAYER_COUNT = Const.MAX_PLAYER_COUNT / Const.NUMBER_OF_SERVERS;
	public static final int DRAGON_COUNT = Const.DRAGON_COUNT / Const.NUMBER_OF_SERVERS;
	public static final int playerCount = (int)((MAX_PLAYER_COUNT - MIN_PLAYER_COUNT) * Math.random() + MIN_PLAYER_COUNT);
	private GameField battlefield;
	private Unit unit;
	private UnitType unitType;

	Client(UnitType ut)
	{
		this.unitType = ut;
	}

	private void connect()
	{

	}

	public void sendMessage(Message m)
	{

	}

	public void getMessage(Message m)
	{

	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		this.connect();
		this.battlefield = this.retrieveBattleField();
		if (this.unitType == UnitType.player)
			this.unit = new Player(this);
		else if (this.unitType == UnitType.dragon)
			this.unit = new Dragon(this);
		if (this.unit == null || !this.unit.running())
			this.unit.stopRunnerThread();
	}

	private GameField retrieveBattleField()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		ArrayList<Thread> runningClients = new ArrayList<Thread>();
		for (int i = 0; i < DRAGON_COUNT; i++)
		{
			Client c = new Client(UnitType.dragon);
			Thread t = new Thread(c);
			runningClients.add(t);
			t.start();
		}
		for (int j = 0; j < Client.playerCount; j++)
		{
			Client c = new Client(UnitType.player);
			Thread t = new Thread(c);
			runningClients.add(t);
			t.start();
		}

		GameFieldViewer gfv = new GameFieldViewer();

		for (Thread t : runningClients)
		{
			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		gfv.stopRunnerThread();

	}

	public GameField getBattleField()
	{
		return this.battlefield;
	}

}
