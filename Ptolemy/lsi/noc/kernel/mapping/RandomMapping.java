package lsi.noc.kernel.mapping;

import java.util.ArrayList;
import java.util.Random;

import lsi.noc.kernel.ApplicationModel;
import lsi.noc.kernel.GroupableTask;
import lsi.noc.kernel.Platform;
import lsi.noc.kernel.ProcessingCore;
import lsi.noc.kernel.Task;
import lsi.noc.kernel.priority.PriorityTask;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 16/11/2010) 
 */

public class RandomMapping extends Mapping {

	Random rnd;

	public RandomMapping(ApplicationModel a, Platform p) {

		super(a, p);
		rnd = new Random();

	}

	public RandomMapping(ApplicationModel a, Platform p, int seed) {

		super(a, p);
		rnd = new Random(seed);

	}

	@Override
	public boolean performMapping() {

		ArrayList<Task> tasks = (ArrayList<Task>) application.getTasks();

		for (int k = 0; k < tasks.size(); k++) {
			Task t = tasks.get(k);
			int pes = rnd.nextInt(platform.getCores().size());
			ProcessingCore pc = platform.getCore(pes);
			mapTaskToCore(t, pc);

		}

		return true;
	}

	public void printTasks(ArrayList<Task> ts) {

		for (int k = 0; k < ts.size(); k++) {

			Task t = ts.get(k);
			System.out.println(t.getName());

		}
	}

}
