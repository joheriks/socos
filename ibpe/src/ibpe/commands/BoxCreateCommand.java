package ibpe.commands;

import ibpe.model.BoxElement;
import ibpe.model.Element;
import ibpe.model.Node;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

public class BoxCreateCommand extends Command {
	
	private Element element;
	private Node parent;
	private Rectangle box = new Rectangle();

	public void setObject(Object newObject)
	{
		if (newObject instanceof Element)
			element = (Element)newObject;
	}

	public void setParent(Node model) {
		parent = model;
	}

	public void setLocation(Point location)
	{
		box.setLocation(location);
		box.setSize(-1, -1);
	}
	
	public boolean canExecute() {
		return parent != null && element != null;
	}
	
	public void execute()
	{
		if (element instanceof BoxElement)
			((BoxElement)element).setBounds(box);
		
		//element.setUniqueName(parent);
				
		parent.add(element);
	}
	
	public void undo() {
		parent.remove(element);
	}

}
