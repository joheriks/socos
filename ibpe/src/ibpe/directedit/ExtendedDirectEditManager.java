package ibpe.directedit;

import ibpe.part.*;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;

public class ExtendedDirectEditManager extends DirectEditManager {
	
	protected Font figureFont;
	protected VerifyListener verifyListener;
	protected ICellEditorValidator validator;
	protected boolean committing = false;
	
	private String initialValue; // initial value, may be null, in which case text is taken from model element
	private boolean undoExtra; // undo prior command if cancelled
	
	private IActionBars actionBars;
    private CellEditorActionHandler actionHandler;
    private IAction copy, cut, paste, undo, redo, find, selectAll, delete;
    
	
	public ExtendedDirectEditManager(AbstractDirectEditPart<?> source, 
									 ICellEditorValidator validator)
	{
		super(source, TextCellEditor.class, new LabelCellEditorLocator(source.getLabel()));
		assert source.getLabel()!=null;
		this.validator = validator;
	}
	

	public void initCellEditor() {
		Text text = (Text) getCellEditor().getControl();
		// Add the VerifyListener to apply changes to the control size
		/*
		verifyListener = new VerifyListener()
			{
				// Changes the size of the editor control to reflect the changed text
				public void verifyText(VerifyEvent event)
				{
					Text text = (Text) getCellEditor().getControl();
					String oldText = text.getText();
					String leftText = oldText.substring(0, event.start);
					String rightText = oldText.substring(event.end, oldText.length());
					GC gc = new GC(text);
					Point size = gc.textExtent(leftText + event.text + rightText);
					gc.dispose();
					if (size.x != 0)
						size = text.computeSize(size.x, SWT.DEFAULT);
					else
						size.x = size.y;
					Point p = new Point(text.getLocation().x+text.getSize().x/2-size.x/2,text.getLocation().y);
					p = new Point(0,0);
					text.setLocation(p);
					text.setSize(size.x, size.y);
				}
	
			};

		text.addVerifyListener(verifyListener);
		 */
		if (initialValue==null)
			getCellEditor().setValue(((AbstractIBPEditPart<?>)getEditPart()).getModel().getText());
		else
			getCellEditor().setValue(initialValue);

		IFigure figure = ((GraphicalEditPart) getEditPart()).getFigure();
		figureFont = figure.getFont();
		FontData data = figureFont.getFontData()[0];
		Dimension fontSize = new Dimension(0, data.getHeight());

		// Set the font to be used
		figure.translateToAbsolute(fontSize);
		data.setHeight(fontSize.height);
		figureFont = new Font(null, data);

		// Set the Validator for the CellEditor
		getCellEditor().setValidator(validator);
		
		text.setFont(figureFont);
		
		actionBars = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorSite().getActionBars();
        saveCurrentActions(actionBars);
		actionHandler = new CellEditorActionHandler(actionBars);
		actionHandler.addCellEditor(getCellEditor());
		actionBars.updateActionBars();
	}
	
	private void saveCurrentActions(IActionBars _actionBars)
    {
        copy = _actionBars.getGlobalActionHandler(ActionFactory.COPY.getId());
        paste = _actionBars.getGlobalActionHandler(ActionFactory.PASTE.getId());
        delete = _actionBars.getGlobalActionHandler(ActionFactory.DELETE.getId());
        selectAll = _actionBars.getGlobalActionHandler(ActionFactory.SELECT_ALL.getId());
        cut = _actionBars.getGlobalActionHandler(ActionFactory.CUT.getId());
        find = _actionBars.getGlobalActionHandler(ActionFactory.FIND.getId());
        undo = _actionBars.getGlobalActionHandler(ActionFactory.UNDO.getId());
        redo = _actionBars.getGlobalActionHandler(ActionFactory.REDO.getId());
    }
    
    private void restoreSavedActions(IActionBars actionBars)
    {
        actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copy);
        actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), paste);
        actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), delete);
        actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAll);
        actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), cut);
        actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(), find);
        actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undo);
        actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redo);
    }
	
    public void setInitialValue( String text ) { initialValue = text; }
	
    public void setUndoExtraIfCancel( boolean b ) { undoExtra = b; }
	
    public void initSelection()
    {
    	Text text = (Text)getCellEditor().getControl(); 
    	// update initial selection
    	if (initialValue!=null)
    		text.setSelection(initialValue.length());
    	else
    		text.selectAll();
    }
    
    
    @Override
    protected void hookListeners()
    {
    	super.hookListeners();

    	// Hook an additional listener which undoes the last command if the undoExtra flag
    	// is true. This is used to undo the whole insertion when directedit is canceled on
    	// a newly inserted item.
    	getCellEditor().addListener(
    		new ICellEditorListener() {
			public void applyEditorValue() {}
			public void cancelEditor() {
				if (undoExtra)
				{
					CommandStack stack = getEditPart().getViewer().getEditDomain().getCommandStack();
					stack.undo();
					stack.flush();
				}
				
			}
			public void editorValueChanged(boolean old, boolean newState) 
			{
				Color c =  (validator.isValid(getCellEditor().getValue())==null)
				          ? ColorConstants.white 
				          : ColorConstants.red;
				((Text)getCellEditor().getControl()).setBackground(c);
			}
		});
    }
    

    @Override
	protected void commit()
	{
    	// TODO: Check why this method has to be overridden
    	
		if (committing)
			return;
		committing = true;
		try 
		{
			getCellEditor().getControl().setVisible(false);
			
			if (validator.isValid(getCellEditor().getValue())==null)
			{
				CommandStack stack = getEditPart().getViewer().getEditDomain().getCommandStack();
				Command command = getEditPart().getCommand(getDirectEditRequest());
				if (command != null && command.canExecute())
				{
					stack.execute(command);
					
					// Select the edited part, taking care that it might have been deleted.
					if (getEditPart().getParent()!=null)
						getEditPart().getViewer().select(getEditPart());
				}
			}
			
		}
		finally 
		{
			bringDown();
			committing = false;
		}
	}
	
	protected void bringDown()
	{
		Font disposeFont = figureFont;
		figureFont = null;
		super.bringDown();
		
		if (disposeFont != null)
			disposeFont.dispose();
		
        if (actionHandler != null)
        {
            actionHandler.dispose();
            actionHandler = null;
        }
        if (actionBars != null)
        {
            restoreSavedActions(actionBars);
            actionBars.updateActionBars();
            actionBars = null;
        }
		
	}
}
