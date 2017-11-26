package com.thirteen.shared.units;

import java.io.Serializable;
import com.thirteen.client.Client;
import com.thirteen.shared.Const;
import com.thirteen.shared.GameState;
import com.thirteen.shared.Message;
import com.thirteen.shared.units.base.Coordinate;
import com.thirteen.shared.units.base.Direction;
import com.thirteen.shared.units.base.Unit;
import com.thirteen.shared.units.base.UnitType;

/**
 * A Player is, as the name implies, a playing character. It can move in the
 * four wind directions, has a hitpoint range between 10 and 20 and an attack
 * range between 1 and 10.
 * 
 * Every player runs in its own thread, simulating individual behaviour, not
 * unlike a distributed server setup.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
@SuppressWarnings("serial")
public class Player extends Unit implements Runnable, Serializable
{
	/* Reaction speed of the player
	 * This is the time needed for the player to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
	protected int timeBetweenTurns;
	private Coordinate dragonToAttack;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	public static final int MIN_HITPOINTS = 20;
	public static final int MAX_HITPOINTS = 10;
	public static final int MIN_ATTACKPOINTS = 1;
	public static final int MAX_ATTACKPOINTS = 10;

	public static final int MAX_HEAL_RANGE = 5;
	public static final int MAX_ATTACT_RANGE = 2;

	/**
	 * Create a player, initialize both the hit and the attackpoints.
	 */
	public Player(Client client)
	{
		/* Initialize the hitpoints and attackpoints */
		super(
			(int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS),
			(int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS));
		this.unitType = UnitType.player;
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
		this.dragonToAttack = null;
		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;
		/* Create a new player thread */
		runnerThread = new Thread(this);
		runnerThread.start();
		this.running = true;
	}

	/**
	 * Roleplay the player. Make the player act once in a while, only stopping
	 * when the player is actually dead or the program has halted.
	 * 
	 * It checks a random direction, if an entity is located there. If there is
	 * a player, it will try to heal that player if the 50% health rule applies.
	 * If there is a dragon, it will attack and if there is nothing, it will
	 * move in that direction.
	 */
	@SuppressWarnings("static-access")
	public void run()
	{
		Direction direction;

		this.running = true;

		while (GameState.getRunningState() && this.running)
		{
			try
			{
				/* Sleep while the player is considering its next move */
				Thread.currentThread().sleep((int)(timeBetweenTurns * 500 * GameState.GAME_SPEED));

				/* Stop if the player runs out of hitpoints */
				if (getHitPoints() <= 0)
					break;

				Coordinate closestHealTarget = this.findClosestHeal(1);
				if (closestHealTarget != null)
				{
					this.client.sendMessage(this.healDamage(closestHealTarget, getAttackPoints()));
				}
				else if (this.dragonToAttack != null)
				{
					this.client.sendMessage(this.dealDamage(this.dragonToAttack, getAttackPoints()));
				}
				else
				{
					direction = Direction.values()[(int)(Direction.values().length * Math.random())];
					Coordinate target = this.makeMove(direction);
					if (target == null)
					{
						continue;
					}
					this.client.sendMessage(this.moveUnit(target));
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				this.dragonToAttack = null;
			}
		}

	}

	/**
	 * Find closest coordinate for player healing. This happens recursively.
	 * 
	 * @param distance
	 * @return Coordinate if other player needs healing. null if no player is in
	 *         range.
	 */
	private Coordinate findClosestHeal(int distance)
	{
		if (distance > Player.MAX_HEAL_RANGE)
		{
			return null;
		}
		for (int i = -distance; i <= distance; i++)
		{
			for (int j = -distance; j <= distance; j++)
			{
				//This player - skip
				if (i == 0 && j == 0)
					continue;
				//Not in search distance.
				if (Math.abs(i) + Math.abs(j) > distance)
					continue;

				//Coordinate of new distance
				Coordinate c = new Coordinate(this.getX() + i, this.getY() + j);
				//If Coordinate is not within the gamefield;
				if (!c.inBound(0, Const.MAP_WIDTH, 0, Const.MAP_HEIGHT))
					continue;

				Unit u = this.battlefield.getUnit(c);
				if (u == null)
					continue;

				UnitType ut = u.getUnitType();
				// Attack can happen only in attack range and only in one direction
				if (distance <= Player.MAX_ATTACT_RANGE && (i == 0 || j == 0))
				{
					if (ut == UnitType.undefined)
						continue; //if in attack range, check for player & dragon
					if (ut == UnitType.dragon)
					{
						this.dragonToAttack = c;
						continue;
					}
				}
				else if (ut != UnitType.player)
					continue; //if not in range, check for player

				Player other = (Player)u;

				// If hitpoints below 50%
				if (other.getHitPoints() < other.getMaxHitPoints() / 2.0)
					return c;
			}
		}
		return this.findClosestHeal(distance + 1);
	}

	private Coordinate makeMove(Direction direction)
	{
		int targetX = 0, targetY = 0;
		switch (direction)
		{
		case up:
			if (this.getY() <= 0)
				// The player was at the edge of the map, so he can't move north and there are no units there
				return null;

			targetX = this.getX();
			targetY = this.getY() - 1;
			break;
		case down:
			if (this.getY() >= Const.MAP_HEIGHT - 1)
				// The player was at the edge of the map, so he can't move south and there are no units there
				return null;

			targetX = this.getX();
			targetY = this.getY() + 1;
			break;
		case left:
			if (this.getX() <= 0)
				// The player was at the edge of the map, so he can't move west and there are no units there
				return null;

			targetX = this.getX() - 1;
			targetY = this.getY();
			break;
		case right:
			if (this.getX() >= Const.MAP_WIDTH - 1)
				// The player was at the edge of the map, so he can't move east and there are no units there
				return null;

			targetX = this.getX() + 1;
			targetY = this.getY();
			break;
		}
		return new Coordinate(targetX, targetY);
	}
}
