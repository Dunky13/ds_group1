package src.units;

import src.BattleField;
import src.units.extra.Coordinate;
import src.units.extra.UnitType;

/**
 * Base class for all players whom can participate in the DAS game. All
 * properties of the units (hitpoints, attackpoints) are initialized in this
 * class.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
public abstract class Unit
{

	// Position of the unit
	protected int x, y;

	// Health
	private int maxHitPoints;
	protected int hitPoints;

	// Attack points
	protected int attackPoints;

	// Identifier of the unit
	private int unitID;

	// The communication socket between this client and the board
	//	protected Socket clientSocket;

	// If this is set to false, the unit will return its run()-method and disconnect from the server
	protected boolean running;

	/* The thread that is used to make the unit run in a separate thread.
	 * We need to remember this thread to make sure that Java exits cleanly.
	 * (See stopRunnerThread())
	 */
	protected Thread runnerThread;

	private BattleField battleField;
	protected UnitType unitType;

	/**
	 * Create a new unit and specify the number of hitpoints. Units hitpoints
	 * are initialized to the maxHitPoints.
	 * 
	 * @param maxHealth
	 *            is the maximum health of this specific unit.
	 */
	//include url + port
	public Unit(int maxHealth, int attackPoints, int x, int y)
	{
		//		Socket localSocket = new Socket();

		// Initialize the max health and health
		hitPoints = maxHitPoints = maxHealth;

		// Initialize the attack points
		this.attackPoints = attackPoints;

		// Get a new unit id
		battleField = BattleField.getBattleField();
		unitID = battleField.getNewUnitID();
		unitType = UnitType.undefined;
		this.x = x;
		this.y = y;
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
			removeUnit();
	}

	public void dealDamage(int x, int y, int damage)
	{
		Coordinate c = new Coordinate(x, y);
		print("Dealing " + damage + " to Unit on: " + c.toString());
		this.battleField.dealDamage(c, damage);
	}

	public void healDamage(int x, int y, int healed)
	{
		Coordinate c = new Coordinate(x, y);
		print("Healing " + healed + "to Unit on: " + c.toString());
		this.battleField.healDamage(c, healed);
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
	public void setPosition(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x position
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * @return the y position
	 */
	public int getY()
	{
		return y;
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
	 * @param x
	 *            x-coordinate of the spawn location
	 * @param y
	 *            y-coordinate of the spawn location
	 * @return true if the unit could spawn at the location on the battlefield
	 */
	protected boolean spawn()
	{
		/* Create a new message, notifying the board
		 * the unit has actually spawned at the
		 * designated position. 
		 */
		return this.battleField.spawnUnit(this);
	}

	/**
	 * Returns whether the indicated square contains a player, a dragon or
	 * nothing.
	 * 
	 * @param x:
	 *            x coordinate
	 * @param y:
	 *            y coordinate
	 * @return UnitType: the indicated square contains a player, a dragon or
	 *         nothing.
	 */
	protected UnitType getType(int x, int y)
	{
		return battleField.getType(new Coordinate(x, y));
	}

	public UnitType getType()
	{
		return this.unitType;
	}

	protected Unit getUnit(int x, int y)
	{
		return battleField.getUnit(x, y);
	}

	protected void removeUnit()
	{
		print("Removing unit on: " + this.getPosition().toString());
		this.battleField.removeUnit(this);
		this.disconnect();
	}

	protected void moveUnit(int x, int y)
	{
		Coordinate c = new Coordinate(x, y);
		print("Move unit from: " + this.getPosition().toString() + " to: " + c.toString());
		this.battleField.moveUnit(this, c);
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

	public Coordinate getPosition()
	{
		return new Coordinate(x, y);
	}

	public void print(String... ss)
	{
		System.out.print("Unit " + this.getUnitID() + ": ");
		for (String s : ss)
		{
			System.out.print(s);
		}
		System.out.println();
	}
}
