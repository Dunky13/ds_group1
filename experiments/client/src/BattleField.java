package src;

import java.util.ArrayList;
import src.units.Unit;
import src.units.extra.Bound;
import src.units.extra.Coordinate;
import src.units.extra.UnitType;

/**
 * The actual battlefield where the fighting takes place. It consists of an
 * array of a certain width and height.
 * 
 * It is a singleton, which can be requested by the getBattleField() method. A
 * unit can be put onto the battlefield by using the putUnit() method.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
public class BattleField
{
	/* The array of units */
	private Unit[][] map;

	/* The static singleton */
	private static BattleField battlefield;

	/* The last id that was assigned to an unit. This variable is used to
	 * enforce that each unit has its own unique id.
	 */
	private int lastUnitID = 0;

	public final static String serverID = "server";
	public final static int MAP_WIDTH = 10;
	public final static int MAP_HEIGHT = 10;
	public final static Bound MAP_BOUND = new Bound(BattleField.MAP_WIDTH, BattleField.MAP_HEIGHT);
	private ArrayList<Unit> units;

	/**
	 * Initialize the battlefield to the specified size
	 * 
	 * @param width
	 *            of the battlefield
	 * @param height
	 *            of the battlefield
	 */
	private BattleField(int width, int height)
	{
		//		Socket local = new LocalSocket();//??? -> Is this tom now?

		synchronized (this)
		{
			map = new Unit[width][height];
			//			local.register(BattleField.serverID);
			//			serverSocket = new SynchronizedSocket(local);
			//			serverSocket.addMessageReceivedHandler(this);
			units = new ArrayList<Unit>();
			//add server is to new
			//			tom = new TomProcedure("1",LC);

		}
	}

	/**
	 * Singleton method which returns the sole instance of the battlefield.
	 * 
	 * @return the battlefield.
	 */
	public static BattleField getBattleField()
	{
		if (battlefield == null)
			battlefield = new BattleField(MAP_WIDTH, MAP_HEIGHT);
		return battlefield;
	}

	/**
	 * Puts a new unit at the specified position. First, it checks whether the
	 * position is empty, if not, it does nothing. In addition, the unit is also
	 * put in the list of known units.
	 * 
	 * @param unit
	 *            is the actual unit being spawned on the specified position.
	 * @param x
	 *            is the x position.
	 * @param y
	 *            is the y position.
	 * @return true when the unit has been put on the specified position.
	 */
	private boolean spawnUnit(Unit unit, int x, int y)
	{
		synchronized (this)
		{
			if (map[x][y] != null)
				return false;

			map[x][y] = unit;
			unit.setPosition(x, y);
		}
		units.add(unit);

		return true;
	}

	/**
	 * Put a unit at the specified position. First, it checks whether the
	 * position is empty, if not, it does nothing.
	 * 
	 * @param unit
	 *            is the actual unit being put on the specified position.
	 * @param x
	 *            is the x position.
	 * @param y
	 *            is the y position.
	 * @return true when the unit has been put on the specified position.
	 */
	private synchronized boolean putUnit(Unit unit, int x, int y)
	{
		if (map[x][y] != null)
			return false;

		map[x][y] = unit;
		unit.setPosition(x, y);

		return true;
	}

	/**
	 * Get a unit from a position.
	 * 
	 * @param x
	 *            position.
	 * @param y
	 *            position.
	 * @return the unit at the specified position, or return null if there is no
	 *         unit at that specific position.
	 */
	public Unit getUnit(int x, int y)
	{
		if (!BattleField.MAP_BOUND.hasCoordinate(new Coordinate(x, y)))
			return null;
		return map[x][y];
	}

	/**
	 * Move the specified unit a certain number of steps.
	 * 
	 * @param unit
	 *            is the unit being moved.
	 * @param deltax
	 *            is the delta in the x position.
	 * @param deltay
	 *            is the delta in the y position.
	 * 
	 * @return true on success.
	 */
	private synchronized boolean moveUnit(Unit unit, int newX, int newY)
	{
		int originalX = unit.getX();
		int originalY = unit.getY();

		if (unit.getHitPoints() <= 0)
			return false;

		if (newX >= 0 && newX < BattleField.MAP_WIDTH)
			if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
				if (map[newX][newY] == null)
				{
					if (putUnit(unit, newX, newY))
					{
						map[originalX][originalY] = null;
						return true;
					}
				}

		return false;
	}

	/**
	 * Remove a unit from a specific position and makes the unit disconnect from
	 * the server.
	 * 
	 * @param x
	 *            position.
	 * @param y
	 *            position.
	 */
	private synchronized void removeUnit(int x, int y)
	{
		Unit unitToRemove = this.getUnit(x, y);
		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		unitToRemove.disconnect();
		units.remove(unitToRemove);
	}

	/**
	 * Returns a new unique unit ID.
	 * 
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID()
	{
		return ++lastUnitID;
	}

	public UnitType getType(Coordinate c)
	{
		Unit u = this.map[c.getX()][c.getY()];
		if (u == null)
			return UnitType.undefined;
		return u.getType();
	}

	public boolean spawnUnit(Unit u)
	{
		return this.spawnUnit(u, u.getX(), u.getY());
	}

	public void putUnit(Unit u)
	{
		this.putUnit(u, u.getX(), u.getY());
	}

	public void dealDamage(Coordinate c, int damage)
	{
		System.out.println("Deal damage to unit on: " + c.toString() + "with points: " + -damage);
		Unit u = this.getUnit(c.getX(), c.getY());
		if (u == null)
		{
			System.out.println("Unit no longer available");
			return;
		}
		u.adjustHitPoints(-damage);
	}

	public void healDamage(Coordinate c, int healPoints)
	{
		System.out.println("Heal unit on: " + c.toString() + "with points: " + healPoints);
		Unit u = this.getUnit(c.getX(), c.getY());
		if (u == null)
		{
			System.out.println("Unit no longer available");
			return;
		}
		u.adjustHitPoints(healPoints);
	}

	public void moveUnit(Unit u, Coordinate c)
	{
		System.out.println("Move unit from: " + u.getPosition().toString() + " to: " + c.toString());
		this.moveUnit(u, c.getX(), c.getY());
	}

	public void removeUnit(Unit u)
	{
		System.out.println("Removing unit on: " + u.getPosition().toString());
		this.removeUnit(u.getX(), u.getY());
	}

	/**
	 * Close down the battlefield. Unregisters the serverSocket so the program
	 * can actually end.
	 */
	public synchronized void shutdown()
	{
		// Remove all units from the battlefield and make them disconnect from the server
		for (Unit unit : units)
		{
			unit.disconnect();
			unit.stopRunnerThread();
		}
		//this.tom.close();
	}

}
