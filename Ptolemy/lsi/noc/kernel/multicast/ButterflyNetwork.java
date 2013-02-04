package lsi.noc.kernel.multicast;

import java.awt.Point;
import java.awt.Rectangle;

import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DNoCFactory;
import lsi.noc.kernel.Mesh2DXYRouting;
import lsi.noc.kernel.MultiRoute;
import lsi.noc.kernel.Route;

public class ButterflyNetwork extends MultiRoute {

	public Point source1, source2, destination1, destination2;
	public Point intermediateS, intermediateD;
	Route s1d1, s1d2, s2d1, s2d2, s1is, s2is, idd1, idd2, isid;
	public int ds1d1, ds1d2, ds2d1, ds2d2, ds1is, ds2is, didd1, didd2, disid;
	Mesh2DXYRouting routing;
	Mesh2DNoC mesh;
	boolean folded = false;

	/*
	 * A model of a butterfly network, as shown below
	 * 
	 * S1_______IS_______S2 | | | D1_______ID_______D2
	 * 
	 * 
	 * If the network is "folded", D1 and D2 exchange places.
	 */

	public ButterflyNetwork(Point s1, Point s2, Point d1, Point d2, Mesh2DNoC m) {
		super();
		network = m;
		mesh = m;

		source1 = s1;
		source2 = s2;
		destination1 = d1;
		destination2 = d2;
		calculateButterfly();

	}

	public ButterflyNetwork(Point s1, Point s2, Point d1, Point d2) {

		super();
		source1 = s1;
		source2 = s2;
		destination1 = d1;
		destination2 = d2;

		generateMesh();
		routing = new Mesh2DXYRouting();
		calculateButterfly();

	}

	public ButterflyNetwork() {
		super();
	}

	protected void generateMesh() {
		Mesh2DNoCFactory factory = new Mesh2DNoCFactory();

		Rectangle rec = new Rectangle(source1);
		rec.add(source2);
		rec.add(destination1);
		rec.add(destination2);

		int maxx = rec.x + rec.width + 1;
		int maxy = rec.y + rec.height + 1;

		mesh = (Mesh2DNoC) factory.createInterconnect(maxx, maxy);
		network = mesh;

	}

	protected void calculateButterfly() {

		defineIntermediates();

		s1d1 = routing.route(mesh.getRouter(source1.x, source1.y),
				mesh.getRouter(destination1.x, destination1.y), mesh);
		ds1d1 = s1d1.getHopCount();

		s1d2 = routing.route(mesh.getRouter(source1.x, source1.y),
				mesh.getRouter(destination2.x, destination2.y), mesh);
		ds1d2 = s1d2.getHopCount();

		s2d1 = routing.route(mesh.getRouter(source2.x, source2.y),
				mesh.getRouter(destination1.x, destination1.y), mesh);
		ds2d1 = s2d1.getHopCount();

		s2d2 = routing.route(mesh.getRouter(source2.x, source2.y),
				mesh.getRouter(destination2.x, destination2.y), mesh);
		ds2d2 = s2d2.getHopCount();

		s1is = routing.route(mesh.getRouter(source1.x, source1.y),
				mesh.getRouter(intermediateS.x, intermediateS.y), mesh);
		ds1is = s1is.getHopCount();

		s2is = routing.route(mesh.getRouter(source2.x, source2.y),
				mesh.getRouter(intermediateS.x, intermediateS.y), mesh);
		ds2is = s2is.getHopCount();

		idd1 = routing.route(mesh.getRouter(intermediateD.x, intermediateD.y),
				mesh.getRouter(destination1.x, destination1.y), mesh);
		didd1 = idd1.getHopCount();

		idd2 = routing.route(mesh.getRouter(intermediateD.x, intermediateD.y),
				mesh.getRouter(destination2.x, destination2.y), mesh);
		didd2 = idd2.getHopCount();

		isid = routing.route(mesh.getRouter(intermediateS.x, intermediateS.y),
				mesh.getRouter(intermediateD.x, intermediateD.y), mesh);
		disid = isid.getHopCount();

		// verifies whether the butterfly is folded
		// i.e. whether s1 sends to d1 or d2
		if (ds1d1 + ds2d2 > ds1d2 + ds2d1) {
			folded = true;
		}

		// creates a multiroute by adding the individual routes
		routes.add(s1is);
		routes.add(s2is);
		routes.add(isid);
		routes.add(idd1);
		routes.add(idd2);

		if (folded) {

			routes.add(s1d2);
			routes.add(s2d1);

		} else {
			routes.add(s1d1);
			routes.add(s2d2);
		}

	}

	public void defineIntermediates() {

		intermediateS = getFlooredMidpoint(source1, source2);
		intermediateD = getFlooredMidpoint(destination1, destination2);

	}

	public Point getFlooredMidpoint(Point a, Point b) {

		double midx = (a.x + b.x) / 2;
		double midy = (a.y + b.y) / 2;

		int midxInt = (int) Math.floor(midx);
		int midyInt = (int) Math.floor(midy);

		return new Point(midxInt, midyInt);

	}

	public int getUnicastHopCount() {

		return ds1d1 + ds1d2 + ds2d1 + ds2d2;

	}

	public int getUnicastIntersectingLinksCount() {

		int c1 = s1d1.getIntersectingLinksCount(s1d2);
		int c2 = s1d1.getIntersectingLinksCount(s2d1);
		int c3 = s1d1.getIntersectingLinksCount(s2d2);
		int c4 = s1d2.getIntersectingLinksCount(s2d1);
		int c5 = s1d2.getIntersectingLinksCount(s2d2);
		int c6 = s2d1.getIntersectingLinksCount(s2d2);

		return c1 + c2 + c3 + c4 + c5 + c6;

	}

	public int getButterflyHopCount() {

		int count;
		if (folded) {
			count = ds1d2 + ds2d1 + ds1is + ds2is + disid + didd1 + didd2;
		} else {
			count = ds1d1 + ds2d2 + ds1is + ds2is + disid + didd1 + didd2;
		}
		return count;

	}

	public int getButterflyIntersectingLinksCount() {

		Route[] routesu = { s1d1, s2d2, s1is, s2is, idd1, idd2, isid };
		Route[] routesf = { s1d2, s2d1, s1is, s2is, idd1, idd2, isid };
		Route[] routes;

		if (folded) {
			routes = routesf;
		} else {
			routes = routesu;
		}

		int count = 0;
		for (int i = 0; i < routes.length; i++) {
			for (int j = 0; j < routes.length; j++) {

				if (i != j) // to avoid counting the intersections of a route
							// with itself
					count = count
							+ routes[i].getIntersectingLinksCount(routes[j]);

			}

		}

		// it counts twice the intersecting links from each pair of
		// routes (A and B, then B and A), so must be halved
		return count / 2;

	}

	// returns the Manhattan distance between two points
	// should be available somewhere on the Java API, but I could only find it
	// for Point3D
	// so here it is
	public static int getManhattanDistance(Point p1, Point p2) {

		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);

	}

}
