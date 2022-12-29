package ibpe.figure;

import org.eclipse.draw2d.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ProofFigure extends Figure 
{
	protected LabelFigure label;

	public ProofFigure( )
	{
		setOpaque(false);

		GridLayout layout = new GridLayout(2,false);
		layout.marginWidth = layout.marginHeight = 0;
		layout.horizontalSpacing = layout.verticalSpacing = 2;			
		setLayoutManager(layout);

		ImageDescriptor imgdesc=AbstractUIPlugin.imageDescriptorFromPlugin("fi.imped.socos.IBPE", "icons/new_proof.png");
		Image img=new Image(null, imgdesc.getImageData());
		add(new Label(img));
		add(new Label("proof"));
	}

	public LabelFigure getLabel() { return label; }

}
