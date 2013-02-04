package lsi.noc.kernel.visualisation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JPanel;

import lsi.noc.kernel.Link;
import lsi.noc.kernel.Linkable;
import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DRouter;
import lsi.noc.kernel.Route;

@SuppressWarnings("serial")
public class RouteViewer extends JPanel {

	ArrayList<Route> routes;
	int unitHop;
	Point origin;
	boolean labels = false;

	public RouteViewer() {

		super();
		routes = new ArrayList<Route>();
		unitHop = 10;
		origin = new Point(0, 0);
	}

	public void reset() {
		routes = new ArrayList<Route>();
	}

	public void addRoute(Route rou) {
		routes.add(rou);
	}

	public void setHopdistance(int h) {
		unitHop = h;
	}

	public void setOrigin(Point po) {
		origin = po;
	}

	public void setLabels(boolean bo) {
		labels = bo;
	}

	public void paintComponent(Graphics g) {

		int n = routes.size();
		int x;
		try {
			x = 255 / n;
		} catch (Exception e) {
			x = 255;
		}

		for (int i = 0; i < n; i++) {

			Color c = new Color(i * x, 100, 255 - i * x);
			if (labels) {
				drawRoute(g, routes.get(i), c, new Integer(i).toString());
			} else {
				drawRoute(g, routes.get(i), c);
			}

		}

	}

	public void drawRoute(Graphics g, Route r, Color c, String s) {

		ArrayList<Link> links = r.getLinks();
		Mesh2DNoC mesh = (Mesh2DNoC) r.getInterconnect();

		for (int i = 0; i < links.size(); i++) {
			Link l = links.get(i);
			Linkable r1 = l.getLinked().get(0);
			Linkable r2 = l.getLinked().get(1);

			Point p1 = mesh.getMeshLocation(r1);
			Point p2 = mesh.getMeshLocation(r2);

			g.setColor(c);

			int x1 = origin.x + p1.x * unitHop;
			int y1 = origin.y + p1.y * unitHop;
			int x2 = origin.x + p2.x * unitHop;
			int y2 = origin.y + p2.y * unitHop;

			g.drawLine(x1, y1, x2, y2);
			g.drawString(s, (x1 + x2) / 2, (y1 + y2) / 2);

		}

	}

	public void drawRoute(Graphics g, Route r, Color c) {

		ArrayList<Link> links = r.getLinks();
		Mesh2DNoC mesh = (Mesh2DNoC) r.getInterconnect();

		for (int i = 0; i < links.size(); i++) {
			Link l = links.get(i);
			Mesh2DRouter r1 = (Mesh2DRouter) l.getLinked().get(0);
			Mesh2DRouter r2 = (Mesh2DRouter) l.getLinked().get(1);

			Point p1 = mesh.getMeshLocation(r1);
			Point p2 = mesh.getMeshLocation(r2);

			g.setColor(c);

			int x1 = origin.x + p1.x * unitHop;
			int y1 = origin.y + p1.y * unitHop;
			int x2 = origin.x + p2.x * unitHop;
			int y2 = origin.y + p2.y * unitHop;

			g.drawLine(x1, y1, x2, y2);

		}
	}

}
