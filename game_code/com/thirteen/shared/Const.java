package com.thirteen.shared;

import com.thirteen.shared.communication.ServerLocation;

public interface Const
{
	int NUMBER_OF_SERVERS = 5;

	int MIN_PLAYER_COUNT = 30;
	int MAX_PLAYER_COUNT = 60;
	int DRAGON_COUNT = 20;
	int TIME_BETWEEN_PLAYER_LOGIN = 5000; // In milliseconds

	int MAP_WIDTH = 25;
	int MAP_HEIGHT = 25;
	
	ServerLocation[] SERVER_LOCATIONS = new ServerLocation[] {
			new ServerLocation("localhost", 8480),
			new ServerLocation("localhost", 8481),
			new ServerLocation("localhost", 8482),
			new ServerLocation("localhost", 8483),
			new ServerLocation("localhost", 8484),
			};
}
