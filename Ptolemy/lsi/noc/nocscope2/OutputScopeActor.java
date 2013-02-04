//Copyright (c) 2007 Leandro Soares Indrusiak
//All rights reserved.
//Darmstadt, 14/06/2007.
//Version 1.0

//Copyright (c) 2008 Leandro Moller
//All rights reserved.
//Darmstadt, 29/10/2008.
//Version 2.0

package lsi.noc.nocscope2;

import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import ptolemy.actor.IOPort;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class OutputScopeActor extends NoCScopeActor {

	private int biggestValue, biggestX, biggestY;
	private int[][] bigTable;

	public OutputScopeActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		_frame = new JFrame("OutputScope");
	}

	public void initialize() throws IllegalActionException {
		super.initialize();
		bigTable = new int[getXDimension()][getYDimension()];
	}

	public void paint(int x, int y, int e, int w, int n, int s, int l) {
		biggestValue = getBiggestLocalValue(e, w, n, s, l);
		bigTable[x][y] = biggestValue;
		biggestValue = getBiggestGlobalValue();
		((OutputScope) _nocscope).getSwitch(x, y).setMaxSize(biggestValue);
		((OutputScope) _nocscope).getSwitch(x, y).setEast(e);
		((OutputScope) _nocscope).getSwitch(x, y).setWest(w);
		((OutputScope) _nocscope).getSwitch(x, y).setNorth(n);
		((OutputScope) _nocscope).getSwitch(x, y).setSouth(s);
		((OutputScope) _nocscope).getSwitch(x, y).setLocal(l);

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

	public int getBiggestLocalPort() {
		int biggest = 1;
		for (int i = 0; i < getXDimension(); i++)
			for (int j = 0; j < getYDimension(); j++)
				if (biggest < ((OutputScope) _nocscope).getSwitch(i, j)
						.getLocal()) {
					biggest = ((OutputScope) _nocscope).getSwitch(i, j)
							.getLocal();
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
		return new OutputScope(getXDimension(), getYDimension());
	}

}
