package lsi.noc.kernel.mapping;

import java.util.ArrayList;

import lsi.noc.kernel.ApplicationModel;
import lsi.noc.kernel.Platform;
import lsi.noc.kernel.ProcessingCore;
import lsi.noc.kernel.Task;
import lsi.noc.kernel.GroupableTask;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 14/08/2010) 
 */

public class FirstFit extends Mapping {

	double[] utilization;

	public FirstFit(ApplicationModel a, Platform p) {

		super(a, p);

		int i = platform.getCores().size();
		utilization = new double[i];

	}

	public boolean performMapping() {
		boolean success = true;
		ArrayList tasks = application.getTasks();
		for (int i = 0; i < tasks.size(); i++) {

			if (tasks.get(i) instanceof GroupableTask) {

				success = success && doFirstFit((GroupableTask) tasks.get(i));
			}

		}
		return success;
	}

	private boolean doFirstFit(GroupableTask p) {

		if (p.getGroupStatus() == 2) {
			return true;
		}

		for (int i = 0; i < utilization.length; i++) {

			// task is not part of a group
			if (p.getGroupStatus() == 0 && utilization[i] + p.getCost() <= 1) {
				// assign task to the first processor that can accept it
				utilization[i] += p.getCost();
				mapTaskToCore(p, platform.getCore(i));
				return true;
			}

			// task is a group head
			else if (p.getGroupStatus() == 1
					&& utilization[i] + p.getGroupCost() <= 1) {
				ProcessingCore pc = platform.getCore(i);
				mapTaskToCore(p, pc);

				for (int m = 0; m < p.getGroupSize(); m++) {
					p.getFromGroup(m).setCore(pc);
					utilization[i] += p.getFromGroup(m).getCost();
				}
				return true;
			}

		}

		return false;

	}

}
