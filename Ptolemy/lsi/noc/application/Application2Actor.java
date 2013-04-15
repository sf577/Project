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
		public class Application2Actor extends TypedAtomicActor {

			public Application2Actor() {

			}

			public Application2Actor(Workspace workspace) {
				super(workspace);

			}

			public Application2Actor(CompositeEntity container, String name)
					throws IllegalActionException, NameDuplicationException {
				super(container, name);
				mapper = (DynamicMapper)getMapper();
				appid = 2;
			}
			
			
			public void initialize() throws IllegalActionException {
				super.initialize();
				fired = 0;
				Time timeToStart = getDirector().getModelTime();
				getDirector().fireAt(this, timeToStart);
			}

			public void fire() throws IllegalActionException{
				if (fired < 333 && mapper.numberofapplications() < 3){
					fired ++;
					try {
						new Application2(appid, mapper);
					} catch (NameDuplicationException e) {
						e.printStackTrace();
					}
					appid = appid + 3;
				} 
			}
			
			public boolean postfire() throws IllegalActionException{
				Time timeToStart = getDirector().getModelTime().add(2300.0);
				if (fired < 333 ){
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
