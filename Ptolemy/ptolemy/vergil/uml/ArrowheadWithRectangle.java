package ptolemy.vergil.uml;

import java.awt.Graphics2D;
import java.awt.Point;

import java.awt.Color;

/**
 * An Arrowhead that is customized for the needs of a MessageConnector. In
 * particular, this Arrowhead will include a rectangle that denotes the
 * computation time over a lifeline.
 * 
 * Update from pre-release: SpecialArrowhead.java, MessageConnector.java
 * 
 * 
 * 
 * @author Leandro Indrusiak
 */

public class ArrowheadWithRectangle extends SpecialArrowhead {

	int height = 20, width = 10;

	public void setWidth(int w) {
		width = w;
	}

	public void setHeight(int h) {
		height = h;
	}

	public ArrowheadWithRectangle(Color color) {
		super(color);
	}

	public void paint(Graphics2D g) {
		super.paint(g);

		Point p = new Point();
		getOrigin(p);

		g.setColor(Color.white);
		g.fillRect((int) p.getX() - 5, (int) p.getY(), width, height);
		g.setColor(Color.black);
		g.drawRect((int) p.getX() - 5, (int) p.getY(), width, height);

	}

}
