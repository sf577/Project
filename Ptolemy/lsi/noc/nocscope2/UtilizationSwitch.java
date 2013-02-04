package lsi.noc.nocscope2;

/* Copyright (c) 2007, 2011 Leandro Soares Indrusiak
 * All rights reserved.
 * Fulford, 04/07/2011.
 * Version 1.0
 */

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Color;

public class UtilizationSwitch {

	private int max = 1;
	private int alocal = 0, an = 0, as = 0, aw = 0, ae = 0, ain = 0, aout = 0;
	private Polygon local, in, out, n, s, w, e;
	private boolean number = true;
	Color c1 = Color.blue;
	Color c2 = Color.red;

	public UtilizationSwitch() {
		e = new Polygon();
		e.addPoint(60, 40);
		e.addPoint(100, 50);
		e.addPoint(60, 60);

		w = new Polygon();
		w.addPoint(40, 40);
		w.addPoint(40, 60);
		w.addPoint(0, 50);

		n = new Polygon();
		n.addPoint(50, 0);
		n.addPoint(60, 40);
		n.addPoint(40, 40);

		s = new Polygon();
		s.addPoint(40, 60);
		s.addPoint(60, 60);
		s.addPoint(50, 100);

		local = new Polygon();
		local.addPoint(40, 40);
		local.addPoint(60, 40);
		local.addPoint(60, 60);
		local.addPoint(40, 60);

		in = new Polygon();
		in.addPoint(35, 35);
		in.addPoint(50, 35);
		in.addPoint(50, 65);
		in.addPoint(35, 65);

		out = new Polygon();
		out.addPoint(50, 35);
		out.addPoint(65, 35);
		out.addPoint(65, 65);
		out.addPoint(50, 65);

	}

	public void paint(Graphics2D g) {
		if (ae > max) {
			g.setColor(Color.red);
		} else {
			g.setColor(getColor((float) ae / (float) max));
		}
		g.fill(e);
		g.setColor(Color.white);
		g.draw(e);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(ae).toString(), 82, 38);

		if (aw > max) {
			g.setColor(Color.red);
		} else {
			g.setColor(getColor((float) aw / (float) max));
		}
		g.fill(w);
		g.setColor(Color.white);
		g.draw(w);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(aw).toString(), 2, 38);

		if (an > max) {
			g.setColor(Color.red);
		} else {
			g.setColor(getColor((float) an / (float) max));
		}
		g.fill(n);
		g.setColor(Color.white);
		g.draw(n);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(an).toString(), 58, 12);

		if (as > max) {
			g.setColor(Color.red);
		} else {
			g.setColor(getColor((float) as / (float) max));
		}
		g.fill(s);
		g.setColor(Color.white);
		g.draw(s);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(as).toString(), 58, 92);

		if (ain > max) {
			g.setColor(Color.red);
		} else {
			g.setColor(getColor((float) ain / (float) max));
		}
		g.fill(in);
		g.setColor(Color.white);
		g.draw(in);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(ain).toString(), 25, 30);

		if (aout > max) {
			g.setColor(Color.red);
		} else {
			g.setColor(getColor((float) aout / (float) max));
		}
		g.fill(out);
		g.setColor(Color.white);
		g.draw(out);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(aout).toString(), 65, 30);

		if (alocal > max) {
			g.setColor(Color.red);
		} else {
			g.setColor(getColor((float) alocal / (float) max));
		}
		g.fill(local);
		g.setColor(Color.white);
		g.draw(local);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(alocal).toString(), 62, 74);
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

	public void setEast(int i) {
		ae = i;
	}

	public void setWest(int i) {
		aw = i;
	}

	public void setNorth(int i) {
		an = i;
	}

	public void setSouth(int i) {
		as = i;
	}

	public void setLocal(int i) {
		alocal = i;
	}

	public void setIn(int i) {
		ain = i;
	}

	public void setOut(int i) {
		aout = i;
	}

	public void setMax(int i) {
		max = i;
	}

	public int getMax() {
		return max;
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

	// only called by the EndToEndScope
	public int getLocal() {
		return alocal;
	}
}
