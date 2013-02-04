package lsi.noc.kernel.testbench;

import lsi.noc.kernel.priority.PriorityApplicationModel;
import lsi.noc.kernel.priority.PriorityTask;
import lsi.noc.kernel.priority.PriorityTrafficFlow;

public class SyntheticApplication extends PriorityApplicationModel {

	double period[];
	double comptime[];
	int dest[];
	int payload[];

	public SyntheticApplication() {

		// creates 50 tasks with the following periods and computation times

		double p[] = { 0.028, 0.034, 0.004, 0.014, 0.028, 0.014, 0.004, 0.044,
				0.025, 0.003, 0.012, 0.012, 0.012, 0.020, 0.015, 0.030, 0.044,
				0.022, 0.014, 0.040, 0.032, 0.08, 0.018, 0.016, 0.032, 0.020,
				0.014, 0.024, 0.011, 0.021, 0.004, 0.022, 0.011, 0.022, 0.006,
				0.002, 0.016, 0.026, 0.005, 0.050, 0.033, 0.034, 0.062, 0.034,
				0.034, 0.042, 0.05, 0.015, 0.056, 0.002 };

		double ct[] = { 0.018, 0.012, 0.001, 0.002, 0.012, 0.007, 0.0022, 0.02,
				0.015, 0.001, 0.002, 0.001, 0.002, 0.007, 0.002, 0.010, 0.007,
				0.002, 0.001, 0.012, 0.006, 0.004, 0.007, 0.009, 0.011, 0.011,
				0.004, 0.009, 0.003, 0.004, 0.001, 0.006, 0.002, 0.012, 0.001,
				0.001, 0.006, 0.002, 0.001, 0.012, 0.003, 0.013, 0.012, 0.008,
				0.009, 0.014, 0.01, 0.003, 0.020, 0.001 };

		this.setPeriods(p);
		this.setComputationTimes(ct);

		int dt[] = { 4, 34, 12, 32, 22, 13, 0, 14, 1, 34, 23, 19, 42, 47, 48,
				23, 3, 3, 35, 5, 36, 40, 34, 4, 13, 13, 47, 4, 12, 12, 13, 47,
				18, 13, 22, 23, 22, 23, 1, 4, 4, 3, 5, 34, 23, 11, 12, 3, 4, 32 };
		int pl[] = { 60, 230, 128, 128, 2400, 990, 1022, 140, 210, 132, 243,
				1128, 320, 140, 140, 140, 412, 412, 1220, 560, 64, 64, 64, 64,
				1200, 122, 56, 314, 224, 160, 42, 42, 86, 124, 212, 344, 822,
				822, 120, 400, 144, 224, 56, 64, 64, 64, 64, 64, 64, 64 };

		this.setDestinations(dt);
		this.setPayloads(pl);

		createApplication();

	}

	public SyntheticApplication(int s) {

		if (s == 0) {

			double p[] = { 0.04, 0.022, 0.09, 0.04, 0.04, 0.022, 0.03, 0.03,
					0.1, 0.1, 0.022, 0.022, 0.09, 0.09, 0.1, 0.1, 0.022, 0.022,
					0.09, 0.055, 0.055, 0.055, 0.1, 0.2, 0.1, 0.1 };

			double ct[] = { 0.015, 0.012, 0.044, 0.011, 0.011, 0.003, 0.012,
					0.012, 0.022, 0.023, 0.009, 0.004, 0.034, 0.034, 0.023,
					0.056, 0.002, 0.002, 0.056, 0.033, 0.033, 0.033, 0.005,
					0.055, 0.023, 0.023

			};

			this.setPeriods(p);
			this.setComputationTimes(ct);

			int dt[] = { 10, // K
					8, // I
					5, // F
					0, // A
					9, // J
					14, // O
					3, // D
					19, // T
					20, // U
					24, // Y
					23, // X
					4, // E
					18, // S
					12, // M
					2, // C
					8, // I
					0, // A
					11, // L
					16, // Q
					17, // R
					2, // C
					20, // U
					21, // V
					15, // P
					19, // T
					0 // A
			};
			int pl[] = { 300000, 300000, 400000, 1000000, 300000, 300000,
					200000, 500000, 1600000, 1100000, 300000, 200000, 400000,
					500000, 500000, 300000, 400000, 700000, 500000, 400000,
					600000, 300000, 5000000, 2000000, 800000, 1600000 };

			this.setDestinations(dt);
			this.setPayloads(pl);

			createApplication();
		}

		else
			new SyntheticApplication();

	}

	public void setPeriods(double[] periods) {

		this.period = periods;

	}

	public void setComputationTimes(double[] times) {

		this.comptime = times;

	}

	public void setDestinations(int[] destinations) {

		this.dest = destinations;
	}

	public void setPayloads(int[] payloads) {

		this.payload = payloads;

	}

	public void createApplication() {

		// creates tasks
		for (int i = 0; i < comptime.length; i++) {

			PriorityTask p = new PriorityTask(1, comptime[i]);
			p.setName("p" + i);
			p.setPeriod(period[i]);
			p.setReleaseJitter(0);
			p.setPriority(i);
			this.addTask(p);
		}

		// creates flows

		for (int i = 0; i < payload.length; i++) {
			PriorityTask source = this.getTasks().get(i);
			PriorityTrafficFlow flow = new PriorityTrafficFlow(source, this
					.getTasks().get(dest[i]));
			flow.setPeriod(source.getPeriod());
			flow.setPriority(source.getPriority());
			flow.setPayload(payload[i]);
			this.addFlow(flow);
		}

	}

}
