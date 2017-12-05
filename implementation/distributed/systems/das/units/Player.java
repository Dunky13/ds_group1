package distributed.systems.das.units;

import java.io.Serializable;
import distributed.systems.das.BattleField;
import distributed.systems.das.GameState;
import distributed.systems.das.units.extra.Coordinate;
import distributed.systems.das.units.extra.Direction;
import distributed.systems.das.units.extra.RelativeBound;
import distributed.systems.das.units.extra.UnitType;

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
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	public static final int MIN_HITPOINTS = 10;///???
	public static final int MAX_HITPOINTS = 20;
	public static final int MIN_ATTACKPOINTS = 1;
	public static final int MAX_ATTACKPOINTS = 10;

	public static final int MAX_HEAL_RANGE = 5;
	public static final int MAX_ATTACT_RANGE = 2;

	class ClosestPlayerDragon
	{
		public Coordinate player;
		public Coordinate dragon;

		public boolean bothFound()
		{
			return player != null && dragon != null;
		}
	}

	/**
	 * Create a player, initialize both the hit and the attackpoints.
	 */
	//include url and ports
	public Player(int x, int y)
	{
		/* Initialize the hitpoints and attackpoints */
		
		super(
			(int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS),
			(int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS));

		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;
		System.out.println("Player constructor called");
		if (!spawn(x, y))
			return; // We could not spawn on the battlefield

		/* Create a new player thread */
		//new Thread(this).start();
		runnerThread = new Thread(this);
		runnerThread.start();
		System.out.println("runnerThread returned");
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
		UnitType adjacentUnitType;

		this.running = true;
		//Add the improved AI here
		while (GameState.getRunningState() && this.running)
		{
			System.out.println("Looping" + this.getUnitID());
			try
			{
				/* Sleep while the player is considering its next move */
				Thread.currentThread().sleep((int)(timeBetweenTurns * 500 * GameState.GAME_SPEED));

				/* Stop if the player runs out of hitpoints */
				if (getHitPoints() <= 0)
					break;

				RelativeBound rb = new RelativeBound(this.getPosition(), 1);

				ClosestPlayerDragon cpd = this.findClosest(Player.MAX_HEAL_RANGE, rb);
				if (cpd.player != null)
				{
					this.healDamage(cpd.player.getX(), cpd.player.getY(), getAttackPoints());
				}
				else if (cpd.dragon != null && this.getPosition().distanceTo(cpd.dragon) <= Player.MAX_ATTACT_RANGE)
				{
					this.dealDamage(cpd.dragon.getX(), cpd.dragon.getY(), getAttackPoints());
				}
				else
				{
					cpd = this.findClosest(BattleField.MAP_BOUND.range(), rb, false, true);

					//TODO - multiple directions?
					direction = this.getPosition().directionTo(cpd.dragon);
					Coordinate target = this.makeMove(direction);
					if (target == null)
					{
						continue;
					}
					this.moveUnit(target.getX(), target.getY());
				}
				//				// Randomly choose one of the four wind directions to move to if there are no units present
				//				direction = Direction.values()[ (int)(Direction.values().length * Math.random()) ];
				//				adjacentUnitType = UnitType.undefined;
				//
				//				switch (direction) {
				//					case up:
				//						if (this.getY() <= 0)
				//							// The player was at the edge of the map, so he can't move north and there are no units there
				//							continue;
				//						
				//						targetX = this.getX();
				//						targetY = this.getY() - 1;
				//						break;
				//					case down:
				//						if (this.getY() >= BattleField.MAP_HEIGHT - 1)
				//							// The player was at the edge of the map, so he can't move south and there are no units there
				//							continue;
				//
				//						targetX = this.getX();
				//						targetY = this.getY() + 1;
				//						break;
				//					case left:
				//						if (this.getX() <= 0)
				//							// The player was at the edge of the map, so he can't move west and there are no units there
				//							continue;
				//
				//						targetX = this.getX() - 1;
				//						targetY = this.getY();
				//						break;
				//					case right:
				//						if (this.getX() >= BattleField.MAP_WIDTH - 1)
				//							// The player was at the edge of the map, so he can't move east and there are no units there
				//							continue;
				//
				//						targetX = this.getX() + 1;
				//						targetY = this.getY();
				//						break;
				//				}
				//
				//				// Get what unit lies in the target square
				//				adjacentUnitType = this.getType(targetX, targetY);
				//				
				//				switch (adjacentUnitType) {
				//					case undefined:
				//						// There is no unit in the square. Move the player to this square
				//						this.moveUnit(targetX, targetY);
				//						break;
				//					case player:
				//						// There is a player in the square, attempt a healing
				//						this.healDamage(targetX, targetY, getAttackPoints());
				//						break;
				//					case dragon:
				//						// There is a dragon in the square, attempt a dragon slaying
				//						this.dealDamage(targetX, targetY, getAttackPoints());
				//						break;
				//				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private ClosestPlayerDragon findClosest(final int MAX_DIST, RelativeBound rb)
	{
		return this.findClosest(MAX_DIST, rb, true, true);
	}

	private ClosestPlayerDragon findClosest(final int MAX_DIST, RelativeBound rb, boolean searchPlayer, boolean searchDragon)
	{
		ClosestPlayerDragon cpd = new ClosestPlayerDragon();
		int distance = rb.range();
		while (distance <= MAX_DIST)
		{
			for (int i = rb.getLeft(); i <= rb.getRight(); i++)
			{
				for (int j = rb.getTop(); j <= rb.getBottom(); j++)
				{
					//This player - skip
					if (i == 0 && j == 0)
						continue;

					// Get coordinate relative to this player.
					Coordinate tmpC = new Coordinate(this.getPosition(), i, j);
					if (this.getPosition().distanceTo(tmpC) > distance)
						continue;

					Unit u = this.getUnit(tmpC.getX(), tmpC.getY());
					if (u == null)
						continue;

					UnitType ut = this.getType(u.getX(), u.getY());
					if (ut == UnitType.undefined)
						continue;

					if (searchDragon && ut == UnitType.dragon)
					{
						if (cpd.dragon == null)
							cpd.dragon = tmpC;
						if (!searchPlayer || cpd.bothFound())
							return cpd;
						else
							continue;
					}

					if (searchPlayer && ut == UnitType.player)
					{
						Player other = (Player)u;

						// If hitpoints below 50%
						if (other.getHitPoints() < other.getMaxHitPoints() / 2.0)
						{
							if (cpd.player == null)
								cpd.player = tmpC;
							if (!searchDragon || cpd.bothFound())
								return cpd;
						}
					}

					if (cpd.bothFound())
						return cpd;
				}
			}
			rb.incrementRange();
			distance = rb.range();
		}
		return cpd;
	}

	private Coordinate makeMove(Direction direction)
	{
		switch (direction)
		{
		case up:
			if (this.getY() <= 0)
				// The player was at the edge of the map, so he can't move north and there are no units there
				return null;

			return this.getPosition().offset(0, -1);
		case down:
			if (this.getY() >= BattleField.MAP_HEIGHT - 1)
				// The player was at the edge of the map, so he can't move south and there are no units there
				return null;
			return this.getPosition().offset(0, 1);
		case left:
			if (this.getX() <= 0)
				// The player was at the edge of the map, so he can't move west and there are no units there
				return null;
			return this.getPosition().offset(-1, 0);
		case right:
			if (this.getX() >= BattleField.MAP_WIDTH - 1)
				// The player was at the edge of the map, so he can't move east and there are no units there
				return null;
			return this.getPosition().offset(1, 0);
		}
		return null;
	}

}
