package lsi.noc.kernel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Leandro Soares Indrusiak, Osmar Marchi dos Santos
 * @version 1.01 (York, 01/2/2011)
 * @version 1.0 (York, 28/7/2010)
 * 
 */

public class ApplicationModel {

	protected ArrayList<Task> tasks;
	protected ArrayList<Communication> flows;
	protected Hashtable<Task, Communication> flowsPerTask;
	protected Hashtable<Task, Vector<Task>> dependenciesPerTask;

	public ApplicationModel() {
		flows = new ArrayList<Communication>();
		tasks = new ArrayList<Task>();

		flowsPerTask = new Hashtable<Task, Communication>();
		dependenciesPerTask = new Hashtable<Task, Vector<Task>>();
	}

	/**
	 * Adds a task to the application.
	 * 
	 * @param t
	 *            The task to be added.
	 */
	public void addTask(Task t) {
		tasks.add(t);
		dependenciesPerTask.put(t, new Vector<Task>());
	}

	/**
	 * Adds a communication flow between tasks of the application. If either of
	 * the flow's source or destination are not part of the application, they
	 * are added to it before the flow is added.
	 * 
	 * @param f
	 *            The communication flow to be added.
	 */

	public void addCommunication(Communication f) {

		Task s = f.getSender();
		Task d = f.getReceiver();

		if (!tasks.contains(s)) {
			addTask(s);
		}
		if (!tasks.contains(d)) {
			addTask(d);
		}

		flows.add(f);
		flowsPerTask.put(s, f);
	}

	/**
	 * Adds a dependency between tasks of the application.
	 * 
	 * @param dependent
	 *            The dependent task.
	 * @param feeder
	 *            The task that the dependent task depends upon.
	 */

	public void setDependency(Task dependent, Task feeder) {

		if (!tasks.contains(dependent)) {
			addTask(dependent);
		}
		if (!tasks.contains(feeder)) {
			addTask(feeder);
		}

		Vector<Task> v = dependenciesPerTask.get(dependent);
		v.add(feeder);
	}

	/**
	 * Returns the communication flow generated by a given task.
	 * 
	 * @param t
	 *            The task in question.
	 */

	public Communication getCommunication(Task t) {
		return flowsPerTask.get(t);
	}

	/**
	 * Returns a vector with all tasks that task <i>t</i> depends on.
	 * 
	 * @param t
	 *            The dependent task.
	 */

	public Vector<? extends Task> getDependencies(Task t) {
		return dependenciesPerTask.get(t);
	}

	/**
	 * Returns a vector with all tasks that depend on task <i>t</i>.
	 * 
	 * @param t
	 *            The task others depend upon.
	 */

	public Vector<? extends Task> getDependentTasks(Task t) {

		Vector<Task> dependents = new Vector<Task>();

		for (int i = 0; i < tasks.size(); i++) {

			Vector<Task> temp = dependenciesPerTask.get(tasks.get(i));
			for (int j = 0; j < temp.size(); j++) {
				if (temp.get(j).equals(t)) {
					dependents.add(tasks.get(i));
				}
			}

		}
		return dependents;
	}

	/**
	 * Checks whether there is a dependency between two tasks of the
	 * application.
	 * 
	 * @param dependent
	 *            The tasks which possibly depends of the other.
	 * @param feeder
	 *            The task that the dependent task may depend upon.
	 */

	public boolean checkDependency(Task dependent, Task feeder) {
		return getDependencies(dependent).contains(feeder);
	}

	/**
	 * Get the list of communications.
	 * 
	 */

	public ArrayList<? extends Communication> getCommunications() {
		return flows;
	}

	public ArrayList<? extends Task> getTasks() {
		return tasks;
	}
}