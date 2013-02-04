package lsi.noc.kernel.priority;

import java.util.ArrayList;

public abstract class PriorityMinimizer {

	protected ArrayList<PriorityTrafficFlow> flows;

	public PriorityMinimizer(ArrayList<PriorityTrafficFlow> f) {

		flows = f;

	}

}
