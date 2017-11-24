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

}
