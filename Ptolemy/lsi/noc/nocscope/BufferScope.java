//Copyright (c) 2007 Leandro Soares Indrusiak
//All rights reserved.
//Darmstadt, 14/06/2007.

package lsi.noc.nocscope;

import java.awt.Graphics;
import java.awt.Graphics2D;

public class BufferScope extends NoCScope {

	BufferSwitch[][] nodes;

	public BufferScope(int xdimension, int ydimension) {

		nodes = new BufferSwitch[xdimension][ydimension];

		for (int i = 0; i < xdimension; i++) {
			for (int j = 0; j < ydimension; j++) {

				nodes[i][j] = new BufferSwitch();
			}
		}
	}

	public BufferSwitch getSwitch(int x, int y) {

		return nodes[x][y];

	}

	public void paint(Graphics go) {

		Graphics2D g = (Graphics2D) go;
		g.fillRect(0, 0, 1000, 1000);

		for (int i = 0; i < nodes.length; i++) {

			for (int j = 0; j < nodes[i].length; j++) {

				g.translate(i * 100, j * 100);
				nodes[i][j].paint(g);
				g.translate(-i * 100, -j * 100);

			}

		}

	}

}
