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
        messagesSent = 0;
        begun = false;
    }

	public void addcommunication(Task t) {
		CommunicatesWith.add(t);
	}


	public void begin() throws IllegalActionException, NameDuplicationException {
		if(begun == false){
				begun = true;
				for(int i=0; i < CommunicatesWith.size(); i++){
					Communication c = new Communication();
					c.setParameters(this, CommunicatesWith.get(i), packetsize, packetsize, delay);
					_mapper.sendMessage(c);
					messagesSent ++;
				}
				if (messagesSent == 0){
					_mapper.Unmap(this, true);
					begun = false;
				}
		}
	}
	
	public void messageRecieved() throws IllegalActionException, NameDuplicationException{
		//messagesRecieved ++;
		//if (messagesRecieved == messagesSent){
			_mapper.Unmap(this, true);
		//	begun = false;
		//}
	}

	public void addMapper(DynamicMapper mapper) {
		_mapper = mapper;
	}

	int Id;
	int applicationid;
	double delay;
	int packetsize;
	int messagesSent;
	int messagesRecieved;
	boolean begun;
	ArrayList<Task> CommunicatesWith;
	DynamicMapper _mapper;
	
}
