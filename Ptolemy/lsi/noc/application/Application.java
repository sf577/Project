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
	            	
		    		Task t = new Task(0, 1, 1200.0, 128);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            
	            	t = new Task(1, 1, 1600.0, 256);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            	
	            	t = new Task(2, 1, 800.0, 128);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            	
	            	t = new Task(3, 1, 1200.0, 512);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            	
	            	t = new Task(4, 1, 2000.0, 128);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            	
	            	t = new Task(5, 1, 2400.0, 1024);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            	
	            	t = new Task(6, 1, 600.0, 512);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            	
	            	t = new Task(7, 1, 1200.0, 128);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            	
	            	t = new Task(8, 1, 1500.0, 512);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            	
	            	t = new Task(9, 1, 2400.0, 1024);
	            	t.addMapper(_mapper);
	            	Tasks.add(t);
	            
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
			Tasks.set(4, t);
			
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