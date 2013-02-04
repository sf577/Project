package lsi.noc.kernel.priority;

/*
 * @author      Osmar Marchi dos Santos
 * @version 1.0 (York, 06/01/2011)
 */

public abstract class PriorityAssignment {

	protected PriorityApplicationModel application;

	// TODO: So far, we do not have a reference to the platform,
	// since the assignment is global to the application (and not directed to
	// each core)

	public PriorityAssignment(PriorityApplicationModel a) {
		application = a;
	}

	// Perform the assignment to the tasks of the application
	public abstract void performAssignmentTasks();

	// Perform the assignment to the flows of the application
	public abstract void performAssignmentFlows();
}
