//Copyright (c) 2008 Leandro Moller
//All rights reserved.
//Darmstadt, 29/10/2008.
//Version 2.0

package lsi.noc.nocscope2;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

public class EndToEndSwitch {
	private Vector connection[][];
	private Point point = new Point();
	private double degree;
	private int thick;
	private String name;
	private int nameX, nameY;
	private Font font;
	private int windowSize;
	private int max = 8, input = 0, output = 0;
	private boolean number = true;
	Color c1 = Color.cyan;
	Color c2 = Color.red;

	public EndToEndSwitch(int width, int height) {
		connection = new Vector[width][height];
		for (int x = 0; x < connection.length; x++) {
			for (int y = 0; y < connection[x].length; y++) {
				connection[x][y] = new Vector();
			}
		}
	}

	public void setPoint(int x, int y) {
		point.x = x;
		point.y = y;
	}

	public void setDegree(double d) {
		degree = d;
	}

	public void setThick(int thick) {
		this.thick = thick;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNameX(int nameX) {
		this.nameX = nameX;
	}

	public void setNameY(int nameY) {
		this.nameY = nameY;
	}

	public void setFont(Font f) {
		font = f;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public void paint(Graphics2D g) {
		// get the color to paint
		g.setColor(getColor((float) output / (float) max));
		// paint the left side of the node (INPUT)
		g.fillArc(point.x - thick / 2, point.y - thick / 2, thick, thick, 90,
				180);
		// get the color to paint
		g.setColor(getColor((float) input / (float) max));
		// paint the right side of the node (OUTPUT)
		g.fillArc(point.x - thick / 2, point.y - thick / 2, thick, thick, 270,
				180);

		if (number) {
			// set font to be used
			g.setFont(font);
			g.setColor(Color.white);
			// string to be written
			g.drawString("" + name, nameX, nameY);
		}
	}

	private Color getColor(float ratio) {
		int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
		int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
		int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
		try {
			return new Color(red, green, blue);
		} catch (IllegalArgumentException e) {
			return Color.gray;
		}
	}

	public void addConnection(int targetX, int targetY, double time) {
		try {
			connection[targetX][targetY].add(time);
			cleanConnections(time);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			System.out
					.println("ERROR: You probably have a problem in your input traffic.");
			System.exit(0);
		}
	}

	public Vector getConnections(int targetX, int targetY) {
		return connection[targetX][targetY];
	}

	public Vector[][] getAllConnections() {
		return connection;
	}

	/**
	 * Update the Vector times of a given scope, paint the new scope and print
	 * info in the router debug screen
	 * 
	 * @param scope_time
	 *            contains the times that a data of this scope was capture
	 * @param scope_name
	 *            name of this scope
	 */
	protected void cleanConnections(double timeNow) {
		double timeTemp;

		// check all targets
		for (int x = 0; x < connection.length; x++) {
			for (int y = 0; y < connection[x].length; y++) {
				// check all times available by a target
				for (int i = 0; i < connection[x][y].size(); i++) {
					try {
						// get a time from the vector
						Object obj = connection[x][y].get(i);
						String str = obj.toString();
						timeTemp = Double.valueOf(str).doubleValue();
						// verify if the time in the scope vector was received a
						// long time ago
						if (timeNow - timeTemp > windowSize)
							// remove the old value of the beginning of the
							// vector
							connection[x][y].removeElementAt(0);
						// the packet has been sent a short time ago
						else {
							// stop the measuring of this port in this moment
							break;
						}
					} catch (NullPointerException npe) {
						System.out
								.println("Unexpected error while acquiring data to Scope");
						break;
					}
				}
			}
		}

	}

	public void setOutput(int i) {
		output = i;
	}

	public void setInput(int i) {
		input = i;
	}

	public void setMax(int i) {
		max = i;
	}

	public void setMaxColor(Color c) {
		c2 = c;
	}

	public void setMinColor(Color c) {
		c1 = c;
	}

	public void setNumberVisible(boolean c) {
		number = c;
	}

	public Point getPoint() {
		return point;
	}

	public double getDegree() {
		return degree;
	}

	public String getName() {
		return name;
	}

	public int getThick() {
		return thick;
	}

	public int getNameX() {
		return nameX;
	}

	public int getNameY() {
		return nameY;
	}
}
