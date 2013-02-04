package lsi.noc.kernel;

import java.util.ArrayList;
import java.util.List;

import lsi.noc.kernel.priority.PriorityTrafficFlow;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.1 (York, 10/12/2009) 
 */

public class Link extends Edge {

	protected ArrayList<Linkable> linked;
	protected double linkUtil;
	protected ArrayList<Flow> flows;

	public Link() {

		linked = new ArrayList<Linkable>();
		flows = new ArrayList<Flow>();
	}

	public void link(Linkable a) {
		linked.add(a);
	}

	public void unlink(Linkable a) {
		linked.remove(a);
	}

	public Linkable traverse(Linkable l1) {

		if (linked.contains(l1)) {

			List<Linkable> clo = getLinked();
			clo.remove(l1);
			return clo.get(0);
		}

		return null;

	}

	public List<Linkable> hypertraverse(Linkable l1) {

		List<Linkable> clo = getLinked();
		clo.remove(l1);
		return clo;
	}

	public ArrayList<Linkable> getLinked() {

		return new ArrayList<Linkable>(linked);

	}

	public double getLinkUtilisation() {

		linkUtil = 0;

		for (int i = 0; i < flows.size(); i++) {
			linkUtil += 100 * flows.get(i).getUtilization(0.00000001,
					0.00000002);
		}

		return linkUtil;
	}

	public void addFlow(PriorityTrafficFlow f) {
		flows.add(f);
	}

	public void clearLink() {
		flows.clear();
	}
}
