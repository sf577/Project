package lsi.noc.application;

import java.util.Hashtable;
import java.util.List;
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
	}

	/**
	 * Message to be sent between two tasks. If the tasks are not mapped then mapping occurs. 
	 * 
	 * @param com Communication Object that specifies the communication behaviour 
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public void sendMessage(Communication com) throws IllegalActionException, NameDuplicationException{
		Task source = com.getSource();
		Task destination = com.getDest();
		
		this.checkMapping(source);
		this.checkMapping(destination);
		
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
	 * checks that the receiving task has been mapped by checking for its
	 * existence in the TaskProducer_ hash table if the task hasn't
	 * been mapped then perform mapping is called
	 */
	protected void checkMapping(Task newTask) throws IllegalActionException, NameDuplicationException {
		
		// if the receiving task has not been mapped then perform the
		// mapping of that task
			if (!(TaskProducer_.containsKey(newTask))) {
				performMapping_(newTask);
			}
		}

	
	
	/**
	 * Maps a single task as communication with it has been requested.
	 * 
	 * @param newTask is the new Task to be mapped
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	protected void performMapping_(Task newTask)
			throws IllegalActionException, NameDuplicationException {
		
		boolean mapped = false;
		//get the list of possible producers
		producers_ = getproducers_();
		int amountOfProducers = producers_.size();

		// Goes through the list of producers and checks their positions on the
		// mesh to find out the NoC dimensions
		xdimension = 3;
		ydimension = 3;
			
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
							System.out.println("Map Task "+ newTask.applicationid +","+ newTask.Id +" to " + x + "," + y);

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

		// No need to store the message with its id in the hashtable any more
		MessagesIds_.remove(new Integer(id));

		// Getting the task that "receives" the message
		Task destination = c.getDest();
		
		destination.begin();

		// logging the receipt time and latency


		// Writing the messages receive time to a file
		// write(time.getDoubleValue(), false, false, m, id);

		// Writing the message's communication delay caused by the network
		// Time time2 = time.subtract(sendTime);
		// write(time2.getDoubleValue(), true, false, m, id);

	}
	
	@SuppressWarnings("rawtypes")
	protected List getproducers_() throws IllegalActionException, NameDuplicationException {

		Nameable container = getContainer();
		return ((CompositeEntity) container).entityList(Producer.class);

	}
	
	public void Unmap(Task t){
		System.out.println("Remove Task "+ t.applicationid + ", "+ t.Id);
		TaskProducer_.remove(t);
	}
	

	protected int xdimension = 0;
	protected int ydimension = 0;
	protected int lastx = 0;
	protected int lasty = 0;
	@SuppressWarnings("rawtypes")
	protected List producers_;
	protected int messageID_;
	protected Hashtable<Task, Producer> TaskProducer_ = new Hashtable<Task, Producer>();
	protected Hashtable<Integer, Communication> MessagesIds_ = new Hashtable<Integer, Communication>();
}
