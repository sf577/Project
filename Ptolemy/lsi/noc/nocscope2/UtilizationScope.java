package lsi.noc.nocscope2;

/* Copyright (c) 2007, 2011 Leandro Soares Indrusiak
 * All rights reserved.
 * Fulford, 04/07/2011.
 * Version 1.0
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

//import lsi.noc.nocscope2.NoCScope;

public class UtilizationScope extends lsi.noc.nocscope.NoCScope {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6618335043931835996L;
	UtilizationSwitch[][] nodes;
	int xdimension, ydimension;

	public UtilizationScope(int xdimension, int ydimension) {
		this.xdimension = xdimension;
		this.ydimension = ydimension;

		nodes = new UtilizationSwitch[xdimension][ydimension];

		for (int i = 0; i < xdimension; i++) {
			for (int j = 0; j < ydimension; j++) {
				UtilizationSwitch cn = new UtilizationSwitch();
				cn.setMaxColor(Color.green);
				cn.setMinColor(Color.yellow);
				cn.setMax(100);
				cn.setNumberVisible(true);
				nodes[i][j] = cn;
			}
		}
	}

	public UtilizationSwitch getSwitch(int x, int y) {
		int i = x;
		int j = (ydimension - 1) - y;
		return nodes[i][j];
	}

	public void paint(Graphics graphics) {

		Graphics2D g = (Graphics2D) graphics;
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, 1000, 1000);

		// go in x dimension
		for (int i = 0; i < nodes.length; i++) {
			// go in y dimension
			for (int j = 0; j < nodes[i].length; j++) {
				g.translate(i * 100, j * 100);
				nodes[i][j].paint(g);
				g.translate(-i * 100, -j * 100);
			}
		}
	}

}
