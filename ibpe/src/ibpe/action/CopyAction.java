package ibpe.action;

import ibpe.io.*;
import ibpe.model.*;

import java.util.*;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

public class CopyAction extends IBPESelectionAction 
{

	public CopyAction(IWorkbenchPart part)
	{
		super(part);
		setLazyEnablementCalculation(false);
	}


	@Override 
	protected void init()
	{
		super.init();  
		setText("Copy"); 
		setId(ActionFactory.COPY.getId());
		
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY)); 
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY)); 
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED)); 
		setEnabled(false); 
	}

	
	protected Fragment<?> createCopyFragment()
	{
		int nSelectedElems = getSelectedElements().size(); 
		
		if (nSelectedElems==0)
			return null;
		
		// Check if all selected elements are textrows and procedures
		List<Element> elems = new LinkedList<Element>();
		for (Element e : getSelectedElements())
			if (e instanceof TextRow ||	e instanceof Procedure)
				elems.add(e);
		if (elems.size()==nSelectedElems)
			return new SequenceFragment(elems);
		
		// Check if all selected elements are BoxElements
		List<BoxElement> boxElems = new LinkedList<BoxElement>();
		for (Element e : getSelectedElements())
			if (e instanceof BoxElement)
				boxElems.add((BoxElement)e);
		if (boxElems.size()==nSelectedElems)
			return new DiagramFragment(boxElems);

		// Not a copyable fragment
		return null;
	}
	
	@Override 
	protected boolean calculateEnabled()
	{ 
		return createCopyFragment()!=null; 
	} 
	
	@Override 
	public void run()
	{ 
		Clipboard.getClipboard().setContentsFromFragment(createCopyFragment());
	}	
	
}
