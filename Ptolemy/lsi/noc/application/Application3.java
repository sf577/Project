package lsi.noc.application;

import java.util.ArrayList;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Application3 extends Application{

	public Application3(int appid, DynamicMapper _mapper, Application3Actor appact) throws IllegalActionException, NameDuplicationException{
		super();
		AppActor = appact;
		Appid = appid;
		Tasks = new ArrayList<Task>();
		Task t = new Task(0, Appid, this, 1024.0, 128);
		t.addMapper(_mapper);
		Tasks.add(t);
		            
		t = new Task(1, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            	
		t = new Task(2, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            	
		t = new Task(3, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            	
		t = new Task(4, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            	
		t = new Task(5, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            	
		t = new Task(6, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            	
		t = new Task(7, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            	
		t = new Task(8, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            	
		t = new Task(9, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            	
		t = new Task(10, Appid, this, 1024.0, 256);
		t.addMapper(_mapper);
		Tasks.add(t);
		            
		initial = Tasks.get(0);
		this.setDependancies();
    	if (Appid % 10 == 0){
    		System.out.println("Application " + Appid + " Released");
    	}
		appid = appid +3;
		initial.begin();
	}
			    
				private void setDependancies() {
					initial.addcommunication(Tasks.get(1));
					Tasks.set(0, initial);
					
					Task t = Tasks.get(1);
					t.addcommunication(Tasks.get(2));
					Tasks.set(1, t);
					
					t = Tasks.get(2);
					t.addcommunication(Tasks.get(3));
					t.addcommunication(Tasks.get(4));
					t.addcommunication(Tasks.get(5));
					t.addcommunication(Tasks.get(6));
					Tasks.set(2, t);
					
					t = Tasks.get(3);
					t.addcommunication(Tasks.get(7));
					Tasks.set(3, t);
					
					t = Tasks.get(4);
					t.addcommunication(Tasks.get(8));
					t.addcommunication(Tasks.get(9));
					Tasks.set(4, t);
					
					t = Tasks.get(6);
					t.addcommunication(Tasks.get(10));
					Tasks.set(6, t);
					
				}
				
				public void TaskFinished(){
			    	tasksfinished ++;
			    	if (tasksfinished == Tasks.size()){
			    		AppActor.ApplicationFinished(Appid);
			    	}
			   }
				    
				    int tasksfinished = 0;
				    Task initial;
				    int Appid;
					ArrayList<Task> Tasks;
					Application3Actor AppActor;
				
		}