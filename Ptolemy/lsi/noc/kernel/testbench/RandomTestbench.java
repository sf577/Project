package lsi.noc.kernel.testbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import lsi.noc.kernel.ApplicationModel;
import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DNoCFactory;
import lsi.noc.kernel.Mesh2DXYRouting;
import lsi.noc.kernel.Platform;
import lsi.noc.kernel.ProcessingCore;
import lsi.noc.kernel.Route;
import lsi.noc.kernel.mapping.RandomMapping;
import lsi.noc.kernel.visualisation.RouteViewer;
import lsi.noc.kernel.priority.*;

public class RandomTestbench extends Thread {

	PriorityApplicationModel app;
	Platform plat;
	int x, y;
	double trav, rout;

	Mesh2DNoC network;
	Mesh2DXYRouting routing;
	RouteViewer panel;

	public static void main(String[] args) {

		RandomTestbench tb = new RandomTestbench();
		tb.start();

	}

	public void run() {

		// NoC dimensions
		x = 14;
		y = 14;

		app = new PriorityApplicationModel();

		Mesh2DNoCFactory factory = new Mesh2DNoCFactory();
		network = (Mesh2DNoC) factory.createInterconnect(x, y);

		routing = new Mesh2DXYRouting();

		plat = new Platform(network);

		createRandomTasks(50);
		createRandomFlows(50);

		RandomMapping mapper = new RandomMapping(app, plat, 32737);
		mapper.performMapping();

		adjustRoutes(0.00000001, 0.00000002, "Random Mapping");

		report();

	}

	public void createRandomTasks(int n) {

		Random rnd = new Random();

		// creates n tasks

		for (int i = 0; i < n; i++) {

			// period between 0.0001-0.2 seconds
			// comptime less than period, aiming for an average taskset
			// utilisation of 25%

			double period = 0.0001 + rnd.nextDouble() * 0.09999;
			double comptime = rnd.nextDouble() * period / 2;
			PriorityTask task = new PriorityTask(i, comptime);
			task.setName("Task" + i);
			task.setPeriod(period);
			task.setReleaseJitter(0);

			app.addTask(task);

		}

	}

	public void createRandomFlows(int n) {

		Random rnd = new Random();

		// creates n traffic flows with randomly generated sources and
		// destinations

		for (int i = 0; i < n && i < app.getTasks().size(); i++) {

			int d;
			do {
				d = rnd.nextInt(app.getTasks().size());
			} while (i == d);

			PriorityTask st = app.getTasks().get(i);
			PriorityTask dt = app.getTasks().get(d);

			PriorityTrafficFlow flow = new PriorityTrafficFlow(st, dt);

			flow.setPeriod(st.getPeriod());
			flow.setPriority(st.getPriority());
			// packets of size 3-28000
			flow.setPayload(3 + rnd.nextInt(27997));

			app.addFlow(flow);

		}

	}

	public void adjustRoutes(double t, double r, String st) {

		JFrame frame = new JFrame(st);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new RouteViewer();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(1200, 800));
		panel.setLabels(true);
		panel.setHopdistance(50);
		panel.setOrigin(new Point(40, 40));

		frame.pack();
		frame.setVisible(true);

		// time a flit takes to traverse a link
		trav = t;
		// time to route a header
		rout = r;

		for (int i = 0; i < app.getFlows().size(); i++) {

			PriorityTrafficFlow flow = app.getFlows().get(i);

			PriorityTask sourcetask = flow.getSourceTask();

			Point s = network.getMeshLocation(sourcetask.getCore());
			Point d = network.getMeshLocation(flow.getDestinationTask()
					.getCore());

			ProcessingCore source = network.getCore(s.x, s.y);
			ProcessingCore dest = network.getCore(d.x, d.y);

			Route route = routing.route(source, dest, network);
			flow.setRoute(route);
			flow.setReleaseJitter(sourcetask.getResponseTime(sourcetask
					.getInterferenceSet(app.getTasks())));

			panel.addRoute(route);
			panel.repaint();

		}

	}

	public void report() {

		System.out.println("----- Tasks ------");
		for (int i = 0; i < app.getTasks().size(); i++) {
			String name = app.getTasks().get(i).getName();
			double wcet = app
					.getTasks()
					.get(i)
					.getResponseTime(
							app.getTasks().get(i)
									.getInterferenceSet(app.getTasks()));
			System.out.println(i + "," + name + "," + wcet + " "
					+ app.getTasks().get(i).getCompTime());
			if (wcet > app.getTasks().get(i).getDeadline())
				System.out.println(name + " NOT SCHEDULABLE");

		}
		System.out.println("----- Flows ------");

		for (int i = 0; i < app.getFlows().size(); i++) {

			int dir = (app.getFlows().get(i).getDirectInterferenceSet(app
					.getFlows())).size();
			int indir = (app.getFlows().get(i).getIndirectInterferenceSet(app
					.getFlows())).size();
			double latency = app.getFlows().get(i).getBasicLatency(trav, rout);
			double dwclatency = app
					.getFlows()
					.get(i)
					.getDirectInterferenceWorstCaseLatency(trav, rout,
							app.getFlows());
			double wclatency = app.getFlows().get(i)
					.getWorstCaseLatency(trav, rout, app.getFlows());

			String source = app.getFlows().get(i).getSourceTask().getName();
			String dest = app.getFlows().get(i).getDestinationTask().getName();

			System.out.println("Flow: " + i + "," + source + "," + dest
					+ " .... Direct Interference: " + dir
					+ " .... Indirect Interference: " + indir
					+ " .... Basic Latency: " + latency + " .... WC Latency: "
					+ wclatency + " .... Direct WC Latency: " + dwclatency);

			if (wclatency > app.getFlows().get(i).getPeriod()) {
				System.out.println("DEADLINE MISSED!");
			}
			// System.out.println(latency+","+iwclatency);

		}
		System.out.println("-----------");

	}

}
