package lsi.noc.application;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class DynamicMapperNN extends DynamicMapper {

	public DynamicMapperNN(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}
	
	public void sendMessage(Communication com) throws IllegalActionException, NameDuplicationException{
		Task source = com.getSource();
		Task destination = com.getDest();
		
		this.checkMapping(source, null);
		this.checkMapping(destination, source);
		
		Producer sender = TaskProducer_.get(source);
		Producer receiver = TaskProducer_.get(destination);
		
		int x = receiver.getAddressX();
		int y = receiver.getAddressY();
		
		int totalPacketSize = com.TotalPacketSize;
		int subPacketSize = com.SubPacketSize;
		
		int priority = 1;
		Token t = new IntToken();
		
		Token delay = new DoubleToken(com.PreComptime);
		
		sender.sendPacket(t, x, y, messageID_, totalPacketSize, subPacketSize,
				delay, priority);
		
		MessagesIds_.put(messageID_, com);
		messageID_ ++;
		
	}
	
	/**
	 * Maps a single task as communication with it has been requested.
	 * 
	 * @param newTask is the new Task to be mapped
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	protected void performMapping(Task newTask, Task Source)
			throws IllegalActionException, NameDuplicationException {
		
		int sourcex;
		int sourcey;
		
		if (!(Source == null)){
			Producer SourceP = TaskProducer_.get(Source);
			sourcex = SourceP.getAddressX();
			sourcey = SourceP.getAddressY();
			
		} else {
			sourcex = newTask.applicationid;
			sourcey = newTask.applicationid;
		}
		
		boolean mapped = false;
		//get the list of possible producers
		producers_ = getproducers_();
		int amountOfProducers = producers_.size();
		while (!mapped){
			for (int hopdistance = 1; hopdistance <= 6 && !mapped; hopdistance ++)	
				for (int i = 0; i < amountOfProducers && !mapped; i++) {
					Producer p = (Producer) producers_.get(i);
					int px = p.getAddressX();
					int py = p.getAddressY();
					if ((Math.abs(px-sourcex) + Math.abs(py - sourcey)) <= hopdistance){
						//check if producer is mapped							
						if(!(TaskProducer_.containsValue(p))){
							//System.out.println("Map Task "+ newTask.applicationid +","+ newTask.Id +" to " + px + "," + py + " : Hopdistance = "+ Math.abs(((px-sourcex) + (py - sourcey))));
							TaskProducer_.put(newTask, p);
							mapped = true;
						}
					}
					
				}
		}
	}	
	
	/**
	 * checks that the receiving task has been mapped by checking for its
	 * existence in the TaskProducer_ hash table if the task hasn't
	 * been mapped then perform mapping is called
	 */
	protected void checkMapping(Task newTask, Task Source) throws IllegalActionException, NameDuplicationException {
		
		// if the receiving task has not been mapped then perform the
		// mapping of that task
			if (!(TaskProducer_.containsKey(newTask))) {
				performMapping(newTask, Source);
			}
		}

}
