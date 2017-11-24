/**
 * 
 */
package com.thirteen.client;

import java.util.ArrayList;

import com.thirteen.shared.*;
import com.thirteen.shared.units.Player;

/**
 * @author mwent
 *
 */
public class Client implements Runnable {
	public static final int MIN_PLAYER_COUNT = Core.MIN_PLAYER_COUNT / Core.NUMBER_OF_SERVERS;
	public static final int MAX_PLAYER_COUNT = Core.MAX_PLAYER_COUNT / Core.NUMBER_OF_SERVERS;
	public static final int DRAGON_COUNT = Core.DRAGON_COUNT / Core.NUMBER_OF_SERVERS;
	public static final int playerCount = (int)((MAX_PLAYER_COUNT - MIN_PLAYER_COUNT) * Math.random() + MIN_PLAYER_COUNT);
	private BattleField battlefield;
	private Player player;
	Client(BattleField b) {
		this.battlefield = b;
	}
	
	private void connect() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(!this.findPosition()) {
			return;
		}
		this.connect();
	}
	
	private boolean findPosition() {
		int x, y, attempt = 0;
		do {
			x = (int)(Math.random() * BattleField.MAP_WIDTH);
			y = (int)(Math.random() * BattleField.MAP_HEIGHT);
			attempt++;
		} while (battlefield.getUnit(x, y) != null && attempt < 10);

		// If we didn't find an empty spot, we won't add a new player
		if (attempt >= 10) return false;

		final int finalX = x;
		final int finalY = y;

		/* Create the new player in a separate
		 * thread, making sure it does not 
		 * block the system.
		 */
		this.player = new Player(finalX, finalY);
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Thread> runningClients = new ArrayList<Thread>();
		BattleField b = Client.getBattleField();
		for(int j = 0; j < Client.playerCount; j++) {
			Client c = new Client(b);
			Thread t = new Thread(c);
			runningClients.add(t);
			t.start();
		}
		
		for(Thread t : runningClients) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static BattleField getBattleField() {
		// TODO Auto-generated method stub
		return null;
	}

	




}
