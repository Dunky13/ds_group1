package com.thirteen.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.thirteen.shared.Const;
import com.thirteen.shared.GameState;
import com.thirteen.shared.units.base.UnitType;

public class ClientManager
{
	public static final int MIN_PLAYER_COUNT = Const.MIN_PLAYER_COUNT / Const.NUMBER_OF_SERVERS;
	public static final int MAX_PLAYER_COUNT = Const.MAX_PLAYER_COUNT / Const.NUMBER_OF_SERVERS;
	public static final int DRAGON_COUNT = Const.DRAGON_COUNT / Const.NUMBER_OF_SERVERS;
	public static final int INITIAL_PLAYER_COUNT = (int)((MAX_PLAYER_COUNT - MIN_PLAYER_COUNT) * Math.random() + MIN_PLAYER_COUNT);

	private AtomicInteger playerCount;
	List<Thread> runningClients;
	GameFieldViewer gfv;

	ClientManager()
	{
		this.runningClients = Collections.synchronizedList(new ArrayList<Thread>());
		this.playerCount = new AtomicInteger(0);
		this.initUnits();

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				gfv = new GameFieldViewer();
			}
		}).start();

		this.addNewPlayers();

		this.shutdown();
	}

	private void addNewPlayers()
	{
		while (GameState.getRunningState())
		{
			try
			{
				Thread.sleep((int)(5000 * GameState.GAME_SPEED));

				// Connect a player to the game if the game still has room for a new player
				if (this.playerCount.get() >= MAX_PLAYER_COUNT)
					continue;

				this.startPlayer();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void initUnits()
	{
		for (int i = 0; i < DRAGON_COUNT; i++)
		{
			Client c = new Client(UnitType.dragon);
			Thread t = new Thread(c);
			synchronized (runningClients)
			{
				runningClients.add(t);
			}
			t.start();
		}
		for (int j = 0; j < INITIAL_PLAYER_COUNT; j++)
		{
			this.startPlayer();
		}
	}

	private synchronized void startPlayer()
	{
		Client c = new Client(UnitType.player);
		Thread t = new Thread(c);
		synchronized (runningClients)
		{
			runningClients.add(t);
			this.playerCount.incrementAndGet();
		}
		t.start();
	}

	private synchronized void shutdown()
	{
		for (Thread t : runningClients)
		{
			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		this.gfv.stopRunnerThread();
	}

	public static void main(String[] args)
	{
		new ClientManager();
	}

}
