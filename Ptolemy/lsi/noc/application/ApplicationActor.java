package lsi.noc.application;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

@SuppressWarnings("serial")
public class ApplicationActor extends TypedAtomicActor {

	public ApplicationActor() {

	}

	public ApplicationActor(Workspace workspace) {
		super(workspace);

	}

	public ApplicationActor(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		_application = (Application)getApplication();
	}
	
	
	public void initialize() throws IllegalActionException {
		super.initialize();
		Time timeToStart = getDirector().getModelTime();
		getDirector().fireAt(this, timeToStart);
	}

	public void fire() throws IllegalActionException{
		System.out.println("fired");
		try {
			_application.begin();
		} catch (NameDuplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean postfire() throws IllegalActionException{
		Time timeToStart = getDirector().getModelTime().add(5000.0);
		getDirector().fireAt(this, timeToStart);
		return true;
	}
	
	protected Attribute getApplication() throws IllegalActionException,
	NameDuplicationException {

		NamedObj container = getContainer();

		return container.getAttribute("Application");
	}

Application _application;
}