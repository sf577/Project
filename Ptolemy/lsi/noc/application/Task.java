/**
 * 
 */
package lsi.noc.application;

import java.util.ArrayList;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @author Steven
 *
 */
@SuppressWarnings("serial")
public class Task extends Attribute {
	
	public Task(){
        super();
        
    }

    public Task(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
            super(container, name);
            
            CommunicatesWith = new ArrayList<Task>(10);
            
    }

	public void addcommunication(Task t) {
		
		CommunicatesWith.add(t);
		
	}


	public void begin() {
		for(int i=0; i < CommunicatesWith.size(); i++){
			Communication c = new Communication();
			c.setParameters(this, CommunicatesWith.get(i), 128, 128, 240);
			DynamicMapper.sendMessage(c);
		}
		
	}	
	
	int Id;
	int applicationid;
	ArrayList<Task> CommunicatesWith;

}
