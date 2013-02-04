package ptolemy.vergil.uml;

import diva.canvas.AbstractSite;
import diva.canvas.Figure;

/**
 * Convenience class to support Sites that are located on a Lifeline. This
 * means, the x-position is always given by the Lifeline whereas the y-position
 * is determined by the designer who wants to draw a Message in the sequence
 * diagram.
 * 
 * @author Andreas Thuy
 * 
 *         last review: 05.06.06
 */
public class LifelineSite extends AbstractSite {

	private LifelineFigure lifeline = null;

	// the yPos is equal to the ID of this site!

	// negative values should never appear during normal execution
	// so, this means, that we are uninitialized
	private double yPos = -1;

	public LifelineSite(LifelineFigure lifeline, double yPos) {
		super();

		if (lifeline != null) {
			this.lifeline = lifeline;
		} else {
			System.out.println("Only Lifelines are supported... because you"
					+ "didn't supply us with a Lifeline, I will crash in a"
					+ "view seconds.");
		}

		// maybe a test to find out if the yPos is allowed could be added
		// here...
		this.yPos = yPos;
	}

	public Figure getFigure() {
		return lifeline;
	}

	public int getID() {
		return (int) getY();
	}

	public double getX() {
		return lifeline.getLifelineX();
	}

	public double getY() {
		return yPos;
	}

}
