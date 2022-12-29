package ibpe.model;

import ibpe.Utils;

import java.util.*;

public abstract class Node extends Element 
{
	// TODO: rename this class, e.g. to Composite, since Node is used in the context of graphs.
	
	public static final String PROPERTY_BOUNDS = "NodeBounds";
	public static final String PROPERTY_ADD = "NodeAddChild";
	public static final String PROPERTY_REMOVE = "NodeRemoveChild";
	
	
	public static final HashMap<String,Node> EMPTY_NS = new HashMap<String,Node>();
	
	protected List<Element> children;
	

	public Node()
	{
		children = new ArrayList<Element>();
	}

	
	public void add(Element e) 
	{
		 add(e,children.size());
	}
	
	
	public void add(Element e, int pos)
	{
		assert !children.contains(e);
		children.add(pos, e);
		e.setParent(this);
		getListeners().firePropertyChange(PROPERTY_ADD, null, e);
	}

	
	public boolean remove( Element e )
	{
		if (children.remove(e))
		{
			e.setParent(null);
			getListeners().firePropertyChange(PROPERTY_REMOVE,e,null);
			return true;
		}
		else
			return false;
	}
	

	public List<Element> getChildren() 
	{
		for (Element ch : children)
			assert ch.parent==this;
		return new ArrayList<Element>(children);
	}
	
	
	/**
	 *  Return a preorder linearization of all contents. Irreflexive.
	 */
	public List<Element> getDescendants()
	{
		ArrayList<Element> retval = new ArrayList<Element>();
		
		for (Element e : this.children)
		{
			retval.add(e);
			if (e instanceof Node)
				retval.addAll(((Node)e).getDescendants());
		}
		return retval;		
	}
	
	
	public <T extends Element> List<T> getChildrenOfType( Class<T> t ) 
	{
		return Utils.filterObjectsOfType(children,t);
	}
	
	
	public <T extends Element> List<T> getDescendantsOfType( Class<T> t )
	{
		return Utils.filterObjectsOfType(getDescendants(),t);
	}

	
	public HashMap<String,Node> getNamespace()
	{
		return EMPTY_NS;
	}
}