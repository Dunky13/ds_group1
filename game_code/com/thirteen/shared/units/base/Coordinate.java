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

	/**
	 * Checks lower, upper bound of 2D coordinate lower_val is inclusive.
	 * upper_val is exclusive.
	 * 
	 * @param lower_x
	 * @param upper_x
	 * @param lower_y
	 * @param upper_y
	 * @return Whether or not Coordinate is in bounds.
	 */
	public boolean inBound(int lower_x, int upper_x, int lower_y, int upper_y)
	{
		return this.x >= lower_x && this.x < upper_x && this.y >= lower_y && this.y < upper_y;
	}
}
