package com.thirteen.shared.units.base;

import java.io.Serializable;

public class Coordinate implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6575274083079140708L;
	private int x, y;

	public Coordinate(int x, int y)
	{
		this.setCoordinates(x, y);
	}

	public Coordinate()
	{
		this(-1, -1);
	}

	public Coordinate(Coordinate position)
	{
		this.setCoordinates(position.getX(), position.getY());
	}

	public Coordinate(Coordinate position, int xMod, int yMod)
	{
		this.setCoordinates(position.getX() + xMod, position.getY() + yMod);
	}

	public int getX()
	{
		return this.x;
	}

	public int getY()
	{
		return this.y;
	}

	public void setCoordinates(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public boolean equals(Coordinate c)
	{
		return this.x == c.getX() && this.y == c.getY();
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int distanceTo(Coordinate target)
	{
		int deltaX = Math.abs(this.getX() - target.getX());
		int deltaY = Math.abs(this.getY() - target.getY());
		return deltaX + deltaY;
	}

	public Coordinate offset(int x, int y)
	{
		return new Coordinate(this, x, y);
	}

	public Direction directionTo(Coordinate target)
	{
		//TODO: Maybe create a list of directions left & up or right & up. If one side is blocked, do the other direction.
		if (this.getX() > target.getX())
			return Direction.left;
		else if (this.getX() < target.getX())
			return Direction.right;
		else if (this.getY() > target.getY())
			return Direction.up;
		else if (this.getY() < target.getY())
			return Direction.down;
		return null;
	}
}
