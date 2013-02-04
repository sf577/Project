package lsi.noc.kernel.mapping;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JFrame;

import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DNoCFactory;
import lsi.noc.kernel.Mesh2DXYRouting;
import lsi.noc.kernel.Platform;
import lsi.noc.kernel.ProcessingCore;
import lsi.noc.kernel.RandomApplicationModel;
import lsi.noc.kernel.Route;
import lsi.noc.kernel.Task;
import lsi.noc.kernel.priority.NoCSchedulabilityAnalysis;
import lsi.noc.kernel.priority.PriorityApplicationModel;
import lsi.noc.kernel.priority.PriorityTask;
import lsi.noc.kernel.priority.PriorityTrafficFlow;
import lsi.noc.kernel.testbench.AutonomousVehicleApplication;
import lsi.noc.kernel.testbench.SyntheticApplication;
import lsi.noc.kernel.visualisation.EvolutionViewer;
import lsi.noc.nocscope2.UtilizationScope;
import lsi.noc.nocscope2.UtilizationSwitch;

//import lsi.noc.kernel.visualisation.RouteViewer;

public class GeneticMapping extends Thread {

	RandomMapping mapping;

	EvolutionViewer panel;
	UtilizationScope utilScope;
	JFrame frame;
	JFrame frameUtil;

	ArrayList<Chromo> population;
	double averageFitness1;
	double averageFitness2;
	Chromo solution;
	Chromo bestCurrentSol;
	boolean solutionFound = false;
	int tableSize, generation, tasksNo, crossoverPoint, minScore;

	Mesh2DNoC network;
	Platform platform;
	Mesh2DXYRouting routingalgorithm;
	Mesh2DRouteFactory routefactory;
	NoCSchedulabilityAnalysis analysis;

	int x, y;
	double trav, rout;

	private class Chromo {

		Map<Task, ProcessingCore> map;
		int score1, score2;

		private Chromo(Map<Task, ProcessingCore> m) {
			map = m;
		}

	}

	public static void main(String[] args) {
		GeneticMapping tb = new GeneticMapping();
		tb.start();
	}

	public void run() {

		// NoC dimensions
		x = 2;
		y = 4;

		// time a flit takes to traverse a link : 10 nanoseconds = 1 cycle @
		// 100MHz
		trav = 0.00000001;
		// time to route a header : 50 nanoseconds = 2 cycles @ 100MHz
		rout = 0.00000002;

		Mesh2DNoCFactory factory = new Mesh2DNoCFactory();
		network = (Mesh2DNoC) factory.createInterconnect(x, y);
		network.setHopDelay(trav);
		network.setRoutingDelay(rout);
		routingalgorithm = new Mesh2DXYRouting();

		platform = new Platform(network);
		utilScope = new UtilizationScope(x, y);

		// SyntheticApplication application = new SyntheticApplication();
		AutonomousVehicleApplication application = new AutonomousVehicleApplication();

		routefactory = new Mesh2DRouteFactory(application, network);

		analysis = new NoCSchedulabilityAnalysis(application, network);

		tasksNo = application.getTasks().size();

		mapping = new RandomMapping(application, platform);

		tableSize = mapping.mapping.size();

		population = new ArrayList<Chromo>();

		initGUI("Random application - " + application.getTasks().size()
				+ " tasks - " + application.getCommunications().size()
				+ " flows");

		// Initial stage of the genetic algorithm - generate population of
		// random solutions//

		generation = 0;

		// Generate initial generation of random soltions - Generation Size

		for (int j = 0; j < 100; j++) {
			mapping.performMapping();
			population.add(new Chromo(mapping.mapping));
		}

		// The Genetic Routine

		while (!solutionFound) {
			evaluate(population);
			if (!solutionFound) {
				reproduce(population);
				panel.register(minScore);
				generation++;

				/* Print the minimum score of each Generation */
				System.out.println(minScore);

				/* Show best solution util on utilscope */
				/*
				 * ArrayList <Chromo> c = new ArrayList<Chromo>();
				 * c.add(bestCurrentSol); evaluate(c);
				 */
				mapping.performMapping(bestCurrentSol.map);
				displayProcUtil();
				displayFlowsUtil();
			}
		}

	}

	// Evaluate each mapping, look for solutions and find the average cost//
	public void evaluate(ArrayList<Chromo> pop) {

		double avgFitness1 = 0;
		double avgFitness2 = 0;
		int minScore2 = Integer.MAX_VALUE;

		Map<Task, ProcessingCore> m;

		loop1: for (int i = 0; i < pop.size(); i++) {

			m = pop.get(i).map;
			mapping.performMapping(m);

			// routefactory.createRoutes(routingalgorithm, mapping);
			adjustRoutes(0.00000001, 0.00000002);
			analysis.performAnalysis();

			pop.get(i).score1 = analysis.getUnschedulableTaskCount();
			pop.get(i).score2 = analysis.getUnschedulableFlowCount();

			if (pop.get(i).score2 < minScore2) {
				bestCurrentSol = null;
				bestCurrentSol = pop.get(i);
				minScore2 = pop.get(i).score2;

			}

			if (analysis.isSchedulable()) {

				solution = pop.get(i);
				solutionFound = true;

				System.out.println("SOLUTION FOUND");
				System.out.println("generation " + generation);
				System.out.println(solution.score2);

				printMapping(solution.map);

				break loop1;
			}

			avgFitness1 = avgFitness1 + pop.get(i).score1;
			avgFitness2 = avgFitness2 + pop.get(i).score2;

		}

		avgFitness1 = avgFitness1 / pop.size();
		avgFitness2 = avgFitness2 / pop.size();

		averageFitness1 = avgFitness1;
		averageFitness2 = avgFitness2;
		minScore = minScore2;

	}

	private void printMapping(Map<Task, ProcessingCore> m) {

		Set<Map.Entry<Task, ProcessingCore>> set = m.entrySet();
		Iterator<Map.Entry<Task, ProcessingCore>> it = set.iterator();

		while (it.hasNext()) {

			Map.Entry<Task, ProcessingCore> entry = (Entry<Task, ProcessingCore>) it
					.next();
			System.out.println(entry.getKey().getName() + " "
					+ entry.getKey().getCore().toString() + " "
					+ this.network.getMeshLocation(entry.getKey().getCore()).x
					+ " "
					+ this.network.getMeshLocation(entry.getKey().getCore()).y);

		}
	}

	// /Selection stage of the genetic algorithm///
	private void reproduce(ArrayList<Chromo> pop) {

		Iterator<Chromo> it = pop.iterator();

		int selectionSize;

		while (it.hasNext()) {

			Chromo c = it.next();

			// Make better selection

			if (c.score2 > averageFitness2) {

				it.remove();
				continue;
			}
		}

		// Get the population back to initial size by adding chromosomes that
		// are crossovers of the initial population
		selectionSize = pop.size();
		Random r = new Random();

		while (pop.size() != 100) {

			Chromo c1 = pop.get(r.nextInt(selectionSize));
			Chromo c2 = pop.get(r.nextInt(selectionSize));

			// Crossover point is random
			pop.add(crossOver(c1, c2, r.nextInt(c1.map.size())));

		}
		// Mutate the initial population's each chromosome with a probability of
		// 50%
		for (int i = 0; i < selectionSize; i++) {

			int f = r.nextInt(2);

			if (f == 1)
				mutate1(pop.get(i));

		}

	}

	private Chromo crossOver(Chromo c1, Chromo c2, int cPoint) {

		Map<Task, ProcessingCore> m = new Hashtable<Task, ProcessingCore>();
		Chromo ChRes = new Chromo(m);
		ChRes.map.clear();

		ArrayList<? extends Task> tasks = mapping.getApplication().getTasks();

		for (int k = 0; k < cPoint; k++) {

			Task t = tasks.get(k);
			// ProcessingCore c = c1.map.get(t);
			mapping.remapTask(t, c1.map.get(t));

		}

		for (int k = cPoint; k < tasks.size(); k++) {

			Task t = tasks.get(k);
			// ProcessingCore c = c2.map.get(t);
			mapping.remapTask(t, c2.map.get(t));

		}

		ChRes.map = mapping.mapping;

		return ChRes;

	}

	private void mutate1(Chromo c) {

		Random r = new Random();

		ArrayList<? extends Task> tasks = mapping.getApplication().getTasks();

		Task t1 = tasks.get(r.nextInt(tasks.size()));

		Task t2 = tasks.get(r.nextInt(tasks.size()));

		mapping.performMapping(c.map);

		mapping.swapTasks(t1, t2);

		c.map = mapping.getMapping();

	}

	public void initGUI(String st) {

		frame = new JFrame(st);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new EvolutionViewer();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(800, 600));

		frameUtil = new JFrame("UtilizationScope");
		frameUtil.getContentPane().add(utilScope);
		frameUtil.setSize(x * 100 + 10, y * 100 + 28);
		frameUtil.setVisible(true);

		frame.pack();
		frame.setVisible(true);

	}

	public void adjustRoutes(double t, double r) {

		routefactory.createRoutes(routingalgorithm);

		ArrayList<PriorityTask> pritasks = new ArrayList<PriorityTask>();

		for (int j = 0; j < mapping.getApplication().getTasks().size(); j++) {
			pritasks.add((PriorityTask) mapping.getApplication().getTasks()
					.get(j));
		}

		for (int i = 0; i < mapping.getApplication().getCommunications().size(); i++) {

			PriorityTrafficFlow flow = (PriorityTrafficFlow) mapping
					.getApplication().getCommunications().get(i);
			/*
			 * PriorityTask sourcetask = flow.getSourceTask(); Mesh2DNoC noc =
			 * (Mesh2DNoC) mapping.getPlatform().getInterconnect(); Point s =
			 * noc.getMeshLocation(sourcetask.getCore()); Point d =
			 * noc.getMeshLocation(flow.getDestinationTask().getCore());
			 * 
			 * ProcessingCore source = noc.getCore(s.x, s.y); ProcessingCore
			 * dest = noc.getCore(d.x, d.y); Route route =
			 * routingalgorithm.route(source, dest, noc);
			 * 
			 * for(int l = 0; l< route.getLinks().size();l++)
			 * 
			 * {route.getLinks().get(l).clearLink();}
			 * 
			 * flow.setRoute(route);
			 */
			flow.setReleaseJitter(flow.getSourceTask().getResponseTime(
					flow.getSourceTask().getInterferenceSet(pritasks)));

		}

	}

	public void displayProcUtil() {
		// Get Processor Utilisation and display on UtilScope
		for (int i = 0; i < this.platform.getCores().size(); i++) {
			UtilizationSwitch s = utilScope.getSwitch(this.network
					.getMeshLocation(this.platform.getCores().get(i)).x,
					this.network.getMeshLocation(this.platform.getCores()
							.get(i)).y);
			s.setLocal((int) (100 * this.platform.getCores().get(i)
					.getUtilization()));
			// System.out.println(this.platform.getCores().get(i).getUtil());
		}
		utilScope.repaint();
	}

	public void displayFlowsUtil() {

		for (int i = 1; i < x; i++) {
			for (int j = 1; j < y; j++)

			{

				UtilizationSwitch s = utilScope.getSwitch(i, j);

				if (this.network.getRouter(i, j).linksOut[0] != null)
					s.setEast((int) (100 * this.network.getRouter(i, j).linksOut[0]
							.getLinkUtilisation()));
				if (this.network.getRouter(i, j).linksOut[1] != null)
					s.setWest((int) (100 * this.network.getRouter(i, j).linksOut[1]
							.getLinkUtilisation()));
				if (this.network.getRouter(i, j).linksOut[2] != null)
					s.setNorth((int) (100 * this.network.getRouter(i, j).linksOut[2]
							.getLinkUtilisation()));
				if (this.network.getRouter(i, j).linksOut[3] != null)
					s.setSouth((int) (100 * this.network.getRouter(i, j).linksOut[3]
							.getLinkUtilisation()));
				if (this.network.getRouter(i, j).linksOut[4] != null)
					s.setIn((int) (100 * this.network.getRouter(i, j).linksOut[4]
							.getLinkUtilisation()));
				s.setOut((int) (100 * this.network.getRouter(i, j).linksOut[4]
						.getLinkUtilisation()));
			}

		}

	}

}
