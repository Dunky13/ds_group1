/**
 * 
 */
package com.thirteen.shared.units.base;

import com.thirteen.shared.Const;

/**
 * @author mwent
 *
 */
public class RelativeBound extends Bound
{
	final private Bound max_bound;
	private Coordinate coordinate;

	public RelativeBound(Coordinate c, int range)
	{
		this(c, range, Const.MAP_BOUND);
	}

	public RelativeBound(Coordinate c, int range, Bound parentBound)
	{
		super(-range, -range, range, range);
		this.max_bound = parentBound;
		this.coordinate = c;
	}

	public void incrementRange()
	{
		incrementLeftBound();
		incrementRightBound();
		incrementTopBound();
		incrementBottomBound();
	}

	public int range()
	{
		// Negative left & top because their range is defined as -range.
		return Math.max(Math.max(-left, right), Math.max(-top, bottom));
	}

	private void incrementLeftBound()
	{
		if (this.coordinate.getX() + this.left <= this.max_bound.getLeft())
			this.left = this.max_bound.getLeft();
		else
			this.left--;
	}

	private void incrementRightBound()
	{
		if (this.coordinate.getX() + this.right >= this.max_bound.getRight())
			this.right = this.max_bound.getRight() - 1;
		else
			this.right++;
	}

	private void incrementTopBound()
	{
		if (this.coordinate.getY() + this.top <= this.max_bound.getTop())
			this.top = this.max_bound.getTop();
		else
			this.top--;
	}

	private void incrementBottomBound()
	{
		if (this.coordinate.getY() + this.bottom >= this.max_bound.getBottom())
			this.bottom = this.max_bound.getBottom() - 1;
		else
			this.bottom++;
	}

	public boolean hasCoordinate(Coordinate c)
	{
		return false;
	}
}