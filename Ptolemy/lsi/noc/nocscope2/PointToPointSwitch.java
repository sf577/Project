//Copyright (c) 2007 Leandro Moller
//All rights reserved.
//Darmstadt, 26/09/2008.
//Version 2.0

package lsi.noc.nocscope2;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Color;

public class PointToPointSwitch {

	int x, y, xdimension, ydimension, numberOfRouters;
	java.awt.Font fontName, fontText;
	private boolean number = true;
	private int arbiter;
	private int ie, iw, in, is, il;
	private Polygon ew, en, es, el;
	private Polygon we, wn, ws, wl;
	private Polygon ne, nw, ns, nl;
	private Polygon se, sw, sn, sl;
	private Polygon le, lw, ln, ls;
	private Color ce, cw, cn, cs, cl;
	private String te, tw, tn, ts, tl;

	// port constant directions
	static final int CLEAN = -1, EAST = 0, WEST = 1, NORTH = 2, SOUTH = 3,
			LOCAL = 4;

	public PointToPointSwitch(int x, int y, int xdimension, int ydimension) {
		this.x = x;
		this.y = y;
		this.xdimension = xdimension;
		this.ydimension = ydimension;
		numberOfRouters = (xdimension * ydimension);

		// create the new font
		fontName = new java.awt.Font("Arial", java.awt.Font.BOLD, 50);
		fontText = new java.awt.Font("Arial", java.awt.Font.PLAIN, 12);

		ce = cw = cn = cs = cl = Color.yellow;
		te = tw = tn = ts = tl = "";
		ie = iw = in = is = il = 0;
		arbiter = -1;

		ew = new Polygon();
		ew.addPoint(100, 52);
		ew.addPoint(6, 52);
		ew.addPoint(6, 49);
		ew.addPoint(0, 54);
		ew.addPoint(6, 59);
		ew.addPoint(6, 56);
		ew.addPoint(100, 56);

		en = new Polygon();
		en.addPoint(100, 52);
		en.addPoint(56, 52);
		en.addPoint(56, 6);
		en.addPoint(59, 6);
		en.addPoint(54, 0);
		en.addPoint(49, 6);
		en.addPoint(52, 6);
		en.addPoint(52, 56);
		en.addPoint(100, 56);

		es = new Polygon();
		es.addPoint(100, 52);
		es.addPoint(44, 52);
		es.addPoint(44, 94);
		es.addPoint(41, 94);
		es.addPoint(46, 100);
		es.addPoint(51, 94);
		es.addPoint(48, 94);
		es.addPoint(48, 56);
		es.addPoint(100, 56);

		el = new Polygon();
		el.addPoint(100, 52);
		el.addPoint(56, 52);
		el.addPoint(97, 11);
		el.addPoint(100, 14);
		el.addPoint(100, 5);
		el.addPoint(91, 5);
		el.addPoint(94, 8);
		el.addPoint(52, 50);
		el.addPoint(52, 56);
		el.addPoint(100, 56);

		we = new Polygon();
		we.addPoint(0, 48);
		we.addPoint(94, 48);
		we.addPoint(94, 51);
		we.addPoint(100, 46);
		we.addPoint(94, 41);
		we.addPoint(94, 44);
		we.addPoint(0, 44);

		wn = new Polygon();
		wn.addPoint(0, 48);
		wn.addPoint(56, 48);
		wn.addPoint(56, 6);
		wn.addPoint(59, 6);
		wn.addPoint(54, 0);
		wn.addPoint(49, 6);
		wn.addPoint(52, 6);
		wn.addPoint(52, 44);
		wn.addPoint(0, 44);

		ws = new Polygon();
		ws.addPoint(0, 48);
		ws.addPoint(44, 48);
		ws.addPoint(44, 94);
		ws.addPoint(41, 94);
		ws.addPoint(46, 100);
		ws.addPoint(51, 94);
		ws.addPoint(48, 94);
		ws.addPoint(48, 44);
		ws.addPoint(0, 44);

		wl = new Polygon();
		wl.addPoint(0, 48);
		wl.addPoint(48, 48);
		wl.addPoint(97, 11);
		wl.addPoint(100, 14);
		wl.addPoint(100, 5);
		wl.addPoint(91, 5);
		wl.addPoint(94, 8);
		wl.addPoint(45, 44);
		wl.addPoint(0, 44);

		ne = new Polygon();
		ne.addPoint(48, 0);
		ne.addPoint(48, 44);
		ne.addPoint(94, 44);
		ne.addPoint(94, 41);
		ne.addPoint(100, 46);
		ne.addPoint(94, 51);
		ne.addPoint(94, 48);
		ne.addPoint(44, 48);
		ne.addPoint(44, 0);

		nw = new Polygon();
		nw.addPoint(48, 0);
		nw.addPoint(48, 56);
		nw.addPoint(6, 56);
		nw.addPoint(6, 59);
		nw.addPoint(0, 54);
		nw.addPoint(6, 49);
		nw.addPoint(6, 52);
		nw.addPoint(44, 52);
		nw.addPoint(44, 0);

		ns = new Polygon();
		ns.addPoint(48, 0);
		ns.addPoint(48, 94);
		ns.addPoint(51, 94);
		ns.addPoint(46, 100);
		ns.addPoint(41, 94);
		ns.addPoint(44, 94);
		ns.addPoint(44, 0);

		nl = new Polygon();
		nl.addPoint(48, 0);
		nl.addPoint(48, 44);
		nl.addPoint(94, 8);
		nl.addPoint(91, 5);
		nl.addPoint(100, 5);
		nl.addPoint(100, 14);
		nl.addPoint(97, 11);
		nl.addPoint(50, 48);
		nl.addPoint(44, 48);
		nl.addPoint(44, 0);

		se = new Polygon();
		se.addPoint(52, 100);
		se.addPoint(52, 44);
		se.addPoint(94, 44);
		se.addPoint(94, 41);
		se.addPoint(100, 46);
		se.addPoint(94, 51);
		se.addPoint(94, 48);
		se.addPoint(56, 48);
		se.addPoint(56, 100);

		sw = new Polygon();
		sw.addPoint(52, 100);
		sw.addPoint(52, 56);
		sw.addPoint(6, 56);
		sw.addPoint(6, 59);
		sw.addPoint(0, 54);
		sw.addPoint(6, 49);
		sw.addPoint(6, 52);
		sw.addPoint(56, 52);
		sw.addPoint(56, 100);

		sn = new Polygon();
		sn.addPoint(52, 100);
		sn.addPoint(52, 6);
		sn.addPoint(49, 6);
		sn.addPoint(54, 0);
		sn.addPoint(59, 6);
		sn.addPoint(56, 6);
		sn.addPoint(56, 100);

		sl = new Polygon();
		sl.addPoint(52, 100);
		sl.addPoint(52, 46);
		sl.addPoint(94, 8);
		sl.addPoint(91, 5);
		sl.addPoint(100, 5);
		sl.addPoint(100, 14);
		sl.addPoint(97, 11);
		sl.addPoint(56, 48);
		sl.addPoint(56, 100);

		le = new Polygon();
		le.addPoint(95, 3);
		le.addPoint(56, 44);
		le.addPoint(94, 44);
		le.addPoint(94, 41);
		le.addPoint(100, 46);
		le.addPoint(94, 51);
		le.addPoint(94, 48);
		le.addPoint(52, 48);
		le.addPoint(52, 42);
		le.addPoint(92, 0);

		lw = new Polygon();
		lw.addPoint(95, 3);
		lw.addPoint(56, 56);
		lw.addPoint(6, 56);
		lw.addPoint(6, 59);
		lw.addPoint(0, 54);
		lw.addPoint(6, 49);
		lw.addPoint(6, 52);
		lw.addPoint(53, 52);
		lw.addPoint(92, 0);

		ln = new Polygon();
		ln.addPoint(95, 3);
		ln.addPoint(56, 48);
		ln.addPoint(52, 48);
		ln.addPoint(52, 6);
		ln.addPoint(49, 6);
		ln.addPoint(54, 0);
		ln.addPoint(59, 6);
		ln.addPoint(56, 6);
		ln.addPoint(56, 42);
		ln.addPoint(92, 0);

		ls = new Polygon();
		ls.addPoint(95, 3);
		ls.addPoint(48, 48);
		ls.addPoint(48, 94);
		ls.addPoint(51, 94);
		ls.addPoint(46, 100);
		ls.addPoint(41, 94);
		ls.addPoint(44, 94);
		ls.addPoint(44, 46);
		ls.addPoint(92, 0);
	}

	public void paint(Graphics2D g) {
		g.setColor(Color.black);
		g.drawRect(0, 0, 100, 100);

		// set font to be used
		g.setFont(fontName);
		// set color to be used
		g.setColor(Color.yellow);
		// string to be written
		g.drawString(x + "" + y, 22, 67);

		// set font to be used
		g.setFont(fontText);

		switch (ie) {
		case CLEAN:
			break;
		case WEST:
			paintE(g, ew);
			if (number) {
				g.setColor(ce);
				g.drawString(te, 5, 67);
			}
			break;
		case NORTH:
			paintE(g, en);
			if (number) {
				g.setColor(ce);
				g.drawString(te, 60, 11);
			}
			break;
		case SOUTH:
			paintE(g, es);
			if (number) {
				g.drawString(te, 30, 92);
			}
			break;
		case LOCAL:
			paintE(g, el);
			if (number) {
				g.drawString(te, 86, 31);
			}
			break;
		}
		switch (iw) {
		case CLEAN:
			break;
		case EAST:
			paintW(g, we);
			if (number) {
				g.drawString(tw, 86, 39);
			}
			break;
		case NORTH:
			paintW(g, wn);
			if (number) {
				g.drawString(tw, 60, 11);
			}
			break;
		case SOUTH:
			paintW(g, ws);
			if (number) {
				g.drawString(tw, 30, 92);
			}
			break;
		case LOCAL:
			paintW(g, wl);
			if (number) {
				g.drawString(tw, 86, 31);
			}
			break;
		}
		switch (in) {
		case CLEAN:
			break;
		case EAST:
			paintN(g, ne);
			if (number) {
				g.drawString(tn, 86, 39);
			}
			break;
		case WEST:
			paintN(g, nw);
			if (number) {
				g.drawString(tn, 5, 67);
			}
			break;
		case SOUTH:
			paintN(g, ns);
			if (number) {
				g.drawString(tn, 30, 92);
			}
			break;
		case LOCAL:
			paintN(g, nl);
			if (number) {
				g.drawString(tn, 86, 31);
			}
			break;
		}
		switch (is) {
		case CLEAN:
			break;
		case EAST:
			paintS(g, se);
			if (number) {
				g.drawString(ts, 86, 39);
			}
			break;
		case WEST:
			paintS(g, sw);
			if (number) {
				g.drawString(ts, 5, 67);
			}
			break;
		case NORTH:
			paintS(g, sn);
			if (number) {
				g.drawString(ts, 60, 11);
			}
			break;
		case LOCAL:
			paintS(g, sl);
			if (number) {
				g.drawString(ts, 86, 31);
			}
			break;
		}
		switch (il) {
		case CLEAN:
			break;
		case EAST:
			paintL(g, le);
			if (number) {
				g.drawString(tl, 86, 39);
			}
			break;
		case WEST:
			paintL(g, lw);
			if (number) {
				g.drawString(tl, 5, 67);
			}
			break;
		case SOUTH:
			paintL(g, ls);
			if (number) {
				g.drawString(tl, 30, 92);
			}
			break;
		case NORTH:
			paintL(g, ln);
			if (number) {
				g.drawString(tl, 60, 11);
			}
			break;
		}

		g.setColor(Color.black);

		switch (arbiter) {
		case CLEAN:
			break;
		case EAST:
			g.fillOval(95, 50, 6, 6);
			break;
		case WEST:
			g.fillOval(5, 50, 6, 6);
			break;
		case NORTH:
			g.fillOval(50, 5, 6, 6);
			break;
		case SOUTH:
			g.fillOval(50, 95, 6, 6);
			break;
		case LOCAL:
			g.fillOval(95, 5, 6, 6);
			break;
		}
	}

	public Color paintE(Graphics2D g, Polygon pol) {
		g.setColor(ce);
		g.fill(pol);
		return ce;
	}

	public Color paintW(Graphics2D g, Polygon pol) {
		g.setColor(cw);
		g.fill(pol);
		return cw;
	}

	public Color paintN(Graphics2D g, Polygon pol) {
		g.setColor(cn);
		g.fill(pol);
		return cn;
	}

	public Color paintS(Graphics2D g, Polygon pol) {
		g.setColor(cs);
		g.fill(pol);
		return cs;
	}

	public Color paintL(Graphics2D g, Polygon pol) {
		g.setColor(cl);
		g.fill(pol);
		return cl;
	}

	public void setColorE(int sourceX, int sourceY, int targetX, int targetY) {
		te = targetX + "" + targetY;
		ce = setColor(sourceX, sourceY, targetX, targetY);
	}

	public void setColorW(int sourceX, int sourceY, int targetX, int targetY) {
		tw = targetX + "" + targetY;
		cw = setColor(sourceX, sourceY, targetX, targetY);
	}

	public void setColorN(int sourceX, int sourceY, int targetX, int targetY) {
		tn = targetX + "" + targetY;
		cn = setColor(sourceX, sourceY, targetX, targetY);
	}

	public void setColorS(int sourceX, int sourceY, int targetX, int targetY) {
		ts = targetX + "" + targetY;
		cs = setColor(sourceX, sourceY, targetX, targetY);
	}

	public void setColorL(int sourceX, int sourceY, int targetX, int targetY,
			int arbiterdir) {
		tl = targetX + "" + targetY;
		cl = setColor(sourceX, sourceY, targetX, targetY);
		arbiter = arbiterdir;
	}

	public Color setColor(int sourceX, int sourceY, int targetX, int targetY) {
		int source = (sourceY * xdimension) + sourceX;
		int target = (targetY * xdimension) + targetX;
		float red = (float) source / (float) (numberOfRouters - 1);
		float green = (float) target / (float) (numberOfRouters - 1);
		float blue = (float) (source + target)
				/ (float) (((numberOfRouters - 1) * 2) - 1);
		return new Color(red, green, blue);
	}

	public void setEast(int i) {
		ie = i;
	}

	public void setWest(int i) {
		iw = i;
	}

	public void setNorth(int i) {
		in = i;
	}

	public void setSouth(int i) {
		is = i;
	}

	public void setLocal(int i) {
		il = i;
	}
}
