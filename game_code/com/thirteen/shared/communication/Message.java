package com.thirteen.shared.communication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import com.thirteen.shared.units.base.Unit;

public class Message implements Serializable {
	
	private static final long serialVersionUID = -5755419668337780923L;

	HashMap<String,Serializable> contents;
	
	public Message(){
		contents = new HashMap<String,Serializable>();
	
	}
	
	public void put(String key,Serializable value){
		contents.put(key, value);
	}
	
	public void put (String key, Unit value){
		contents.put(key, value);
	}
	
	public void put (String key, int value){
		contents.put(key, value);
	}
	
	public Serializable get(String key) {
		return contents.get(key);
	}
	
	
	public String toString(){
		return contents.toString();
	}
	
	public Iterator<?> getIterator() {
		return contents.entrySet().iterator();
	}
	
}