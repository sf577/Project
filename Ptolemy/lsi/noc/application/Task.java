package lsi.noc.application;

import java.util.ArrayList;


/**
 * @author Steven
 *
 */
@SuppressWarnings("serial")
public class Task {
	
	public Task(){
        CommunicatesWith = new ArrayList<Task>();
    }

	public void addcommunication(Task t) {
		CommunicatesWith.add(t);
	}


	public void begin() {
		System.out.println("Task begun");
		for(int i=0; i < CommunicatesWith.size(); i++){
			Communication c = new Communication();
			c.setParameters(this, CommunicatesWith.get(i), 128, 128, 1600.0);
			DynamicMapper.sendMessage(c);
		}
		
	}	
	
	int Id;
	int applicationid;
	ArrayList<Task> CommunicatesWith;

}
