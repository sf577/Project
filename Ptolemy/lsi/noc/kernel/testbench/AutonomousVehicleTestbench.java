package lsi.noc.kernel.testbench;

import java.awt.Point;
import java.util.StringTokenizer;

import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DNoCFactory;
import lsi.noc.kernel.Mesh2DXYRouting;
import lsi.noc.kernel.Platform;
import lsi.noc.kernel.ProcessingCore;
import lsi.noc.kernel.mapping.Mesh2DRouteFactory;
import lsi.noc.kernel.priority.NoCSchedulabilityAnalysis;
import lsi.noc.kernel.priority.PrecedenceBasedPriorityMinimizer;
import lsi.noc.kernel.priority.PriorityTask;
import lsi.noc.kernel.priority.PriorityTrafficFlow;

public class AutonomousVehicleTestbench extends Testbench {

	public static void main(String[] args) {

		AutonomousVehicleTestbench tb = new AutonomousVehicleTestbench();
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
		x = 5;
		y = 5;

		// time a flit takes to traverse a link
		trav = 0.00000001;
		// time to route a header
		rout = 0.00000002;

		Mesh2DNoCFactory factory = new Mesh2DNoCFactory();
		network = (Mesh2DNoC) factory.createInterconnect(x, y);
		network.setHopDelay(trav);
		network.setRoutingDelay(rout);

		routingalgorithm = new Mesh2DXYRouting();

		plat = new Platform(network);

		// to reproduce experiments from IJERTCS paper, uncomment the lines
		// below and comment the three subsequent lines
		// app = new PriorityApplicationModel();
		// createApplicationIJERTCS();
		// mapTasks(0);

		app = new AutonomousVehicleApplication();

		// RandomMapping mapper = new RandomMapping(app,plat);
		// mapper.performMapping();
		mapTasks(33);

		Mesh2DRouteFactory routefactory = new Mesh2DRouteFactory(app, network);
		routefactory.createRoutes(routingalgorithm);

		NoCSchedulabilityAnalysis analysis = new NoCSchedulabilityAnalysis(app,
				network);
		analysis.performAnalysis();

		System.out.println("unschedulable tasks: "
				+ analysis.getUnschedulableTaskCount());
		System.out.println("unschedulable flows: "
				+ analysis.getUnschedulableFlowCount());

		setTitle("Autonomous Vehicle");

		displayRoutes(title);

		displayApplicationGraph(title);

		report();

		PrecedenceBasedPriorityMinimizer minim = new PrecedenceBasedPriorityMinimizer(
				app.getFlows());

	}

	// Not used anymore. Now an external class creates the application model.

	@Deprecated
	public void createApplicationDATE() {
		// creates traffic flows according to the strings below
		// models BasicVehicleApplication v0.6 with 320/240 resolution, 8 bit
		// color and 128 bit flits

		String[] flowdesc = new String[39];

		flowdesc[1] = "BFE1 FDF1 0.02 7680 60 62 0.04 13";
		flowdesc[2] = "BFE2 FDF1 0.02 7680 60 62 0.04 14";
		flowdesc[3] = "BFE3 FDF1 0.02 7680 60 62 0.04 15";
		flowdesc[4] = "BFE4 FDF1 0.02 7680 60 62 0.04 16";
		flowdesc[5] = "BFE5 FDF2 0.02 7680 60 62 0.04 17";
		flowdesc[6] = "BFE6 FDF2 0.02 7680 60 62 0.04 18";
		flowdesc[7] = "BFE7 FDF2 0.02 7680 60 62 0.04 19";
		flowdesc[8] = "BFE8 FDF2 0.02 7680 60 62 0.04 20";
		flowdesc[9] = "FBU1 BFE1 0.01 153600 1200 1202 0.04 5";
		flowdesc[10] = "FBU2 BFE2 0.01 153600 1200 1202 0.04 6";
		flowdesc[11] = "FBU3 BFE3 0.01 153600 1200 1202 0.04 1";
		flowdesc[12] = "FBU3 VOD1 0.01 153600 1200 1202 0.04 7";
		flowdesc[13] = "FBU4 BFE4 0.01 153600 1200 1202 0.04 8";
		flowdesc[14] = "FBU5 BFE5 0.01 153600 1200 1202 0.04 9";
		flowdesc[15] = "FBU6 BFE6 0.01 153600 1200 1202 0.04 10";
		flowdesc[16] = "FBU7 BFE7 0.01 153600 1200 1202 0.04 11";
		flowdesc[17] = "FBU8 BFE8 0.01 153600 1200 1202 0.04 2";
		flowdesc[18] = "FBU8 VOD2 0.01 153600 1200 1202 0.04 12";
		flowdesc[19] = "FDF1 STPH 0.01 30720 240 242 0.04 21";
		flowdesc[20] = "FDF2 STPH 0.01 30720 240 242 0.04 22";
		flowdesc[21] = "NAVC DIRC 0.02 16384 128 130 0.1 24";
		flowdesc[22] = "NAVC OBDB 0.01 32768 256 258 2.0 37";
		flowdesc[23] = "NAVC THRC 0.01 32768 256 258 0.1 26";
		flowdesc[24] = "OBDB NAVC 0.15 524288 4096 4098 2.0 38";
		flowdesc[25] = "OBDB OBMG 0.15 524288 4096 4098 2.0 36";
		flowdesc[26] = "OBMG OBDB 0.4 131072 1024 1026 1 34";
		flowdesc[27] = "POSI NAVC 0.005 16384 128 130 0.5 31";
		flowdesc[28] = "POSI OBMG 0.005 16384 128 130 0.5 32";
		flowdesc[29] = "SPES NAVC 0.005 16384 128 130 0.1 25";
		flowdesc[30] = "SPES STAC 0.005 32768 256 258 0.1 29";
		flowdesc[31] = "STAC THRC 0.01 16384 128 130 0.1 30";
		flowdesc[32] = "STAC TPRC 0.02 32768 256 258 1 35";
		flowdesc[33] = "STPH OBMG 0.02 131072 1024 1026 0.04 23";
		flowdesc[34] = "TPMS STAC 0.005 65536 512 514 0.5 33";
		flowdesc[35] = "USOS OBMG 0.005 32768 256 258 0.1 27";
		flowdesc[36] = "VIBS STAC 0.005 16384 128 130 0.1 28";
		flowdesc[37] = "VOD1 NAVC 0.02 16384 128 130 0.04 3";
		flowdesc[38] = "VOD2 NAVC 0.02 16384 128 130 0.04 4";

		for (int i = 1; i < flowdesc.length; i++) {

			StringTokenizer st = new StringTokenizer(flowdesc[i]);
			String so = st.nextToken();
			String de = st.nextToken();
			String scomptime = st.nextToken();
			double comptime = Double.parseDouble(scomptime);
			String bits = st.nextToken();
			String flits = st.nextToken();
			String total = st.nextToken();
			int payload = Integer.parseInt(total);
			String speriod = st.nextToken();
			double period = Double.parseDouble(speriod);
			String spriority = st.nextToken();
			int priority = Integer.parseInt(spriority);
			// String sjitter = st.nextToken();
			// double jitter = Double.parseDouble(sjitter);

			PriorityTask source = new PriorityTask(priority, comptime);
			source.setName(so);
			source.setPeriod(period);

			PriorityTask dest = new PriorityTask(priority * 100, 0.0);
			dest.setName(de);
			dest.setPeriod(period);

			source.setReleaseJitter(0);
			dest.setReleaseJitter(0);
			app.addTask(source);
			app.addTask(dest);

			PriorityTrafficFlow flow = new PriorityTrafficFlow(source, dest);
			flow.setPeriod(period);
			flow.setPriority(priority);
			flow.setPayload(payload);

			app.addFlow(flow);

		}

	}

	// Not used anymore. Now an external class creates the application model.
	@Deprecated
	public void createApplicationIJERTCS() {
		// creates traffic flows according to the strings below
		// models VehicleApplication as appears in IJERTCS v1 n2 2010

		String[] flowdesc = new String[39];

		flowdesc[1] = "BFE1 FDF1 0.00 7680 60 2048 0.04 13";
		flowdesc[2] = "BFE2 FDF1 0.00 7680 60 2048 0.04 14";
		flowdesc[3] = "BFE3 FDF1 0.00 7680 60 2048 0.04 15";
		flowdesc[4] = "BFE4 FDF1 0.00 7680 60 2048 0.04 16";
		flowdesc[5] = "BFE5 FDF2 0.00 7680 60 2048 0.04 17";
		flowdesc[6] = "BFE6 FDF2 0.00 7680 60 2048 0.04 18";
		flowdesc[7] = "BFE7 FDF2 0.00 7680 60 2048 0.04 19";
		flowdesc[8] = "BFE8 FDF2 0.00 7680 60 2048 0.04 20";
		flowdesc[9] = "FBU1 BFE1 0.00 153600 1200 38400 0.04 5";
		flowdesc[10] = "FBU2 BFE2 0.00 153600 1200 38400 0.04 6";
		flowdesc[11] = "FBU3 BFE3 0.00 153600 1200 38400 0.04 7";
		flowdesc[12] = "FBU3 VOD1 0.00 153600 1200 38400 0.04 1";
		flowdesc[13] = "FBU4 BFE4 0.00 153600 1200 38400 0.04 8";
		flowdesc[14] = "FBU5 BFE5 0.00 153600 1200 38400 0.04 9";
		flowdesc[15] = "FBU6 BFE6 0.00 153600 1200 38400 0.04 10";
		flowdesc[16] = "FBU7 BFE7 0.00 153600 1200 38400 0.04 11";
		flowdesc[17] = "FBU8 BFE8 0.00 153600 1200 38400 0.04 12";
		flowdesc[18] = "FBU8 VOD2 0.00 153600 1200 38400 0.04 2";
		flowdesc[19] = "FDF1 STPH 0.00 30720 240 8192 0.04 21";
		flowdesc[20] = "FDF2 STPH 0.00 30720 240 8192 0.04 22";
		flowdesc[21] = "NAVC DIRC 0.00 16384 128 512 0.1 24";
		flowdesc[22] = "NAVC OBDB 0.00 32768 256 2048 0.5 32";
		flowdesc[23] = "NAVC THRC 0.00 32768 256 1024 0.1 26";
		flowdesc[24] = "OBDB NAVC 0.00 524288 4096 16384 0.5 33";
		flowdesc[25] = "OBDB OBMG 0.00 524288 4096 16384 0.5 34";
		flowdesc[26] = "OBMG OBDB 0.0 131072 1024 4096 1 37";
		flowdesc[27] = "POSI NAVC 0.00 16384 128 1024 0.5 31";
		flowdesc[28] = "POSI OBMG 0.00 16384 128 1024 0.5 35";
		flowdesc[29] = "SPES NAVC 0.00 16384 128 512 0.1 25";
		flowdesc[30] = "SPES STAC 0.00 32768 256 1024 0.1 29";
		flowdesc[31] = "STAC THRC 0.0 16384 128 1024 0.1 30";
		flowdesc[32] = "STAC TPRC 0.0 32768 256 2048 1 38";
		flowdesc[33] = "STPH OBMG 0.0 131072 1024 4096 0.04 23";
		flowdesc[34] = "TPMS STAC 0.0 65536 512 2048 0.5 36";
		flowdesc[35] = "USOS OBMG 0.0 32768 256 1024 0.1 27";
		flowdesc[36] = "VIBS STAC 0.0 16384 128 512 0.1 28";
		flowdesc[37] = "VOD1 NAVC 0.0 16384 128 512 0.04 3";
		flowdesc[38] = "VOD2 NAVC 0.0 16384 128 512 0.04 4";

		for (int i = 1; i < flowdesc.length; i++) {

			StringTokenizer st = new StringTokenizer(flowdesc[i]);
			String so = st.nextToken();
			String de = st.nextToken();
			String scomptime = st.nextToken();
			double comptime = Double.parseDouble(scomptime);
			String bits = st.nextToken();
			String flits = st.nextToken();
			String total = st.nextToken();
			int payload = Integer.parseInt(total);
			String speriod = st.nextToken();
			double period = Double.parseDouble(speriod);
			String spriority = st.nextToken();
			int priority = Integer.parseInt(spriority);
			// String sjitter = st.nextToken();
			// double jitter = Double.parseDouble(sjitter);

			PriorityTask source = new PriorityTask(priority, comptime);
			source.setName(so);
			source.setPeriod(period);

			PriorityTask dest = new PriorityTask(priority * 100, 0.0);
			dest.setName(de);
			dest.setPeriod(period);

			source.setReleaseJitter(0);
			dest.setReleaseJitter(0);
			app.addTask(source);
			app.addTask(dest);

			PriorityTrafficFlow flow = new PriorityTrafficFlow(source, dest);
			flow.setPeriod(period);
			flow.setPriority(priority);
			flow.setPayload(payload);

			app.addFlow(flow);

		}

	}

	private void mapTasks(int version) {
		for (int k = 0; k < app.getTasks().size(); k++) {
			PriorityTask t = app.getTasks().get(k);
			Point p;
			if (version == 21) {
				p = mappingMADES21(t.getName());
			}
			if (version == 32) {
				p = mappingMADES32(t.getName());
			} else if (version == 33) {
				p = mappingMADES33(t.getName());
			} else {
				p = mappingIJERTCS(t.getName());
			}
			ProcessingCore pc = network.getCore(p.x, p.y);
			t.setCore(pc);

		}
	}

	private Point mappingIJERTCS(String task) {

		// Mapping IJERTCS
		if (task.equals("FBU4"))
			return new Point(0, 0);
		else if (task.equals("TPMS"))
			return new Point(0, 0);
		else if (task.equals("USOS"))
			return new Point(0, 0);
		else if (task.equals("BFE4"))
			return new Point(1, 0);
		else if (task.equals("THRC"))
			return new Point(1, 0);
		else if (task.equals("BFE8"))
			return new Point(2, 0);
		else if (task.equals("VOD2"))
			return new Point(2, 0);
		else if (task.equals("FBU8"))
			return new Point(3, 0);
		else if (task.equals("DIRC"))
			return new Point(3, 0);
		else if (task.equals("FBU3"))
			return new Point(0, 1);
		else if (task.equals("OBDB"))
			return new Point(0, 1);
		else if (task.equals("FDF2"))
			return new Point(1, 1);
		else if (task.equals("BFE3"))
			return new Point(1, 1);
		else if (task.equals("STPH"))
			return new Point(2, 1);
		else if (task.equals("BFE7"))
			return new Point(2, 1);
		else if (task.equals("FBU7"))
			return new Point(3, 1);
		else if (task.equals("SPES"))
			return new Point(3, 1);
		else if (task.equals("FBU2"))
			return new Point(0, 2);
		else if (task.equals("OBMG"))
			return new Point(0, 2);
		else if (task.equals("BFE2"))
			return new Point(1, 2);
		else if (task.equals("NAVC"))
			return new Point(1, 2);
		else if (task.equals("BFE6"))
			return new Point(2, 2);
		else if (task.equals("FDF1"))
			return new Point(2, 2);
		else if (task.equals("FBU6"))
			return new Point(3, 2);
		else if (task.equals("STAC"))
			return new Point(3, 2);
		else if (task.equals("FBU1"))
			return new Point(0, 3);
		else if (task.equals("POSI"))
			return new Point(0, 3);
		else if (task.equals("VOD1"))
			return new Point(1, 3);
		else if (task.equals("BFE1"))
			return new Point(1, 3);
		else if (task.equals("BFE5"))
			return new Point(2, 3);
		else if (task.equals("VIBS"))
			return new Point(2, 3);
		else if (task.equals("FBU5"))
			return new Point(3, 3);
		else if (task.equals("TPRC"))
			return new Point(3, 3);
		else
			return null;
	}

	private Point mappingMADES21(String task) {

		// Mapping MADES 2.1
		if (task.equals("FBU4"))
			return new Point(3, 2);
		else if (task.equals("TPMS"))
			return new Point(4, 4);
		else if (task.equals("USOS"))
			return new Point(0, 1);
		else if (task.equals("BFE4"))
			return new Point(0, 2);
		else if (task.equals("THRC"))
			return new Point(3, 2);
		else if (task.equals("BFE8"))
			return new Point(4, 0);
		else if (task.equals("VOD2"))
			return new Point(3, 4);
		else if (task.equals("FBU8"))
			return new Point(3, 1);
		else if (task.equals("DIRC"))
			return new Point(0, 1);
		else if (task.equals("FBU3"))
			return new Point(4, 4);
		else if (task.equals("OBDB"))
			return new Point(2, 1);
		else if (task.equals("FDF2"))
			return new Point(2, 3);
		else if (task.equals("BFE3"))
			return new Point(0, 3);
		else if (task.equals("STPH"))
			return new Point(1, 3);
		else if (task.equals("BFE7"))
			return new Point(3, 2);
		else if (task.equals("FBU7"))
			return new Point(4, 4);
		else if (task.equals("SPES"))
			return new Point(3, 4);
		else if (task.equals("FBU2"))
			return new Point(4, 0);
		else if (task.equals("OBMG"))
			return new Point(0, 1);
		else if (task.equals("BFE2"))
			return new Point(2, 1);
		else if (task.equals("NAVC"))
			return new Point(3, 4);
		else if (task.equals("BFE6"))
			return new Point(2, 3);
		else if (task.equals("FDF1"))
			return new Point(0, 3);
		else if (task.equals("FBU6"))
			return new Point(1, 0);
		else if (task.equals("STAC"))
			return new Point(0, 1);
		else if (task.equals("FBU1"))
			return new Point(3, 4);
		else if (task.equals("POSI"))
			return new Point(0, 1);
		else if (task.equals("VOD1"))
			return new Point(3, 3);
		else if (task.equals("BFE1"))
			return new Point(0, 1);
		else if (task.equals("BFE5"))
			return new Point(1, 1);
		else if (task.equals("VIBS"))
			return new Point(1, 3);
		else if (task.equals("FBU5"))
			return new Point(1, 1);
		else
			return null;

	}

	private Point mappingMADES32(String task) {

		// Mapping MADES 3.2
		if (task.equals("FBU4"))
			return new Point(0, 2);
		else if (task.equals("TPMS"))
			return new Point(3, 0);
		else if (task.equals("USOS"))
			return new Point(1, 1);
		else if (task.equals("BFE4"))
			return new Point(0, 2);
		else if (task.equals("THRC"))
			return new Point(3, 2);
		else if (task.equals("BFE8"))
			return new Point(0, 0);
		else if (task.equals("VOD2"))
			return new Point(2, 2);
		else if (task.equals("FBU8"))
			return new Point(3, 1);
		else if (task.equals("DIRC"))
			return new Point(0, 1);
		else if (task.equals("FBU3"))
			return new Point(0, 3);
		else if (task.equals("OBDB"))
			return new Point(2, 0);
		else if (task.equals("FDF2"))
			return new Point(2, 3);
		else if (task.equals("BFE3"))
			return new Point(1, 3);
		else if (task.equals("STPH"))
			return new Point(1, 2);
		else if (task.equals("BFE7"))
			return new Point(3, 2);
		else if (task.equals("FBU7"))
			return new Point(3, 0);
		else if (task.equals("SPES"))
			return new Point(2, 2);
		else if (task.equals("FBU2"))
			return new Point(0, 0);
		else if (task.equals("OBMG"))
			return new Point(1, 1);
		else if (task.equals("BFE2"))
			return new Point(2, 1);
		else if (task.equals("NAVC"))
			return new Point(2, 2);
		else if (task.equals("BFE6"))
			return new Point(2, 3);
		else if (task.equals("FDF1"))
			return new Point(1, 3);
		else if (task.equals("FBU6"))
			return new Point(2, 0);
		else if (task.equals("STAC"))
			return new Point(3, 3);
		else if (task.equals("FBU1"))
			return new Point(3, 2);
		else if (task.equals("POSI"))
			return new Point(0, 1);
		else if (task.equals("VOD1"))
			return new Point(3, 3);
		else if (task.equals("BFE1"))
			return new Point(0, 1);
		else if (task.equals("BFE5"))
			return new Point(1, 0);
		else if (task.equals("VIBS"))
			return new Point(1, 2);
		else if (task.equals("FBU5"))
			return new Point(1, 0);
		else
			return null;

	}

	private Point mappingMADES33(String task) {

		// Mapping MADES 3.3
		if (task.equals("FBU4"))
			return new Point(2, 1);
		else if (task.equals("TPMS"))
			return new Point(1, 0);
		else if (task.equals("USOS"))
			return new Point(2, 3);
		else if (task.equals("BFE4"))
			return new Point(3, 0);
		else if (task.equals("THRC"))
			return new Point(2, 2);
		else if (task.equals("BFE8"))
			return new Point(0, 1);
		else if (task.equals("VOD2"))
			return new Point(3, 1);
		else if (task.equals("FBU8"))
			return new Point(2, 3);
		else if (task.equals("DIRC"))
			return new Point(3, 1);
		else if (task.equals("FBU3"))
			return new Point(1, 3);
		else if (task.equals("OBDB"))
			return new Point(0, 1);
		else if (task.equals("FDF2"))
			return new Point(0, 2);
		else if (task.equals("BFE3"))
			return new Point(2, 0);
		else if (task.equals("STPH"))
			return new Point(3, 2);
		else if (task.equals("BFE7"))
			return new Point(0, 2);
		else if (task.equals("FBU7"))
			return new Point(2, 3);
		else if (task.equals("SPES"))
			return new Point(3, 0);
		else if (task.equals("FBU2"))
			return new Point(2, 2);
		else if (task.equals("OBMG"))
			return new Point(3, 2);
		else if (task.equals("BFE2"))
			return new Point(1, 1);
		else if (task.equals("NAVC"))
			return new Point(0, 1);
		else if (task.equals("BFE6"))
			return new Point(2, 2);
		else if (task.equals("FDF1"))
			return new Point(3, 1);
		else if (task.equals("FBU6"))
			return new Point(1, 0);
		else if (task.equals("STAC"))
			return new Point(3, 1);
		else if (task.equals("FBU1"))
			return new Point(1, 3);
		else if (task.equals("POSI"))
			return new Point(3, 1);
		else if (task.equals("VOD1"))
			return new Point(2, 1);
		else if (task.equals("BFE1"))
			return new Point(0, 0);
		else if (task.equals("BFE5"))
			return new Point(1, 0);
		else if (task.equals("VIBS"))
			return new Point(1, 0);
		else if (task.equals("FBU5"))
			return new Point(3, 0);
		else if (task.equals("TPRC"))
			return new Point(1, 0);
		else
			return null;

	}

}
