package ptolemy.vergil.uml;

import java.awt.Color;
import java.awt.Graphics2D;

import diva.canvas.connector.Arrowhead;

/**
 * An Arrowhead that is customized for the needs of a MessageConnector. In
 * particular, this Arrowhead will be painted in a specified color.
 * 
 * @author Andreas Thuy, Leandro Indrusiak
 */
public class SpecialArrowhead extends Arrowhead {

	private Color arrowColor;

	public SpecialArrowhead() {
		super();

		// assign default color
		this.arrowColor = Color.BLACK;
	}

	public SpecialArrowhead(Color color) {
		super();

		this.arrowColor = color;
	}

	public SpecialArrowhead(double x, double y, double normal) {
		super(x, y, normal);

		// assign default color
		this.arrowColor = Color.BLACK;
	}

	/**
	 * @return the color of this SpecialArrowhead
	 */
	public Color getArrowColor() {
		return this.arrowColor;
	}

	public void paint(Graphics2D g) {
		g.setColor(this.arrowColor);
		super.paint(g);
	}

	/**
	 * Set the color of this SpecialArrowhead.
	 * 
	 * @param arrowColor
	 *            - color of the SpecialArrowhead
	 */
	public void setArrowColor(Color arrowColor) {
		this.arrowColor = arrowColor;
	}
}