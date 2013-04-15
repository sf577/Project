package lsi.noc.application;

import java.util.ArrayList;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @author Steven
 *
 */
public class Application{
	
	public Application(int appid, DynamicMapper _mapper) throws IllegalActionException, NameDuplicationException{
		Tasks = new ArrayList<Task>();
		Task t = new Task(0, appid, 1200.0, 64);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    
    	t = new Task(1, appid, 1600.0, 64);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(2, appid, 800.0, 32);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(3, appid, 1200.0, 128);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(4, appid, 2000.0, 64);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(5, appid, 2400.0, 32);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(6, appid, 600.0, 16);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(7, appid, 1200.0, 8);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(8, appid, 1500.0, 64);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(9, appid, 2400.0, 128);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    
    	initial = Tasks.get(0);
    	this.setDependancies();
		System.out.println("Application " + appid+ " Released");
    	initial.begin();    		
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
	    
	    Task initial;
		ArrayList<Task> Tasks;

}