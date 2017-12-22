package mchw1.profiling;

public class EdgeData
{
	public enum Type
	{
		CALL,
		DATA
	}
	
	
	public final Type type;
	
	
	public
	EdgeData()
	{
		this.type = Type.CALL;
	}
	
	
	public
	EdgeData(Type type)
	{
		this.type = type;
	}
}
