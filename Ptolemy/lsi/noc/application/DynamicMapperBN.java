package lsi.noc.application;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class DynamicMapperBN extends DynamicMapperPL{

	public DynamicMapperBN(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

	public void sendMessage(Communication com) throws IllegalActionException, NameDuplicationException{
		if (mappingQueue.isEmpty()){
			Task source = com.getSource();
			Task destination = com.getDest();
			
			boolean mappedSource = this.checkMapping(com, source, null);
			boolean mappedDest = false;
			boolean stop = false;
			if (mappedSource == false){
				stop = true;
			}
			if (stop == false){
				mappedDest = this.checkMapping(com,destination, source);
				if (mappedDest == false){
					Unmap(source, false);
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
				MessagesSources_.put(com, sender);
				MessagesDestinations_.put(com, receiver);
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
			
			boolean mappedSource = this.checkMapping(com, source, null);
			boolean mappedDest = false;
			boolean stop = false;
			if (mappedSource == false){
				stop = true;
			}
			if (stop == false){
				mappedDest = this.checkMapping(com, destination, source);
				if (mappedDest == false){
					Unmap(source,false);
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
				MessagesSources_.put(com, sender);
				MessagesDestinations_.put(com, receiver);
				messageID_ ++;
			}
	}
	
	protected boolean checkMapping(Communication com, Task newTask, Task sourceTask) throws IllegalActionException, NameDuplicationException {
		// if the receiving task has not been mapped then perform the
		// mapping of that task
			if (!(TaskProducer_.containsKey(newTask))) {
				performMapping(newTask, sourceTask,com);
			}
			if (!(TaskProducer_.containsKey(newTask))){
				return false;
			} else {
				return true;
			}
	}

	protected void performMapping(Task newTask, Task Source, Communication com) throws IllegalActionException, NameDuplicationException {
		List<Producer> possiblemappings = new ArrayList<Producer>();
		int sourcex = 0;
		int sourcey = 0;
		
		if (!(Source == null)){
			Producer SourceP = TaskProducer_.get(Source);
			sourcex = SourceP.getAddressX();
			sourcey = SourceP.getAddressY();
			
		} else {
			performMapping(newTask, Source);
		}
		boolean mapped = false;
		//get the list of possible producers
		producers_ = getproducers_();
		int amountOfProducers = producers_.size();
		for (int hopdistance = 0; hopdistance <= 6 && !mapped; hopdistance ++){	
			for (int i = 0; i < amountOfProducers; i++) {
				Producer p = (Producer) producers_.get(i);
				int px = p.getAddressX();
				int py = p.getAddressY();
				if ((Math.abs(px-sourcex) + Math.abs(py - sourcey)) == hopdistance){
					//check if producer is mapped
					if(!(TaskProducer_.containsValue(p))){
						//add to list of possible neighbours
						possiblemappings.add(p);
					}
				}
			}
			Producer bestmapping = null;
			if (!(possiblemappings.isEmpty())){
				int maxcongestion = 0;
				for (int i = 0; i < possiblemappings.size(); i++) {
					Producer p = possiblemappings.get(i);
					if(maxcongestion == 0){
						maxcongestion = possibleMappingCost(com, p);
						bestmapping = p;
					} else if (possibleMappingCost(com, p) < maxcongestion){
							bestmapping = p;
					}
				}
			}
			if (bestmapping != null){
				updateEdgeLoads(com, bestmapping);
				TaskProducer_.put(newTask, bestmapping);
			}
		}
	}
}
