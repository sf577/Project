//Copyright (c) 2007 Leandro Soares Indrusiak
//All rights reserved.
//Darmstadt, 07/12/2007.
//Version 1.0

//Copyright (c) 2008 Leandro Moller
//All rights reserved.
//Darmstadt, 29/10/2008.
//Version 2.0

package lsi.noc.nocscope2;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Color;

public class InputSwitch {

	private int max = 1;
	private int alocal = 0, an = 0, as = 0, aw = 0, ae = 0;
	private Polygon local, n, s, w, e;
	private boolean number = true;
	Color c1 = Color.blue;
	Color c2 = Color.red;

	public InputSwitch() {
		e = new Polygon();
		e.addPoint(100, 40);
		e.addPoint(60, 50);
		e.addPoint(100, 60);

		w = new Polygon();
		w.addPoint(0, 40);
		w.addPoint(0, 60);
		w.addPoint(40, 50);

		n = new Polygon();
		n.addPoint(50, 40);
		n.addPoint(60, 0);
		n.addPoint(40, 0);

		s = new Polygon();
		s.addPoint(40, 100);
		s.addPoint(60, 100);
		s.addPoint(50, 60);

		local = new Polygon();
		local.addPoint(40, 40);
		local.addPoint(60, 40);
		local.addPoint(60, 60);
		local.addPoint(40, 60);
	}

	public void paint(Graphics2D g) {
		g.setColor(getColor((float) ae / (float) max));
		g.fill(e);
		g.setColor(Color.white);
		g.draw(e);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(ae).toString(), 62, 38);

		g.setColor(getColor((float) aw / (float) max));
		g.fill(w);
		g.setColor(Color.white);
		g.draw(w);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(aw).toString(), 2, 38);

		g.setColor(getColor((float) an / (float) max));
		g.fill(n);
		g.setColor(Color.white);
		g.draw(n);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(an).toString(), 12, 15);

		g.setColor(getColor((float) as / (float) max));
		g.fill(s);
		g.setColor(Color.white);
		g.draw(s);
		g.setColor(Color.black);
		if (number)
			g.drawString(new Long(as).toString(), 58, 92);

		g.setColor(getColor((float) alocal / (float) max));
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

	public void setMaxSize(int i) {
		max = i;
	}

	public int getMaxSize() {
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
