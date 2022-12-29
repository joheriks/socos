package ibpe.directedit;

import ibpe.figure.LabelFigure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

public class LabelCellEditorLocator implements CellEditorLocator {
	
	protected LabelFigure label;
	
	public LabelCellEditorLocator(LabelFigure figure) {
		label = figure;
	}
	
	public void relocate(CellEditor cellEditor)
	{
		Text text = (Text) cellEditor.getControl();
		Point pref = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Rectangle rect = label.getBounds().getCopy();

		if (label.getLabelAlignment()==PositionConstants.CENTER)
		{
			if(text.getCharCount() > 0)
				rect.translate(label.getBounds().width/2-pref.x/2,0);
			else
				rect.translate(label.getBounds().width/2,0);
		}

		label.translateToAbsolute(rect);
		
		if (text.getCharCount() > 1)
			text.setBounds(rect.x - 1, rect.y - 1, pref.x + 1, pref.y + 1);
		else
			text.setBounds(rect.x - 1, rect.y - 1, pref.y + 1, pref.y + 1);
	}

}
