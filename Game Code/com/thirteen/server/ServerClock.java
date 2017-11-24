package com.thirteen.server;
/**
 * Representation of Lamport's logical(software) clock.
 * This class will be instantiated on every server,
 * representing time relative to event occurrences.
 * 
 */


public class ServerClock {
	int currentValue;
	
	//Everyone starts at 0
	public ServerClock(){
		currentValue=0;
	}
	
	//No params = increase by 1
	public synchronized void advanceClock(){
		currentValue++;
	}
	
	//Call when clock needs to advance by more than 1
	public synchronized void advanceClock(int recvValue){
		currentValue = (currentValue > recvValue) ? currentValue + 1 : recvValue + 1;
	}

	public synchronized int getClockValue(){
		return currentValue;
	}
	
	//Avoid overflow in lengthy simulations by first emptying queue
	//and then reseting all clocks (May not apply in our case).
	public void resetClock(){
		currentValue=0;
	}
	
}
