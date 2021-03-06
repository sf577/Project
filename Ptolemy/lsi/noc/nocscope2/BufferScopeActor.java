//Copyright (c) 2007 Leandro Soares Indrusiak
//All rights reserved.
//Darmstadt, 14/06/2007.
//Version 1.0

//Copyright (c) 2008 Leandro Moller
//All rights reserved.
//Darmstadt, 29/10/2008.
//Version 2.0

package lsi.noc.nocscope2;

import javax.swing.JFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class BufferScopeActor extends NoCScopeActor {

	private int biggestValue, biggestX, biggestY;
	private int[][] bigTable;

	public BufferScopeActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		_frame = new JFrame("BufferScope");
	}

	public void initialize() throws IllegalActionException {
		super.initialize();
		bigTable = new int[getXDimension()][getYDimension()];
	}

	public void paint(int x, int y, int e, int w, int n, int s, int l) {
		biggestValue = getBiggestLocalValue(e, w, n, s, l);
		bigTable[x][y] = biggestValue;
		biggestValue = getBiggestGlobalValue();
		((BufferScope) _nocscope).getSwitch(x, y).setMaxSize(biggestValue);
		((BufferScope) _nocscope).getSwitch(x, y).setEast(e);
		((BufferScope) _nocscope).getSwitch(x, y).setWest(w);
		((BufferScope) _nocscope).getSwitch(x, y).setNorth(n);
		((BufferScope) _nocscope).getSwitch(x, y).setSouth(s);
		((BufferScope) _nocscope).getSwitch(x, y).setLocal(l);

		_nocscope.repaint();
	}

	public int getBiggestLocalValue(int e, int w, int n, int s, int l) {
		int value = e;
		if (value < w)
			value = w;
		if (value < n)
			value = n;
		if (value < s)
			value = s;
		if (value < l)
			value = l;
		if (value == 0)
			value = 1;
		return value;
	}

	public int getBiggestGlobalValue() {
		int biggest = 1;
		for (int i = 0; i < getXDimension(); i++)
			for (int j = 0; j < getYDimension(); j++)
				if (biggest < bigTable[i][j]) {
					biggest = bigTable[i][j];
					biggestX = i;
					biggestY = j;
				}
		return biggest;
	}

	public int getBiggestGlobalX() {
		return biggestX;
	}

	public int getBiggestGlobalY() {
		return biggestY;
	}

	public NoCScope createScope() {
		return new BufferScope(getXDimension(), getYDimension());
	}

}
