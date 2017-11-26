package com.thirteen.shared.units;

import java.io.Serializable;
import java.util.ArrayList;
import com.thirteen.client.Client;
import com.thirteen.shared.Const;
import com.thirteen.shared.GameState;
import com.thirteen.shared.Message;
import com.thirteen.shared.units.base.Coordinate;
import com.thirteen.shared.units.base.Direction;
import com.thirteen.shared.units.base.Unit;
import com.thirteen.shared.units.base.UnitType;

/**
 * A dragon is a non-playing character, which can't move, has a hitpoint range
 * between 50 and 100 and an attack range between 5 and 20.
 * 
 * Every dragon runs in its own thread, simulating individual behaviour, not
 * unlike a distributed server setup.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
@SuppressWarnings("serial")
public class Dragon extends Unit implements Runnable, Serializable
{
	/* Reaction speed of the dragon
	 * This is the time needed for the dragon to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
	protected int timeBetweenTurns;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	// The minimum and maximum amount of hitpoints that a particular dragon starts with
	public static final int MIN_HITPOINTS = 50;
	public static final int MAX_HITPOINTS = 100;
	// The minimum and maximum amount of hitpoints that a particular dragon has
	public static final int MIN_ATTACKPOINTS = 5;
	public static final int MAX_ATTACKPOINTS = 20;

	/**
	 * Spawn a new dragon, initialize the reaction speed
	 *
	 */
	public Dragon(Client client)
	{
		/* Spawn the dragon with a random number of hitpoints between
		 * 50..100 and 5..20 attackpoints. */
		super(
			(int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS),
			(int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS));

		this.unitType = UnitType.dragon;
		this.client = client;
		this.setBattlefield(client.getBattleField());

		this.c = this.battlefield.findPosition();
		Message spawn = spawn(c);
		if (spawn == null)
		{
			return; // We could not spawn on the battlefield
		}
		else
		{
			this.client.sendMessage(spawn);
		}

		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;

		/* Awaken the dragon */
		runnerThread = new Thread(this);
		runnerThread.start();
	}

	/**
	 * Roleplay the dragon. Make the dragon act once a while, only stopping when
	 * the dragon is actually dead or the program has halted.
	 * 
	 * It checks if an enemy is near and, if so, it attacks that specific enemy.
	 */
	@SuppressWarnings("static-access")
	public void run()
	{
		ArrayList<Direction> adjacentPlayers = new ArrayList<Direction>();

		this.running = true;

		while (GameState.getRunningState() && this.running)
		{
			try
			{
				/* Sleep while the dragon is considering its next move */
				Thread.currentThread().sleep((int)(timeBetweenTurns * 500 * GameState.GAME_SPEED));

				/* Stop if the dragon runs out of hitpoints */
				if (getHitPoints() <= 0)
					break;

				// Decide what players are near
				Coordinate c = new Coordinate(this.getPosition());
				Coordinate up = new Coordinate(c, 0, -1);
				Coordinate down = new Coordinate(c, 0, 1);
				Coordinate left = new Coordinate(c, -1, 0);
				Coordinate right = new Coordinate(c, 1, 0);
				if (getY() > 0)
					if (getUnit(up).getUnitType() == UnitType.player)
						adjacentPlayers.add(Direction.up);
				if (getY() < Const.MAP_WIDTH - 1)
					if (getUnit(down).getUnitType() == UnitType.player)
						adjacentPlayers.add(Direction.down);
				if (getX() > 0)
					if (getUnit(left).getUnitType() == UnitType.player)
						adjacentPlayers.add(Direction.left);
				if (getX() < Const.MAP_WIDTH - 1)
					if (getUnit(right).getUnitType() == UnitType.player)
						adjacentPlayers.add(Direction.right);

				// Pick a random player to attack
				if (adjacentPlayers.size() == 0)
					continue; // There are no players to attack
				Direction playerToAttack = adjacentPlayers.get((int)(Math.random() * adjacentPlayers.size()));

				// Attack the player
				switch (playerToAttack)
				{
				case up:
					this.dealDamage(up, this.getAttackPoints());
					break;
				case right:
					this.dealDamage(right, this.getAttackPoints());
					break;
				case down:
					this.dealDamage(down, this.getAttackPoints());
					break;
				case left:
					this.dealDamage(left, this.getAttackPoints());
					break;
				}

			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
