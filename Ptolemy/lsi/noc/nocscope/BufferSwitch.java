//Copyright (c) 2007 Leandro Soares Indrusiak
//All rights reserved.
//Darmstadt, 14/06/2007.

package lsi.noc.nocscope;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Color;

public class BufferSwitch {

	private int max = 8;
	private int alocal = 0, an = 0, as = 0, aw = 0, ae = 0;
	private Polygon local, n, s, w, e;

	public BufferSwitch() {

		e = new Polygon();
		e.addPoint(100, 0);
		e.addPoint(100, 100);
		e.addPoint(70, 70);
		e.addPoint(70, 30);

		w = new Polygon();
		w.addPoint(0, 0);
		w.addPoint(30, 30);
		w.addPoint(30, 70);
		w.addPoint(0, 100);

		n = new Polygon();
		n.addPoint(0, 0);
		n.addPoint(100, 0);
		n.addPoint(70, 30);
		n.addPoint(30, 30);

		s = new Polygon();
		s.addPoint(0, 100);
		s.addPoint(30, 70);
		s.addPoint(70, 70);
		s.addPoint(100, 100);

		local = new Polygon();
		local.addPoint(30, 30);
		local.addPoint(70, 30);
		local.addPoint(70, 70);
		local.addPoint(30, 70);

	}

	public void paint(Graphics2D g) {

		g.setColor(getColor((float) ae / (float) max));
		g.fill(e);
		g.setColor(Color.white);
		g.draw(e);
		g.drawString(new Integer(ae).toString(), 82, 54);

		g.setColor(getColor((float) aw / (float) max));
		g.fill(w);
		g.setColor(Color.white);
		g.draw(w);
		g.drawString(new Integer(aw).toString(), 16, 54);

		g.setColor(getColor((float) an / (float) max));
		g.fill(n);
		g.setColor(Color.white);
		g.draw(n);
		g.drawString(new Integer(an).toString(), 44, 20);

		g.setColor(getColor((float) as / (float) max));
		g.fill(s);
		g.setColor(Color.white);
		g.draw(s);
		g.drawString(new Integer(as).toString(), 44, 92);

		g.setColor(getColor((float) alocal / (float) max));
		g.fill(local);
		g.setColor(Color.white);
		g.draw(local);
		g.drawString(new Integer(alocal).toString(), 44, 54);

	}

	private Color getColor(float ratio) {
		Color c1 = Color.green;
		Color c2 = Color.red;
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

	public void setMaxBufferSize(int i) {
		max = i;
	}

}
