package lsi.noc.kernel.priority;

import java.util.ArrayList;

import lsi.noc.kernel.ApplicationModel;
import lsi.noc.kernel.Communication;
import lsi.noc.kernel.Platform;
import lsi.noc.kernel.ProcessingCore;
import lsi.noc.kernel.Task;

public class MemoryOptimization {

	public static boolean optimizeApplication(ApplicationModel app,
			Platform platform, long maxMemory) {

		// instantiates a list to store cores which have memory requirements
		// that
		// are larger than the maximum allowed memory size
		ArrayList<ProcessingCore> maxedCores = new ArrayList<ProcessingCore>();

		// goes through all cores of the platform and tests whether
		// their memory requirement is larger than the maximum allowed memory
		// size
		for (int i = 0; i < platform.getCores().size(); i++) {
			long memReq = getMemoryRequirement(app, platform.getCores().get(i));
			if (memReq > maxMemory) {
				maxedCores.add(platform.getCores().get(i));
			}
		}

		// if are there maxed-out cores, choose one of them, solve the problem
		// with that one and recurse
		if (maxedCores.size() > 0) {

			ProcessingCore core = chooseCore(maxedCores, app);
			boolean successful = optimizeCore(core, app, maxMemory);
			if (successful) {
				optimizeApplication(app, platform, maxMemory);
			} else {
				return false;
			}
		}

		// if there are no maxed-out cores, optimization was successful
		else {
			return true;
		}

		return false; // should never be reached

	}

	//
	// chooses one of the cores to be optimized and returns it.
	// default is the first of the list.
	// subclasses may want to choose a different strategy.
	//
	protected static ProcessingCore chooseCore(ArrayList<ProcessingCore> cores,
			ApplicationModel app) {

		return cores.get(0);

	}

	//
	// optimizes the memory usage of the provided core by changing the periods
	// of the
	// incoming flows so that the total memory requirement is less than the
	// specified limit.
	//
	// this implementation will only optimize periodic tasks and flows by
	// increasing their periods and reducing the communication volume of each
	// message.
	// if tasks/flows are not periodic, then do nothing.
	//
	//
	// default implementation chooses the communication with the largest volume
	// and reduces it until memory requirements are not violated.
	//
	// subclasses may want to choose a different strategy.
	//
	// returns true if optimization was successful (i.e. memory requirements of
	// the core are no longer
	// violated by the provided application model), false if no optimization was
	// achieved.
	//
	protected static boolean optimizeCore(ProcessingCore core,
			ApplicationModel app, long maxMemory) {

		ArrayList<Communication> incomingComms = getIncomingCommunications(app,
				core);
		long total = getMemoryRequirement(app, core);
		Communication largest = null;
		long max = 0;
		for (int i = 0; i < incomingComms.size(); i++) {
			Communication ci = incomingComms.get(i);
			if (ci.getVolume() > max) {
				max = ci.getVolume();
				largest = ci;
			}
		}

		long untouched = total - largest.getVolume(); // memory requirements of
														// all comms but the
														// largest

		// if the memory constraint would not be met by optimizing
		// the largest comm, no optimization can be done
		if (untouched > maxMemory) {
			return false;
		}

		long optimizedVolume = maxMemory - untouched;

		// calculate a division factor that can break the comm volume
		// into multiple bursts that fit the available memory in the core
		//
		// e.g. if the maximum allowed memory usage is 2 and the comm volume is
		// 9,
		// the factor should be ceil(9/2) = 5, so that each burst will be 9/5 =
		// 1.8
		// which is < 2 and therefore fulfils the memory requirement
		//
		int divFactor = (int) Math.ceil(largest.getVolume() / optimizedVolume);

		return transformTaskChain(largest, divFactor);

	}

	// convenience method to break one flow into multiple ones
	private static boolean transformTaskChain(Communication comm, int factor) {

		Task tsender = comm.getSender();
		PriorityTask sender = null;
		if (tsender instanceof PriorityTask) {
			sender = (PriorityTask) tsender;
		} else {
			return false;
		} // task is not periodic, no optimisation is possible

		comm.setVolume(comm.getVolume() / factor); // divide the volume by the
													// calculated factor
		sender.setPeriod(sender.getPeriod() * factor); // multiply the period by
														// the calculated factor

		// checks whether there are tasks and communications down the task
		// chain.
		// if there are, apply the factor to them as well.
		//

		return true;

		// TODO: rest of the chain

	}

	// calculates the memory requirements of a core
	// by iterating over all comms that are sent to
	// tasks mapped to that core

	protected static long getMemoryRequirement(ApplicationModel app,
			ProcessingCore core) {

		long memReq = 0;

		ArrayList<Communication> comms = getIncomingCommunications(app, core);
		for (int i = 0; i < comms.size(); i++) {
			memReq = memReq + comms.get(i).getVolume();
		}
		return memReq;

	}

	// returns a list of all communications of an application that are
	// received by a core (i.e. by tasks mapped to that core)

	protected static ArrayList<Communication> getIncomingCommunications(
			ApplicationModel app, ProcessingCore core) {
		ArrayList<Communication> comms = new ArrayList<Communication>();
		for (int i = 0; i < app.getCommunications().size(); i++) {
			Communication flowi = app.getCommunications().get(i);
			if (core.getTasks().contains(flowi.getReceiver())) {
				comms.add(flowi);
			}
		}
		return comms;
	}

}
