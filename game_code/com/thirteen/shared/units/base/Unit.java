package com.thirteen.shared.units.base;

import java.io.Serializable;
import com.thirteen.client.Client;
import com.thirteen.shared.GameField;
import com.thirteen.shared.Message;
import com.thirteen.shared.MessageRequest;

/**
 * Base class for all players whom can participate in the DAS game. All
 * properties of the units (hitpoints, attackpoints) are initialized in this
 * class.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
public abstract class Unit implements Serializable
{
	private static final long serialVersionUID = -4550572524008491160L;

	// Position of the unit
	protected Coordinate c;
	protected Client client;
	protected GameField battlefield;

	// Health
	private int maxHitPoints;
	protected int hitPoints;

	// Attack points
	protected int attackPoints;

	// Identifier of the unit
	private int unitID;

	// If this is set to false, the unit will return its run()-method and disconnect from the server
	protected boolean running;

	// Is used for mapping an unique id to a message sent by this unit
	private int localMessageCounter = 0;

	/* The thread that is used to make the unit run in a separate thread.
	 * We need to remember this thread to make sure that Java exits cleanly.
	 * (See stopRunnerThread())
	 */
	protected Thread runnerThread;

	/**
	 * Create a new unit and specify the number of hitpoints. Units hitpoints
	 * are initialized to the maxHitPoints.
	 * 
	 * @param maxHealth
	 *            is the maximum health of this specific unit.
	 */
	public Unit(int maxHealth, int attackPoints)
	{

		// Initialize the max health and health
		hitPoints = maxHitPoints = maxHealth;

		// Initialize the attack points
		this.attackPoints = attackPoints;

		// Get a new unit id
		unitID = GameField.getBattleField().getNewUnitID();
	}

	/**
	 * Adjust the hitpoints to a certain level. Useful for healing or dying
	 * purposes.
	 * 
	 * @param modifier
	 *            is to be added to the hitpoint count.
	 */
	public synchronized void adjustHitPoints(int modifier)
	{
		if (hitPoints <= 0)
			return;

		hitPoints += modifier;

		if (hitPoints > maxHitPoints)
			hitPoints = maxHitPoints;

		if (hitPoints <= 0)
			removeUnit(c);
	}

	public Message dealDamage(Coordinate c, int damage)
	{
		/* Create a new message, notifying the board
		 * that a unit has been dealt damage.
		 */
		int id;
		Message damageMessage;
		synchronized (this)
		{
			id = localMessageCounter++;

			damageMessage = new Message();
			damageMessage.put("request", MessageRequest.dealDamage);
			damageMessage.put("x", c.getX());
			damageMessage.put("y", c.getY());
			damageMessage.put("damage", damage);
			damageMessage.put("id", id);
		}
		return damageMessage;
	}

	public Message healDamage(Coordinate c, int healed)
	{
		/* Create a new message, notifying the board
		 * that a unit has been healed.
		 */
		int id;
		Message healMessage;
		synchronized (this)
		{
			id = localMessageCounter++;

			healMessage = new Message();
			healMessage.put("request", MessageRequest.healDamage);
			healMessage.put("x", c.getX());
			healMessage.put("y", c.getY());
			healMessage.put("healed", healed);
			healMessage.put("id", id);
		}

		return healMessage;
	}

	/**
	 * @return the maximum number of hitpoints.
	 */
	public int getMaxHitPoints()
	{
		return maxHitPoints;
	}

	/**
	 * @return the unique unit identifier.
	 */
	public int getUnitID()
	{
		return unitID;
	}

	/**
	 * Set the position of the unit.
	 * 
	 * @param x
	 *            is the new x coordinate
	 * @param y
	 *            is the new y coordinate
	 */
	public void setPosition(Coordinate c)
	{
		this.c = c;
	}

	public Coordinate getPosition()
	{
		return this.c;
	}

	/**
	 * @return the x position
	 */
	public int getX()
	{
		//TODO: Refactor to remove this function
		return c.getX();
	}

	/**
	 * @return the y position
	 */
	public int getY()
	{
		//TODO: Refactor to remove this function
		return c.getY();
	}

	/**
	 * @return the current number of hitpoints.
	 */
	public int getHitPoints()
	{
		return hitPoints;
	}

	/**
	 * @return the attack points
	 */
	public int getAttackPoints()
	{
		return attackPoints;
	}

	/**
	 * Tries to make the unit spawn at a certain location on the battlefield
	 * 
	 * @param c
	 *            x,y-coordinate of the spawn location
	 * @return true iff the unit could spawn at the location on the battlefield
	 */
	protected Message spawn(Coordinate c)
	{
		/* Create a new message, notifying the board
		 * the unit has actually spawned at the
		 * designated position. 
		 */
		if (c != null && this.battlefield.spawnUnit(this, c))
		{
			int id = localMessageCounter++;
			Message spawnMessage = new Message();
			spawnMessage.put("request", MessageRequest.spawnUnit);
			spawnMessage.put("x", c.getX());
			spawnMessage.put("y", c.getY());
			spawnMessage.put("unit", this);
			spawnMessage.put("id", id);
			return spawnMessage;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns whether the indicated square contains a player, a dragon or
	 * nothing.
	 * 
	 * @param c:
	 *            x, y coordinate
	 * @return UnitType: the indicated square contains a player, a dragon or
	 *         nothing.
	 */
	protected UnitType getType(Coordinate c)
	{
		return this.battlefield.getType(c);

	}

	protected Unit getUnit(Coordinate c)
	{
		return this.battlefield.getUnit(c);
	}

	protected Message removeUnit(Coordinate c)
	{
		Message removeMessage = new Message();
		int id = localMessageCounter++;
		removeMessage.put("request", MessageRequest.removeUnit);
		removeMessage.put("x", c.getX());
		removeMessage.put("y", c.getY());
		removeMessage.put("id", id);

		return removeMessage;
	}

	protected Message moveUnit(Coordinate c)
	{
		Message moveMessage = new Message();
		int id = localMessageCounter++;
		moveMessage.put("request", MessageRequest.moveUnit);
		moveMessage.put("x", c.getX());
		moveMessage.put("y", c.getY());
		moveMessage.put("id", id);
		moveMessage.put("unit", this);

		return moveMessage;
	}

	/**
	 * @return the battlefield
	 */
	public GameField getBattlefield()
	{
		return battlefield;
	}

	/**
	 * @param battlefield
	 *            the battlefield to set
	 */
	public void setBattlefield(GameField battlefield)
	{
		this.battlefield = battlefield;
	}

	// Disconnects the unit from the battlefield by exiting its run-state
	public void disconnect()
	{
		running = false;
	}

	/**
	 * Stop the running thread. This has to be called explicitly to make sure
	 * the program terminates cleanly.
	 */
	public void stopRunnerThread()
	{
		try
		{
			runnerThread.join();
		}
		catch (InterruptedException ex)
		{
			assert (false) : "Unit stopRunnerThread was interrupted";
		}
	}

	public boolean running()
	{
		return this.running;
	}
}
