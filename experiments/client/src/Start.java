package src;

import src.presentation.BattleFieldViewer;
import src.units.Dragon;
import src.units.Player;

public class Start
{

	Start()
	{
		BattleFieldViewer bfv = new BattleFieldViewer();
	}

	public void start()
	{
		Player p1 = new Player(7, 3);
		Dragon d1 = new Dragon(1, 5);
		Player p2 = new Player(9, 8);

		startDragon(d1);
		startPlayer(p1);
		startPlayer(p2);

	}

	public void startPlayer(Player u)
	{
		new Thread(u).start();
	}

	public void startDragon(Dragon d)
	{
		new Thread(d).start();
	}

	public static void main(String[] args)
	{
		new Start().start();
	}

}
