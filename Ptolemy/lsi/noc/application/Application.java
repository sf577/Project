package lsi.noc.application;

import java.util.ArrayList;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * @author Steven
 *
 */
@SuppressWarnings("serial")
public class Application extends Attribute {
	
	    public Application(ComponentEntity container, String name)
	    throws IllegalActionException, NameDuplicationException {
	            super(container, name);
	            _mapper = (DynamicMapper)getMapper(); 
		    	Tasks = new ArrayList<Task>();
	            for(int i = 0; i < 10; i++){
	            	Task t = new Task();
	            	t.applicationid = 1;
	            	t.Id = i;
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            }
	            initial = Tasks.get(0);
	            this.setDependancies();
	    }
	    
	    private void setDependancies() {
			initial.addcommunication(Tasks.get(1));
			initial.addcommunication(Tasks.get(2));
			Tasks.set(0, initial);
			
			Task t = Tasks.get(1);
			t.addcommunication(Tasks.get(3));
			Tasks.set(1, t);
			
			t = Tasks.get(2);
			t.addcommunication(Tasks.get(4));
			t.addcommunication(Tasks.get(5));
			Tasks.set(2, t);
			
			t = Tasks.get(3);
			t.addcommunication(Tasks.get(6));
			t.addcommunication(Tasks.get(7));
			Tasks.set(3, t);
			
			t = Tasks.get(4);
			t.addcommunication(Tasks.get(8));
			Tasks.set(5, t);
			
			t = Tasks.get(5);
			t.addcommunication(Tasks.get(9));
			Tasks.set(5, t);
			
		}
	    
	    public void begin() throws IllegalActionException, NameDuplicationException {
				initial.begin();
			
		}
	    
	    protected Attribute getMapper() throws IllegalActionException,
		NameDuplicationException {

	    	NamedObj container = getContainer();
	    	return container.getAttribute("DynamicMapper");

	    }
	    
	    Task initial;
	    DynamicMapper _mapper;
		ArrayList<Task> Tasks;
        
}