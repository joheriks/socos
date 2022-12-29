package ibpe.model;


public class TextRow extends Element
{
	public TextRow() 
	{
		super();
	}
	
	public TextRow( String contents ) 
	{
		setText(contents);
	}
	
	public boolean allowedChild(Element kid) 
	{
		return false;
	}
	
	public void errorAt(int columnIndex)
	{
		//TODO: fixme
	}
		
}