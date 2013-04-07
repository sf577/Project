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
				mapper = (DynamicMapper)getMapper();
				appid = 3;
			}
			
			
			public void initialize() throws IllegalActionException {
				super.initialize();
				fired = 0;
				Time timeToStart = getDirector().getModelTime();
				getDirector().fireAt(this, timeToStart);
			}

			public void fire() throws IllegalActionException{
				if (fired < 33 && mapper.numberofapplications() < 5){
					fired ++;
					try {
						new Application3(appid, mapper);
					} catch (NameDuplicationException e) {
						e.printStackTrace();
					}
					appid = appid + 3;
				}
			}
			
			public boolean postfire() throws IllegalActionException{
				Time timeToStart = getDirector().getModelTime().add(1500.0);
				if (fired != 33){
					getDirector().fireAt(this, timeToStart);
				}
				return true;
			}
			
		    protected Attribute getMapper() throws IllegalActionException,
			NameDuplicationException {

		    	NamedObj container = getContainer();
		    	return container.getAttribute("DynamicMapper");

		    }

		
		DynamicMapper mapper;    
		int fired;
		int appid;
		
	}