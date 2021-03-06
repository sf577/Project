//Copyright (c) 2009 Luciano Ost
package lsi.noc.application;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class DependentActor extends TypedCompositeActor {

	public Parameter actor_name;

	public DependentActor(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		setClassName("lsi.noc.application.DepedenceActor");

		actor_name = new StringParameter(this, "actor_name");
	}

	/**
	 * Method returns the Self-Firing Actor Name
	 * 
	 * @return
	 * @throws IllegalActionException
	 */
	public String getActorName() throws IllegalActionException {

		return ((StringToken) actor_name.getToken()).stringValue();

	}

}
