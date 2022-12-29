package ibpe;

import ibpe.action.*;
import ibpe.figure.DisplayMode;
import ibpe.figure.TransitionFigure;
import ibpe.figure.VisibleDisplayMode;
import ibpe.io.*;
import ibpe.model.*;
import ibpe.part.*;
import ibpe.tool.*;

import java.io.*;
import java.util.*;

import org.antlr.runtime.tree.Tree;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.dnd.*;
import org.eclipse.gef.editparts.*;
import org.eclipse.gef.palette.*;
import org.eclipse.gef.requests.*;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.palette.*;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.*;
import org.eclipse.gef.ui.parts.*;
import org.eclipse.gef.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.gef.ui.views.palette.PaletteViewerPage;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IGotoMarker;



public class IBPEditor extends EditorPart 
       implements CommandStackListener, ISelectionListener, IGotoMarker 
{
	public static final String ID = "ibpe.IBPEditor";

	private static final String FONT_NAME = "Monospace";
	public static final int FONT_SIZE = 10;
	private static Font font = null;

	public static final int GRID_SIZE = 10;

	// The following are templates for new procedures, situations and constraints.
	// The final value of the digit part in the identifier depends on the context 
	// into which the element is inserted; if elements are added in sequence, they
	// will get incremental digits, e.g. p1, p2, p3, ... The renaming is done in
	// io.Fragment.

	public static final String procTemplate =
		"p1: PROCEDURE\n"+
		"PRE BEGIN\n"+
		"    %:[10 10 100 50]\n"+
		"END\n"+
		"POST BEGIN\n"+
	    "    %:[320 10 100 50]\n"+
        "END\n"+
		"BEGIN\n"+
		"END p1\n"
		;
	
	public static final String situationTemplate = 
		"s1: SITUATION\n"+
		"BEGIN\n"+
		"    %:[0 0 160 100]\n"+
		"END s1\n"
		;
	
	public static final String preconditionTemplate = 
		"PRE BEGIN\n"+
	    "    %:[0 0 100 50]\n"+
	    "END\n";
	
	public static final String postconditionTemplate = 
		"POST BEGIN\n"+
	    "    %:[0 0 100 50]\n"+
	    "END\n";

	public static final String declTemplate = "new declaration;\n";
	
	public static ZoomManager manager;
	public static Shell shell;

	private DefaultEditDomain editDomain;
	private IBPGraphicalViewer graphicalViewer;
	private SelectionSynchronizer synchronizer;
	
	private ActionRegistry actionRegistry;
	private List<String> selectionActions = new ArrayList<String>();
	private List<String> insertActions = new ArrayList<String>();
	private List<String> stackActions = new ArrayList<String>();
	private List<String> propertyActions = new ArrayList<String>();
	
	private PaletteViewerProvider provider;
	private FlyoutPaletteComposite splitter;
	private CustomPalettePage page;
	
	private Context context;
	
	private EditPart contextEditPart;
	
	private Serializer serializer;

	private boolean justChecked;


	class FragmentFactory implements CreationFactory
	{
		String s;
		FragmentFactory( String s_) { s=s_; }
		public Object getNewObject() { return (new ParserInterface()).parseAsFragment(s); }
		public Object getObjectType() { return Fragment.class; }
		
	}
	
	class TextRowFactory implements CreationFactory
	{
		public Object getNewObject() { return new NewTextRowFragment(); }
		public Object getObjectType() { return Fragment.class; }
		
	}

	class ProofFactory implements CreationFactory
	{
		public Object getNewObject() { return new NewProof(); }
		public Object getObjectType() { return Fragment.class; }
		
	}

	public IBPEditor()
	{
		getPalettePreferences().setPaletteState(FlyoutPaletteComposite.STATE_PINNED_OPEN);
		
		editDomain = new DefaultEditDomain(this);
		editDomain.setActiveTool(new IBPSelectionTool());
		editDomain.setPaletteRoot(getPaletteRoot());
		
		getCommandStack().setUndoLimit(-1);
		
		if (font==null)
			font = new Font(null,FONT_NAME,FONT_SIZE,SWT.NORMAL);

	}
	
	public Font getFont() 
	{
		return font;
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		setSite(site);
		setInput(input); 
					
		getCommandStack().addCommandStackListener(this);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		
		createActions();
		updateActions(propertyActions);
		updateActions(stackActions);
		
		shell = site.getShell();
				
		if (input instanceof IURIEditorInput)
		{
			String file = ((IURIEditorInput)input).getURI().getPath();
			
			ParserInterface r = new ParserInterface();

			try { context = r.fromFile(file); }
			catch (IOException e) 
			{ 
				r.reportError(e.getMessage());
				throw new PartInitException("Unable to read file "+file);
			}
			if (!r.getErrors().isEmpty())
			{
				MessageBox box = new MessageBox(IBPEditor.shell, SWT.OK);
				box.setText("Error reading file");
				box.setMessage(Utils.join(r.getErrors(),"\n"));
				box.open();
			}

			if (context==null)
			{
				// Code to close the document which is opened needs to be put here
				throw new PartInitException("Unable to read file "+file);
			}

			setPartName(file);
		}
	}
	
	public void setFocus() 
	{
		getGraphicalViewer().getControl().setFocus();
	}

	public ActionRegistry getActionRegistry() 
	{
		if (actionRegistry == null)
			actionRegistry = new ActionRegistry();
		return actionRegistry;
	}

	protected void updateActions(List<String> actionIds) 
	{
		ActionRegistry registry = getActionRegistry();
		Iterator<String> iter = actionIds.iterator();
		while (iter.hasNext()) {
			IAction action = registry.getAction(iter.next());
			if (action instanceof UpdateAction)
				((UpdateAction) action).update();
		}
	}
	
	protected SelectionSynchronizer getSelectionSynchronizer() 
	{
		if (synchronizer == null)
			synchronizer = new SelectionSynchronizer();
		return synchronizer;
	}

	public IBPGraphicalViewer getGraphicalViewer() 
	{
		return graphicalViewer;
	}
	
	protected void setGraphicalViewer(IBPGraphicalViewer viewer) 
	{
		editDomain.addViewer(viewer);
		graphicalViewer = viewer;
	}
	
	public void createPartControl(Composite parent) 
	{
		splitter = new FlyoutPaletteComposite(parent, SWT.NONE, getSite()
				.getPage(), getPaletteViewerProvider(), getPalettePreferences());
		createGraphicalViewer(splitter);
		splitter.setGraphicalControl(getGraphicalViewer().getControl());
		if (page != null) {
			splitter.setExternalViewer(page.getPaletteViewer());
			page = null;
		}
	//	System.out.println("create part control");
	}
	
	protected void createGraphicalViewer(Composite parent)
	{
		IBPGraphicalViewer viewer = new IBPGraphicalViewer();
		viewer.createControl(parent);
		
		setGraphicalViewer(viewer);
		configureGraphicalViewer();
		
		//hookGraphicalViewer();
		getSelectionSynchronizer().addViewer(getGraphicalViewer());
		getSite().setSelectionProvider(getGraphicalViewer());

		initializeGraphicalViewer(); 
	}

	protected void configureGraphicalViewer()
	{
		final GraphicalViewer viewer = getGraphicalViewer();

        ArrayList<String> zoomContributions;
        IBPRootEditPart rootEditPart = new IBPRootEditPart();
        viewer.setRootEditPart(rootEditPart);
        rootEditPart.getZoomManager();
        
        manager = rootEditPart.getZoomManager();
        
        //double[] zoomLevels = new double[] {0.5, 0.6, 0.7, 0.75, 0.8, 0.9, 1.0, 2, 3, 4 ,5};
        double[] zoomLevels = new double[] {1};
        manager.setZoomLevels(zoomLevels);
        
        zoomContributions = new ArrayList<String>();
        zoomContributions.add(ZoomManager.FIT_ALL);
        zoomContributions.add(ZoomManager.FIT_HEIGHT);
        zoomContributions.add(ZoomManager.FIT_WIDTH);
        manager.setZoomLevelContributions(zoomContributions);
        
		viewer.setEditPartFactory(new PartFactory());

		ContextMenuProvider cmp = new ContextMenuProvider(viewer) 
		{
			@Override
			public void buildContextMenu(IMenuManager menu) {
				menu.add(new Separator(GEFActionConstants.GROUP_UNDO));
				menu.add(new Separator(GEFActionConstants.GROUP_COPY));
				menu.add(new Separator(GEFActionConstants.GROUP_EDIT));
				menu.add(new Separator(GEFActionConstants.GROUP_PRINT));
				menu.add(new Separator(GEFActionConstants.MB_ADDITIONS));

				menu.appendToGroup(GEFActionConstants.GROUP_UNDO,actionRegistry.getAction(ActionFactory.UNDO.getId()));
				menu.appendToGroup(GEFActionConstants.GROUP_UNDO,actionRegistry.getAction(ActionFactory.REDO.getId()));
				menu.appendToGroup(GEFActionConstants.GROUP_COPY,actionRegistry.getAction(ActionFactory.COPY.getId()));
				menu.appendToGroup(GEFActionConstants.GROUP_COPY,actionRegistry.getAction(ActionFactory.CUT.getId()));
				menu.appendToGroup(GEFActionConstants.GROUP_COPY,actionRegistry.getAction(ActionFactory.PASTE.getId()));
				menu.appendToGroup(GEFActionConstants.GROUP_EDIT,actionRegistry.getAction(ActionFactory.DELETE.getId()));
				menu.appendToGroup(GEFActionConstants.GROUP_EDIT,actionRegistry.getAction("NEW_PROCEDURE_AFTER"));
				menu.appendToGroup(GEFActionConstants.GROUP_EDIT,actionRegistry.getAction("NEW_DECL_AFTER"));
				menu.appendToGroup(GEFActionConstants.GROUP_EDIT,actionRegistry.getAction("SET_CHOICE"));
			}
		};
		viewer.setContextMenu(cmp);
		getSite().registerContextMenu("fi.imped.ibpe.IBPEditor",cmp,viewer);
		
		IBPEditorKeyHandler keyhandler = new IBPEditorKeyHandler(this);
		
		/*
		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.CTRL),
				MouseWheelZoomHandler.SINGLETON);
		*/
		
		viewer.setKeyHandler(keyhandler);
		viewer.setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, true);
		viewer.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, true);
		viewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, true);
		viewer.setProperty(SnapToGrid.PROPERTY_GRID_SPACING, new Dimension(GRID_SIZE,GRID_SIZE));
		
		/*
		RootEditPart root = viewer.getRootEditPart();
        
        if (root instanceof LayerManager)
        {
            ((ConnectionLayer) ((LayerManager) root)
                    .getLayer(LayerConstants.CONNECTION_LAYER))
                    .setConnectionRouter(new TransitionRouter());
        }*/
              
        viewer.getControl().addMenuDetectListener(new MenuDetectListener() 
        {
	        public void menuDetected(MenuDetectEvent e) 
	        {
	        	org.eclipse.swt.graphics.Point pt = viewer.getControl().toControl(e.x, e.y);
	        	for (String id : insertActions)
	        	{
	        		InsertAction action = (InsertAction)(getActionRegistry().getAction(id));
	        		action.setMouseClickPosition(new Point(pt.x,pt.y));
	        	}
	        	PasteAction action = (PasteAction)(getActionRegistry().getAction(ActionFactory.PASTE.getId()));
				action.setMouseClickPosition(new Point(pt.x,pt.y));
	        }
	       });
	}
	

	@SuppressWarnings("deprecation")
	protected FlyoutPreferences getPalettePreferences() 
	{
		return FlyoutPaletteComposite.createFlyoutPreferences(Activator.getDefault().getPluginPreferences());
	}
	
	protected PaletteViewerProvider createPaletteViewerProvider()
	{
		return new PaletteViewerProvider(editDomain)
			{
				protected void configurePaletteViewer(PaletteViewer viewer)
				{
					super.configurePaletteViewer(viewer);
					viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
				}
			};
	}
	
	protected final PaletteViewerProvider getPaletteViewerProvider() {
		if (provider == null)
			provider = createPaletteViewerProvider();
		return provider;
	}

	public void initializeGraphicalViewer()
	{
		splitter.hookDropTargetListener(getGraphicalViewer());
		
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setContents(context);
		viewer.addDropTargetListener(new TemplateTransferDropTargetListener(viewer));
		
		contextEditPart = viewer.getFocusEditPart();
		viewer.select(contextEditPart);
	}

	public Context getContext()
	{
		return context;
	}
	
	public void commandStackChanged(EventObject event)
	{
		if (justChecked) {
			justChecked = false;
			for (AbstractIBPEditPart<?> part : getIBPEditParts())
				if (part.getFigure() instanceof VisibleDisplayMode) {
					VisibleDisplayMode fig = (VisibleDisplayMode)part.getFigure();
					if (fig.getDisplayMode()==DisplayMode.ERROR)
						fig.setDisplayMode(DisplayMode.AMBIGUOUS);
				}
		}
		updateActions(stackActions);
	    firePropertyChange(IEditorPart.PROP_DIRTY);
	}
	
	public boolean isDirty() 
	{
		return getCommandStack().isDirty();
	}

	public CommandStack getCommandStack() 
	{
		return editDomain.getCommandStack();
	}

	public boolean isSaveAllowed() 
	{
		return contextEditPart instanceof TextContainerPart;
	}

	public boolean isSaveAsAllowed()
	{
		return false;
	}
	
	public void doSaveAs()
	{
		/*
		Shell shell = getSite().getWorkbenchWindow().getShell();
		SaveAsDialog sad = new SaveAsDialog(shell);
		//String path = ((IURIEditorInput) getEditorInput()).getURI().
		//sad.setOriginalName(path);
		sad.open();
		
		IWorkspace workspace =  ResourcesPlugin.getWorkspace();
		String path = workspace.getRoot().getLocationURI().getPath() + sad.getResult().toOSString();
		try {
			save(path);
			getCommandStack().markSaveLocation();
		}
		catch (IOException e) {
			MessageBox box = new MessageBox(shell,SWT.ERROR);
			box.setMessage("Save failed:\n"+e.getMessage());
			box.open();
		}*/
	}
	

	public void doSave(IProgressMonitor monitor)
	{
		IURIEditorInput input = (IURIEditorInput) getEditorInput(); 
		String path = input.getURI().getPath();
		try {
			save(path);
			getCommandStack().markSaveLocation();
		}
		catch (IOException e) {
			MessageBox box = new MessageBox(getSite().getWorkbenchWindow().getShell(),SWT.ERROR);
			box.setMessage("Save failed:\n"+e.getMessage());
			box.open();
		}
		
		try 
		{
			if (input instanceof FileEditorInput)
				((FileEditorInput)input).getFile().refreshLocal(IResource.DEPTH_ZERO, null);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void save(String path) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		serializer = new Serializer(sb);
		serializer.serialize(context);
		
		File file = new File(path);
		if (!file.exists()) 
			file.createNewFile();

		FileWriter fw = new FileWriter(path, false);
		for (int i = 0; i < sb.length(); i++)
			fw.write(sb.charAt(i));
		fw.close();
	}

	
	protected PaletteRoot getPaletteRoot()
	{
		CreationToolEntry transitionToolEntry = 
	    	 	new CreationToolEntry(
	    	 			"Transition", 
	    	 			"Transition creator", 
	    	 			null,
	    	 			AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", "icons/new_transition.png"), 
	    	 			null) 
				{
					@Override
					public Tool createTool() {
						IBPTransitionCreationTool tool = new IBPTransitionCreationTool() {
							@Override
							protected Fork createFork() {
								return new IFChoice(new Point());
							}
						};
						tool.setProperties(getToolProperties());
						return tool;
					}
				};
		//transitionToolEntry.setToolClass(IBPTransitionCreationTool.class);

		IBPSelectionToolEntry selectionToolEntry = new IBPSelectionToolEntry();
		MarqueeToolEntry marqueeToolEntry = new MarqueeToolEntry();
		
		CreationToolEntry procedurecallToolEntry = 
	    	 	new CreationToolEntry(
	    	 			"Procedure call", 
	    	 			"Procedure call", 
	    	 			null,
	    	 			AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", "icons/new_call.png"), 
	    	 			null)
			{
				@Override
				public Tool createTool() {
					IBPTransitionCreationTool tool = new IBPTransitionCreationTool() {
						@Override
						protected Fork createFork() {
							return new MultiCall(new Point());
						}
					};
					tool.setProperties(getToolProperties());
					return tool;
				}
			};
		//procedurecallToolEntry.setToolClass(IBPTransitionCreationTool.class);

	
	     
		CombinedTemplateCreationEntry procedure = 
			new CombinedTemplateCreationEntry(
					"Procedure", 
					"New procedure", 
					new FragmentFactory(procTemplate), 
					AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", "icons/new_procedure.png"), 
					null);
	    procedure.setToolClass(IBPCreationTool.class);

	    CombinedTemplateCreationEntry declaration = 
	    	new CombinedTemplateCreationEntry(
	    			"Declaration", 
	    			"New declaration", 
	    			new TextRowFactory(), 
	    			AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", "icons/new_declaration.png"), 
	    			null);
	    declaration.setToolClass(IBPCreationTool.class);

	    CombinedTemplateCreationEntry proof = 
	    	new CombinedTemplateCreationEntry(
	    			"Proof", 
	    			"New proof", 
	    			new ProofFactory(), 
	    			AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", "icons/new_proof.png"), 
	    			null);

	    CombinedTemplateCreationEntry situation = 
	    	new CombinedTemplateCreationEntry(
	    			"Situation", 
	    			"New situation", 
	    			new FragmentFactory(situationTemplate), 
	    			AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", "icons/new_situation.png"), 
	    			null);
	    situation.setToolClass(IBPCreationTool.class);

	    CombinedTemplateCreationEntry pre = 
	    	new CombinedTemplateCreationEntry(
	    			"Precondition", 
	    			"New precondition", 
	    			new FragmentFactory(preconditionTemplate), 
	    			AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", "icons/new_precondition.png"), 
	    			null);
	    pre.setToolClass(IBPCreationTool.class);

	    
	    CombinedTemplateCreationEntry post = 
	    	new CombinedTemplateCreationEntry(
	    			"Postcondition", 
	    			"New postcondition", 
	    			new FragmentFactory(postconditionTemplate), 
	    			AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", "icons/new_postcondition.png"), 
	    			null);
	    post.setToolClass(IBPCreationTool.class);

     
	    PaletteGroup manipGroup = new PaletteGroup("Invariant diagram tools");
	    manipGroup.add(selectionToolEntry);
	    manipGroup.add(marqueeToolEntry);
	    manipGroup.add(new PaletteSeparator());
	    manipGroup.add(procedure);
	    manipGroup.add(declaration);
	    manipGroup.add(proof);
	    manipGroup.add(new PaletteSeparator());
	    manipGroup.add(situation);
	    manipGroup.add(pre);
	    manipGroup.add(post);
	    manipGroup.add(new PaletteSeparator());
	    manipGroup.add(transitionToolEntry);
	    manipGroup.add(procedurecallToolEntry);
	     
	    PaletteRoot root = new PaletteRoot();
	    root.add(manipGroup);
	     
	    root.setDefaultEntry(selectionToolEntry);
	    return root;
	}
	
	public void createActions()
	{
		ActionRegistry registry = getActionRegistry();
		IAction action;

		action = new UndoAction(this);
		registry.registerAction(action);
		stackActions.add(action.getId());

		action = new RedoAction(this);
		registry.registerAction(action);
		stackActions.add(action.getId());

		action = new SelectAllAction(this);
		registry.registerAction(action);

		action = new DeleteAction((IWorkbenchPart) this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new SaveAction(this);
		registry.registerAction(action);
		propertyActions.add(action.getId());

		action = new InsertAction(this,
                "NEW_PROCEDURE_BEFORE",
                "Insert Procedure",
                "icons/new_procedure.png",
                new FragmentFactory(procTemplate),
                false);
		registry.registerAction(action);
		selectionActions.add(action.getId());
		
		action = new InsertAction(this,
                "NEW_PROCEDURE_AFTER",
                "Add Procedure",
                "icons/new_procedure.png",
                new FragmentFactory(procTemplate),
                true);
		registry.registerAction(action);
		selectionActions.add(action.getId());
		insertActions.add(action.getId());

		action = new InsertAction(this,
                "NEW_DECL_BEFORE",
                "Insert Declaration",
                "icons/new_declaration.png",
                new TextRowFactory(),
                false);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new InsertAction(this,
                "NEW_DECL_AFTER",
                "Add Declaration",
                "icons/new_declaration.png",
                new TextRowFactory(),
                true);
		registry.registerAction(action);
		selectionActions.add(action.getId());
		insertActions.add(action.getId());

		action = new DeleteAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new CopyAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new CutAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new PasteAction(this);	
		registry.registerAction(action);
		selectionActions.add(action.getId());
		insertActions.add(action.getId());
		
		registry.registerAction(action = new SetForkTypeAction(this,"SET_CHOICE","CHOICE"));
		selectionActions.add(action.getId());
		
		/*registry.registerAction(action = new SetForkTypeAction(this,"SET_IF","IF"));
		selectionActions.add(action.getId());

		registry.registerAction(action = new SetForkTypeAction(this,"SET_CALL","CALL"));
		selectionActions.add(action.getId());*/

		registry.registerAction(new CheckAction(this));
		
		registry.registerAction(new StepIntoAction(this));
		registry.registerAction(new StepOverAction(this));
		registry.registerAction(new StopAction(this));
		registry.registerAction(new PlayAction(this));
		registry.registerAction(new PauseAction(this));
	}
	
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{
		// If not the active editor, ignore selection changed.
		if (this.equals(getSite().getPage().getActiveEditor()))
			updateActions(selectionActions);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class type)
	{
		if (type == ZoomManager.class)
			return ((ScalableRootEditPart) getGraphicalViewer().getRootEditPart()).getZoomManager();
		if (type == PalettePage.class) 
		{
			if (splitter == null) 
				return page = new CustomPalettePage(getPaletteViewerProvider());
			else
				return new CustomPalettePage(getPaletteViewerProvider());
		}
		if (type == org.eclipse.ui.views.properties.IPropertySheetPage.class) {
			PropertySheetPage page = new PropertySheetPage();
			page.setRootEntry(new UndoablePropertySheetEntry(getCommandStack()));
			return page;
		}
		if (type == GraphicalViewer.class)
			return getGraphicalViewer();
		if (type == CommandStack.class)
			return getCommandStack();
		if (type == ActionRegistry.class)
			return getActionRegistry();
		if (type == EditPart.class && getGraphicalViewer() != null)
			return getGraphicalViewer().getRootEditPart();
		if (type == IFigure.class && getGraphicalViewer() != null)
			return ((GraphicalEditPart) getGraphicalViewer().getRootEditPart()).getFigure();
		
		return super.getAdapter(type);
	}
	
	/*
	public static Dimension snapToGrid(Dimension dim)
	{
		Dimension snapDim = new Dimension();
		snapDim.width = gridSize*(dim.width/gridSize + 1);
		snapDim.height = gridSize*(dim.height/gridSize + 1);
		return snapDim;
	}
	
	public static Point snapToGrid(Point p)
	{
		Point snapDim = new Point();
		snapDim.x = fontSize*(p.x/fontSize + 1);
		snapDim.y = fontSize*(p.y/fontSize + 1);
		return snapDim;
	}
	*/
	
	protected class CustomPalettePage extends PaletteViewerPage {
		public CustomPalettePage(PaletteViewerProvider provider) {
			super(provider);
		}

		public void createControl(Composite parent) {
			super.createControl(parent);
			if (splitter != null)
				splitter.setExternalViewer(viewer);
			
		}

		public void dispose() {
			if (splitter != null)
				splitter.setExternalViewer(null);
			super.dispose();
		}

		public PaletteViewer getPaletteViewer() {
			return viewer;
		}
	}
	
	
	public AbstractIBPEditPart<?> getMarkedPart( int line )
	{
		if (serializer!=null && 0<=line && line<serializer.getLineCount())
		{
			Element elem = serializer.lineToElement(line);
			return (AbstractIBPEditPart<?>)graphicalViewer.getEditPartRegistry().get(elem);
		}
		return null;
	}
	
	
	protected AbstractIBPEditPart<?> getMarkedPart( IMarker marker )
	{
		int line = marker.getAttribute(IMarker.LINE_NUMBER,0) - 1;
		return getMarkedPart(line);
	}
	

	public void gotoMarker( IMarker marker )
	{
		EditPart ep = getMarkedPart(marker);
		if (ep!=null)
			graphicalViewer.setSelection(new StructuredSelection(ep));

	}
	
	public List<AbstractIBPEditPart<?>> getIBPEditParts() {
		LinkedList<AbstractIBPEditPart<?>> l = new LinkedList<AbstractIBPEditPart<?>>(); 
		for (Object o : graphicalViewer.getEditPartRegistry().values())
			if (o instanceof AbstractIBPEditPart<?>)
				l.add((AbstractIBPEditPart<?>)o);
		return l;
		
	}
	
	public void clearDisplayedMarkers()
	{
		for (AbstractIBPEditPart<?> o : getIBPEditParts())
			o.clearMarkers();
	}
	
	public void displayMarker( IMarker marker )
	{
		AbstractIBPEditPart<?> ep = getMarkedPart(marker);
		if (ep!=null)
		{
			ep.showMarker(marker);
		}
	}
	
	 
	public void disableEditor()
	{
		getGraphicalViewer().getFigureCanvas().getContents().setEnabled(false);
//		CustomPalettePage pg = new CustomPalettePage(provider);
//		System.out.println("pg: "+pg);
//		pg.getPaletteViewer().getControl().setEnabled(false);
		
		//viewer.getControl().setEnabled(false);
	}
	

	public void enableEditor()
	{
		getGraphicalViewer().getFigureCanvas().getContents().setEnabled(true);
	}
	
	
	public IFile getFile() throws CoreException  
	{
		IFile f = null;
		
		if((getEditorInput() instanceof FileEditorInput))
		{
			FileEditorInput op = (FileEditorInput)getEditorInput();
			f = op.getFile();
			
		}
		else if (getEditorInput() instanceof FileStoreEditorInput)
		{
			FileStoreEditorInput op = (FileStoreEditorInput)getEditorInput();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();			
			java.net.URI uri = op.getURI();
			
			if (root.findFilesForLocationURI(uri).length == 0)
			{
				IWorkspace ws = ResourcesPlugin.getWorkspace();
				IProject project = ws.getRoot().getProject("External Files 2");
				
				if (!project.exists())
				{
				    project.create(null);
				    project.setHidden(true); 
				}

				if (!project.isOpen())
				{
				    project.open(null); 
				    project.setHidden(true); 
				}
				
				String name = uri.getPath();
				IPath location = new Path(name);
				IFile file = project.getFile(name.replace("/", "_"));
				file.createLink(location, IResource.REPLACE, null);
				
				return file;
			}
			else
				f = root.findFilesForLocationURI(uri)[0];
		
		}
		
		return f;
	}
	
	public void createTree (String [] args) {
		Display display = new Display ();
		Shell shell = new Shell (display);
		shell.setLayout(new FillLayout());
		//TreeViewer treeViewer = new TreeViewer();
		/*
		Tree tree = new Tree (shell, SWT.BORDER);
		for (int i=0; i<4; i++) {
			TreeItem iItem = new TreeItem (tree, 0);
			iItem.setText ("TreeItem (0) -" + i);
			for (int j=0; j<4; j++) {
				TreeItem jItem = new TreeItem (iItem, 0);
				jItem.setText ("TreeItem (1) -" + j);
				for (int k=0; k<4; k++) {
					TreeItem kItem = new TreeItem (jItem, 0);
					kItem.setText ("TreeItem (2) -" + k);
					for (int l=0; l<4; l++) {
						TreeItem lItem = new TreeItem (kItem, 0);
						lItem.setText ("TreeItem (3) -" + l);
					}
				}
			}
		} */
		shell.setSize (200, 200);
		shell.open ();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
	
	
	public void marker()
	{
		IMarker[] markers = null;
		
		try {
			markers = getFile().findMarkers("fi.imped.socos.IBPE.ibpmarker",
						          		    false,IResource.DEPTH_INFINITE);
			for (IMarker m : markers)
				displayMarker(m);
			
			justChecked = true;
		}
		catch (CoreException e)	{
			// TODO: fixme!
			e.printStackTrace();
		}
	}
}
