package lsi.noc.application;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Hashtable;
import java.util.Vector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.vergil.uml.Message;
import ptolemy.vergil.uml.Lifeline;
import ptolemy.data.Token;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.domains.uml.kernel.SDPrecedenceGraph;
import ptolemy.actor.util.Time;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import lsi.noc.application.uml.directors.MappableSDDirector;
import lsi.noc.application.Producer;

import lsi.noc.stats.CommunicationLatencyAnalysis;

/**
 * Abstract superclass for different mappers (e.g. RandomMapper or
 * StaticMapper).
 * 
 * @author Sanna Maatta, Leandro Soares Indrusiak
 */
@SuppressWarnings({ "unchecked", "serial" })
public abstract class LifelineMapper extends Attribute {

	public LifelineMapper() {
		super();
		// appmodel = new ApplicationModel();

	}

	public LifelineMapper(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		directorsLifelines_ = new Hashtable();
		lifelinesDirector_ = new Hashtable();
		lifelinesProducer_ = new Hashtable();

		messagesIDs_ = new Hashtable();
		IDsMessage_ = new Hashtable();

		directors_ = new Vector();
		messageID_ = 0;
		mappingDone_ = false;

		analysis_ = new Vector();

	}

	public void addCommunicationLatencyAnalysis(CommunicationLatencyAnalysis cla) {
		if (!analysis_.contains(cla))
			analysis_.add(cla);

	}

	/**
	 * Directors are calling this when they want to send a token to the platform
	 * 
	 * @param m
	 * @param t
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */

	public void sendMessage(Message m, Token t) throws IllegalActionException,
			NameDuplicationException {

		// If the function is called for the first time, this does the mapping
		this.checkMapping();

		// Getting sending and receiving lifeline due to the message
		Lifeline sendingLifeline = m.getSendEvent().getLifeline();
		Lifeline receivingLifeline = m.getReceiveEvent().getLifeline();

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

		// put the message with its id to a hastable, see description of the
		// method below
		updateMessages(messageID_, m);
		messageID_ = messageID_ + 1;

	}

	protected void checkMapping() throws IllegalActionException,
			NameDuplicationException {

		if (!mappingDone_) {
			performMapping_();
			mappingDone_ = true;
		}

	}

	/**
	 * Puts the messages with its id into a Hashtable so that the mapper can
	 * notify the right director when a token with this id arrives at the right
	 * consumer
	 * 
	 * @param id
	 * @param m
	 */

	protected void updateMessages(int id, Message m) {

		messagesIDs_.put(new Integer(id), m);
		IDsMessage_.put(m, id);

	}

	/**
	 * Consumers are calling this when they receive a token
	 * 
	 * @param id
	 * @param sendTime
	 * @param time
	 * @throws IllegalActionException
	 */

	public void notifyMessageReceipt(int id, double sendTime, Time time)
			throws IllegalActionException {

		// Getting the message due to the id from the consumer
		Message m = (Message) messagesIDs_.get(new Integer(id));

		// No need to store the message with its id in the hashtable any more
		messagesIDs_.remove(new Integer(id));

		// Getting the lifeline that "receives" the message
		Lifeline messagesReceiver = m.getReceiveEvent().getLifeline();

		// Getting the right director to which the firing of this message
		// belongs
		MappableSDDirector dir = (MappableSDDirector) lifelinesDirector_
				.get(messagesReceiver);
		dir.fireMessage(m);

		// logging the receipt time and latency

		write(m, time.getDoubleValue(), time.subtract(sendTime)
				.getDoubleValue());

		// Writing the messages receive time to a file
		// write(time.getDoubleValue(), false, false, m, id);

		// Writing the message's communication delay caused by the network
		// Time time2 = time.subtract(sendTime);
		// write(time2.getDoubleValue(), true, false, m, id);

	}

	public int getMessageID(Message m) {

		return ((IntToken) IDsMessage_.get(m)).intValue();

	}

	public void notifyMessageReceipt(int id, double sendTime, Time time,
			double compDelay) throws IllegalActionException {

		// Getting the message due to the id from the consumer
		Message m = (Message) messagesIDs_.get(new Integer(id));

		this.notifyMessageReceipt(id, sendTime, time);

		// Writing the computation delay
		write(compDelay, true, true, m, id);

	}

	/**
	 * Gets all producers from the workspace
	 * 
	 * @return
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */

	protected List getproducers_() throws IllegalActionException,
			NameDuplicationException {

		Nameable container = getContainer();

		return ((CompositeEntity) container).entityList(Producer.class);

	}

	/**
	 * All directors are calling this to register themselves so that the mapper
	 * knows about their existence
	 * 
	 * @param dir
	 *            The director that will be registered.
	 * @param lifelines
	 *            A list of all lifelines under the scope of that director.
	 * @param messages
	 *            A list of all messages between the lifelines under the scope
	 *            of that director.
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */

	public void registerUMLSD(MappableSDDirector dir, List lifelines,
			List messages, SDPrecedenceGraph graph)
			throws IllegalActionException, NameDuplicationException {

		// Adding a director to a vector containing all directors in order to
		// perform the mapping
		directors_.add(dir);
		// Adding all lifelines "belonging" to a director in Hashtable in order
		// to perform the mapping
		directorsLifelines_.put(dir, lifelines);

		// Adding a correct director to a lifeline so that a right director can
		// be found when firing a message
		for (int i = 0; i < lifelines.size(); i++) {

			lifelinesDirector_.put(lifelines.get(i), dir);

		}

		// ****************************
		// updating application model
		/*
		 * 
		 * for (int i = 0; i < messages.size(); i++) {
		 * 
		 * //create tasks for each send event
		 * 
		 * Message m = (Message) messages.get(i);
		 * 
		 * Lifeline sl = m.getSendEvent().getLifeline(); SimpleTask s = new
		 * SimpleTask(sl.getUMLName());
		 * 
		 * Lifeline rl = m.getReceiveEvent().getLifeline(); SimpleTask r = new
		 * SimpleTask(rl.getUMLName());
		 * 
		 * //add them to the application model registerTask(s); registerTask(r);
		 * 
		 * //create one communication SimpleCommunication c = new
		 * SimpleCommunication(s,r); c.setName(""+getMessageID(m));
		 * 
		 * //add it to the application model registerCommunication(c);
		 * 
		 * 
		 * }
		 * 
		 * 
		 * // //***************************
		 */

	}

	/**
	 * The mapping algorithm is implemented in the subclass
	 * 
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	protected abstract void performMapping_() throws IllegalActionException,
			NameDuplicationException;

	/**
	 * This methods adds a log entry to all registered
	 * CommunicationLatencyAnalysis instances that are monitoring this mapper.
	 * 
	 * @param m
	 *            The message that was delivered.
	 * @param time
	 *            The delivery time of the message.
	 * @param latency
	 *            The time it took from the message release to its delivery.
	 *            TODO: create proper methods and analysis tools to computation
	 *            delay TODO: better document the method calls
	 */

	private void write(Message m, Double time, Double latency) {

		String info = m.getSendEvent().getLifeline().getUMLName() + "-"
				+ m.getReceiveEvent().getLifeline().getUMLName();

		Lifeline messagesReceiver = m.getReceiveEvent().getLifeline();
		MappableSDDirector dir = (MappableSDDirector) lifelinesDirector_
				.get(messagesReceiver);
		String name = dir.toString();

		for (Enumeration<CommunicationLatencyAnalysis> k = analysis_.elements(); k
				.hasMoreElements();) {

			k.nextElement().notifyReceipt(name + info, time, latency);
		}

	}

	/**
	 * This writes message's send time and communication delay to a file
	 * 
	 * @param time
	 * @param latency
	 * @param m
	 * 
	 * @deprecated An instance of a CommunicationLatencyAnalysis should be used,
	 *             instead of directly writing to a file.
	 * 
	 */
	@Deprecated
	private void write(Double time, boolean delay, boolean computation,
			Message m, int id) {

		if (delay && !computation) {

			String info = m.getSendEvent().getLifeline().getUMLName() + "-"
					+ m.getReceiveEvent().getLifeline().getUMLName();
			// log.log(info, time);
		}

		// if(_debugging){

		Lifeline messagesReceiver = m.getReceiveEvent().getLifeline();

		MappableSDDirector dir = (MappableSDDirector) lifelinesDirector_
				.get(messagesReceiver);
		String name = dir.toString();
		double preCompTime = ((DoubleToken) m.getPreCompDelay()).doubleValue();

		try {

			BufferedWriter out;

			if (delay && !computation) { // Communication latency

				// out = new BufferedWriter(new
				// FileWriter("/usr/oma/smaatta/simulationResults/" + name +
				// "CommunicationLatency.txt", true));
				out = new BufferedWriter(new FileWriter(
						"d:\\code\\simulationResults\\" + name
								+ "CommunicationLatency.txt", true));
				out.write("ID: " + id + "\t" + "COMM LATENCY: " + time + "\n");

			} else if (!delay && !computation) { // Packet receive time

				out = new BufferedWriter(new FileWriter(
						"/usr/oma/smaatta/simulationResults/" + name
								+ "PacketReceive.txt", true));
				// out = new BufferedWriter(new
				// FileWriter("d:\\code\\simulationResults\\" + name +
				// "PacketReceive.txt", true));
				// out.write("ID: " + id + "\t" + "RECEIVE TIME: " + time +
				// "\n");

			} else { // Computation latency

				out = new BufferedWriter(new FileWriter(
						"/usr/oma/smaatta/simulationResults/" + name
								+ "ComputationDelay.txt", true));
				// out = new BufferedWriter(new
				// FileWriter("d:\\code\\simulationResults\\" + name +
				// "ComputationDelay.txt", true));
				// out.write("ID: " + id + "\t" + "PRE COMP TIME: " +
				// preCompTime + "\t" + "ELAPSED TIME: " + time + "\n");
			}

			out.close();

		} catch (IOException ioe) {
		}
		// }
	}

	public void report() {

		for (Enumeration<CommunicationLatencyAnalysis> k = analysis_.elements(); k
				.hasMoreElements();) {

			k.nextElement().report();
		}

	}

	/**********************************************************************
	 * //methods below are used to maintain an internal representation of //the
	 * application model // public void registerTask(Task t){
	 * appmodel.addTask(t); }
	 * 
	 * public void registerCommunication(Communication c){
	 * appmodel.addCommunication(c); }
	 * 
	 * public void addDependency(Task dependent, Task feeder){
	 * appmodel.setDependency(dependent, feeder); }
	 * 
	 * // //
	 **********************************************************************/

	// protected ApplicationModel appmodel;

	protected Vector directors_; // Contains all total or partial order
									// directors of the workspace
	protected Hashtable directorsLifelines_; // Contains all lifelines belonging
												// to one director
	protected Hashtable lifelinesDirector_; // Tells the director of the
											// sequence diagram containing a
											// lifeline

	protected Hashtable <Lifeline, Producer> lifelinesProducer_ = new Hashtable<Lifeline, Producer>(); // Tells where a lifeline is mapped
	private Hashtable messagesIDs_; // Stores the messages with their ID's so
									// that a right director is asked to fire
									// the message
	private Hashtable IDsMessage_; // A message is the key to the ID. Needed
									// when figuring out the execution round in
									// the pipelined applic. exec.
	protected int messageID_; // Each message is assigned an unique ID
	private boolean mappingDone_; // Mapping is done only once

	private Vector<CommunicationLatencyAnalysis> analysis_; // logs and analyses
															// communication
															// latency results

}