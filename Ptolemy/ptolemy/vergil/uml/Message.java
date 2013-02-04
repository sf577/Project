package ptolemy.vergil.uml;

import java.util.Iterator;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

/**
 * This class is an implementation of the class Message specified in the UML
 * Superstructure specification within the PtolemyII environment.
 * 
 * This class is the starting point used in this implementation, this means that
 * the whole underlying hierarchy that is given by the UML specification is
 * simplified here. For example, NamedElement that is the super class of Message
 * is not implemented here.
 * 
 * Messages contain two MessageOccurrenceSpecifications which know the Lifeline
 * where they occur. So, Messages 'connect' two MessageOccurrenceSpecifications.
 * 
 * Because it is not enough to specify that, for example, actor A sends data to
 * actor B, another construct from the UML specification is added here: the
 * Connector. One can specify the ports here, that are involved in the
 * communication action (not yet implemented *g*)
 * 
 * Messages can be of different sort: asynchronous calls, asynchronous signals
 * and synchronous calls. However, the sort is specified at the creation of a
 * Message and can't be changed after that. If you want to use an asynchronous
 * call instead of a synchronous call, you have to delete the synchronous
 * Message and create an asynchronous instead. The default value is
 * MessageSort.ASYNCH_CALL.
 * 
 * @author Andreas Thuy, Leandro Soares Indrusiak, Sanna Maatta
 * 
 *         last review: 07.04.09
 */
public class Message extends UMLParameter implements Comparable, ChangeListener {

	public Parameter preCompTime;
	public Parameter totalPacketSize;
	public Parameter subPacketSize;
	public Parameter priority;

	// result is based on the SendEvents of the Messages
	public int compareTo(Object msg) {
		if (msg instanceof Message) {
			Message otherMsg = (Message) msg;
			if (this.getSendEvent().compareTo(otherMsg.getSendEvent()) == -1) {
				return -1;
			} else if (this.getSendEvent().compareTo(otherMsg.getSendEvent()) == 1) {
				return 1;
			} else if (this.getSendEvent().compareTo(otherMsg.getSendEvent()) == 0) {
				return 0;
			}
		}
		return Integer.MAX_VALUE;
	}

	// the kind of message, by default this is is set to be an asynch_call
	private Parameter msgSort = new Parameter(this, "msgSort");

	// MsgOccSpec describing the ReceiveEvent
	private MessageOccurrenceSpecification receiveEvent = new MessageOccurrenceSpecification(
			this, "receiveEvent");

	// MsgOccSpec describing the SendEvent
	private MessageOccurrenceSpecification sendEvent = new MessageOccurrenceSpecification(
			this, "sendEvent");

	// default initialization here, for example after loading a model
	public Message(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		// the type of a Message parameter is String
		// information is only stored to recover the model that was created
		// in an earlier session => Director won't complain about unknown
		// names in the model
		// !!! But there is actually no expression set for a Message...
		// !!! this is more to keep consistent with Lifeline and MsgOccSpec!
		this.setStringMode(true);

		// that is not so cool as there is no real event used here,
		// see also MsgOccSpec and according events
		this.sendEvent.setEvent(new SendEvent());
		this.sendEvent.addChangeListener(this);

		// that is not so cool, as there is no real event used here,
		// see also MsgOccSpec and according events
		this.receiveEvent.setEvent(new ReceiveEvent());
		this.receiveEvent.addChangeListener(this);

		// The MessageSort is hard-coded to an asynchronous call by default...
		setMessageSort(MessageSort.ASYNCH_CALL);

		// Creating Message's parameters and initialising them
		setParameters();

	}

	// this one is used for the creation by users
	// see also comments in the constructor above!
	public Message(NamedObj container, String name, int messageSort,
			MessageOccurrenceSpecification sendOcc,
			MessageOccurrenceSpecification receiveOcc)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		// the type of a Message parameter is String
		// information is only stored to recover the model that was created
		// in an earlier session => Director won't complain about unknown
		// names in the model
		this.setStringMode(true);

		setMessageSort(messageSort);

		if (sendOcc != null) {
			// bring information from sendOcc to sendEvent
			// setSendEvent(sendOcc);
			this.sendEvent = sendOcc; // TODO this has to be checked...
										// incomplete!
		}
		this.sendEvent.addChangeListener(this);

		if (receiveOcc != null) {
			// bring information from receiveOcc to receiveEvent
			// setReceiveEvent(receiveOcc);
			this.receiveEvent = receiveOcc; // TODO this has to be checked...
											// incomplete!
		}
		this.receiveEvent.addChangeListener(this);

		createPorts();
		// Creating Message's parameters and initialising them
		setParameters();

	}

	public Token getPreCompDelay() {
		// return delay_;
		Token token = null;

		try {

			token = preCompTime.getToken();

		} catch (IllegalActionException iae) {

			iae.printStackTrace();
		}

		return token;

	}

	public Token getTotalPacketSize() {

		Token token = null;

		try {

			token = totalPacketSize.getToken();

		} catch (IllegalActionException iae) {

			iae.printStackTrace();
		}

		return token;

	}

	public Token getSubPacketSize() {

		Token token = null;

		try {

			token = subPacketSize.getToken();

		} catch (IllegalActionException iae) {

			iae.printStackTrace();
		}

		return token;

	}

	public Token getPriority() {

		Token token = null;

		try {

			token = priority.getToken();

		} catch (IllegalActionException iae) {

			iae.printStackTrace();
		}

		return token;

	}

	// Changes attribute values

	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {

		if (attribute == preCompTime) {

			double newPreCompDelay = ((DoubleToken) preCompTime.getToken())
					.doubleValue();
			preCompTime_ = newPreCompDelay;

		} else if (attribute == totalPacketSize) {

			int newTotalPacketSize = ((IntToken) totalPacketSize.getToken())
					.intValue();
			totalPacketSize_ = newTotalPacketSize;

		} else if (attribute == priority) {

			int newPriority = ((IntToken) priority.getToken()).intValue();
			priority_ = newPriority;

		} else if (attribute == subPacketSize) {

			int newSubPacketSize = ((IntToken) subPacketSize.getToken())
					.intValue();
			subPacketSize_ = newSubPacketSize;

		} else {
			super.attributeChanged(attribute);
		}
	}

	// customization of Message done when a MessageConnector is dropped on a
	// LifelineFigure is done here; not cool, will be changed

	public void changeExecuted(ChangeRequest change) {

		if (change.getSource() instanceof MessageOccurrenceSpecification) {
			MessageOccurrenceSpecification occurrence = (MessageOccurrenceSpecification) change
					.getSource();
			Iterator occAttribs = occurrence.attributeList().iterator();
			boolean hasLifeline = false;
			boolean hasLocation = false;
			while (occAttribs.hasNext()) {
				Object attrib = occAttribs.next();
				if (attrib instanceof Lifeline) {
					occurrence.setLifeline((Lifeline) attrib);
					hasLifeline = true;
				} else if (attrib instanceof Location) {
					occurrence.setLocTime((Location) attrib);
					hasLocation = true;
				}
				if (hasLifeline && hasLocation) {
					if (occurrence.getName().equals("sendEvent")) {
						occurrence.setEvent(new SendEvent());

						// occurrence.get.dispatchGraphEvent(new GraphEvent(
						// this, GraphEvent.STRUCTURE_CHANGED,
						// receiverLifeline.getContainer()));
					} else if (occurrence.getName().equals("receiveEvent")) {
						occurrence.setEvent(new ReceiveEvent());
					}
				}
			}
		}
	}

	// we don't react if something is going wrong
	public void changeFailed(ChangeRequest change, Exception exception) {
	}

	/**
	 * @return - one of the MessageSorts defined in the class MessageSort
	 *         depending on the sort of this Message
	 */
	// no further check is applied here... it is expected that only valid
	// values were given to msgSort which is ensured as long as the
	// setMessageSort of this class was used, but as this is an ordinary
	// Parameter, there is no guarantee... maybe it would be good to do that
	public int getMsgSort() {
		IntToken token = null;
		try {
			token = (IntToken) this.msgSort.getToken();
		} catch (IllegalActionException iAE) {
			iAE.printStackTrace();
		}
		if (token != null) {
			return token.intValue();
		}
		// this is the else case
		// there is no MessageSort defined with a negative value!
		// this is a sign that something goes wrong
		return -1;
	}

	/**
	 * @return - ReceiveEvent of this Message
	 */
	public MessageOccurrenceSpecification getReceiveEvent() {
		return this.receiveEvent;
	}

	/**
	 * @return - SendEvent of this Message
	 */
	public MessageOccurrenceSpecification getSendEvent() {
		return this.sendEvent;
	}

	// is only set once! when this object is created... therefore no access
	// from outside!
	// you can't simply make an asynchronous call out of a synchronous call!
	protected void setMessageSort(int messageSort) {
		if (messageSort == MessageSort.ASYNCH_CALL
				|| messageSort == MessageSort.ASYNCH_SIGNAL
				|| messageSort == MessageSort.SYNCH_CALL
				|| messageSort == MessageSort.RETURN) {
			Integer msgSortInt = new Integer(messageSort);
			this.msgSort.setExpression(msgSortInt.toString());
		}
		// condition above must always be fulfilled!!!
		else {
			System.out.println("CHEATER!");
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // private methods ////
	// Handle the creation and destruction of input and output
	// ports associated to sending and receiving end of a message.

	public void createPorts() {
		try {
			ComponentEntity patternholder = (ComponentEntity) getContainer();

			_inputport = new BehavioralPatternPort(patternholder, getName()
					+ "_send", true, false, this);
			_outputport = new BehavioralPatternPort(patternholder, getName()
					+ "_receive", false, true, this);

			String inputPortMoMLRep = "<port name=\"" + _inputport.getName()
					+ "\" class=\"ptolemy.vergil.uml.BehavioralPatternPort\">"
					+ "  <property name=\"input\"/>" + "  <property name=\""
					+ this.getName()
					+ "\" class=\"ptolemy.vergil.uml.Message\" />"
					+ "  <property name=\"_showName\" "
					+ "class=\"ptolemy.data.expr.SingletonParameter\""
					+ " value=\"true\"/>" + "  <property name=\"_location\" "
					+ "class=\"ptolemy.kernel.util.Location\""
					+ " value=\"-10.0,-10.0\"/>" + "</port>";

			String outputPortMoMLRep = "<port name=\"" + _outputport.getName()
					+ "\" class=\"ptolemy.vergil.uml.BehavioralPatternPort\">"
					+ "  <property name=\"output\"/>" + "  <property name=\""
					+ this.getName()
					+ "\" class=\"ptolemy.vergil.uml.Message\" />"
					+ "  <property name=\"_showName\" "
					+ "class=\"ptolemy.data.expr.SingletonParameter\""
					+ " value=\"true\"/>" + "  <property name=\"_location\" "
					+ "class=\"ptolemy.kernel.util.Location\""
					+ " value=\"-10.0,-10.0\"/>" + "</port>";

			MoMLChangeRequest request = new MoMLChangeRequest(this,
					patternholder, inputPortMoMLRep);
			patternholder.requestChange(request);
			request = new MoMLChangeRequest(this, patternholder,
					outputPortMoMLRep);
			patternholder.requestChange(request);

			_relation = new TypedIORelation(
					(ptolemy.kernel.CompositeEntity) getContainer(), getName());
			_inputport.link(_relation);
			_outputport.link(_relation);

			request = new MoMLChangeRequest(this, patternholder,
					_relation.exportMoML());
			patternholder.requestChange(request);

			this.addChangeListener(_inputport);
			this.addChangeListener(_outputport);
		} catch (NameDuplicationException nde) {

		} catch (IllegalActionException iae) {
			System.out.println("Illegal Action:" + iae.toString());

		}

	}

	private void setParameters() throws IllegalActionException,
			NameDuplicationException {

		preCompTime = new Parameter(this, "preCompTime");
		preCompTime.setTypeEquals(BaseType.DOUBLE);
		preCompTime.setExpression("0.0");
		preCompTime_ = ((DoubleToken) preCompTime.getToken()).doubleValue();

		totalPacketSize = new Parameter(this, "totalPacketSize");
		totalPacketSize.setTypeEquals(BaseType.INT);
		totalPacketSize.setExpression("100");
		totalPacketSize_ = ((IntToken) totalPacketSize.getToken()).intValue();

		subPacketSize = new Parameter(this, "subPacketSize");
		subPacketSize.setTypeEquals(BaseType.INT);
		subPacketSize.setExpression("48");
		subPacketSize_ = ((IntToken) subPacketSize.getToken()).intValue();

		priority = new Parameter(this, "priority");
		priority.setTypeEquals(BaseType.INT);
		priority.setExpression("0");
		priority_ = ((IntToken) priority.getToken()).intValue();

	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////
	// Associated input and output ports.

	BehavioralPatternPort _inputport, _outputport;
	TypedIORelation _relation;

	double preCompTime_;
	int totalPacketSize_;
	int subPacketSize_;
	int priority_;
}
