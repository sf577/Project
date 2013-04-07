package lsi.noc.application;

import java.util.ArrayList;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Application2{

	public Application2(int appid, DynamicMapper _mapper) throws IllegalActionException, NameDuplicationException{
		Tasks = new ArrayList<Task>();
		Task t = new Task(0, appid, 1200.0, 128);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    
    	t = new Task(1, appid, 1600.0, 256);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(2, appid, 800.0, 128);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(3, appid, 1200.0, 512);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(4, appid, 2000.0, 128);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(5, appid, 2400.0, 1024);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(6, appid, 600.0, 512);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(7, appid, 1200.0, 128);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(8, appid, 1500.0, 512);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(9, appid, 2400.0, 1024);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    	
    	t = new Task(10, appid, 2400.0, 1024);
    	t.addMapper(_mapper);
    	Tasks.add(t);
    
    	initial = Tasks.get(0);
    	this.setDependancies();
    	System.out.println("Application " + appid+ " Released");
    	appid = appid + 3;
    	initial.begin();	            	
    }
			    
			    private void setDependancies() {
					initial.addcommunication(Tasks.get(1));
					initial.addcommunication(Tasks.get(2));
					initial.addcommunication(Tasks.get(3));
					Tasks.set(0, initial);
					
					Task t = Tasks.get(1);
					t.addcommunication(Tasks.get(4));
					t.addcommunication(Tasks.get(5));
					Tasks.set(1, t);
					
					t = Tasks.get(2);
					t.addcommunication(Tasks.get(6));
					Tasks.set(2, t);
					
					t = Tasks.get(3);
					t.addcommunication(Tasks.get(8));
					t.addcommunication(Tasks.get(7));
					Tasks.set(3, t);
					
					t = Tasks.get(4);
					t.addcommunication(Tasks.get(9));
					Tasks.set(4, t);
					
					t = Tasks.get(7);
					t.addcommunication(Tasks.get(10));
					Tasks.set(7, t);
					
				}
			    
			    Task initial;
				ArrayList<Task> Tasks;

		        
		}