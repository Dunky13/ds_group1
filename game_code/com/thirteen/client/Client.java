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
	
	private void connect() {
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.connect();
		this.battlefield = this.getBattleField();
		this.player = new Player(battlefield);
		if(!this.player.running())
		this.player.stopRunnerThread();
	}
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Thread> runningClients = new ArrayList<Thread>();
		for(int j = 0; j < Client.playerCount; j++) {
			Client c = new Client();
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

	private BattleField getBattleField() {
		// TODO Auto-generated method stub
		return null;
	}

	




}
