package lsi.noc.application;

import java.util.Hashtable;
import java.util.List;

import lsi.noc.stats.BasicCommunicationLatencyAnalysis;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.uml.Lifeline;
import ptolemy.vergil.uml.Message;

/**
 * @author Steven Fisher
 * @version 1.0 (York, 22/1/2013)
 * 
 * 
 *          This Mapper maps Lifelines onto producers/consumers in a dynamic
 *          first free fashion.
 * 
 */

@SuppressWarnings("serial")
public class DynamicMapper extends Attribute {

	public DynamicMapper(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

	
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
		Token t = new Token();
		
		sender.sendPacket(t, x, y, messageID_, totalPacketSize, subPacketSize,
				com.PreComptime.getToken(), priority);
		
		messageID_ ++;
		
	}
	/**@Override
	public void sendMessage(Message m, Token t) throws IllegalActionException,
			NameDuplicationException {
		// Getting sending and receiving lifeline due to the message
		Lifeline sendingLifeline = m.getSendEvent().getLifeline();
		Lifeline receivingLifeline = m.getReceiveEvent().getLifeline();

		// checks the receiving lifeline has been mapped
		this.checkMapping(sendingLifeline);
		this.checkMapping(receivingLifeline);

		// Finds out the right producer to send the message and the producer of
		// the receiving node in order to find out the receiver node's address
		// (only producers have the address x and y coordinates, consumers not)
		Producer sender = (Producer) lifelinesProducer_.get(sendingLifeline);
		Producer receiver = (Producer) lifelinesProducer_.get(receivingLifeline);

		// Receiving node's address
		int x = receiver.getAddressX();
		int y = receiver.getAddressY();

		// Packet size is asked from the message (parameter of Message). Size
		// can be set in the GUI, default is 128.
		int totalPacketSize = ((IntToken) m.getTotalPacketSize()).intValue();
		int subPacketSize = ((IntToken) m.getSubPacketSize()).intValue();

		// Receiving message's priority
		int priority = ((IntToken) m.getPriority()).intValue();

		// Asking the producer to send a packet containing the message's token
		// (i.e. what comes to the input port of the behavioural pattern)
		// messageID_ starts from 0 and each message has an unique id
		sender.sendPacket(t, x, y, messageID_, totalPacketSize, subPacketSize,
				m.getPreCompDelay(), priority);

		// put the message with its id to a hashtable, see description of the
		// method below
		updateMessages(messageID_, m);
		messageID_ = messageID_ + 1;
	}
	**/

	/**
	 * checks that the receiving lifeline has been mapped by checking for its
	 * existence in the lifelineproducers_ hash table if the lifeline hasn't
	 * been mapped then perform mapping is called
	 */
	protected void checkMapping(Task newTask) throws IllegalActionException, NameDuplicationException {
		/**
		// if the receiving lifeline has not been mapped then perform the
		// mapping of that lifeline
		boolean needsmapping = true;
		Lifeline[] alreadyMappedLifelines = lifelinesProducer_.keySet().toArray(new Lifeline[0]);
		for (int i=0; i < alreadyMappedLifelines.length; i++){
			Lifeline mappedLifeline = alreadyMappedLifelines[i];
			if (newLifeline.getUMLName().equals(mappedLifeline.getUMLName())) {
				needsmapping = false;
				confirmMapping(newLifeline, mappedLifeline);
			}
		}
		if(needsmapping == true){		
			System.out.println("Mapping Required for "+ newLifeline.getUMLName());
			performMapping_(newLifeline);
		}
		**/
	}

	private void confirmMapping(Lifeline lifeline, Lifeline previousmapping) {
		lifelinesProducer_.put(lifeline, lifelinesProducer_.get(previousmapping));
	}

	/**
	 * Maps a single task as communication with it has been requested.
	 * 
	 * @param newTask is the new lifeline to be mapped
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */

	@SuppressWarnings("unchecked")
	protected void performMapping_(Lifeline newTask)
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
						if(!(lifelinesProducer_.containsValue(p))){
							System.out.println("Map to " + x + "," + y);
							//map
							lifelinesProducer_.put(newTask, p);
							mapped = true;
						}
					}
					
				}
				x++;
			}
			y++;
		
		}
	}	
	

	protected int xdimension = 0;
	protected int ydimension = 0;
	protected int lastx = 0;
	protected int lasty = 0;
	protected List<Producer> producers_;
	protected int messageID_;
	
	protected Hashtable<Task, Producer> TaskProducer_ = new Hashtable<Task, Producer>();
}
