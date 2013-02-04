//Copyright (c) 2008 Leandro Moller
//All rights reserved.
//Darmstadt, 29/10/2008.
//Version 2.0

package lsi.noc.nocscope2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.BasicStroke;
import java.util.*;

public class EndToEndScope extends NoCScope {

	private EndToEndSwitch[][] node;
	private boolean first;
	private java.awt.Font font;

	public EndToEndScope(int xdimension, int ydimension) {
		node = new EndToEndSwitch[xdimension][ydimension];
		for (int i = 0; i < xdimension; i++) {
			for (int j = 0; j < ydimension; j++) {
				node[i][j] = new EndToEndSwitch(xdimension, ydimension);
			}
		}
		first = true;
	}

	public void setFirst() {
		first = true;
	}

	public EndToEndSwitch getSwitch(int x, int y) {
		return node[x][y];
	}

	public void paint(Graphics graphics) {

		Graphics2D g = (Graphics2D) graphics;
		g.fillRect(0, 0, 1000, 1000);

		// go in x dimension for all sources
		for (int x = 0; x < node.length; x++) {
			// go in y dimension for all sources
			for (int y = 0; y < node[x].length; y++) {
				paintArrow(x, y, g);
			}
		}

		// go in x dimension
		for (int i = 0; i < node.length; i++) {
			// go in y dimension
			for (int j = 0; j < node[i].length; j++) {
				node[i][j].paint(g);
			}
		}

		if (first)
			paintFirst(g);
	}

	public void paintArrow(int sourceX, int sourceY, Graphics2D g) {
		// get the source switch
		EndToEndSwitch source = node[sourceX][sourceY];
		// instantiate a vector to get all connections from one source to one
		// target
		Vector connections;
		// go in x dimension to get each target of the current switch source
		for (int x = 0; x < node.length; x++) {
			// go in y dimension to get each target of the current switch source
			for (int y = 0; y < node[x].length; y++) {
				// get all the connections made in the current window from this
				// source-target
				connections = source.getConnections(x, y);
				// get the target switch
				EndToEndSwitch target = node[x][y];
				// if there is a connection, paint it
				if (connections.size() > 0) {
					// get the middle point between source and target
					int middleX = (source.getPoint().x + target.getPoint().x) / 2;
					int middleY = (source.getPoint().y + target.getPoint().y) / 2;
					// create half of the line
					Line2D line = new Line2D.Double(source.getPoint().x,
							source.getPoint().y, middleX, middleY);
					// set line color
					g.setColor(Color.blue);
					// set line stroke
					g.setStroke(new BasicStroke(1));
					// draw a simple line
					g.drawLine(source.getPoint().x, source.getPoint().y,
							target.getPoint().x, target.getPoint().y);
					// draw the full line created
					g.setStroke(new BasicStroke(5));
					g.draw(line);
				}
			}
		}

	}

	public void paintFirst(Graphics2D g) {
		// get the position of the first point to be painted
		Point point = getSwitch(0, 0).getPoint();

		// TEXT OF THE CIRCLE
		// string to be written
		String s = "00";
		// get information about the screen
		java.awt.font.FontRenderContext frc = g.getFontRenderContext();
		// initialize the font size as big and then reduce to fit in the circle
		int fontSize = 51;
		// size variables of the text to be written
		float width, height;

		do {
			// reduce the font size
			fontSize--;
			// create the new font
			font = new java.awt.Font("Arial", java.awt.Font.PLAIN, fontSize);
			// set font to be used
			g.setFont(font);
			// get the bounds of the text
			java.awt.geom.Rectangle2D bounds = g.getFont().getStringBounds(s,
					frc);
			// get the width of this text
			width = (float) bounds.getWidth();
			// get the height of this text
			height = (float) bounds.getHeight();
		} while (width > node[0][0].getThick() * 0.85);

		// go in x dimension
		for (int i = 0; i < node.length; i++) {
			// go in y dimension
			for (int j = 0; j < node[i].length; j++) {
				point = node[i][j].getPoint();
				// set the position to write the name of the node
				node[i][j].setNameX((int) (point.x - width / 2));
				node[i][j].setNameY((int) (point.y + height / 3));
				node[i][j].setFont(font);
			}
		}
		// set not to be executed anymore
		first = false;
		// repaint the window
		repaint();
	}

}
