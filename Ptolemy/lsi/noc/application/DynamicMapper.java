package lsi.noc.application;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;

/**
 * @author Steven Fisher
 * @version 2.0 (York, 22/1/2013)
 * 
 * 
 * This Mapper maps Lifelines onto producers/consumers in a dynamic
 * first free fashion.
 * 
 */

@SuppressWarnings("serial")
public class DynamicMapper extends Attribute {

	public DynamicMapper(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		filename = "C://Users/Steven/Desktop/Results/" + this.getClassName() + "Latency.csv";
		try {
			output = new FileWriter(filename);
			output.append("Latency,\n");
		} catch (IOException e) {
			e.printStackTrace();
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
			
			boolean mappedSource = this.checkMapping(source);
			boolean mappedDest = false;
			boolean stop = false;
			if (mappedSource == false){
				stop = true;
			}
			if (stop == false){
				mappedDest = this.checkMapping(destination);
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
			
			boolean mappedSource = this.checkMapping(source);
			boolean mappedDest = false;
			boolean stop = false;
			if (mappedSource == false){
				stop = true;
			}
			if (stop == false){
				mappedDest = this.checkMapping(destination);
				if (mappedDest == false){
					Unmap(source, false);
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
	/**
	 * checks that the receiving task has been mapped by checking for its
	 * existence in the TaskProducer_ hash table if the task hasn't
	 * been mapped then perform mapping is called
	 */
	protected boolean checkMapping(Task newTask) throws IllegalActionException, NameDuplicationException {
		
		// if the receiving task has not been mapped then perform the
		// mapping of that task
			if (!(TaskProducer_.containsKey(newTask))) {
				performMapping(newTask);
			}
			if (!(TaskProducer_.containsKey(newTask))){
				return false;
			} else {
				return true;
			}
		}
	
	/**
	 * Maps a single task as communication with it has been requested.
	 * 
	 * @param newTask is the new Task to be mapped
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	protected void performMapping(Task newTask)
			throws IllegalActionException, NameDuplicationException {
		
		boolean mapped = false;
		//get the list of possible producers
		producers_ = getproducers_();
		int amountOfProducers = producers_.size();

		//traverse list in xy format		
		int y = 0;
		while (y <= ydimension && !mapped) {
			int x = 0;
			while (x <= xdimension && !mapped){
				for (int i = 0; i < amountOfProducers; i++) {
					Producer p = (Producer) producers_.get(i);
					int px = p.getAddressX();
					int py = p.getAddressY();
					if (px == x && py == y){
						//check if producer is mapped							
						if(!(TaskProducer_.containsValue(p))){
							TaskProducer_.put(newTask, p);
							mapped = true;
						}
					}
					
				}
				x++;
			}
			y++;
		
		}
	}	
	
	public void notifyMessageReceipt(int id, double sendTime, Time time, double latency)
			throws IllegalActionException, NameDuplicationException {

		// Getting the message due to the id from the consumer
		Communication c = MessagesIds_.get(new Integer(id));
		
		// Writing the messages receive time  and latency to a file
		 write(c,time.getDoubleValue(), time.subtract(sendTime).getDoubleValue());
		
		// No need to store the message with its id in the hashtable any more
		MessagesIds_.remove(new Integer(id));
		MessagesSources_.remove(c);
		MessagesDestinations_.remove(c);

		// Getting the task that "receives" the message
		Task Source = c.getSource();
		Source.messageRecieved();
		Task destination = c.getDest();
		destination.begin();
		


	}
	
	@SuppressWarnings({ "unchecked" })
	protected List<Producer> getproducers_() throws IllegalActionException, NameDuplicationException {
		Nameable container = getContainer();
		return ((CompositeEntity) container).entityList(Producer.class);

	}
	
	public void Unmap(Task t, boolean TaskFinished) throws IllegalActionException, NameDuplicationException{
		TaskProducer_.remove(t);
		if (!(mappingQueue.isEmpty()) && TaskFinished){
			Communication head = mappingQueue.peek();
			sendQueuedMessage(head);
		}
	}
	
	protected void write(Communication com, Double time, Double latency) throws IllegalActionException {
		double nocUtilisation = ((double)TaskProducer_.size()/16.0)*100;
		int sourcex = MessagesSources_.get(com).getAddressX();
		int sourcey = MessagesSources_.get(com).getAddressY();
		int destx = MessagesDestinations_.get(com).getAddressX();
		int desty = MessagesDestinations_.get(com).getAddressY();
		int hopdistance = ((Math.abs(destx-sourcex) + Math.abs(desty - sourcey)));
		try {
			output.append(latency + " , " + nocUtilisation + ", " + hopdistance + ",\n");
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int numberofapplications(){
		ArrayList<Task> MappedTasks = new ArrayList<Task>(TaskProducer_.keySet());
		ArrayList<Integer> MappedApplications = new ArrayList<Integer>();
		ArrayList<Communication> QueuedApplications = new ArrayList<Communication>(mappingQueue);
		for (int i = 0; i < MappedTasks.size(); i++){
			int app = MappedTasks.get(i).applicationid;
			if (!(MappedApplications.contains(app))){
				MappedApplications.add(app);
			}
		}
		for (int j = 0; j < QueuedApplications.size(); j++){
			int app = QueuedApplications.get(j).SourceTask.applicationid;
			if (!(MappedApplications.contains(app))){
				MappedApplications.add(app);
			}
		}
		return MappedApplications.size();
		
	}

	String filename;
	FileWriter output;
	protected int xdimension = 3;
	protected int ydimension = 3;
	protected List<Producer> producers_;
	protected int messageID_;
	protected Queue<Communication> mappingQueue = new LinkedList<Communication>();
	protected Hashtable<Task, Producer> TaskProducer_ = new Hashtable<Task, Producer>();
	protected Hashtable<Integer, Communication> MessagesIds_ = new Hashtable<Integer, Communication>();
	protected Hashtable<Communication, Producer> MessagesSources_ = new Hashtable<Communication, Producer>();
	protected Hashtable<Communication, Producer> MessagesDestinations_ = new Hashtable<Communication, Producer>();
}
