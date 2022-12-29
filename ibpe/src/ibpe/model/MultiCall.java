package ibpe.model;

import org.eclipse.draw2d.geometry.Point;

public class MultiCall extends Fork {

	public MultiCall(Point pos) {
		super(pos);
		setChoiceType("CALL");
		setText("Procedure");
	}

}
