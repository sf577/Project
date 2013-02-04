package ptolemy.vergil.uml;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.toolbox.BasicHighlighter;

public class LifelineHighlighter extends BasicHighlighter {

	public LifelineHighlighter() {
		// TODO Auto-generated constructor stub
	}

	public LifelineHighlighter(Paint paint, float halo) {
		super(paint, halo);
		// TODO Auto-generated constructor stub
	}

	public LifelineHighlighter(Paint paint, float halo, Composite composite) {
		super(paint, halo, composite);
		// TODO Auto-generated constructor stub
	}

	public LifelineHighlighter(Paint paint, float halo, Composite composite,
			Stroke stroke) {
		super(paint, halo, composite, stroke);
		// TODO Auto-generated constructor stub
	}

	@Override
	public FigureDecorator newInstance(Figure f) {
		return new LifelineHighlighter(getPaint(), getHalo(), getComposite(),
				getStroke());
	}

	@Override
	public void paint(Graphics2D g) {
		if (getChild() instanceof LifelineFigure) {
			if (getComposite() != null) {
				g.setComposite(getComposite());
			}

			g.setPaint(getPaint());

			LifelineFigure lFigure = (LifelineFigure) getChild();
			Rectangle2D bounds = lFigure.getBoxBounds();
			double x = bounds.getX() - getHalo();
			double y = bounds.getY() - getHalo();
			double w = bounds.getWidth() + (2 * getHalo());
			double h = bounds.getHeight() + (2 * getHalo());

			if (getStroke() == null) {
				g.fill(new Rectangle2D.Double(x, y, w, h));
			} else {
				g.setStroke(getStroke());
				g.draw(new Rectangle2D.Double(x, y, w, h));
			}

			// Draw the child
			getChild().paint(g);
		} else {
			super.paint(g);
		}
	}
}
