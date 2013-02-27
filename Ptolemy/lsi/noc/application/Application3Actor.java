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
		public class Application3Actor extends TypedAtomicActor {

			public Application3Actor() {

			}

			public Application3Actor(Workspace workspace) {
				super(workspace);

			}

			public Application3Actor(CompositeEntity container, String name)
					throws IllegalActionException, NameDuplicationException {
				super(container, name);
				_application = (Application3)getApplication();
			}
			
			
			public void initialize() throws IllegalActionException {
				super.initialize();
				Time timeToStart = getDirector().getModelTime();
				getDirector().fireAt(this, timeToStart);
			}

			public void fire() throws IllegalActionException{
				try {
					_application.begin();
				} catch (NameDuplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			public boolean postfire() throws IllegalActionException{
				Time timeToStart = getDirector().getModelTime().add(6000.0);
				getDirector().fireAt(this, timeToStart);
				return true;
			}
			
			protected Attribute getApplication() throws IllegalActionException,
			NameDuplicationException {

				NamedObj container = getContainer();

				return container.getAttribute("Application3");
			}

		Application3 _application;
		
	}