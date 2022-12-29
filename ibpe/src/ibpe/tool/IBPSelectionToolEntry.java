package ibpe.tool;

import org.eclipse.gef.palette.PanningSelectionToolEntry;

public class IBPSelectionToolEntry extends PanningSelectionToolEntry {

	public IBPSelectionToolEntry()
	{
			super(null, null);
			setToolClass(IBPSelectionTool.class);
	}
}
