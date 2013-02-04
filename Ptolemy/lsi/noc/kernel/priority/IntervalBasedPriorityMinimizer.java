package lsi.noc.kernel.priority;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.JFrame;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.DirectedGraph;

import edu.uci.ics.jung.visualization.BasicVisualizationServer;

public class IntervalBasedPriorityMinimizer extends PriorityMinimizer {

	Hashtable<PriorityTrafficFlow, Point> intervals;

	UndirectedGraph<PriorityTrafficFlow, DefaultEdge> intervalGraph;
	SparseMultigraph<PriorityTrafficFlow, String> jungGraph;

	public IntervalBasedPriorityMinimizer(ArrayList<PriorityTrafficFlow> f) {

		super(f);

		intervals = new Hashtable<PriorityTrafficFlow, Point>();
		calculatePriorityIntervals();

		// debugging
		System.out.println("");
		System.out.println("");

		for (int i = 0; i < flows.size(); i++) {
			System.out.println("Flow " + i + "  .... Max priority: "
					+ intervals.get(flows.get(i)).x + " .... Min priority: "
					+ intervals.get(flows.get(i)).y);
		}
		System.out.println("");
		System.out.println("");

		createIntervalGraph();
		minimizePriorities();

	}

	public void calculatePriorityIntervals() {

		// initialise intervals with maximum priority to the maximum possible
		// priority
		// and minimum priority to the minimum necessary priority of the given
		// flow set

		for (int i = 0; i < flows.size(); i++) {

			intervals.put(flows.get(i), new Point(0, flows.size() - 1));

		}

		for (int i = 0; i < flows.size(); i++) {

			PriorityTrafficFlow flowi = flows.get(i);
			int pi = flowi.getPriority();
			ArrayList<PriorityTrafficFlow> sd = flowi
					.getDirectInterferenceSet(flows);

			// checks whether are there traffics which should have higher
			// priority than flowi
			// if positive, sets flowi's maximum priority to the highest
			// priority which is lower than all of them
			// at the same time, update all of their minimum priorities to be
			// higher than own's current priority
			for (int j = 0; j < sd.size(); j++) {

				PriorityTrafficFlow flowj = sd.get(j);
				int pj = flowj.getPriority();
				if (pj >= intervals.get(flowi).x) {
					intervals.get(flowi).x = pj + 1;
				}

			}
			for (int j = 0; j < sd.size(); j++) {
				PriorityTrafficFlow flowj = sd.get(j);
				int pimax = intervals.get(flowi).x;
				if (intervals.get(flowj).y >= pimax) {
					intervals.get(flowj).y = pimax - 1;
				}
			}
		}

	}

	public void createIntervalGraph() {

		jungGraph = new SparseMultigraph<PriorityTrafficFlow, String>();
		intervalGraph = new SimpleGraph<PriorityTrafficFlow, DefaultEdge>(
				DefaultEdge.class);

		for (int i = 0; i < flows.size(); i++) {
			jungGraph.addVertex(flows.get(i));
			intervalGraph.addVertex(flows.get(i));
		}
		// for all i flows
		for (int i = 0; i < flows.size(); i++) {

			// check for every flow j
			for (int j = 0; j < flows.size(); j++) {

				if (i != j) {
					// whether the interval of j intersects the interval of i
					Point p = getIntervalIntersection(
							intervals.get(flows.get(i)),
							intervals.get(flows.get(j)));
					// if there's an intersection, add an edge to the graph
					// between i and j
					if (p.y >= p.x
							&& !jungGraph
									.isNeighbor(flows.get(i), flows.get(j))) {
						jungGraph.addEdge(i + "-" + j, flows.get(i),
								flows.get(j));
						intervalGraph.addEdge(flows.get(i), flows.get(j));
					}
				}
			}
		}
	}

	public void visualizeIntervalGraph() {

		// The Layout<V, E> is parameterized by the vertex and edge types
		Layout<PriorityTrafficFlow, String> layout = new CircleLayout(jungGraph);
		layout.setSize(new Dimension(900, 900)); // sets the initial size of the
													// space
		// The BasicVisualizationServer<V,E> is parameterized by the edge types
		BasicVisualizationServer<PriorityTrafficFlow, String> vv = new BasicVisualizationServer<PriorityTrafficFlow, String>(
				layout);
		vv.setPreferredSize(new Dimension(950, 950)); // Sets the viewing area
														// size
		JFrame frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);

	}

	public void minimizePriorities() {

		while (intervalGraph.vertexSet().size() > 0) {

			// visualizeIntervalGraph();

			BronKerboschCliqueFinder find = new BronKerboschCliqueFinder(
					intervalGraph);
			Collection<Set> collection = find.getAllMaximalCliques();

			System.out.println("Number of maximal cliques: "
					+ collection.size());

			Collection<Set> collbiggest = find.getBiggestMaximalCliques();
			System.out.println("Number of biggest maximal cliques: "
					+ collbiggest.size());

			System.out.println("Size of the biggest maximal clique: "
					+ collbiggest.iterator().next().size());

			Set clique = collbiggest.iterator().next();
			Object[] cliq = clique.toArray();

			Point intersection = new Point(0, flows.size());

			for (int i = 0; i < cliq.length; i++) {
				PriorityTrafficFlow fl = (PriorityTrafficFlow) cliq[i];
				intersection = getIntervalIntersection(intersection,
						intervals.get(fl));
				intervalGraph.removeVertex(fl);

				// System.out.println("interval "+fl+" ["+intervals.get(fl).x+" - "+intervals.get(fl).y+"]");

			}
			System.out.println("intersection interval of this clique is ["
					+ intersection.x + " - " + intersection.y + "]");
			System.out
					.println("-----------------------------------------------------------------");

		}

	}

	public Point getIntervalIntersection(Point int1, Point int2) {

		int lowendpoint;
		int highendpoint;
		if (int1.x >= int2.x) {
			lowendpoint = int1.x;
		} else {
			lowendpoint = int2.x;
		}
		if (int1.y <= int2.y) {
			highendpoint = int1.y;
		} else {
			highendpoint = int2.y;
		}
		return new Point(lowendpoint, highendpoint);

	}

}
