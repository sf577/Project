package lsi.noc.kernel.multicast;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import lsi.noc.kernel.Interconnect;
import lsi.noc.kernel.Link;
import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DNoCFactory;
import lsi.noc.kernel.Mesh2DRouter;
import lsi.noc.kernel.MultiRoute;
import lsi.noc.kernel.Route;
import lsi.noc.kernel.Router;

/**
 * An implementation of the RPM multicast algorithm for NoCs proposed by Wang et
 * al.
 * 
 * L. Wang, Y. Jin, H. Kim, and E.J. Kim, “Recursive partitioning multicast: A
 * bandwidth-efficient routing for Networks-on-Chip,” Proc. 3rd ACM/IEEE
 * International Symposium on Networks-on-Chip, IEEE Computer Society, 2009, pp.
 * 64-73.
 * 
 * @author Leandro Soares Indrusiak
 * @version 1.0 (York, 6/1/2010)
 */

public class RPMRoute extends MultiRoute {

	Point source;
	Point[] destinations;

	// defines identifiers of the network partitions as described in the paper
	static final int E = 7;
	static final int W = 3;
	static final int N = 1;
	static final int S = 5;
	static final int NE = 0;
	static final int SE = 6;
	static final int NW = 2;
	static final int SW = 4;

	public RPMRoute(Point s1, Point[] d) {
		super();
		source = s1;
		destinations = d;

		generateMesh();
		route(source, destinations);

	}

	public RPMRoute(Point s1, Point[] d, Interconnect net) {
		super(net);
		source = s1;
		destinations = d;

		route(source, destinations);

	}

	public void generateMesh() {

		Mesh2DNoCFactory factory = new Mesh2DNoCFactory();

		Rectangle rec = new Rectangle(new Point(0, 0));
		rec.add(source);
		for (int i = 0; i < destinations.length; i++) {
			rec.add(destinations[i]);
		}
		int maxx = rec.width + 1;
		int maxy = rec.height + 1;

		network = (Mesh2DNoC) factory.createInterconnect(maxx, maxy);

	}

	// implements recursively the routing computation algorithm by Wang et al

	public void route(Point s1, Point[] d) {

		if (!(d.length == 1 & s1.equals(d[0]))) { // stop recursion if source
													// equals destination

			boolean isN = false;
			boolean isS = false;
			boolean isE = false;
			boolean isW = false;
			boolean isNE = false;
			boolean isNW = false;
			boolean isSE = false;
			boolean isSW = false;

			ArrayList<Point> destN = new ArrayList<Point>();
			ArrayList<Point> destNE = new ArrayList<Point>();
			ArrayList<Point> destNW = new ArrayList<Point>();
			ArrayList<Point> destS = new ArrayList<Point>();
			ArrayList<Point> destSE = new ArrayList<Point>();
			ArrayList<Point> destSW = new ArrayList<Point>();
			ArrayList<Point> destE = new ArrayList<Point>();
			ArrayList<Point> destW = new ArrayList<Point>();

			for (int i = 0; i < d.length; i++) {

				if (getDestinationPart(s1, d[i]) == RPMRoute.N) {
					isN = true;
					destN.add(d[i]);
				} else if (getDestinationPart(s1, d[i]) == RPMRoute.S) {
					isS = true;
					destS.add(d[i]);
				} else if (getDestinationPart(s1, d[i]) == RPMRoute.E) {
					isE = true;
					destE.add(d[i]);
				} else if (getDestinationPart(s1, d[i]) == RPMRoute.W) {
					isW = true;
					destW.add(d[i]);
				} else if (getDestinationPart(s1, d[i]) == RPMRoute.SE) {
					isSE = true;
					destSE.add(d[i]);
				} else if (getDestinationPart(s1, d[i]) == RPMRoute.SW) {
					isSW = true;
					destSW.add(d[i]);
				} else if (getDestinationPart(s1, d[i]) == RPMRoute.NE) {
					isNE = true;
					destNE.add(d[i]);
				} else if (getDestinationPart(s1, d[i]) == RPMRoute.NW) {
					isNW = true;
					destNW.add(d[i]);
				}

			}

			boolean goN = false;
			boolean goS = false;
			boolean goE = false;
			boolean goW = false;

			if (isE | isSE & !isS & !isSW)
				goE = true;
			if (isN | (isNE & (!isE | (!isSW & isSE))) | (isNE & isNW))
				goN = true;
			if (isW | isNW & !isN & !isNE)
				goW = true;
			if (isS | (isSW & (!isW | (!isNE & isSW))) | (isSW & isSE))
				goS = true;

			if (goN) {
				Router current = ((Mesh2DNoC) network).getRouter(s1.x, s1.y);
				Link out = current.linksOut[Mesh2DRouter.NORTH];
				Route next = new Route();
				next.add(out);
				this.routes.add(next);

				destN.addAll(destNE);
				if (!goW)
					destN.addAll(destNW);

				Point[] newdests = new Point[1];
				newdests = destN.toArray(newdests);

				route(new Point(s1.x, s1.y + 1), newdests);
			}

			if (goS) {
				Router current = ((Mesh2DNoC) network).getRouter(s1.x, s1.y);
				Link out = current.linksOut[Mesh2DRouter.SOUTH];
				Route next = new Route();
				next.add(out);
				this.routes.add(next);

				destS.addAll(destSW);
				if (!goE)
					destS.addAll(destSE);

				Point[] newdests = new Point[1];
				newdests = destS.toArray(newdests);

				route(new Point(s1.x, s1.y - 1), newdests);
			}

			if (goE) {
				Router current = ((Mesh2DNoC) network).getRouter(s1.x, s1.y);
				Link out = current.linksOut[Mesh2DRouter.EAST];
				Route next = new Route();
				next.add(out);
				this.routes.add(next);

				destE.addAll(destSE);
				if (!goN)
					destE.addAll(destNE);

				Point[] newdests = new Point[1];
				newdests = destE.toArray(newdests);

				route(new Point(s1.x + 1, s1.y), newdests);
			}

			if (goW) {
				Router current = ((Mesh2DNoC) network).getRouter(s1.x, s1.y);
				Link out = current.linksOut[Mesh2DRouter.WEST];
				Route next = new Route();
				next.add(out);
				this.routes.add(next);

				destW.addAll(destNW);
				if (!goS)
					destE.addAll(destSW);

				Point[] newdests = new Point[1];
				newdests = destW.toArray(newdests);

				route(new Point(s1.x - 1, s1.y), newdests);
			}

		}

	}

	// for a given source, returns the part of the network where the destination
	// lies
	public int getDestinationPart(Point src, Point dst) {

		if (dst.x > src.x & dst.y > src.y) {
			return 0;
		} else if (dst.x == src.x & dst.y > src.y) {
			return 1;
		} else if (dst.x < src.x & dst.y > src.y) {
			return 2;
		} else if (dst.x < src.x & dst.y == src.y) {
			return 3;
		} else if (dst.x < src.x & dst.y < src.y) {
			return 4;
		} else if (dst.x == src.x & dst.y < src.y) {
			return 5;
		} else if (dst.x > src.x & dst.y < src.y) {
			return 6;
		} else if (dst.x > src.x & dst.y == src.y) {
			return 7;
		} else
			return 8; // src == dst

	}

}
