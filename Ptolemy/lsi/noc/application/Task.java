package lsi.noc.application;

import java.util.ArrayList;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


/**
 * @author Steven
 *
 */
@SuppressWarnings("serial")
public class Task{
	
	public Task() throws IllegalActionException, NameDuplicationException{
        CommunicatesWith = new ArrayList<Task>();
    }

	public void addcommunication(Task t) {
		CommunicatesWith.add(t);
	}


	public void begin() throws IllegalActionException, NameDuplicationException {
		System.out.println("Task begun");
		for(int i=0; i < CommunicatesWith.size(); i++){
			Communication c = new Communication();
			c.setParameters(this, CommunicatesWith.get(i), 128, 128, 1600.0);
			_mapper.sendMessage(c);
		}
		
	}

	public void addMapper(DynamicMapper mapper) {
		_mapper = mapper;
	}

	int Id;
	int applicationid;
	ArrayList<Task> CommunicatesWith;
	DynamicMapper _mapper;
	
}
