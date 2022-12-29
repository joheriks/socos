package ibpe.model;

import java.beans.*;
import java.util.*;


public abstract class Element 
{
	public static final String PROPERTY_TEXT = "ElementText";
	
	protected String text;
	protected Node parent; 

	public Element() 
	{
		text = null;
		parent = null;
	}
	
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) 
	{ 
		listeners.addPropertyChangeListener(listener); 
	}
	
	public PropertyChangeSupport getListeners()
	{ 
		return listeners; 
	} 
	
	public void removePropertyChangeListener(PropertyChangeListener listener) 
	{ 
		listeners.removePropertyChangeListener(listener); 
	}
	
	public void setParent(Node parent) 
	{ 
		this.parent = parent;
	} 
	
	public Node getParent() 
	{ 
		return parent; 
	}	
	
	public String getText() {
		return text;
	}
	
	public void setText(String newtext)
	{
		assert newtext!=null;
		String oldtext = text;
		text = newtext;
		getListeners().firePropertyChange(PROPERTY_TEXT, oldtext,text);
	}
	
	
	public int getIndex() 
	{
		return (parent==null) ? -1 : parent.children.indexOf(this);
	}
	
	
	/**
	 *  Return a reverse list of ancestors. Irreflexive.
	 */
	public List<Element> getAncestors()
	{
		ArrayList<Element> retval = new ArrayList<Element>();
		for (Element e = this.getParent(); e!=null; e=e.getParent())
			retval.add(e);
		return retval;
	}


	/**
	 * Finds the closest parent to <i>this</i> of type parentClass
	 * @param parentClass The type of class of the parent
	 * @return The closest parent of type parentClass
	 */
	public Object getParentOfType(Class<? extends Element> parentClass)
	{
		Element temp = this;
		
		while (temp != null && temp instanceof Element &&
				!parentClass.isAssignableFrom(temp.getClass())
			   /*!temp.getClass().equals(parentClass)*/)
			temp = temp.getParent();
			
		return temp;
	}
	
	
}
