package com.thirteen.shared;

import java.io.Serializable;
import java.util.ArrayList;
import com.thirteen.shared.units.Dragon;
import com.thirteen.shared.units.Player;
import com.thirteen.shared.units.base.Coordinate;
import com.thirteen.shared.units.base.Unit;
import com.thirteen.shared.units.base.UnitType;

public class GameField implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4098701539568319013L;

	/* The array of units */
	private Unit[][] map;

	/* The static singleton */
	private static GameField battlefield;

	/**
	 * Singleton method which returns the sole instance of the battlefield.
	 * 
	 * @return the battlefield.
	 */
	public static GameField getBattleField()
	{
		if (battlefield == null)
			battlefield = new GameField(Const.MAP_WIDTH, Const.MAP_HEIGHT);
		return battlefield;
	}

	/* The last id that was assigned to an unit. This variable is used to
	 * enforce that each unit has its own unique id.
	 */
	private int lastUnitID = 0;

	private ArrayList<Unit> units;

	/**
	 * Initialize the battlefield to the specified size
	 * 
	 * @param width
	 *            of the battlefield
	 * @param height
	 *            of the battlefield
	 */
	private GameField(int width, int height)
	{
		synchronized (this)
		{
			map = new Unit[width][height];
			units = new ArrayList<Unit>();
		}
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
	public boolean spawnUnit(Unit unit, Coordinate c)
	{
		synchronized (this)
		{
			if (map[c.getX()][c.getY()] != null)
				return false;

			map[c.getX()][c.getY()] = unit;
			unit.setPosition(c);
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
	private synchronized boolean putUnit(Unit unit, Coordinate c)
	{
		if (map[c.getX()][c.getY()] != null)
			return false;

		map[c.getX()][c.getY()] = unit;
		unit.setPosition(c);

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
	public Unit getUnit(Coordinate c)
	{
		int x = c.getX();
		int y = c.getY();

		assert x >= 0 && x < map.length;
		assert y >= 0 && x < map[0].length;

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
	public synchronized boolean moveUnit(Unit unit, Coordinate newCoordinate)
	{
		int originalX = unit.getPosition().getX();
		int originalY = unit.getPosition().getY();

		int newX = newCoordinate.getX();
		int newY = newCoordinate.getY();

		if (unit.getHitPoints() <= 0)
			return false;

		if (newX >= 0 && newX < Const.MAP_WIDTH)
			if (newY >= 0 && newY < Const.MAP_HEIGHT)
				if (map[newX][newY] == null)
				{
					//TODO: Check if valid move?
					if (putUnit(unit, newCoordinate))
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
	public synchronized void removeUnit(Coordinate c)
	{
		Unit unitToRemove = this.getUnit(c);
		if (unitToRemove == null)
			return; // There was no unit here to remove
		//TODO: Check if unit can be removed
		map[c.getX()][c.getY()] = null;
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

	public synchronized UnitType getType(Coordinate c)
	{
		Unit u = this.getUnit(c);
		if (u instanceof Player)
		{
			return UnitType.player;
		}
		else if (u instanceof Dragon)
		{
			return UnitType.dragon;
		}
		else
		{
			return UnitType.undefined;
		}
	}

	public synchronized void dealDamage(Coordinate c, int damage)
	{
		Unit u = this.getUnit(c);
		if (u != null)
		{
			u.adjustHitPoints(damage);
		}
	}

	public synchronized void healDamage(Coordinate c, int heal)
	{
		this.dealDamage(c, heal);
	}

	public Coordinate findPosition()
	{
		int attempt = 0;
		Coordinate c = new Coordinate();
		do
		{
			c.setX((int)(Math.random() * Const.MAP_WIDTH));
			c.setY((int)(Math.random() * Const.MAP_HEIGHT));
			attempt++;
		} while (this.getUnit(c) != null && attempt < 10);

		// If we didn't find an empty spot, we won't add a new player
		if (attempt >= 10)
			return null;

		return c;
	}
}
