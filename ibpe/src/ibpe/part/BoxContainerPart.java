package ibpe.part;

import ibpe.IBPEditor;
import ibpe.editpolicies.*;
import ibpe.figure.*;
import ibpe.model.*;

import java.beans.PropertyChangeEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.ConnectionDragCreationTool;
import org.eclipse.gef.tools.MarqueeDragTracker;

public class BoxContainerPart extends AbstractIBPEditPart<BoxContainer>
{
	public static final int nDirections = Direction.values().length;

	
	public enum Direction
	{
		LEFT_TO_RIGHT,
		RIGHT_TO_LEFT,
		TOP_TO_BOTTOM,
		BOTTOM_TO_TOP;
		
		public static final int toInt(Direction en)
		{
			switch (en) {
				case LEFT_TO_RIGHT: return 0; 
				case RIGHT_TO_LEFT: return 1; 
				case TOP_TO_BOTTOM: return 2; 
				case BOTTOM_TO_TOP: return 3; 
				default: return -1; 
			}
		}
		public static final Direction fromInt(int num)
		{
			switch (num) {
				case 0: return LEFT_TO_RIGHT;
				case 1: return RIGHT_TO_LEFT;
				case 2: return TOP_TO_BOTTOM;
				case 3: return BOTTOM_TO_TOP;
				default: return null;
			}
		}
	};

	/**
	 * <b>system</b> A list of lists of BoxElementParts.
	 * The outermost list is of size 4, and each one of those lists consist of lists of BoxElementParts.
	 * This is used to quickly find BoxElementParts next to one another, so as to avoid looping through
	 * the whole list every time (which could become time consuming).
	 * The four outermost lists stand for the directions up, down, left, right. Each BoxElementPart has a
	 * place in each list, depending on in what order the parts are "encountered" when moving e.g.
	 * from down-to-up or right-to-left in a 2d coordinate system.
	 */
	private List<List<BoxElementPart>>[] system;
	private BoxContainerFigure boxFigure;
	private int xySpacing = IBPEditor.GRID_SIZE;
	
	@SuppressWarnings("unchecked")
	@Override
	protected IFigure createFigure()
	{
		if (boxFigure != null)
			return boxFigure;
		 
		system = new LinkedList[nDirections];
		for(int i = 0; i < nDirections; i++) {
			system[i] = new LinkedList();
		}
		boxFigure = new BoxContainerFigure();
		return boxFigure;
	}

	
	/**
	 * Make sure that a newly added model element gets an appropriate location and size
	 * in the BoxContainer
	 */
	protected void addChild(EditPart childEditPart, int index)
	{
		super.addChild(childEditPart, index);
		
		BoxElementPart<?> ep = (BoxElementPart<?>) childEditPart;
		BoxElement model = (BoxElement) ep.getModel();
		Rectangle rect = model.getBounds();

		// if model has no size information, e.g. it has just been created.
		if(rect.x == 0 && rect.y == 0 && rect.height < 1 && rect.width < 1)
		{
			int x = 0, y = 0;
			int bottomToTop = Direction.toInt(Direction.BOTTOM_TO_TOP);
			
			//if((ep instanceof CompositePart || ep instanceof BranchPart) && !system[0].isEmpty())
			if (!system[0].isEmpty())
			{
				x = 0;
				y = system[bottomToTop].get(0).get(0).getModelBounds().getBottom().y + xySpacing;
			}
			model.setPosition(new Point(x, y));
		}
		if (rect.height < 1 || rect.width < 1)
		{
			/*if(childEditPart instanceof CompositePart)
				rect.setSize(((ContainerFigure)ep.getFigure()).getPreferredSizeSnap());
			else*/
				rect.setSize(ep.getFigure().getPreferredSize());
		}
		addEditPart(ep);
		childEditPart.refresh();
	}

	
	/* 
	 * Make sure we also remove child from system-list.
	 */
	protected void removeChild(EditPart child)
	{
		super.removeChild(child);
		removeEditPart((BoxElementPart<?>)child);
	}
	
	
	/**
	 * This is where a new BoxElementPart is added to <b>system</b>
	 * @param part the part to be added
	 */
	public void addEditPart(BoxElementPart part)
	{
		Rectangle b = ((BoxElement)part.getModel()).getBounds().getCopy();

		// Do the following for each direction
		for (Direction dir : Direction.values())
		{
			List<BoxElementPart> list = new LinkedList<BoxElementPart>();
			int d = Direction.toInt(dir);

			if (system[d].size() == 0)
			{
				system[d].add(0, list);
				system[d].get(0).add(part);
				part.setParent(d, system[d].get(0));
				continue;
			}
			// Compare the part to be added with the parts already there
			for (int i = 0; i < system[d].size(); i++)
			{
				Rectangle compare = ((BoxElement) system[d].get(i).get(0).getModel()).getBounds();
						
				 /* Look for the right column/row index.
				  * If coordinates are equal we've found the right column.
				  * If we've iterated past our coordinates in the given direction,
				  * a new LinkedList is created
				  */
				if (	
						(dir == Direction.LEFT_TO_RIGHT && (compare.x > b.x)) ||
					    (dir == Direction.TOP_TO_BOTTOM && (compare.y > b.y)) ||
					    (dir == Direction.RIGHT_TO_LEFT && (compare.x + compare.width ) < (b.x + b.width)) ||
					    (dir == Direction.BOTTOM_TO_TOP && (compare.y + compare.height) < (b.y + b.height))
					) {
						system[d].add(i, list);
						system[d].get(i).add(part);
						part.setParent(d, system[d].get(i));
						break;
				} 
				else if (
						/* If there's a LinkedList for the given row/column, find the right index to add at.
						 * This is done by iterating, and when we're past our coordinates in the given direction,
						 * the part is inserted at the given index.
						 */
						
						(dir == Direction.LEFT_TO_RIGHT && (compare.x == b.x)) || 
						(dir == Direction.TOP_TO_BOTTOM && (compare.y == b.y)) || 
						(dir == Direction.RIGHT_TO_LEFT && (compare.x + compare.width ) == (b.x + b.width)) || 
						(dir == Direction.BOTTOM_TO_TOP && (compare.y + compare.height) == (b.y + b.height))
				){
					int j;
					for (j = 0; j < system[d].get(i).size(); j++)
					{
						compare = ((BoxElement) system[d].get(i).get(j).getModel()).getBounds();
						
						if (
								((dir == Direction.LEFT_TO_RIGHT || dir == Direction.RIGHT_TO_LEFT) && (compare.y > b.y)) || 
								((dir == Direction.TOP_TO_BOTTOM || dir == Direction.BOTTOM_TO_TOP) && (compare.x > b.x))
						   )
						{
							// We've found the index
							break;
						} 
						else if (
								((dir == Direction.LEFT_TO_RIGHT || dir == Direction.RIGHT_TO_LEFT) && (compare.y == b.y)) ||
								((dir == Direction.TOP_TO_BOTTOM || dir == Direction.BOTTOM_TO_TOP) && (compare.x == b.x))
								)
						{
							System.out.println("Tried to insert box in already occupied position (should've been nested inside the part at the given position)");
						}
					}
					// Inserting at index, or last in list
					system[d].get(i).add(j, part);
					part.setParent(d, system[d].get(i));
					break;
				}
				else if(i == system[d].size() - 1)
				{
					system[d].add(list);
					system[d].get(i+1).add(part);
					part.setParent(d, system[d].get(i+1));
					break;
				}
			}
		}
	}

	
	/**
	 * Removes a BoxElementPart from <b>system</b>
	 * @param node the BoxElementPart to be removed
	 */
	public void removeEditPart(BoxElementPart<?> node)
	{
		for (int d = 0; d < nDirections; d++)
		{
			List<BoxElementPart<?>> list = node.getParent(d);
			list.remove(node);
			if (list.isEmpty())
				system[d].remove(list);
		}
	}

	
	/**
	 * Calculates a Rectangle which contains all <i>parts</i> (after they have been translated by
	 * moveDelta) and then calls the main move-method
	 */
	public Queue<BoxElementPart<?>> move(List<BoxElementPart<?>> parts, Point moveDelta)
	{
		// Only a copy is translated, not the model bounds themselves
		Rectangle rect = parts.get(0).getModelBounds().getCopy().translate(moveDelta);
		for (int i = 1; i < parts.size(); i++)
			rect.union(parts.get(i).getModelBounds().getCopy().translate(moveDelta));
		
		return move(parts, rect);
	}
	
	
	/**
	 * This method handles moving and resizing of BoxElements. The information of where a BoxElement
	 * will be moved is stored in the BoxElements themselves, which are returned in a queue.
	 * @param parts the parts to be moved
	 * @param rect a Rectangle containing all <b>parts</b>' bounds
	 * @return a queue of BoxElementParts that are to be moved
	 */
	public Queue<BoxElementPart<?>> move(List<BoxElementPart<?>> parts, Rectangle rect)
	{
		List<PriorityQueue<BoxElementPart<?>>> toBeMoved = new LinkedList<PriorityQueue<BoxElementPart<?>>>();
		
		for ( @SuppressWarnings("unused") 
			  Direction dir : Direction.values())
		{
			// One PriorityQueue for each direction
			toBeMoved.add(new PriorityQueue<BoxElementPart<?>>());
		}
		// Collect all intersecting children
		for (int i = 0; i < system[0].size(); i++) {
			for(int j = 0; j < system[0].get(i).size(); j++)
			{
				BoxElementPart<?> comparePart = system[0].get(i).get(j);
				Rectangle compareBounds = comparePart.getMovedBounds(); //Hmm
				
				updateOldEditParts(parts, comparePart);
							
				if(rect.intersects(compareBounds) && (parts == null || !parts.contains(comparePart)))
				{
					Direction direction = getDirection(rect, compareBounds);
					comparePart.setMoveDirection(direction);
					
					// Calculate new relative position based on bounds and direction
					comparePart.setMoveDelta(getRelativePosition(rect, compareBounds, direction));
					toBeMoved.get(Direction.toInt(direction)).add(comparePart);
				}
			}
		}
		// Collect all children overlapping toBeMoved children
		Queue<BoxElementPart<?>> allMovedParts = new LinkedList<BoxElementPart<?>>();
		
		for (Direction dir : Direction.values())
		{
			while (!toBeMoved.get(Direction.toInt(dir)).isEmpty())
			{
				BoxElementPart<?> toBeMovedPart = toBeMoved.get(Direction.toInt(dir)).poll();
				// Check for queued parts which might have moved already because of move-propagation
				if (!allMovedParts.contains(toBeMovedPart))
				{
					moveOverlappedParts(toBeMovedPart, parts, allMovedParts);
					if (!allMovedParts.contains(toBeMovedPart)) 
						allMovedParts.add(toBeMovedPart);
				}
			}
		}
		return allMovedParts;
	}

	
	/**
	 * Example: A Situation is created and then moved to the right. The user then uses "undo" twice so that the Situation is removed.
	 * This will make GEF automatically remove the corresponding EditPart associated with the Situation model. This EditPart, however,
	 * will still be stored in the MoveCommand on the command stack. When the user uses "redo" twice, the old Situation will be added
	 * back to the system, but a <i>new</i> EditPart will be created for it. This method checks if two EditParts (<b>comparePart</b> and an 
	 * EditPart in <b>parts</b>) have the same model element. If so, it will remove the old one and substitute it for the new one.
	 * @param parts the parts that are being moved
	 * @param comparePart 
	 */
	private void updateOldEditParts(List<BoxElementPart<?>> parts, BoxElementPart<?> comparePart)
	{
		if(parts != null)
		{
			Element compareModel = (Element) comparePart.getModel();
			for(EditPart ep : parts)
			{
				// if model equals but edit part does not
				if(ep.getModel().equals(compareModel) && !ep.equals(comparePart))
				{
					parts.remove(ep);
					parts.add(comparePart);
				}
			}
		}
	}
	

	/**
	 * This method handles how other BoxElements move due to overlapping. The method propagates to all
	 * BoxElements that move as a result of this. If a BoxElement moves e.g. to the left due to overlap, 
	 * all other BoxElements that overlap with that BoxElement also move to the left.
	 * @param part the part that is forced to move due to overlap
	 * @param parts the parts that were originally moved
	 * @param allMovedParts all parts moved so far
	 * @return a queue of BoxElementParts that are to be moved
	 */
	public Queue<BoxElementPart<?>> moveOverlappedParts(BoxElementPart<?> part, List<BoxElementPart<?>> parts, Queue<BoxElementPart<?>> allMovedParts)
	{
		Direction direction = part.getMoveDirection();
		int d = Direction.toInt(direction);
		boolean horizontalDirection = direction == Direction.LEFT_TO_RIGHT || direction == Direction.RIGHT_TO_LEFT;
		
		Rectangle rect = part.getMovedArea();
		int rightEdge = rect.x + rect.width;
		int bottomEdge = rect.y + rect.height;

		// Index of second level list (in the current directions first level list) which contains this part 
		int i = system[d].indexOf(part.getParent(d));
		
		for (i++; i < system[d].size(); i++) {
			for (int j = 0; j < system[d].get(i).size(); j++)
			{
				// The temporary moved bounds are needed here, to avoid making duplicate moves.
				BoxElementPart<?> comparePart = system[d].get(i).get(j);
				Rectangle compareBounds = comparePart.getMovedBounds();
				
				if (comparePart != part && rect.intersects(compareBounds) && (parts == null || !parts.contains(comparePart)))
				{
					if (comparePart.getMoveDirection() == null)
						comparePart.setMoveDirection(direction);
					
					comparePart.translateMoveDelta(getRelativePosition(rect, compareBounds, comparePart.getMoveDirection()));
					moveOverlappedParts(comparePart, parts, allMovedParts);
					
					if (!allMovedParts.contains(comparePart))
						allMovedParts.add(comparePart);
					
				} else if (((horizontalDirection && comparePart.getModelBounds().y > bottomEdge) || (!horizontalDirection && comparePart.getModelBounds().x > rightEdge)))
				{
					// We need to use model bounds here because the list structure is consistent with that, not the temporary moved bounds.
					break;
				}
			}
		}
		return allMovedParts;
	}
	
	
	/**
	 * Calculates what direction a BoxElement should move due to an overlap.
	 * @param moved bounds of already moved BoxElement
	 * @param toBeMoved bounds of BoxElement that is being overlapped
	 * @return the direction in which <b>toBeMoved</b> will be moved
	 */
	private Direction getDirection(Rectangle moved, Rectangle toBeMoved)
	{
		Rectangle intersection = moved.getCopy().intersect(toBeMoved);
		Point midPoint = intersection.getCenter();
		
		Direction dir = Direction.LEFT_TO_RIGHT;
		int minDist = Math.abs(midPoint.x - moved.getRight().x);
		
		if(Math.abs((midPoint.x - moved.x)) 			< minDist) {dir = Direction.RIGHT_TO_LEFT; minDist = Math.abs((midPoint.x - moved.x)); }
		if(Math.abs((midPoint.y - moved.getBottom().y)) < minDist) {dir = Direction.TOP_TO_BOTTOM; minDist = Math.abs((midPoint.y - moved.getBottom().y)); }
		if(Math.abs((midPoint.y - moved.y)) 			< minDist) {dir = Direction.BOTTOM_TO_TOP; minDist = Math.abs((midPoint.y - moved.y)); }
		
		if(dir == Direction.LEFT_TO_RIGHT || dir == Direction.RIGHT_TO_LEFT)
		{
			if(moved.x <= intersection.x && intersection.right() <= moved.right())
			{
				if(moved.getCenter().x < toBeMoved.getCenter().x)
					dir = Direction.LEFT_TO_RIGHT;
				else
					dir = Direction.RIGHT_TO_LEFT;
			}
		}
		else {
			if(moved.y <= intersection.y && intersection.bottom() <= moved.bottom())
			{
				if(moved.getCenter().y < toBeMoved.getCenter().y)
					dir = Direction.TOP_TO_BOTTOM;
				else
					dir = Direction.BOTTOM_TO_TOP;
			}
		}
		
		return dir;
	}

	
	/**
	 * Returns the move delta, that is, how many pixels a BoxElement will move and what direction.
	 * @param moved bounds of already moved BoxElement
	 * @param toBeMoved bounds of BoxElement that is being overlapped
	 * @param direction the direction in which <b>toBeMoved</b> will be moved
	 * @return the move delta
	 */
	private Point getRelativePosition(Rectangle moved, Rectangle toBeMoved, Direction direction)
	{
		Point relativePosition;
		
		if (direction == Direction.TOP_TO_BOTTOM)
			relativePosition = new Point(0, moved.bottom() - toBeMoved.y);
		else if (direction == Direction.BOTTOM_TO_TOP)
			relativePosition = new Point(0, moved.y - toBeMoved.bottom());
		else if (direction == Direction.LEFT_TO_RIGHT)
			relativePosition = new Point(moved.right() - toBeMoved.x, 0);
		else
			relativePosition = new Point(moved.x - toBeMoved.right(), 0);
		
		return relativePosition;
	}

	
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(Node.PROPERTY_ADD)		|| 
			evt.getPropertyName().equals(Node.PROPERTY_REMOVE)	|| 
			evt.getPropertyName().equals(Node.PROPERTY_BOUNDS))
		{
			refresh();
		}
	}

	
	@Override
	public List<Element> getModelChildren()
	{
		return getModel().getChildren();
	}
	
	
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new BoxContainerEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,new GraphNodeEditPolicy());
	}
	
	
	@Override
	public DragTracker getDragTracker(Request request)
	{
		SelectionRequest req = (SelectionRequest)request;
		if (req.isControlKeyPressed() && !(getParent() instanceof ProcedurePart))
		{
			getViewer().select(this);
			return new ConnectionDragCreationTool();
		}
		else if (req.isShiftKeyPressed())
		{
			return new MarqueeDragTracker();
		}
		return super.getDragTracker(request);
	}

	
	/** Translates from absolute coordinates into coordinates relative to this boxcontainer */
	public Point fromAbsolute( Point abs )
	{
		return abs.getTranslated(getFigure().getBounds().getLocation().getNegated());
	}
	
	
	/** Translates point relative to this boxcontainer to absolute coordinates */
	public Point toAbsolute( Point rel )
	{
		return rel.getTranslated(getFigure().getBounds().getLocation());
	}
	
	
	/** Takes an absolute coordinate and aligns it to the BoxContainer's grid */ 
	public Point snapToGrid( Point p, boolean horiz, boolean vert )
	{
		Point p2 = p.getCopy();
		p2 = fromAbsolute(p2);
		if (horiz) p2.x = IBPEditor.GRID_SIZE * ((p2.x + IBPEditor.GRID_SIZE/2) / IBPEditor.GRID_SIZE);
		if (vert) p2.y = IBPEditor.GRID_SIZE * ((p2.y + IBPEditor.GRID_SIZE/2) / IBPEditor.GRID_SIZE);
		p2 = toAbsolute(p2);
		return p2;
	}
	
	public Point snapToGrid( Point p ) { return snapToGrid(p,true,true); }
	
	/** Returns the BoxContainer of the procedure that contains this part */
	public BoxContainerPart getRootBoxContainer()
	{
		return (BoxContainerPart)modelToPart(this.getModel().getProcedure().getBoxContainer());
	}

}
