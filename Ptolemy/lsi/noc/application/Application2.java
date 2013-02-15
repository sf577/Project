package lsi.noc.application;

import java.util.ArrayList;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class Application2 extends Attribute {

	public Application2(ComponentEntity container, String name)
			    throws IllegalActionException, NameDuplicationException {
			            super(container, name);
			            _mapper = (DynamicMapper)getMapper(); 
				    	Tasks = new ArrayList<Task>();
			            	
				    		Task t = new Task(0, 2, 1200.0, 128);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            
			            	t = new Task(1, 2, 1600.0, 256);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            	
			            	t = new Task(2, 2, 800.0, 128);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            	
			            	t = new Task(3, 2, 1200.0, 512);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            	
			            	t = new Task(4, 2, 2000.0, 128);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            	
			            	t = new Task(5, 2, 2400.0, 1024);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            	
			            	t = new Task(6, 2, 600.0, 512);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            	
			            	t = new Task(7, 2, 1200.0, 128);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            	
			            	t = new Task(8, 2, 1500.0, 512);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            	
			            	t = new Task(9, 2, 2400.0, 1024);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            	
			            	t = new Task(10, 2, 2400.0, 1024);
			            	t.addMapper(_mapper);
			            	Tasks.add(t);
			            
			            initial = Tasks.get(0);
			            this.setDependancies();
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