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
		lastassignedcluster = 0;
	}
	
	public void sendMessage(Communication com) throws IllegalActionException, NameDuplicationException{
		if (mappingQueue.isEmpty()){
			Task source = com.getSource();
			Task destination = com.getDest();
			
			boolean mappedSource = this.checkMapping(source, null);
			boolean mappedDest = false;
			boolean stop = false;
			if (mappedSource == false){
				stop = true;
			}
			if (stop == false){
				mappedDest = this.checkMapping(destination, source);
				if (mappedDest == false){
					Unmap(source);
					stop = true;
				}
			}
			if (stop == false){
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
			else{
				mappingQueue.offer(com);
			}
		} else {
			mappingQueue.offer(com);
			Communication head = mappingQueue.peek();
			sendQueuedMessage(head);
		}
			
	}
	
	protected void sendQueuedMessage(Communication com) throws IllegalActionException, NameDuplicationException{
			Task source = com.getSource();
			Task destination = com.getDest();
			
			boolean mappedSource = this.checkMapping(source, null);
			boolean mappedDest = false;
			boolean stop = false;
			if (mappedSource == false){
				stop = true;
			}
			if (stop == false){
				mappedDest = this.checkMapping(destination, source);
				if (mappedDest == false){
					Unmap(source);
					stop = true;
				}
			}
			if (stop == false){	
				mappingQueue.remove();
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
		
		int sourcex = 0;
		int sourcey = 0;
		
		if (!(Source == null)){
			Producer SourceP = TaskProducer_.get(Source);
			sourcex = SourceP.getAddressX();
			sourcey = SourceP.getAddressY();
			
		} else {
			switch (lastassignedcluster){
			case 0: sourcex = 0;
					sourcey = ydimension;
					lastassignedcluster = 1;
					break;
			case 1: sourcex = xdimension;
					sourcey = ydimension;
					lastassignedcluster = 2;
					break;
			case 2: sourcex = xdimension;
					sourcey = 0;
					lastassignedcluster = 3;
					break;
			case 3: sourcex = 0;
					sourcey = 0;
					lastassignedcluster = 0;
					break;
			}
		}
		
		boolean mapped = false;
		//get the list of possible producers
		producers_ = getproducers_();
		int amountOfProducers = producers_.size();
		for (int hopdistance = 0; hopdistance <= (xdimension + ydimension) && !mapped; hopdistance ++){
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
	protected boolean checkMapping(Task newTask, Task Source) throws IllegalActionException, NameDuplicationException {
		
		// if the receiving task has not been mapped then perform the
		// mapping of that task
			if (!(TaskProducer_.containsKey(newTask))) {
				performMapping(newTask, Source);
			}
			if (!(TaskProducer_.containsKey(newTask))){
				return false;
			} else {
				return true;
			}
		}

	int lastassignedcluster;
}