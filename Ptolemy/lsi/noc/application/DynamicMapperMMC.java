package lsi.noc.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class DynamicMapperMMC extends DynamicMapperNN {

	public DynamicMapperMMC(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		int i = 0;
		while (i < 12){
			HorizontalEdgeLoads.add(i, 0);
			VerticalEdgeLoads.add(i, 0);
			i++;
		}
	}
	
	/**
	 * Message to be sent between two tasks. If the tasks are not mapped then mapping occurs. 
	 * 
	 * @param com Communication Object that specifies the communication behaviour 
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public void sendMessage(Communication com) throws IllegalActionException, NameDuplicationException{
		if (mappingQueue.isEmpty()){
			Task source = com.getSource();
			Task destination = com.getDest();
			
			boolean mappedSource = this.checkMapping(source, com);
			boolean mappedDest = false;
			boolean stop = false;
			if (mappedSource == false){
				stop = true;
			}
			if (stop == false){
				mappedDest = this.checkMapping(destination, com);
				if (mappedDest == false){
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
		} else {
			mappingQueue.offer(com);
			Communication head = mappingQueue.poll();
			sendQueuedMessage(head);
		}
			
	}
	
	protected void sendQueuedMessage(Communication com) throws IllegalActionException, NameDuplicationException{
			Task source = com.getSource();
			Task destination = com.getDest();
			
			boolean mappedSource = this.checkMapping(source);
			boolean mappedDest = false;
			boolean stop = false;
			if (mappedSource == false){
				stop = true;
			}
			if (stop == false){
				mappedDest = this.checkMapping(destination);
				if (mappedDest == false){
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
			if (!(mappingQueue.isEmpty())){
				Communication head = mappingQueue.poll();
				sendQueuedMessage(head);
			}
	}

	
	/**
	 * checks that the receiving task has been mapped by checking for its
	 * existence in the TaskProducer_ hash table if the task hasn't
	 * been mapped then perform mapping is called
	 */
	protected boolean checkMapping(Task newTask, Communication com) throws IllegalActionException, NameDuplicationException {
		
		// if the receiving task has not been mapped then perform the
		// mapping of that task
			if (!(TaskProducer_.containsKey(newTask))) {
				performMapping(newTask, com);
			}
			if (!(TaskProducer_.containsKey(newTask))){
				return false;
			} else {
				return true;
			}
	}
	
	protected int getCongestion(List<Integer> HEL, List<Integer> VEL){
		int min = Collections.max(HEL);
		int min2 = Collections.max(VEL);
		if( min2 < min){
			return min2; 
		}
		return min;
	}
	
	public List<List<Integer>> calculateXYpath(Producer source, Producer dest) throws IllegalActionException{
		List<Integer> HIndexes = new ArrayList<Integer>();
		List<Integer> VIndexes = new ArrayList<Integer>();
		List<List<Integer>> Indexes = new ArrayList<List<Integer>>();
		int currentx = source.getAddressX();
		int currenty = source.getAddressY();
		int destx = dest.getAddressX();
		int desty = dest.getAddressY();
		while(currentx != destx){
			if (currentx < destx){
				HIndexes.add(currenty*3 + currentx);
				currentx ++;
			} else {
				currentx --;
				HIndexes.add(currenty*3 + currentx);
			}
		}
		while(currenty != desty){
			if (currenty < desty){
				VIndexes.add(currentx*3 + currenty);
				currenty ++;
			} else {
				currenty --;
				VIndexes.add(currentx*3 + currenty);
			}
		}
		Indexes.add(HIndexes);
		Indexes.add(VIndexes);
		return Indexes;
	}
	
	protected void performMapping(Task newTask, Communication com)
			throws IllegalActionException, NameDuplicationException {
		if (com.SourceTask == newTask){
			performMapping(newTask);
			return;
		}
		List<Producer>producers = this.getproducers_();
		int amountOfProducers = producers.size();
		int maxcongestion = 0;
		Producer bestmapping = null;
		for (int i = 0; i < amountOfProducers; i++) {
			Producer p = producers.get(i);
			if (!(TaskProducer_.containsValue(p))){
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
	
	protected void updateEdgeLoads(Communication com, Producer P) throws IllegalActionException{
		List<List<Integer>> indexes = calculateXYpath(TaskProducer_.get(com.SourceTask), P);
		List<Integer> Hindexes = indexes.get(0);
		List<Integer> Vindexes = indexes.get(1);
		for (int i = 0; i < Hindexes.size(); i++) {
			int edge = Hindexes.get(i);
			int edgeweight = HorizontalEdgeLoads.get(edge) + com.TotalPacketSize;
			HorizontalEdgeLoads.set(edge, edgeweight);
		}
		for (int i = 0; i < Vindexes.size(); i++) {
			int edge = Vindexes.get(i);
			int edgeweight = VerticalEdgeLoads.get(edge) + com.TotalPacketSize;
			VerticalEdgeLoads.set(edge, edgeweight);
		}
	}
		
	protected int possibleMappingCost(Communication com, Producer perspectivemapping) throws IllegalActionException{
		List<List<Integer>> indexes = calculateXYpath(TaskProducer_.get(com.SourceTask), perspectivemapping);
		List<Integer> Hindexes = indexes.get(0);
		List<Integer> Vindexes = indexes.get(1);
		List<Integer> newHELs = new ArrayList<Integer>(HorizontalEdgeLoads);
		List<Integer> newVELs = new ArrayList<Integer>(VerticalEdgeLoads);
		for (int i = 0; i < Hindexes.size(); i++) {
			int edge = Hindexes.get(i);
			int edgeweight = newHELs.get(edge) + com.TotalPacketSize;
			newHELs.set(edge, edgeweight);
		}
		for (int i = 0; i < Vindexes.size(); i++) {
			int edge = Vindexes.get(i);
			int edgeweight = newVELs.get(edge);
			edgeweight = edgeweight + com.TotalPacketSize;
			newVELs.set(edge, edgeweight);
		}		
		int congestion = getCongestion(newHELs, newVELs);
		return congestion;
	}
	
	public void notifyMessageReceipt(int id, double sendTime, Time time, double latency)
			throws IllegalActionException, NameDuplicationException {

		// Getting the message due to the id from the consumer
		Communication c = MessagesIds_.get(new Integer(id));
		removeEdgeLoads(c, MessagesSources_.get(c), MessagesDestinations_.get(c));

		// No need to store the message with its id in the hashtable any more
		MessagesIds_.remove(new Integer(id));
		MessagesSources_.remove(c);
		MessagesDestinations_.remove(c);

		// Getting the task that "receives" the message
		Task Source = c.getSource();
		Source.messageRecieved();
		Task destination = c.getDest();
		destination.begin();
		
		// Writing the messages receive time  and latency to a file
		 write(c,time.getDoubleValue(), time.subtract(sendTime).getDoubleValue());

	}
	
	protected void removeEdgeLoads(Communication com, Producer Source, Producer Destination) throws IllegalActionException {
		List<List<Integer>> indexes = calculateXYpath(Source, Destination);
		List<Integer> Hindexes = indexes.get(0);
		List<Integer> Vindexes = indexes.get(1);
		for (int i = 0; i < Hindexes.size(); i++) {
			int edge = Hindexes.get(i);
			int edgeweight = HorizontalEdgeLoads.get(edge) - com.TotalPacketSize;
			HorizontalEdgeLoads.set(edge, edgeweight);
		}
		for (int i = 0; i < Vindexes.size(); i++) {
			int edge = Vindexes.get(i);
			int edgeweight = VerticalEdgeLoads.get(edge) - com.TotalPacketSize;
			VerticalEdgeLoads.set(edge, edgeweight);
		}
	}

	List<Integer> HorizontalEdgeLoads = new ArrayList<Integer>(12);
	List<Integer> VerticalEdgeLoads = new ArrayList<Integer>(12);
	protected Hashtable<Communication, Producer> MessagesSources_ = new Hashtable<Communication, Producer>();
	protected Hashtable<Communication, Producer> MessagesDestinations_ = new Hashtable<Communication, Producer>();
}

