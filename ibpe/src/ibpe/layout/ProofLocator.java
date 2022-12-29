package ibpe.layout;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;

public class ProofLocator implements CellEditorLocator
{
	public void relocate(CellEditor cellEditor)
	{
		Text text = (Text) cellEditor.getControl();
		text.setBounds(100, 100, 400, 400);
	}
}
