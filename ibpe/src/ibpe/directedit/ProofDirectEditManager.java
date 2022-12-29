package ibpe.directedit;

import ibpe.layout.ProofLocator;
import ibpe.part.AbstractIBPEditPart;
import ibpe.part.ProofPart;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.tools.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.CellEditorActionHandler;

public class ProofDirectEditManager extends DirectEditManager
{
	protected Font figureFont;
	protected VerifyListener verifyListener;
	protected ICellEditorValidator validator;
	protected boolean committing = false;

	//	private boolean undoExtra; // undo prior command if cancelled
	
	private IActionBars actionBars;
    private CellEditorActionHandler actionHandler;
//    private IAction copy, cut, paste, undo, redo, find, selectAll, delete;

	public ProofDirectEditManager(ProofPart source, ICellEditorValidator validator)
	{
		super(source, TextCellEditor.class, new ProofLocator());
		this.validator = null;
	}

	protected CellEditor createCellEditorOn(Composite composite)
	{
		return new TextCellEditor(composite, SWT.MULTI);
	}

	@Override
	protected void initCellEditor()
	{
		Text text = (Text) getCellEditor().getControl();
		text.setBackground(ColorConstants.white);
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
					
					text.setSize(size.x, size.y);
				}
	
			};

		text.addVerifyListener(verifyListener);
		*/

		getCellEditor().setValue(((AbstractIBPEditPart<?>)getEditPart()).getModel().getText());

		IFigure figure = ((GraphicalEditPart) getEditPart()).getFigure();
		figureFont = figure.getFont();
		FontData data = figureFont.getFontData()[0];
		Dimension fontSize = new Dimension(0, data.getHeight());

		// Set the font to be used
		figure.translateToAbsolute(fontSize);
		data.setHeight(fontSize.height);
		figureFont = new Font(null, data);

		// Set the Validator for the CellEditor
		//getCellEditor().setValidator(validator);
		
		text.setFont(figureFont);
		
		actionBars = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorSite().getActionBars();
        //saveCurrentActions(actionBars);
		actionHandler = new CellEditorActionHandler(actionBars);
		actionHandler.addCellEditor(getCellEditor());
		actionBars.updateActionBars();		
	}


    //public void setUndoExtraIfCancel( boolean b ) { undoExtra = b; }

    /*
    public void setInitialValue( String text ) { initialValue = text; }
	
    public void initSelection()
    {
    	Text text = (Text)getCellEditor().getControl(); 
    	// update initial selection
    	if (initialValue!=null)
    		text.setSelection(initialValue.length());
    	else
    		text.selectAll();
    }
    */	
	
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
				
				/*
				if (undoExtra)
				{
					CommandStack stack = getEditPart().getViewer().getEditDomain().getCommandStack();
					stack.undo();
					stack.flush();
				}
				*/
				
			}
			public void editorValueChanged(boolean old, boolean newState) 
			{
				/*
				Color c =  (validator.isValid(getCellEditor().getValue())==null)
				          ? ColorConstants.white 
				          : ColorConstants.red;
				((Text)getCellEditor().getControl()).setBackground(c);
				*/
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

			CommandStack stack = getEditPart().getViewer().getEditDomain().getCommandStack();
			Command command = getEditPart().getCommand(getDirectEditRequest());
			stack.execute(command);
			
			/*
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
			*/
			
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
//            restoreSavedActions(actionBars);
            actionBars.updateActionBars();
            actionBars = null;
        }
		
	}	

}
