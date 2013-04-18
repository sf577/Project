package lsi.noc.application;

import java.util.ArrayList;

/**
 * @author Steven
 *
 */
public abstract class Application{
	public Application(){
		
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
	ApplicationActor AppActor;

	
	
}