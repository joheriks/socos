package ibpe.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class ProcedureFigure extends Figure
{
	private static final Color procedureColor = new Color(null,240,240,240);
	
	private LabelFigure label;
	private IFigure signatureCompartment; 
	private IFigure variantCompartment;
	private IFigure declCompartment;
	private IFigure boxCompartment;
	
	/*
	 * Grid layout of a procedure.
	 * 
	 * R
	 * O          
	 * W  COLS  0       1         2         3      4        5
	 * S   ________________________________________________________
	 *    |         |     |             |     |       |            |
	 * 0  | <label> | '[' | <signature> | ']' | <sep> |  <variant> |
	 *    |_________|_____|_____________|_____|_______|____________|
     *    |                                                        |
	 * 1  |                      <separator>                       |
	 *    |________________________________________________________|
     *    |                                                        |
	 * 2  |                     <declarations>                     |
	 *    |________________________________________________________|
     *    |                                                        |
	 * 3  |                       <separator>                      |
	 *    |________________________________________________________|
     *    |                                                        |
	 * 4  |                        <diagram>                       |
	 *    |________________________________________________________|
	 * 
	 */
	
	public ProcedureFigure()
	{
		GridLayout layout = new GridLayout(6,false);
		layout.marginWidth = 4; layout.marginHeight = 4;
		layout.horizontalSpacing = layout.verticalSpacing = 0;
		setLayoutManager(layout);
		
		add(label=new LabelFigure(),new GridData(SWT.BEGINNING,SWT.BEGINNING,false,false,1,1));									// 0
		add(new Label(" ["),new GridData(SWT.BEGINNING,SWT.BEGINNING,false,false,1,1));										// 1
		add(signatureCompartment=new Label("<signatureCompartment>"));														// 2
		add(new Label("] "),new GridData(SWT.BEGINNING,SWT.END,false,false,1,1));											// 3
		add(new SeparatorFigure(),new GridData(SWT.END,SWT.FILL,true,false,1,1));											// 4
		add(variantCompartment=new Label("<variantCompartment>"),new GridData(SWT.END,SWT.BEGINNING,false,false,1,1));		// 5
		
		add(new SeparatorFigure(),new GridData(SWT.FILL,SWT.BEGINNING,false,false,6,1));									// 6
		add(declCompartment=new Label("<declCompartment>"));																// 7
		add(new SeparatorFigure(),new GridData(SWT.FILL,SWT.BEGINNING,false,false,6,1));									// 8
		add(boxCompartment=new Label("<boxCompartment>"));																	// 9
		
		setBackgroundColor(procedureColor);
		
		// Add margins (left margin also to protect from the selection strip), and add a line border.
		setBorder(new CompoundBorder(new MarginBorder(8,8,8,8),new LineBorder(ColorConstants.gray)));	
	}
	
	public void setSignatureCompartment( IFigure f )
	{
		int index = 2; //getChildren().indexOf(signatureCompartment);
		if (getChildren().contains(signatureCompartment)) remove(signatureCompartment);
		add(signatureCompartment=f,new GridData(SWT.BEGINNING,SWT.FILL,false,false,1,1),index);
	}
	
	
	public void setVariantCompartment( IFigure f )
	{
		int index = 5; //getChildren().indexOf(variantCompartment);
		if (getChildren().contains(variantCompartment)) remove(variantCompartment);
		add(variantCompartment=f,new GridData(SWT.END,SWT.FILL,false,false,1,1),index);
	}
	
	
	public void setDeclCompartment( IFigure f )
	{
		int index = 7; // getChildren().indexOf(declCompartment);
		if (getChildren().contains(declCompartment)) remove(declCompartment);
		add(declCompartment=f,new GridData(SWT.FILL,SWT.BEGINNING,false,false,6,1),index);
		
		// set a non-zero vertical margin so that the compartment does not disappear when empty 
		((GridLayout)declCompartment.getLayoutManager()).marginHeight = 2;
	}
	
	
	public void setBoxCompartment( IFigure f )
	{
		int index = 9; //getChildren().indexOf(boxCompartment);
		if (getChildren().contains(boxCompartment)) remove(boxCompartment);
		add(boxCompartment=f,new GridData(SWT.FILL,SWT.FILL,true,true,6,1),index);
	}

	
	public IFigure getBoxCompartment()
	{
		return boxCompartment;
	}
	
	
	public LabelFigure getLabel()
	{
		return label;
	}
	
	
	public void setText( String text)
	{
		label.setText(text);
	}
	
}
