package lsi.noc.kernel.testbench;

import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DNoCFactory;
import lsi.noc.kernel.Mesh2DXYRouting;
import lsi.noc.kernel.Platform;
import lsi.noc.kernel.mapping.FirstFit;
import lsi.noc.kernel.mapping.Mesh2DRouteFactory;
import lsi.noc.kernel.mapping.RandomMapping;
import lsi.noc.kernel.priority.NoCSchedulabilityAnalysis;
import lsi.noc.kernel.priority.PrecedenceBasedPriorityMinimizer;
import lsi.noc.kernel.priority.PriorityApplicationModel;
import lsi.noc.kernel.priority.PriorityTask;
import lsi.noc.kernel.priority.PriorityTrafficFlow;

public class SimpleTestbench extends Testbench {

	public static void main(String[] args) {

		SimpleTestbench tb = new SimpleTestbench();
		tb.start();

	}

	public void run() {

		/*
		 * Recipe for a testbench:
		 * 
		 * 1 - define the NoC parameters (dimensions, switch latencies) 2 -
		 * create the NoC and use it to create a platform model 3 - create an
		 * application model 4 - create a mapper and use it to map all
		 * application tasks onto platform processing elements 5 - create a
		 * routefactory to create flows and their respective routes for all
		 * mapped tasks
		 * 
		 * optionals:
		 * 
		 * 6 - create a schedulability analysis, to add the task response time
		 * as release jitter to their respective flows and then evaluate
		 * schedulability 7 - use a RouteViewer to visualise all routes 8 - use
		 * a priority minimizer to verify the minimum number of priority levels
		 */

		// NoC dimensions
		x = 3;
		y = 2;

		// time a flit takes to traverse a link
		trav = 0.00000001;
		// time to route a header
		rout = 0.00000002;

		Mesh2DNoCFactory factory = new Mesh2DNoCFactory();
		network = (Mesh2DNoC) factory.createInterconnect(x, y);
		network.setHopDelay(trav);
		network.setRoutingDelay(rout);
		plat = new Platform(network);

		routingalgorithm = new Mesh2DXYRouting();

		app = new PriorityApplicationModel();

		PriorityTask p1 = new PriorityTask(1, 0.02);
		PriorityTask p2 = new PriorityTask(2, 0.01);
		PriorityTask p3 = new PriorityTask(3, 0.004);
		PriorityTask p4 = new PriorityTask(4, 0.012);
		PriorityTask p5 = new PriorityTask(5, 0.011);
		PriorityTask p6 = new PriorityTask(6, 0.024);
		PriorityTask p7 = new PriorityTask(7, 0.1);
		PriorityTask p8 = new PriorityTask(8, 0.002);
		PriorityTask p9 = new PriorityTask(9, 0.023);
		PriorityTask p10 = new PriorityTask(10, 0.12);

		p1.setName("p1");
		p1.setPeriod(0.04);
		app.addTask(p1);
		p2.setName("p2");
		p2.setPeriod(0.03);
		app.addTask(p2);
		p3.setName("p3");
		p3.setPeriod(0.008);
		app.addTask(p3);
		p4.setName("p4");
		p4.setPeriod(0.1);
		app.addTask(p4);
		p5.setName("p5");
		p5.setPeriod(0.4);
		app.addTask(p5);
		p6.setName("p6");
		p6.setPeriod(0.12);
		app.addTask(p6);
		p7.setName("p7");
		p7.setPeriod(0.4);
		app.addTask(p7);
		p8.setName("p8");
		p8.setPeriod(0.05);
		app.addTask(p8);
		p9.setName("p9");
		p9.setPeriod(0.09);
		app.addTask(p9);
		p10.setName("p10");
		p10.setPeriod(0.4);
		app.addTask(p10);

		PriorityTrafficFlow t1 = new PriorityTrafficFlow(p1, p4);
		PriorityTrafficFlow t2 = new PriorityTrafficFlow(p2, p3);
		PriorityTrafficFlow t3 = new PriorityTrafficFlow(p3, p1);
		PriorityTrafficFlow t4 = new PriorityTrafficFlow(p4, p10);
		PriorityTrafficFlow t5 = new PriorityTrafficFlow(p5, p3);
		PriorityTrafficFlow t6 = new PriorityTrafficFlow(p6, p5);
		PriorityTrafficFlow t7 = new PriorityTrafficFlow(p7, p9);
		PriorityTrafficFlow t8 = new PriorityTrafficFlow(p8, p2);
		PriorityTrafficFlow t9 = new PriorityTrafficFlow(p9, p6);
		PriorityTrafficFlow t10 = new PriorityTrafficFlow(p10, p6);

		t1.setPriority(p1.getPriority());
		t1.setPeriod(p1.getPeriod());
		t1.setPayload(400);
		t2.setPriority(p2.getPriority());
		t2.setPeriod(p2.getPeriod());
		t2.setPayload(100);
		t3.setPriority(p3.getPriority());
		t1.setPeriod(p3.getPeriod());
		t3.setPayload(800);
		t4.setPriority(p4.getPriority());
		t1.setPeriod(p4.getPeriod());
		t4.setPayload(230);
		t5.setPriority(p5.getPriority());
		t1.setPeriod(p5.getPeriod());
		t5.setPayload(140);
		t6.setPriority(p6.getPriority());
		t1.setPeriod(p6.getPeriod());
		t6.setPayload(110);
		t7.setPriority(p7.getPriority());
		t1.setPeriod(p7.getPeriod());
		t7.setPayload(430);
		t8.setPriority(p8.getPriority());
		t1.setPeriod(p8.getPeriod());
		t8.setPayload(330);
		t9.setPriority(p9.getPriority());
		t1.setPeriod(p9.getPeriod());
		t9.setPayload(800);
		t10.setPriority(p10.getPriority());
		t1.setPeriod(p10.getPeriod());
		t10.setPayload(120);

		app.addFlow(t1);
		app.addFlow(t2);
		app.addFlow(t3);
		app.addFlow(t4);
		app.addFlow(t5);
		app.addFlow(t6);
		app.addFlow(t7);
		app.addFlow(t8);
		app.addFlow(t9);
		app.addFlow(t10);

		// RandomMapping mapper = new RandomMapping(app,plat);
		// mapper.performMapping();

		FirstFit mapper = new FirstFit(app, plat);
		mapper.performMapping();

		Mesh2DRouteFactory routefactory = new Mesh2DRouteFactory(app, network);
		routefactory.createRoutes(routingalgorithm);

		NoCSchedulabilityAnalysis analysis = new NoCSchedulabilityAnalysis(app,
				network);
		analysis.performAnalysis();

		System.out.println("unschedulable tasks: "
				+ analysis.getUnschedulableTaskCount());
		System.out.println("unschedulable flows: "
				+ analysis.getUnschedulableFlowCount());

		setTitle("Simple Testbench");

		displayRoutes(title);

		displayApplicationGraph(title);

		report();

		PrecedenceBasedPriorityMinimizer minim = new PrecedenceBasedPriorityMinimizer(
				app.getFlows());

	}

}
