package lsi.noc.kernel.mapping;

import java.awt.Point;

import lsi.noc.kernel.ApplicationModel;
import lsi.noc.kernel.Flow;
import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.ProcessingCore;
import lsi.noc.kernel.Route;
import lsi.noc.kernel.RoutingAlgorithm;
import lsi.noc.kernel.Task;

public class Mesh2DRouteFactory {

	protected ApplicationModel app;
	protected Mesh2DNoC noc;

	public Mesh2DRouteFactory(ApplicationModel application, Mesh2DNoC platform) {

		this.app = application;
		this.noc = platform;

	}

	public void createRoutes(RoutingAlgorithm routing) {

		for (int i = 0; i < app.getCommunications().size(); i++) {

			Flow flow = app.getCommunications().get(i).getFlow();

			Task sourcetask = flow.getSender();

			Point s = noc.getMeshLocation(sourcetask.getCore());
			Point d = noc.getMeshLocation(flow.getReceiver().getCore());

			ProcessingCore source = noc.getCore(s.x, s.y);
			ProcessingCore dest = noc.getCore(d.x, d.y);

			Route route = routing.route(source, dest, noc);

			for (int l = 0; l < route.getLinks().size(); l++) {
				route.getLinks().get(l).clearLink();
			}

			flow.setRoute(route);

		}

	}

}
