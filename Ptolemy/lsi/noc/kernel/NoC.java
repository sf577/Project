package lsi.noc.kernel;

import java.util.ArrayList;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 25/09/2009) 
 */

public class NoC extends Interconnect {

	ArrayList<Router> routers;
	double routingDelay;
	double hopDelay;

	public NoC() {

		super();
		routers = new ArrayList<Router>();

	}

	// returns true if Router r is one
	// of the routers of this NoC
	public boolean contains(Router r) {

		if (routers.contains(r))
			return true;
		else
			return false;
	}

	// returns true if ProcessingCore c is connected to one
	// of the routers of this NoC
	public boolean links(ProcessingCore c) {
		Linkable router = c.getOutput().traverse(c);
		if (router instanceof Mesh2DRouter)
			return contains((Mesh2DRouter) router);
		else
			return false;

	}

	/**
	 * @return the routingDelay
	 */
	public double getRoutingDelay() {
		return routingDelay;
	}

	/**
	 * @param routingDelay
	 *            the routingDelay to set
	 */
	public void setRoutingDelay(double routingDelay) {
		this.routingDelay = routingDelay;
	}

	/**
	 * @return the hopDelay
	 */
	public double getHopDelay() {
		return hopDelay;
	}

	/**
	 * @param hopDelay
	 *            the hopDelay to set
	 */
	public void setHopDelay(double hopDelay) {
		this.hopDelay = hopDelay;
	}

}
