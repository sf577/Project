package lsi.noc.kernel;

import java.util.ArrayList;

import lsi.noc.kernel.priority.PriorityTask;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 14/08/2010) 
 */

public class GroupableTask extends SimpleTask {

	// 0 - no group
	// 1 - head of a group
	// 2 - part of a group

	protected int groupstatus = 0;

	protected ArrayList<Task> group;

	public GroupableTask(String s) {
		super(s);
		group = new ArrayList<Task>();
		// group.add(this);

	}

	public GroupableTask() {
		super();
		group = new ArrayList<Task>();
		// group.add(this);

	}

	public void setGroupStatus(int groupstatus) {
		this.groupstatus = groupstatus;
	}

	public int getGroupStatus() {
		return groupstatus;
	}

	public void addToGroup(Task t) {

		group.add(t);
	}

	public GroupableTask getFromGroup(int i) {

		return (GroupableTask) group.get(i);

	}

	public int getGroupSize() {

		return group.size();
	}

	private void setGroupCore(ProcessingCore core) {

		if (this.core != null) {
			this.core.unmapTask(this);
		}
		this.core = core;
		if (core != null)
			core.mapTask(this);
	}

	public void setCore(ProcessingCore core) {

		if (groupstatus == 0) {
			this.setGroupCore(core);
		} else if (groupstatus == 1) {
			this.core = core;
			for (int i = 0; i < this.getGroupSize(); i++) {
				this.getFromGroup(i).setGroupCore(core);
			}
		}
		// if groupstatus==2 do nothing - parts of a group are mapped when the
		// group head is mapped

	}

	public double getGroupCost() {
		double util = 0;
		for (int i = 0; i < group.size(); i++) {
			util += group.get(i).getCost();
		}
		return util + getCost(); // group head is not stored at the group vector
	}

}
