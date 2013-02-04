package lsi.noc.kernel.priority;

/**

 * @author Leandro Soares Indrusiak
 * @version 1.0 (Bremen - Frankfurt, 08/06/2012)

 */

import java.util.Vector;

public class SingleChannelProbabilisticAnalysis {

	protected Vector<PriorityTrafficFlow> flows;

	public SingleChannelProbabilisticAnalysis(Vector<PriorityTrafficFlow> f) {

		flows = f;
	}

	public boolean performSimulation() {

		double currentTime = 0;
		double lastActivation = 0;
		Vector<PriorityTrafficFlow> releasedFlows = new Vector<PriorityTrafficFlow>();
		PriorityTrafficFlow active = null;

		while (flows.size() != 0 & releasedFlows.size() != 0) {

			// TODO advance time!!!!!!

			boolean updating = true;
			// registers all flows that have been released at the current time
			while (updating) {
				PriorityTrafficFlow next = getNextRelease(currentTime);
				if (next.getReleaseTime() == currentTime) {
					releasedFlows.add(next);
					flows.remove(next);
				} else {
					// updates active flow if needed
					PriorityTrafficFlow high = getHigherPriorityFlow(releasedFlows);
					if (high != active) {
						// preempts currently active flow
						active.setTransmissionTime(active.getTransmissionTime()
								- (currentTime - lastActivation));
						lastActivation = currentTime;
						active = high;

					}

					// closes the update loop
					updating = false;
				}
			}

		}

		return true;

	}

	private PriorityTrafficFlow getNextRelease(double time) {
		double interval = Double.MAX_VALUE;
		PriorityTrafficFlow chosen = null;

		for (PriorityTrafficFlow flow : flows) {
			if (flow.getReleaseTime() >= time
					& flow.getReleaseTime() - time <= interval) {
				interval = flow.getReleaseTime() - time;
				chosen = flow;
			}

		}
		return chosen;
	}

	private PriorityTrafficFlow getHigherPriorityFlow(
			Vector<PriorityTrafficFlow> flowset) {
		PriorityTrafficFlow chosen = null;

		for (PriorityTrafficFlow flow : flowset) {
			if (chosen == null | flow.getPriority() > chosen.getPriority()) {
				chosen = flow;
			}
		}

		return chosen;
	}

}
