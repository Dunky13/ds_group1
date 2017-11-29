package distributed.systems.core;

import java.util.HashMap;
import java.io.Serializable;

import distributed.systems.das.units.Unit;


public class Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
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
	
		
	public Iterator getIterator() {
		return contents.entrySet().iterator();
	}

	public void removeMsgKeyVal(String key) {
		contents.remove(key);
	}
	
	
}
