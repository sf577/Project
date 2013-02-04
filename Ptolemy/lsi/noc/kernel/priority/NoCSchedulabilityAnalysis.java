package lsi.noc.kernel.priority;

import java.util.ArrayList;

import lsi.noc.kernel.NoC;

public class NoCSchedulabilityAnalysis {

	PriorityApplicationModel application;
	NoC platform;
	protected int unschedulableTasks;
	protected int unschedulableFlows;
	protected ArrayList<PriorityTask> unschedulableTaskList;
	protected ArrayList<PriorityTrafficFlow> unschedulableFlowList;

	public NoCSchedulabilityAnalysis(PriorityApplicationModel app, NoC p) {

		application = app;
		platform = p;
		unschedulableTasks = 0;
		unschedulableFlows = 0;
		unschedulableTaskList = new ArrayList<PriorityTask>();
		unschedulableFlowList = new ArrayList<PriorityTrafficFlow>();

		// performAnalysis();

	}

	public boolean performAnalysis() {

		try {
			unschedulableFlows = 0;
			unschedulableTasks = 0;
			unschedulableTaskList.clear();
			unschedulableFlowList.clear();

			for (int i = 0; i < application.getTasks().size(); i++) {

				PriorityTask t = application.getTasks().get(i);
				if (!t.isSchedulable(application.getTasks())) {
					unschedulableTasks++;
					unschedulableTaskList.add(t);
				}

			}

			for (int i = 0; i < application.getFlows().size(); i++) {

				PriorityTrafficFlow f = application.getFlows().get(i);
				PriorityTask t = f.getSourceTask();
				f.setReleaseJitter(t.getResponseTime(t
						.getInterferenceSet(application.getTasks())));

				if (f.getWorstCaseLatency(platform.getHopDelay(),
						platform.getRoutingDelay(), application.getFlows()) > f
						.getPeriod()) {
					unschedulableFlows++;
					unschedulableFlowList.add(f);
				}

			}

		} catch (Exception e) {
			System.out
					.println("Schedulability analysis failed: application was not properly mapped or priorities were not properly assigned.");
			System.out.println(e);
			return false;
		}

		return true;
	}

	public boolean isSchedulable() {

		if (unschedulableTasks == 0 && unschedulableFlows == 0) {
			return true;
		} else
			return false;

	}

	public boolean isComputationSchedulable() {

		if (unschedulableTasks == 0) {
			return true;
		} else
			return false;

	}

	public boolean isCommunicationSchedulable() {

		if (unschedulableFlows == 0) {
			return true;
		} else
			return false;

	}

	public int getUnschedulableTaskCount() {
		return unschedulableTasks;
	}

	public int getUnschedulableCommunicationCount() {
		return unschedulableFlows;
	}

	public int getUnschedulableFlowCount() {
		return unschedulableFlows;
	}

	/**
	 * @return the unschedulableTaskList
	 */
	public ArrayList<PriorityTask> getUnschedulableTaskList() {
		return unschedulableTaskList;
	}

	/**
	 * @return the unschedulableFlowList
	 */
	public ArrayList<PriorityTrafficFlow> getUnschedulableFlowList() {
		return unschedulableFlowList;
	}

}
