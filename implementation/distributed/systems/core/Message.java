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

	private static final long serialVersionUID = -2791452299144895125L;
	HashMap<String, Serializable> contents;

	public Message()
	{
		contents = new HashMap<String, Serializable>();

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

	public byte[] serialize() throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(this);
		return bos.toByteArray();
	}

	public static Message deserialze(byte[] bytes) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = new ObjectInputStream(bis);
		return (Message)in.readObject();
	}
}
