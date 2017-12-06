package distributed.systems.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import distributed.systems.das.units.Unit;

public class Message implements Serializable
{

	public enum ImportantMessageKeys {
		id, port, x, y, request, LC
	}
	private static final long serialVersionUID = -2791452299144895125L;
	HashMap<String, Serializable> contents;

	public Message()
	{
		contents = new HashMap<String, Serializable>();
	}
	
	public boolean containsKey(String key)
	{
		return contents.containsKey(key);
	}
	
	public void put(String key, Serializable value)
	{
		contents.put(key, value);
	}

	public void put(String key, Unit value)
	{
		contents.put(key, value);
	}

	public void put(String key, int value)
	{
		contents.put(key, value);
	}

	public Serializable get(String key)
	{
		return contents.get(key);
	}
	
	public int getInt(String key) {
		Serializable val = contents.get(key);
		return Integer.parseInt((String) val.toString());
	}

	public String toString()
	{
		return contents.toString();
	}

	public Iterator<?> getIterator()
	{
		return contents.entrySet().iterator();
	}

	public void removeMsgKeyVal(String key)
	{
		contents.remove(key);
	}

	public void setContent(HashMap<String,Serializable> hMap) {
		contents=hMap;		
	}
	
	public byte[] serialize() throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(contents);
		return bos.toByteArray();
	}

	public static Message deserialze(byte[] bytes) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = new ObjectInputStream(bis);
		HashMap<String,Serializable> tmpMap = (HashMap) in.readObject();
		Message msg = new Message();

		msg.setContent(tmpMap);
		return msg;
	}
}
