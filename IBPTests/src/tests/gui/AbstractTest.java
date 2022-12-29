package tests.gui;

import ibpe.IBPEditor;
import ibpe.model.BoxElement;
import ibpe.model.Element;
import ibpe.part.NamedEditPart;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;


public abstract class AbstractTest extends TestCase {
	
	EditPart moduleEditPart = null;
	IProgressMonitor progressMonitor = null;
	IProject project = null;
	IFile file = null;

	protected void closeActiveEditor() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.closeEditor(page.getActiveEditor(), false);
	}
	
	protected void createAndOpenFile(String fileName, String contents) {
		file = project.getFile(fileName);
		InputStream is = null;
		if(!file.exists()) {
			try {
				is = new ByteArrayInputStream(contents.getBytes("UTF-8"));
				file.create(is, true, progressMonitor);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			page.openEditor(new FileEditorInput(file), IBPEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	protected void createConnection(EditPart source, EditPart target) {
		CreateConnectionRequest req = new CreateConnectionRequest();
		req.setTargetEditPart(source);
		req.setLocation(new Point(25, 25));
		req.setType(RequestConstants.REQ_CONNECTION_START);
		source.performRequest(req);
		
		if(req.getStartCommand() == null)
			return;
		
		req.setTargetEditPart(target);
		req.setType(RequestConstants.REQ_CONNECTION_END);
		target.performRequest(req);
	}
	
	protected void createElement(Object type, EditPart host) {
		CreateRequest req = new CreateRequest(type);
		host.performRequest(req);
	}

	protected void createProject(String name) {
		this.progressMonitor = new NullProgressMonitor();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		this.project = root.getProject(name);
		if(!project.exists()) {
			try {
				project.create(progressMonitor);
				project.open(progressMonitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected boolean editPartExistsAssertion(String name) {
		return findEditPart(name) != null;
	}
	
	protected boolean editPartNotExistsAssertion(String name) {
		return !editPartExistsAssertion(name);
	}
	
	protected EditPart findEditPart(EditPart editPart, String name) {
		if( !(editPart instanceof NamedEditPart) )
			return null;
		if(((NamedEditPart)editPart).getEditPartId().equals(name))
			return editPart;
		// Children
		for(Object o : editPart.getChildren()) {
			EditPart match = findEditPart((EditPart) o, name);
			if(match != null) 
				return match;
		}
		// Transitions
		if(editPart instanceof NodeEditPart) {
			for(Object o : ((NodeEditPart) editPart).getSourceConnections()) {
				EditPart match = findEditPart((EditPart) o, name);
				if(match != null) 
					return match;
			}
		}
		return null;
	}
	
	protected EditPart findEditPart(String name) {
		return findEditPart(moduleEditPart, name);
	}
	
	protected boolean isParentAssertion(EditPart editPart, EditPart parent) {
		return ((Element)editPart.getModel()).getParent().equals(parent.getModel());
	}
	
	protected void moveEditPart(EditPart host, EditPart editPart, Point moveDelta) {
		List<EditPart> list = new ArrayList<EditPart>();
		list.add(editPart);
		moveEditPart(host, list, moveDelta);
	}
	
	protected void moveEditPart(EditPart host, List<EditPart> editParts, Point moveDelta) {
		ChangeBoundsRequest req = new ChangeBoundsRequest();
		req.setType(RequestConstants.REQ_MOVE_CHILDREN);
		req.setEditParts(editParts);
		req.setMoveDelta(moveDelta);
		host.performRequest(req);
	}
	
	protected void moveTextRow(EditPart editPart, EditPart insertionReference) {
		ChangeBoundsRequest req = new ChangeBoundsRequest();
		req.setType(RequestConstants.REQ_ADD);
		req.setEditParts(editPart);
		
		Map<String, EditPart> map = new HashMap<String, EditPart>();
		map.put("insertionReference", insertionReference);
		req.setExtendedData(map);
		
		editPart.performRequest(req);
	}
	
	protected boolean noIntersectionAssertion(EditPart editPart1, EditPart editPart2) {
		if(!(editPart1.getModel() instanceof BoxElement && 
			 editPart2.getModel() instanceof BoxElement))
			return false;
		
		Rectangle bounds1 = ((BoxElement)editPart1.getModel()).getBounds();
		Rectangle bounds2 = ((BoxElement)editPart2.getModel()).getBounds();
		
		return !bounds1.intersects(bounds2);
	}

	protected boolean noIntersectionAssertion(List<EditPart> editParts) {
		for(int i = 0; i < editParts.size()-1; i++) {
			for(int j = i+1; j < editParts.size(); j++) {
				if(!noIntersectionAssertion(editParts.get(i), editParts.get(j)))
					return false;
			}
		}
		return true;
	}
	
	protected int NumberOfEditParts() {
		return NumberOfEditParts(moduleEditPart);
	}
	
	protected int NumberOfEditParts(EditPart editPart) {
		int num = 1;
		for(Object o : editPart.getChildren()) {
			num += NumberOfEditParts((EditPart) o);
		}
		if(editPart instanceof NodeEditPart) {
			for(Object o : ((NodeEditPart) editPart).getSourceConnections())
			num += NumberOfEditParts((EditPart) o);
		}
		return num;
	}
	
	protected void renameEditPart(EditPart editPart, String name) {
		((Element) editPart.getModel()).setName(name);
//		DirectEditRequest req = (DirectEditRequest) new Request(RequestConstants.REQ_DIRECT_EDIT);
//		req.
	}
	
	protected void reparentEditPart(EditPart editPart, EditPart newParent) {
		List<EditPart> editParts = new ArrayList<EditPart>();
		editParts.add(editPart);
		Map<String, Rectangle> map = new HashMap<String, Rectangle>();
		map.put("rect", ((BoxElement)editPart.getModel()).getBounds());
		
		ChangeBoundsRequest req = new ChangeBoundsRequest();
		req.setEditParts(editParts);
		req.setType(RequestConstants.REQ_ORPHAN);
		editPart.performRequest(req);
		
		ChangeBoundsRequest req2 = new ChangeBoundsRequest();
		req2.setEditParts(editParts);
		req2.setExtendedData(map);
		req2.setType(RequestConstants.REQ_ADD);
		newParent.performRequest(req2);
	}
	

	protected void resizeEditPart(EditPart editPart,Dimension resizeDelta) {
		resizeEditPart(editPart, resizeDelta, new Point());
	}
	
	protected void resizeEditPart(EditPart editPart, Dimension resizeDelta, Point moveDelta) {
		List<EditPart> list = new ArrayList<EditPart>();
		list.add(editPart);
		ChangeBoundsRequest req = new ChangeBoundsRequest();
		req.setType(RequestConstants.REQ_RESIZE);
		req.setEditParts(list);
		req.setSizeDelta(resizeDelta);
		req.setMoveDelta(moveDelta);
		editPart.performRequest(req);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createProject("MyProject");
		createAndOpenFile("test.ibp", "");
		moduleEditPart = IBPEditor.moduleEditPart;
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		closeActiveEditor();
	}
	
}
