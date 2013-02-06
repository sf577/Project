/**
 * 
 */
package lsi.noc.application;

import java.util.ArrayList;

import ptolemy.kernel.CompositeEntity;
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
	
	 public Application(){
	        super();
	        
	    }

	    public Application(NamedObj container, String name)
	    throws IllegalActionException, NameDuplicationException {
	            super(container, name);
	            
	            Tasks = new ArrayList<Task>(10);
	            for(int i = 0; i == 9; i++){
	            	Task t = new Task();
	            	t.applicationid = 1;
	            	t.Id = i;
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
			
			initial.begin();
			
		}

	    Task initial;
		ArrayList<Task> Tasks;
        
}