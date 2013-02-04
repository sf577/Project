package lsi.noc.nocscope;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class ChannelScope extends NoCScope {

	ChannelSwitch[][] nodes;

	public ChannelScope(int xdimension, int ydimension) {

		nodes = new ChannelSwitch[xdimension][ydimension];

		for (int i = 0; i < xdimension; i++) {
			for (int j = 0; j < ydimension; j++) {
				ChannelSwitch cn = new ChannelSwitch();
				cn.setMaxColor(Color.red);
				cn.setMinColor(Color.white);
				cn.setNumberVisible(true);
				nodes[i][j] = cn;
			}
		}
	}

	public ChannelSwitch getSwitch(int x, int y) {

		return nodes[x][y];

	}

	public void paint(Graphics go) {

		Graphics2D g = (Graphics2D) go;
		g.setColor(Color.lightGray);
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
