//Copyright (c) 2007 Leandro Moller
//All rights reserved.
//Darmstadt, 26/09/2008.
//Version 2.0

package lsi.noc.nocscope2;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

public class PointToPointScope extends NoCScope {

	PointToPointSwitch[][] nodes;
	int xdimension, ydimension;

	public PointToPointScope(int xdimension, int ydimension) {
		this.xdimension = xdimension;
		this.ydimension = ydimension;

		nodes = new PointToPointSwitch[xdimension][ydimension];

		for (int i = 0; i < xdimension; i++) {
			for (int j = 0; j < ydimension; j++) {
				nodes[i][j] = new PointToPointSwitch(i, (ydimension - 1) - j,
						xdimension, ydimension);
			}
		}
	}

	public PointToPointSwitch getSwitch(int x, int y) {
		int i = x;
		int j = (ydimension - 1) - y;
		return nodes[i][j];
	}

	public void paint(Graphics graphics) {

		Graphics2D g = (Graphics2D) graphics;
		// g.setColor(new Color(110, 19, 72));
		g.setColor(Color.white);
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
