//Copyright (c) 2007 Leandro Moller
//All rights reserved.
//Darmstadt, 26/09/2008.
//Version 2.0

package lsi.noc.nocscope2;

import javax.swing.JFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PointToPointScopeActor extends NoCScopeActor {

	public PointToPointScopeActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		_frame = new JFrame("PointToPointScope");
	}

	public void initialize() throws IllegalActionException {
		super.initialize();
	}

	public void paint(int x, int y, int e, int w, int n, int s, int l) {
		((PointToPointScope) _nocscope).getSwitch(x, y).setEast(e);
		((PointToPointScope) _nocscope).getSwitch(x, y).setWest(w);
		((PointToPointScope) _nocscope).getSwitch(x, y).setNorth(n);
		((PointToPointScope) _nocscope).getSwitch(x, y).setSouth(s);
		((PointToPointScope) _nocscope).getSwitch(x, y).setLocal(l);

		_nocscope.repaint();
	}

	public void setColorE(int x, int y, int sourceX, int sourceY, int targetX,
			int targetY) {
		((PointToPointScope) _nocscope).getSwitch(x, y).setColorE(sourceX,
				sourceY, targetX, targetY);
	}

	public void setColorW(int x, int y, int sourceX, int sourceY, int targetX,
			int targetY) {
		((PointToPointScope) _nocscope).getSwitch(x, y).setColorW(sourceX,
				sourceY, targetX, targetY);
	}

	public void setColorN(int x, int y, int sourceX, int sourceY, int targetX,
			int targetY) {
		((PointToPointScope) _nocscope).getSwitch(x, y).setColorN(sourceX,
				sourceY, targetX, targetY);
	}

	public void setColorS(int x, int y, int sourceX, int sourceY, int targetX,
			int targetY) {
		((PointToPointScope) _nocscope).getSwitch(x, y).setColorS(sourceX,
				sourceY, targetX, targetY);
	}

	public void setColorL(int x, int y, int sourceX, int sourceY, int targetX,
			int targetY, int arbiterdir) {
		((PointToPointScope) _nocscope).getSwitch(x, y).setColorL(sourceX,
				sourceY, targetX, targetY, arbiterdir);
	}

	public NoCScope createScope() {
		return new PointToPointScope(getXDimension(), getYDimension());
	}

}
