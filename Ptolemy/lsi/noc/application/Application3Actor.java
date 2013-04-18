package lsi.noc.application;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

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
				filename = "C://Users/Steven/Desktop/Results/" + this.getClassName() + " Application Latency.csv";
				try {
					output = new FileWriter(filename);
					output.append("Latency,\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
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
						new Application3(appid, mapper, this);
						ApplicationReleaseTime.put(appid, getDirector().getModelTime());
						appid = appid + 3;
					} catch (NameDuplicationException e) {
						e.printStackTrace();
					}
				} 
			}
			
			public boolean postfire() throws IllegalActionException{
				Time timeToStart = getDirector().getModelTime().add(1000.0*(0.8 + (Math.random()* (1.2-0.8))));
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

		    public void ApplicationFinished(int id) {
				Time applatency = getDirector().getModelTime().subtract(ApplicationReleaseTime.get(id));
				try {
					output.append(applatency + " , \n");
					output.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		String filename;
		FileWriter output;
		DynamicMapper mapper;    
		int fired;
		int appid;
		protected Hashtable<Integer, Time> ApplicationReleaseTime = new Hashtable<Integer, Time>();
	}