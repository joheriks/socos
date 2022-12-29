package ibpe.part;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.AutoexposeHelper;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ViewportAutoexposeHelper;
import org.eclipse.gef.editparts.ZoomManager;

public class IBPRootEditPart extends ScalableRootEditPart {
	
	@Override
	public Object getAdapter(Class key)
	{
		if (key == AutoexposeHelper.class)
			return new ViewportAutoexposeHelper(this, new Insets(50));
		else
			return super.getAdapter(key);
	}

}
