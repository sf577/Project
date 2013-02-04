package lsi.noc.application;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.domains.uml.kernel.SDPrecedenceGraph;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.uml.Lifeline;
import ptolemy.vergil.uml.Message;
import ptolemy.vergil.uml.MessageSort;
import lsi.noc.application.uml.directors.MappableSDDirector;

public class StaticMapperMIMDReqResp extends StaticMapperMIMD {

	// ApplicationModel appmodel;

	protected Hashtable<MappableSDDirector, SDPrecedenceGraph> directorPrecedenceGraph_;

	public StaticMapperMIMDReqResp(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		directorPrecedenceGraph_ = new Hashtable<MappableSDDirector, SDPrecedenceGraph>();

		// appmodel = new ApplicationModel();

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

		// Adding a local reference to the precedence graph of each director
		directorPrecedenceGraph_.put(dir, graph);

		/* ***************************
		 * //updating application model //
		 * 
		 * 
		 * for (int i = 0; i < messages.size(); i++) {
		 * 
		 * //create tasks for each send event
		 * 
		 * Message m = (Message) messages.get(i);
		 * 
		 * if(m.getMsgSort()==MessageSort.RETURN){break;} //return messages are
		 * added with respective requests
		 * 
		 * Lifeline sl = m.getSendEvent().getLifeline();
		 * 
		 * double compTime =
		 * ((DoubleToken)m.preCompTime.getToken()).doubleValue(); int pri =
		 * ((IntToken)m.priority.getToken()).intValue(); PriorityTask s = new
		 * PriorityTask(pri,compTime); s.setName(sl.getUMLName());
		 * 
		 * Lifeline rl = m.getReceiveEvent().getLifeline(); PriorityTask r = new
		 * PriorityTask(0,0.0); r.setName(rl.getUMLName());
		 * 
		 * //add them to the application model registerTask(s); registerTask(r);
		 * 
		 * //create one communication PriorityTrafficFlow c = new
		 * PriorityTrafficFlow(s,r); int payload =
		 * ((IntToken)m.getTotalPacketSize()).intValue(); c.setPayload(payload);
		 * //add it to the application model registerCommunication(c);
		 * 
		 * 
		 * //add return message and dependency for synchronous message
		 * if(m.getMsgSort()==MessageSort.SYNCH_CALL){
		 * 
		 * 
		 * Iterator it = graph.successors(m).iterator();
		 * 
		 * while(it.hasNext()){ Message re = (Message)it.next();
		 * if(re.getMsgSort()==MessageSort.RETURN){
		 * 
		 * Lifeline slret = re.getSendEvent().getLifeline(); Lifeline rlret =
		 * re.getReceiveEvent().getLifeline();
		 * 
		 * double compTimeret =
		 * ((DoubleToken)re.preCompTime.getToken()).doubleValue(); int priret =
		 * ((IntToken)re.priority.getToken()).intValue(); int payloadret =
		 * ((IntToken)re.getTotalPacketSize()).intValue();
		 * 
		 * PriorityTask sret = new PriorityTask(priret,compTimeret);
		 * sret.setName(slret.getUMLName());
		 * 
		 * PriorityTask rret = new PriorityTask(0,0.0);
		 * rret.setName(rlret.getUMLName());
		 * 
		 * registerTask(sret); registerTask(rret);
		 * 
		 * PriorityTrafficFlow cret = new PriorityTrafficFlow(sret,rret);
		 * cret.setPayload(payloadret);
		 * 
		 * registerCommunication(cret);
		 * 
		 * addDependency(sret,r);
		 * 
		 * 
		 * 
		 * }
		 * 
		 * }
		 * 
		 * }
		 * 
		 * 
		 * 
		 * 
		 * 
		 * }
		 * 
		 * 
		 * // //***************************
		 */

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
		checkMapping();

		boolean isRequest = checkRequestResponseBehavior(m);
		int responseSize = 0;

		if (isRequest) {
			responseSize = getResponseSize(m);
		}

		// Getting sending and receiving lifeline due to the message
		Lifeline sendingLifeline = m.getSendEvent().getLifeline();
		Lifeline receivingLifeline = m.getReceiveEvent().getLifeline();

		// Finds out the right producer to send the message and the producer of
		// the receiving node in order to find out the receiver node's address
		// (only producers have the address x and y coordinates, consumers not)
		Producer sender = (Producer) lifelinesProducer_.get(sendingLifeline);
		Producer receiver = (Producer) lifelinesProducer_
				.get(receivingLifeline);

		// Receiving node's address
		int x = receiver.getAddressX();
		int y = receiver.getAddressY();

		// Packet size is asked from the message (parameter of Message). Size
		// can be set in the GUI, default is 128.
		int totalPacketSize = ((IntToken) m.getTotalPacketSize()).intValue();
		int subPacketSize = ((IntToken) m.getSubPacketSize()).intValue();

		// Message's priority
		int priority = ((IntToken) m.getPriority()).intValue();

		// Asking the producer to send a packet containing the message's token
		// (i.e. what comes to the input port of the behavioural pattern)
		// messageID_ starts from 0 and each message has an unique id

		if (responseSize > 0) {
			sender.sendPacket(t, x, y, messageID_, totalPacketSize,
					subPacketSize, m.getPreCompDelay(), priority, true,
					responseSize);
		} else {
			sender.sendPacket(t, x, y, messageID_, totalPacketSize,
					subPacketSize, m.getPreCompDelay(), priority);
		}

		// put the message with its id to a hastable, see description of the
		// method below
		updateMessages(messageID_, m);
		messageID_ = messageID_ + 1;

	}

	public boolean checkRequestResponseBehavior(Message m) {

		if (m.getMsgSort() == MessageSort.SYNCH_CALL) {
			return true;
		}

		return false;
	}

	public int getResponseSize(Message m) {

		if (checkRequestResponseBehavior(m)) {

			MappableSDDirector dir = (MappableSDDirector) lifelinesDirector_
					.get(m.getSendEvent().getLifeline());

			SDPrecedenceGraph graph = directorPrecedenceGraph_.get(dir);
			Iterator it = graph.successors(m).iterator();

			while (it.hasNext()) {
				Message re = (Message) it.next();
				if (re.getMsgSort() == MessageSort.RETURN) {

					return ((IntToken) re.getTotalPacketSize()).intValue();
				}
			}
		}
		return 0;

	}

	/*
	 * public void registerTask(PriorityTask t){ appmodel.addTask(t); }
	 * 
	 * public void registerCommunication(PriorityTrafficFlow f){
	 * appmodel.addFlow(f);
	 * 
	 * }
	 * 
	 * public void addDependency(PriorityTask dependent, PriorityTask feeder){
	 * 
	 * appmodel.setDependency(dependent, feeder);
	 * 
	 * }
	 */

}
