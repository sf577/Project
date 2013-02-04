package lsi.noc.kernel.multicast;

import java.awt.Point;

import lsi.noc.kernel.Link;
import lsi.noc.kernel.Linkable;
import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DRouter;
import lsi.noc.kernel.Mesh2DXYRouting;
import lsi.noc.kernel.Route;

public class ConstructiveButterflyNetworkwithThreeDestinations extends
		ButterflyNetwork {

	public Point destination3;

	public ConstructiveButterflyNetworkwithThreeDestinations(Point s1,
			Point s2, Point d1, Point d2, Point d3, Mesh2DNoC m) {

		super();
		source1 = s1;
		source2 = s2;
		destination1 = d1;
		destination2 = d2;
		destination3 = d3;
		network = m;
		mesh = m;
		routing = new Mesh2DXYRouting();
		calculateButterfly();

	}

	public void defineIntermediates() {

		int bs1d1 = getManhattanDistance(source1, destination1);
		int bs1d2 = getManhattanDistance(source1, destination2);
		int bs1d3 = getManhattanDistance(source1, destination3);
		int bs2d1 = getManhattanDistance(source2, destination1);
		int bs2d2 = getManhattanDistance(source2, destination2);
		int bs2d3 = getManhattanDistance(source2, destination3);

		Point iD1, iD2, iSt;

		int bd1 = bs1d1 + bs2d1;
		int bd2 = bs1d2 + bs2d2;
		int bd3 = bs1d3 + bs2d3;

		if (bd1 <= bd2 & bd1 <= bd3) {
			Point temp = new Point(destination1);
			destination1 = new Point(destination3);
			destination3 = temp;
		} else if (bd2 <= bd3 & bd2 <= bd1) {
			Point temp = new Point(destination2);
			destination2 = new Point(destination3);
			destination3 = temp;
		}

		iD1 = new Point(destination1);
		iD2 = new Point(destination2);
		iSt = new Point(destination3);

		while (!iD1.equals(iD2)) {

			// destination 1

			// gets the probable next step towards opposite destination
			Link nextlD1 = routing.nextHop(mesh.getRouter(iD1.x, iD1.y),
					mesh.getRouter(iD2.x, iD2.y), mesh);
			Linkable nextD1 = nextlD1.traverse(mesh.getRouter(iD1.x, iD1.y));

			if (nextD1 instanceof Mesh2DRouter) {

				// gets the route towards 3rd destination
				Route iD1riSt = routing.route(mesh.getRouter(iD1.x, iD1.y),
						mesh.getRouter(iSt.x, iSt.y), mesh);

				// gets the route from probable next step towards 3rd
				// destination
				Route iD1rnext = routing.route(nextD1,
						mesh.getRouter(iSt.x, iSt.y), mesh);

				if (iD1riSt.getHopCount() >= iD1rnext.getHopCount()) {
					iD1 = mesh.getMeshLocation((Mesh2DRouter) nextD1);
				}
			}

			// destination 2

			// gets the probable next step towards opposite destination
			Link nextlD2 = routing.nextHop(mesh.getRouter(iD2.x, iD2.y),
					mesh.getRouter(iD1.x, iD1.y), mesh);
			Linkable nextD2 = nextlD2.traverse(mesh.getRouter(iD2.x, iD2.y));

			if (nextD2 instanceof Mesh2DRouter) {

				// gets the route towards 3rd destination
				Route iD2riSt = routing.route(mesh.getRouter(iD2.x, iD2.y),
						mesh.getRouter(iSt.x, iSt.y), mesh);

				// gets the route from probable next step towards 3rd
				// destination
				Route iD2rnext = routing.route(nextD2,
						mesh.getRouter(iSt.x, iSt.y), mesh);

				if (iD2riSt.getHopCount() >= iD2rnext.getHopCount()) {
					iD2 = mesh.getMeshLocation((Mesh2DRouter) nextD2);
				}
			}

		}

		intermediateS = iSt;
		intermediateD = new Point(iD1);

	}

	public int getUnicastHopCount() {
		int bs1d1 = getManhattanDistance(source1, destination1);
		int bs1d2 = getManhattanDistance(source1, destination2);
		int bs1d3 = getManhattanDistance(source1, destination3);
		int bs2d1 = getManhattanDistance(source2, destination1);
		int bs2d2 = getManhattanDistance(source2, destination2);
		int bs2d3 = getManhattanDistance(source2, destination3);
		return bs1d1 + bs1d2 + bs2d1 + bs2d2 + bs1d3 + bs2d3;

	}

}
