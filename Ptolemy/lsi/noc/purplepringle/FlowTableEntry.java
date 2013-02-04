package lsi.noc.purplepringle;

import java.util.Enumeration;
import java.util.Vector;
import ptolemy.actor.util.Time;

import lsi.noc.kernel.priority.PriorityTrafficFlow;

public class FlowTableEntry implements Comparable<FlowTableEntry> {

	protected PriorityTrafficFlow flow;
	protected Time releaseTime;
	protected Time remainingTime;
	protected boolean active;
	protected Time lastActivationTime;
	protected Vector<FlowTableEntry> waitsFor;

	public FlowTableEntry(PriorityTrafficFlow flow, Time releaseTime) {
		this.flow = flow;
		this.releaseTime = releaseTime;
		active = false;
		waitsFor = new Vector<FlowTableEntry>();

	}

	public PriorityTrafficFlow getFlow() {
		return flow;
	}

	public void setActive(Time currentTime) {

		active = true;
		lastActivationTime = currentTime;
		System.out.println("activating flow " + flow.getPriority()
				+ " at time " + currentTime);
		for (int i = 0; i < waitsFor.size(); i++) {
			System.out.println("blocking: "
					+ waitsFor.get(i).getFlow().getPriority());
		}

	}

	public void setInactive() {
		active = false;

		System.out.println("deactivating flow " + flow.getPriority());
		for (int i = 0; i < waitsFor.size(); i++) {
			FlowTableEntry temp = waitsFor.get(i);
			System.out.println("waiting: " + temp.getFlow().getPriority() + " "
					+ temp.isActive());
		}

	}

	public boolean isActive() {
		return active;
	}

	public Time getLastActivationTime() {
		return lastActivationTime;
	}

	public Time getReleaseTime() {
		return releaseTime;
	}

	public Time getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(Time remainingTime) {
		this.remainingTime = remainingTime;
	}

	public void addInterferenceSource(FlowTableEntry entry) {
		waitsFor.add(entry);
	}

	public void removeInterferenceSource(FlowTableEntry entry) {
		waitsFor.remove(entry);
	}

	public Enumeration<FlowTableEntry> getInterferenceFlows() {
		return waitsFor.elements();
	}

	@Override
	public int compareTo(FlowTableEntry fte) {
		// higher the number, lower the priority
		if (fte.getFlow().getPriority() < this.getFlow().getPriority()) {
			return -1;
		} else if (fte.getFlow().getPriority() > this.getFlow().getPriority()) {
			return 1;
		} else {
			return 0;
		}

	}

}
