package lsi.noc.application;

import java.util.ArrayList;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/**
 * @author Steven
 *
 */

public class Task{
	
	public Task(int id, int appid, double Computationtime, int Packetsize) throws IllegalActionException, NameDuplicationException{
        CommunicatesWith = new ArrayList<Task>();
        Id = id;
        applicationid = appid;
        delay = Computationtime;
        packetsize = Packetsize;
    }

	public void addcommunication(Task t) {
		CommunicatesWith.add(t);
	}


	public void begin() throws IllegalActionException, NameDuplicationException {
		
		for(int i=0; i < CommunicatesWith.size(); i++){
			Communication c = new Communication();
			c.setParameters(this, CommunicatesWith.get(i), packetsize, packetsize, delay);
			_mapper.sendMessage(c);
		}
		_mapper.Unmap(this);
	}

	public void addMapper(DynamicMapper mapper) {
		_mapper = mapper;
	}

	int Id;
	int applicationid;
	double delay;
	int packetsize;
	ArrayList<Task> CommunicatesWith;
	DynamicMapper _mapper;
	
}
