package src.units.extra;

public class Bound
{
	protected int bottom;
	protected int right;
	protected int top;
	protected int left;

	public Bound(int width, int height)
	{
		this(0, 0, width, height);
	}

	public Bound(int left, int top, int right, int bottom)
	{
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	/**
	 * Checks if Coordinate is in Bound
	 * 
	 * @param c
	 * @return true when Coordinate is in Bound, false if not
	 */
	public boolean hasCoordinate(Coordinate c)
	{
		boolean inX = this.left <= c.getX() && this.right > c.getX();
		boolean inY = this.top <= c.getY() && this.bottom > c.getY();
		return inX && inY;
	}

	/**
	 * @return the bottom
	 */
	public int getBottom()
	{
		return bottom;
	}

	/**
	 * @return the right
	 */
	public int getRight()
	{
		return right;
	}

	/**
	 * @return the top
	 */
	public int getTop()
	{
		return top;
	}

	/**
	 * @return the left
	 */
	public int getLeft()
	{
		return left;
	}

	public int range()
	{
		return Math.max(Math.abs(left - right), Math.abs(top - bottom));
	}

	@Override
	public String toString()
	{
		return this.left + " - " + this.right + " ; " + this.top + " - " + this.bottom;
	}

}
