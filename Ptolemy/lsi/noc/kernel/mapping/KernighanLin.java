package lsi.noc.kernel.mapping;

import java.util.ArrayList;
import java.util.Hashtable;

import lsi.noc.kernel.ApplicationModel;
import lsi.noc.kernel.Communication;
import lsi.noc.kernel.Interconnect;
import lsi.noc.kernel.Platform;
import lsi.noc.kernel.PointToPointInterconnectFactory;
import lsi.noc.kernel.ProcessingCore;
import lsi.noc.kernel.RandomApplicationModel;
import lsi.noc.kernel.SimpleCommunication;
import lsi.noc.kernel.SimpleTask;
import lsi.noc.kernel.Task;
import lsi.noc.kernel.sdf.SDFCommunication;
import lsi.noc.kernel.sdf.SDFTask;

public class KernighanLin extends Mapping {

	protected ArrayList<Communication> comms;

	private double[] deltas;
	private Task[] xlist;
	private Task[] ylist;
	private Hashtable<Task, Boolean> locked;

	public KernighanLin(ApplicationModel a, Platform p) {
		super(a, p);

		// Kernighan Lin algorithm partitions taskset onto two partitions only.
		// If platform model has more than two cores, discard the remaining
		// ones. Those will not be mapped any tasks.
		if (platform.getCores().size() > 2) {

			for (int i = 2; i < platform.getCores().size(); i++) {
				platform.getCores().remove(i);
			}
		}

		comms = (ArrayList<Communication>) application.getCommunications();

		// System.out.println("cores "+ platform.getCores().size());
		// System.out.println("inittasks "+ application.getTasks().size());

		// Kernighan Lin algorithm partitions a taskset with an even number of
		// tasks.
		// If application model has an odd number of tasks, discard the last one
		// and its communications.
		if (application.getTasks().size() % 2 != 0) {

			Task toremove = application.getTasks().get(
					application.getTasks().size() - 1);
			application.getTasks().remove(toremove);
			application.getCommunications().removeAll(
					getCommunications(toremove));

		}

		// System.out.println("tasks "+ application.getTasks().size());
	}

	@Override
	public boolean performMapping() {

		// step 1 - uses a random mapper to create an initial partition

		RandomMapping rm = new RandomMapping(application, platform);
		rm.performMapping();

		// Makes sure that both partitions have the same number of tasks

		int d = platform.getCore(0).getTasks().size()
				- platform.getCore(1).getTasks().size();
		// System.out.println("difference "+d);
		if (d > 0) {
			for (int i = 0; i < d / 2; i++) {
				Task t = platform.getCore(0).getTasks().get(i);
				remapTask(t, platform.getCore(1));
			}
		} else if (d < 0) {
			for (int i = 0; i < -d / 2; i++) {
				Task t = platform.getCore(1).getTasks().get(i);
				remapTask(t, platform.getCore(0));
			}

		}

		// step 2 - swap vertices for which delta is maximal
		//

		// create an array to store the value of delta for each iteration
		int n = platform.getCore(0).getTasks().size();
		if (platform.getCore(1).getTasks().size() != n) {
			System.out.println("problem!!! " + n + " "
					+ platform.getCore(1).getTasks().size());
		}
		deltas = new double[n];

		xlist = new Task[n];
		ylist = new Task[n];

		// create a hashtable to record whether a given task was locked to a
		// particular core
		// initialize the hashtable so that all tasks are unlocked

		locked = new Hashtable<Task, Boolean>();

		for (int i = 0; i < n; i++) {

			locked.put(platform.getCore(0).getTasks().get(i),
					new Boolean(false));
			locked.put(platform.getCore(1).getTasks().get(i),
					new Boolean(false));

		}

		// perform iterations
		for (int a = 0; a < n; a++) {

			Task opti = null;
			Task optj = null;
			double bestdelta = Double.NEGATIVE_INFINITY;

			// for all pairs of tasks where taski is in core 0 and taskj is in
			// core 1
			for (int i = 0; i < n; i++) {

				for (int j = 0; j < n; j++) {
					Task taski = platform.getCore(0).getTasks().get(i);
					Task taskj = platform.getCore(1).getTasks().get(j);

					// check whether both are still unlocked
					if (!locked.get(taski).booleanValue()
							& !locked.get(taskj).booleanValue()) {

						// check whether the delta of taski and taskj is the
						// highest of all unlocked pairs
						double ijdelta = getDelta(taski, taskj);
						if (ijdelta > bestdelta) {

							bestdelta = ijdelta;
							opti = taski;
							optj = taskj;
						}
					}
				}
			}

			locked.put(opti, new Boolean(true));
			locked.put(optj, new Boolean(true));

			xlist[a] = opti;
			ylist[a] = optj;
			swapTasks(opti, optj);
			deltas[a] = bestdelta;

			// System.out.println("lock "+ opti +" "+optj );
			// System.out.println("delta "+ bestdelta );

		}

		// step 3 - find the portion of deltas for which the sum of all elements
		// is maximum
		double deltamax = 0;
		double sumdelta = 0;
		int index = -1;

		for (int a = 0; a < n; a++) {

			sumdelta = sumdelta + deltas[a];
			if (sumdelta > deltamax) {
				deltamax = sumdelta;
				index = a;

			}
		}

		System.out.println("deltamax " + deltamax);
		System.out.println("index " + index);

		// return all tasks which are not in the chosen portion of deltas to
		// their original cores

		for (int a = index + 1; a < n; a++) {

			swapTasks(xlist[a], ylist[a]);

		}

		return true;

	}

	public double getDelta(Task a, Task b) {

		// finds all communications between both tasks
		ArrayList<Communication> comm = getCommunications(a);
		comm.retainAll(getCommunications(b));

		double unchanged = 0;

		// adds costs of communications between both tasks
		for (int i = 0; i < comm.size(); i++) {

			unchanged = unchanged + comm.get(i).getVolume();

		}

		// calculate delta according to algorithm
		double delta = getDifference(a) + getDifference(b) - 2 * unchanged;

		return delta;

	}

	public double getDifference(Task a) {

		return getExternalCost(a) - getInternalCost(a);
	}

	public double getInternalCost(Task a) {

		return getCost(a, true);
	}

	public double getExternalCost(Task a) {

		return getCost(a, false);
	}

	private double getCost(Task a, boolean internal) {

		double internalCost = 0;
		double externalCost = 0;

		for (int i = 0; i < comms.size(); i++) {

			ProcessingCore c = null;
			boolean communicates = false;
			// checks whether task a is sender or receiver of each communication
			// if positive, create a reference to the core that hosts the task
			// it communicates with
			if (comms.get(i).getSender().equals(a)) {
				c = comms.get(i).getReceiver().getCore();
				communicates = true;
			} else if (comms.get(i).getReceiver().equals(a)) {
				c = comms.get(i).getSender().getCore();
				communicates = true;

			}

			// if both tasks are in the same core, add communication weight to
			// the internal cost
			// otherwise, add it to the external cost
			if (communicates) {
				if (a.getCore().equals(c)) {
					internalCost = internalCost + comms.get(i).getVolume();
				} else {
					externalCost = externalCost + comms.get(i).getVolume();
				}
			}
		}

		if (internal) {
			return internalCost;
		} else {
			return externalCost;
		}
	}

	private ArrayList<Communication> getCommunications(Task a) {

		ArrayList<Communication> list = new ArrayList<Communication>();

		for (int i = 0; i < comms.size(); i++) {

			// checks whether task a is sender or receiver of each communication
			if (comms.get(i).getSender().equals(a)
					| comms.get(i).getReceiver().equals(a)) {
				list.add(comms.get(i));
			}
		}
		return list;
	}

	// allows the heuristic to be executed as a Java application
	public static void main(String[] args) {

		PointToPointInterconnectFactory fac = new PointToPointInterconnectFactory();
		Interconnect twoConnectedCores = fac.createInterconnect(2);
		Platform twoCores = new Platform(twoConnectedCores);

		// RandomApplicationModel app = new RandomApplicationModel();

		ApplicationModel app = new ApplicationModel();

		/*
		 * 
		 * SDFTask a = new SDFTask(); a.setName("a"); SDFTask b = new SDFTask();
		 * b.setName("b"); SDFTask c = new SDFTask(); c.setName("c"); SDFTask d
		 * = new SDFTask(); d.setName("d"); SDFTask e = new SDFTask();
		 * e.setName("e"); SDFTask f = new SDFTask(); f.setName("f"); SDFTask g
		 * = new SDFTask(); g.setName("g"); SDFTask h = new SDFTask();
		 * h.setName("h"); SDFTask i = new SDFTask(); i.setName("i"); SDFTask j
		 * = new SDFTask(); j.setName("j");
		 * 
		 * app.addTask(a); app.addTask(b); app.addTask(c); app.addTask(d);
		 * app.addTask(e); app.addTask(f); app.addTask(g); app.addTask(h);
		 * app.addTask(i); app.addTask(j);
		 * 
		 * 
		 * SDFCommunication c1 = new SDFCommunication(a,b); SDFCommunication c2
		 * = new SDFCommunication(a,c); SDFCommunication c3 = new
		 * SDFCommunication(b,d); SDFCommunication c4 = new
		 * SDFCommunication(c,d); SDFCommunication c5 = new
		 * SDFCommunication(d,a); SDFCommunication c6 = new
		 * SDFCommunication(c,e); SDFCommunication c7 = new
		 * SDFCommunication(a,f); SDFCommunication c8 = new
		 * SDFCommunication(e,g); SDFCommunication c9 = new
		 * SDFCommunication(f,g); SDFCommunication c10 = new
		 * SDFCommunication(d,h); SDFCommunication c11 = new
		 * SDFCommunication(h,i); SDFCommunication c12 = new
		 * SDFCommunication(g,j); SDFCommunication c13 = new
		 * SDFCommunication(i,j);
		 * 
		 * c1.setVolume(64); c2.setVolume(64); c3.setVolume(8);
		 * c4.setVolume(128); c5.setVolume(16); c6.setVolume(16);
		 * c7.setVolume(16); c8.setVolume(48); c9.setVolume(48);
		 * c10.setVolume(48); c11.setVolume(12); c12.setVolume(48);
		 * c13.setVolume(24);
		 * 
		 * app.addCommunication(c1); app.addCommunication(c2);
		 * app.addCommunication(c3); app.addCommunication(c4);
		 * app.addCommunication(c5); app.addCommunication(c6);
		 * app.addCommunication(c7); app.addCommunication(c8);
		 * app.addCommunication(c9); app.addCommunication(c10);
		 * app.addCommunication(c11); app.addCommunication(c12);
		 * app.addCommunication(c13);
		 */

		SimpleTask a = new SimpleTask("A");
		SimpleTask b = new SimpleTask("B");
		SimpleTask c = new SimpleTask("C");
		SimpleTask d = new SimpleTask("D");
		SimpleTask e = new SimpleTask("E");
		SimpleTask f = new SimpleTask("F");
		SimpleTask g = new SimpleTask("G");
		SimpleTask h = new SimpleTask("H");
		SimpleTask i = new SimpleTask("I");
		SimpleTask j = new SimpleTask("J");
		SimpleTask k = new SimpleTask("K");
		SimpleTask l = new SimpleTask("L");

		SimpleCommunication c1 = new SimpleCommunication(a, c);
		SimpleCommunication c2 = new SimpleCommunication(b, c);
		SimpleCommunication c3 = new SimpleCommunication(a, f);
		SimpleCommunication c4 = new SimpleCommunication(c, d);
		SimpleCommunication c5 = new SimpleCommunication(d, b);
		SimpleCommunication c6 = new SimpleCommunication(f, a);
		SimpleCommunication c7 = new SimpleCommunication(f, g);
		SimpleCommunication c8 = new SimpleCommunication(d, e);
		SimpleCommunication c9 = new SimpleCommunication(d, h);
		SimpleCommunication c10 = new SimpleCommunication(h, i);
		SimpleCommunication c11 = new SimpleCommunication(i, k);
		SimpleCommunication c12 = new SimpleCommunication(j, k);
		SimpleCommunication c13 = new SimpleCommunication(k, l);
		SimpleCommunication c14 = new SimpleCommunication(e, g);
		SimpleCommunication c15 = new SimpleCommunication(g, l);

		c1.setVolume(64);
		c2.setVolume(64);
		c3.setVolume(8);
		c4.setVolume(128);
		c5.setVolume(16);
		c6.setVolume(16);
		c7.setVolume(16);
		c8.setVolume(48);
		c9.setVolume(48);
		c10.setVolume(48);
		c11.setVolume(12);
		c12.setVolume(48);
		c13.setVolume(24);
		c14.setVolume(16);
		c15.setVolume(8);

		app.addCommunication(c1);
		app.addCommunication(c2);
		app.addCommunication(c3);
		app.addCommunication(c4);
		app.addCommunication(c5);
		app.addCommunication(c6);
		app.addCommunication(c7);
		app.addCommunication(c8);
		app.addCommunication(c9);
		app.addCommunication(c10);
		app.addCommunication(c11);
		app.addCommunication(c12);
		app.addCommunication(c13);
		app.addCommunication(c14);
		app.addCommunication(c15);

		KernighanLin test = new KernighanLin(app, twoCores);
		test.performMapping();

		System.out.println("Core 0");
		for (int mi = 0; mi < twoCores.getCore(0).getTasks().size(); mi++) {
			System.out
					.println(twoCores.getCore(0).getTasks().get(mi).getName());
		}

		System.out.println("Core 1");
		for (int mi = 0; mi < twoCores.getCore(1).getTasks().size(); mi++) {
			System.out
					.println(twoCores.getCore(1).getTasks().get(mi).getName());
		}

	}
}
