package lsi.noc.kernel;

import java.util.ArrayList;
import java.util.Iterator;

import lsi.noc.kernel.priority.PriorityTask;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 25/09/2009) 
 * @version 1.1 (West Drayton, 28/11/2010)
 */

public class ProcessingCore extends Node implements Linkable {

	Link input, output;
	ArrayList<Task> tasks; // from version 1.1, references all tasks mapped to
							// this core

	public ProcessingCore() {

		tasks = new ArrayList<Task>();

	}

	public void setInput(Link l) {

		if (input != null) {
			input.unlink(this);
		} // unlink previous link, if any
		input = l;
		l.link(this);
	}

	public void setOutput(Link l) {

		if (output != null) {
			output.unlink(this);
		} // unlink previous link, if any
		output = l;
		l.link(this);
	}

	public Link getInput() {
		return input;
	}

	public Link getOutput() {
		return output;
	}

	public void mapTask(Task t) {
		tasks.add(t);
	}

	public void unmapTask(Task t) {
		tasks.remove(t);
	}

	public ArrayList<Task> getTasks() {
		return tasks;
	}

	public double getUtilization()

	{
		double u = 0;

		Iterator<Task> it = tasks.iterator();

		while (it.hasNext()) {
			PriorityTask pt = (PriorityTask) it.next();
			u += pt.getUtilization();
		}

		return u;
	}

}
