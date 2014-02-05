package namespaceGenerator;

/*
 * This class is the a object in the file system.
 */
public class NamespaceEntry {
	protected long name;
	protected long creationStamp;
	protected long size = -1;
	
	public NamespaceEntry(long c, long n)
	{
		this.name = n;
		this.creationStamp = c;
	}
	
	
	public long getName()
	{
		return this.name;
	}
	
	public long getSize()
	{
		if (this.size < 0)
			throw new IllegalStateException("Method getSize() cannot be called for file " + this.name + " because size has not been set yet.");
		return this.size;
	}
	
	public void setSize(long s)
	{
		if (s < 0)
			throw new IllegalArgumentException("File size cannot be negative: size(" + this.name + ") = " + this.size);
		
		this.size = s;
	}
	
	public void setCreationStamp(long newStamp)
	{
		this.creationStamp = newStamp;
	}
	
	public long getCreationStamp()
	{
		return this.creationStamp;
	}
	
	public String toString()
	{
		String out = "File " + name  + ": ";
		out += "\tCreation: " + this.creationStamp ;
		
		return out;
	}
}

