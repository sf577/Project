package lsi.noc.kernel.multicast;

import java.awt.Point;

import lsi.noc.kernel.Link;
import lsi.noc.kernel.Linkable;
import lsi.noc.kernel.Mesh2DRouter;
import lsi.noc.kernel.Route;

public class ConstructiveButterflyNetwork extends ButterflyNetwork {

	public ConstructiveButterflyNetwork(Point s1, Point s2, Point d1, Point d2) {

		super(s1, s2, d1, d2);

	}

	public void defineIntermediates() {

		Point iS1 = new Point(source1);
		Point iS2 = new Point(source2);
		Point iD1 = new Point(destination1);
		Point iD2 = new Point(destination2);

		/*
		 * 
		 * double inteSx = (source1.x+source2.x)/2; double inteSy =
		 * (source1.y+source2.y)/2; double inteDx =
		 * (destination1.x+destination2.x)/2; double inteDy =
		 * (destination1.y+destination2.y)/2;
		 * 
		 * int intSx, intDx, intSy, intDy; intSx = (int)Math.floor(inteSx);
		 * intSy = (int)Math.floor(inteSy); intDx = (int)Math.floor(inteDx);
		 * intDy = (int)Math.floor(inteDy);
		 */
		Point iSt = getFlooredMidpoint(source1, source2);
		Point iDt = getFlooredMidpoint(destination1, destination2);

		while (!iS1.equals(iS2) | !iD1.equals(iD2)) {

			// source 1

			// gets the probable next step towards mirror
			Link nextlS1 = routing.nextHop(mesh.getRouter(iS1.x, iS1.y),
					mesh.getRouter(iS2.x, iS2.y), mesh);
			Linkable nextS1 = nextlS1.traverse(mesh.getRouter(iS1.x, iS1.y));

			if (nextS1 instanceof Mesh2DRouter) {

				// gets the route towards opposite theoretical intermediate
				Route iS1riDt = routing.route(mesh.getRouter(iS1.x, iS1.y),
						mesh.getRouter(iDt.x, iDt.y), mesh);

				// gets the route from probable next step towards opposite
				// theoretical intermediate
				Route iS1rnext = routing.route(nextS1,
						mesh.getRouter(iDt.x, iDt.y), mesh);

				if (iS1riDt.getHopCount() >= iS1rnext.getHopCount()) {
					iS1 = mesh.getMeshLocation((Mesh2DRouter) nextS1);
				}
			}

			// source 2

			// gets the probable next step towards mirror
			Link nextlS2 = routing.nextHop(mesh.getRouter(iS2.x, iS2.y),
					mesh.getRouter(iS1.x, iS1.y), mesh);
			Linkable nextS2 = nextlS2.traverse(mesh.getRouter(iS2.x, iS2.y));

			if (nextS2 instanceof Mesh2DRouter) {

				// gets the route towards opposite theoretical intermediate
				Route iS2riDt = routing.route(mesh.getRouter(iS2.x, iS2.y),
						mesh.getRouter(iDt.x, iDt.y), mesh);

				// gets the route from probable next step towards opposite
				// theoretical intermediate
				Route iS2rnext = routing.route(nextS2,
						mesh.getRouter(iDt.x, iDt.y), mesh);

				if (iS2riDt.getHopCount() >= iS2rnext.getHopCount()) {
					iS2 = mesh.getMeshLocation((Mesh2DRouter) nextS2);
				}
			}

			// destination 1

			// gets the probable next step towards mirror
			Link nextlD1 = routing.nextHop(mesh.getRouter(iD1.x, iD1.y),
					mesh.getRouter(iD2.x, iD2.y), mesh);
			Linkable nextD1 = nextlD1.traverse(mesh.getRouter(iD1.x, iD1.y));

			if (nextD1 instanceof Mesh2DRouter) {

				// gets the route towards opposite theoretical intermediate
				Route iD1riSt = routing.route(mesh.getRouter(iD1.x, iD1.y),
						mesh.getRouter(iSt.x, iSt.y), mesh);

				// gets the route from probable next step towards opposite
				// theoretical intermediate
				Route iD1rnext = routing.route(nextD1,
						mesh.getRouter(iSt.x, iSt.y), mesh);

				if (iD1riSt.getHopCount() >= iD1rnext.getHopCount()) {
					iD1 = mesh.getMeshLocation((Mesh2DRouter) nextD1);
				}
			}

			// destination 2

			// gets the probable next step towards mirror
			Link nextlD2 = routing.nextHop(mesh.getRouter(iD2.x, iD2.y),
					mesh.getRouter(iD1.x, iD1.y), mesh);
			Linkable nextD2 = nextlD2.traverse(mesh.getRouter(iD2.x, iD2.y));

			if (nextD2 instanceof Mesh2DRouter) {

				// gets the route towards opposite theoretical intermediate
				Route iD2riSt = routing.route(mesh.getRouter(iD2.x, iD2.y),
						mesh.getRouter(iSt.x, iSt.y), mesh);

				// gets the route from probable next step towards opposite
				// theoretical intermediate
				Route iD2rnext = routing.route(nextD2,
						mesh.getRouter(iSt.x, iSt.y), mesh);

				if (iD2riSt.getHopCount() >= iD2rnext.getHopCount()) {
					iD2 = mesh.getMeshLocation((Mesh2DRouter) nextD2);
				}
			}

		}

		intermediateS = new Point(iS1);
		intermediateD = new Point(iD1);

	}

}
