package ibpe.action;

import ibpe.io.*;

import org.eclipse.ui.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.ui.actions.ActionFactory;

public class PasteAction extends InsertAction
{
	public PasteAction( IWorkbenchPart part  )
	{
		super(part,ActionFactory.PASTE.getId(),"Paste","",
				new CreationFactory() 
				{
					public Object getNewObject() { return (new ParserInterface()).parseAsFragment(Clipboard.getClipboard().getContents()); }
					public Object getObjectType() { return Fragment.class; }
				},
			  true);
	}
	
	@Override
	protected void init()
	{ 
		super.init(); 
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE)); 
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE)); 
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED)); 
	} 
	
	@Override
	protected CreateRequest getRequest()
	{
		// We do not want directedit on pasted parts.
		CreateRequest r = super.getRequest();
		if (r!=null) r.getExtendedData().remove("directedit");
		return r;
	}
	
	@Override 
	protected boolean calculateEnabled() 
	{ 
		return getSelectedOne()!=null && Clipboard.getClipboard().hasPossibleContents();
	} 
	
}
