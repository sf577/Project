package lsi.noc.kernel;

import java.util.Random;

import lsi.noc.kernel.priority.PriorityTask;
import lsi.noc.kernel.priority.PriorityTrafficFlow;

public class RandomApplicationModel extends ApplicationModel {

	Random rnd;
	public double lowerPeriod = 0.0001;
	public double upperPeriod = 0.2;

	public RandomApplicationModel() {

		super();
		rnd = new Random();
		createApplication();

	}

	public RandomApplicationModel(long seed) {

		super();
		rnd = new Random(seed);
		createApplication();

	}

	public void createApplication() {

		int ntasks = rnd.nextInt(100);
		int ncomms = rnd.nextInt(100);

		for (int i = 0; i < ntasks; i++) {
			// period between lowerPeriod and upperPeriod
			double period = lowerPeriod + rnd.nextDouble()
					* (upperPeriod - lowerPeriod);

			// comptime as a percentage of the period
			// average utilisation 50%
			double comptime = rnd.nextDouble() * period;

			PriorityTask task = new PriorityTask(i, comptime);
			task.setName("task " + i);
			task.setPeriod(period);

			// no jitter
			task.setReleaseJitter(0);
			this.addTask(task);
		}

		for (int i = 0; i < ncomms; i++) {

			Task source = this.getTasks().get(rnd.nextInt(ntasks));
			Task dest;
			do {
				dest = this.getTasks().get(rnd.nextInt(ntasks));
			} while (source == dest);
			WeightedCommunication comm = new WeightedCommunication(source, dest);
			comm.setWeight(rnd.nextDouble() * 100);
			// PriorityTrafficFlow comm = new
			// PriorityTrafficFlow((PriorityTask)source, (PriorityTask)dest);
			this.addCommunication(comm);
		}

	}

}
