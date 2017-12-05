package distributed.systems.executors;

import distributed.systems.core.Constants;
import distributed.systems.core.Message;
import distributed.systems.core.ServerAndPorts;
import distributed.systems.core.logger.Logger;
import distributed.systems.das.BattleField;
import distributed.systems.das.GameState;
import distributed.systems.das.presentation.BattleFieldViewer;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Player;
import distributed.systems.server.ServerSendReceive;

public class ServerExecutor
{
	public static final int MIN_PLAYER_COUNT = 30;
	public static final int MAX_PLAYER_COUNT = 60;
	public static final int DRAGON_COUNT = 20;
	public static final int TIME_BETWEEN_PLAYER_LOGIN = 5000; // In milliseconds
	public static BattleField battlefield; 
	public static int playerCount;
	public static Logger logger;
	static String testpath; //= getClass().getProtectionDomain().getCodeSource().getLocation().toString();
	private ServerAndPorts sp;
	private ServerSendReceive serverSendReceive;
	//private BattleField b;

	ServerExecutor(int serverID)
	{
		sp = Constants.SERVER_PORT[serverID];
		battlefield = BattleField.getBattleField();
		battlefield.init(this);
		System.out.println("b4 serverSendReceive");
		serverSendReceive = new ServerSendReceive(this);
		System.out.println("after serverSendReceive");
		logger = new Logger();
		testpath = getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		if (serverID == 0 || serverID == 1)
		{
			GameState.setAmIaLoger(true);
			logger.logText("I am a logger server (id:" + serverID + ")");
			System.out.println("I am a logger!");
		}
	}

	public static void main(String[] args)
	{
		System.out.println("Reading command line arguements");
		if (args.length != 1)
		{
			System.out.println("missing serverID argument");
			System.exit(1);
		}
		ServerExecutor se = new ServerExecutor(Integer.parseInt(args[0]));
		System.out.println(testpath);
		//battlefield = BattleField.getBattleField();
		if (GameState.getAmIaLogger())logger.createLogFile();
		if (GameState.getAmIaLogger())logger.logText("Game started!");
		System.out.println("Game started!");
		/* All the dragons connect */
		for(int i = 0; i < DRAGON_COUNT; i++) {
			/* Try picking a random spot */
			int x, y, attempt = 0;
			do {
				x = (int)(Math.random() * BattleField.MAP_WIDTH);
				y = (int)(Math.random() * BattleField.MAP_HEIGHT);
				attempt++;
			} while (battlefield.getUnit(x, y) != null && attempt < 10);

			// If we didn't find an empty spot, we won't add a new dragon
			if (battlefield.getUnit(x, y) != null) break;
			
			final int finalX = x;
			final int finalY = y;

			/* Create the new dragon in a separate
			 * thread, making sure it does not 
			 * block the system.
			 */
			new Thread(new Runnable() {
				public void run() {
					new Dragon(finalX, finalY);
				}
			}).start();

		}

		if (GameState.getAmIaLogger())logger.logText("Dragons connected, Initializing players...");
		/* Initialize a random number of players (between [MIN_PLAYER_COUNT..MAX_PLAYER_COUNT] */
		playerCount = (int)((MAX_PLAYER_COUNT - MIN_PLAYER_COUNT) * Math.random() + MIN_PLAYER_COUNT);
		for(int i = 0; i < playerCount; i++)
		{
			/* Once again, pick a random spot */
			int x, y, attempt = 0;
			do {
				x = (int)(Math.random() * BattleField.MAP_WIDTH);
				y = (int)(Math.random() * BattleField.MAP_HEIGHT);
				attempt++;
			} while (battlefield.getUnit(x, y) != null && attempt < 10);

			// If we didn't find an empty spot, we won't add a new player
			if (battlefield.getUnit(x, y) != null) break;

			final int finalX = x;
			final int finalY = y;

			/* Create the new player in a separate
			 * thread, making sure it does not 
			 * block the system.
			 */
			new Thread(new Runnable() {
				public void run() {
					new Player(finalX, finalY);
				}
			}).start();
			
		}
		System.out.println("All players initialized. Starting viewer...");
		if (GameState.getAmIaLogger())logger.logText("All players initialized. Starting viewer...");
		/* Spawn a new battlefield viewer */
		new Thread(new Runnable() {
			public void run() {
				new BattleFieldViewer();
			}
		}).start();
		
		/* Add a random player every (5 seconds x GAME_SPEED) so long as the
		 * maximum number of players to enter the battlefield has not been exceeded. 
		 */
		while(GameState.getRunningState()) {
			try {
				Thread.sleep((int)(5000 * GameState.GAME_SPEED));

				// Connect a player to the game if the game still has room for a new player
				if (playerCount >= MAX_PLAYER_COUNT) continue;

				// Once again, pick a random spot
				int x, y, attempts = 0;
				do {
					// If finding an empty spot just keeps failing then we stop adding the new player
					x = (int)(Math.random() * BattleField.MAP_WIDTH);
					y = (int)(Math.random() * BattleField.MAP_HEIGHT);
					attempts++;
				} while (battlefield.getUnit(x, y) != null && attempts < 10);

				// If we didn't find an empty spot, we won't add the new player
				if (battlefield.getUnit(x, y) != null) continue;

				final int finalX = x;
				final int finalY = y;

				if (battlefield.getUnit(x, y) == null) {
					new Player(finalX, finalY);
					/* Create the new player in a separate
					 * thread, making sure it does not 
					 * block the system.
					 *
					new Thread(new Runnable() {
						public void run() {
							new Player(finalX, finalY);
						}
					}).start();
					*/
					playerCount++;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/* Make sure both the battlefield and
		 * the socketmonitor close down.
		 */
		BattleField.getBattleField().shutdown();
		System.exit(0); // Stop all running processes
	}

		
		
	

	public void sendMessageToMany(Message msg)
	{
		serverSendReceive.sendToAll(msg);
	}

	public void sendMessageToOne(ServerAndPorts sp, Message msg)
	{
		serverSendReceive.sendToOne(sp, msg);
	}

	public void receiveMessage(Message msg)
	{
		battlefield.receivedClientMessage(msg);
	}

	public void receiveServerMessage(Message msg)
	{
		battlefield.receivedServerMessage(msg);
	}

	public ServerAndPorts getServerPortData()
	{
		return this.sp;
	}
}
