package ptolemy.vergil.uml;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.CompositeFigure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;

/**
 * This figure represents objects in a UML sequence diagram. They are
 * represented through a rectangle with a label and a life-line.
 * 
 * @author Andreas Thuy
 * 
 *         last review: 10.09.06
 * 
 */
public class LifelineFigure extends CompositeFigure {

	private BasicRectangle box = null;

	private double boxHeight = 50;

	private double boxWidth = 100;

	private LabelFigure label = null;

	private double length = 500;

	private BasicFigure lifeLine = null;

	/**
	 * Constructs a LifelineFigure at the given position with the label
	 * typeText.
	 * 
	 * @param typeText
	 *            - label that is displayed in the rectangle
	 * @param x
	 *            - x-coordinate of the figure
	 * @param y
	 *            - y-coordinate of the figure
	 */
	// attendez: the coordinates x and y determine the origin of the
	// BasicRectangle => the coordinates are in the middle of the rectangle!!!!
	// nae, sind se nicht!!!!!!!!!!!!!!!!!!! links oben, wie immer!
	// aber dann wird das ding nach origin translationiert!
	// WEIL _centered = true in BasicFigure per default!!!
	// da muss man erstmal drauf kommen *g*
	public LifelineFigure(String typeText, double x, double y) {
		// create rectangle and position label at fixed position within
		// rectangle
		this.box = new BasicRectangle(x, y, this.boxWidth, this.boxHeight);
		this.box.setFillPaint(Color.WHITE);

		this.label = new LabelFigure(typeText);
		Point2D labelOrigin = this.label.getOrigin();

		// calculation of label-position
		this.label.translate(this.box.getOrigin().getX() - labelOrigin.getX(),
				this.box.getOrigin().getY() - labelOrigin.getY());

		this.add(this.box);
		this.add(this.label);

		// create a dashed line, it will start at the bottom of box
		Line2D.Double line = new Line2D.Double(this.box.getOrigin().getX(),
				this.box.getOrigin().getY() + this.boxHeight / 2, this.box
						.getOrigin().getX(), this.box.getOrigin().getY()
						+ this.length);
		this.lifeLine = new BasicFigure(line);

		float f[] = { 6 };
		BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, f, 0.0f);
		this.lifeLine.setStroke(stroke);

		this.add(this.lifeLine);
	}

	public Rectangle2D getBoxBounds() {
		return this.box.getBounds();
	}

	public LabelFigure getLabel() {
		return label;
	}

	/**
	 * @return the x-coordinate of the live-line, this should be the vertical
	 *         center of the figure
	 */
	public double getLifelineX() {
		return this.lifeLine.getOrigin().getX();
	}

	/**
	 * Check if point lies on this Lifeline's line (without the box).
	 * 
	 * @param point
	 * @return - true if 'point' lies on this class's lifeLine
	 */
	public boolean isOnLiveLine(Point2D point) {
		double x = point.getX();
		double y = point.getY();
		Point2D upperPoint = new Point2D.Double(this.lifeLine.getOrigin()
				.getX(), this.lifeLine.getOrigin().getY() - this.length / 2);
		Point2D lowerPoint = new Point2D.Double(this.lifeLine.getOrigin()
				.getX(), this.lifeLine.getOrigin().getY() + this.length / 2);
		if (upperPoint.getY() < y && lowerPoint.getY() > y) {
			// y-value lies in the range of the lifeLine
			// 5 is hardcoded here as left and right bounds
			// to be correct, setSnapResolution would have to be tested, but if
			// it would not have been ok, the necessary event would not have
			// occurred
			// so, let's be gentle here with 5
			if ((getLifelineX() + 5) > x && (getLifelineX() - 5) < x) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the label that is displayed in the rectangle.
	 * 
	 * @param label
	 *            - label to be displayed
	 */
	public void setLabel(LabelFigure label) {
		this.label = label;
	}

	/**
	 * Only move in the horizontal direction => y will always be set to '0'
	 * 
	 * @see diva.canvas.Figure#translate(double, double)
	 */
	public void translate(double x, double y) {
		this.box.translate(x, 0);
		this.lifeLine.translate(x, 0);
		this.label.translate(x, 0);
	}

}
