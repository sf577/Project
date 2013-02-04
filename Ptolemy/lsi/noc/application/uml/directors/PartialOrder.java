package lsi.noc.application.uml.directors;

import java.util.Hashtable;
import java.util.List;

import lsi.noc.application.CombinedFragmentPort;
import lsi.noc.application.LifelineMapper;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.uml.CombinedFragment;
import ptolemy.vergil.uml.UMLSeqActor;
import ptolemy.vergil.uml.Lifeline;
import ptolemy.vergil.uml.BehavioralPatternPort;
import ptolemy.vergil.uml.Message;

/**
 * @author Leandro Soares Indrusiak, Sanna Maatta
 */
@SuppressWarnings("unchecked")
public class PartialOrder extends MappableSDDirector {
	/**
	 * Construct a director in the default workspace with an empty string as its
	 * name. The director is added to the list of objects in the workspace.
	 * Increment the version number of the workspace.
	 */
	public PartialOrder() {
		super();

	}

	/**
	 * Construct a director in the given container with the given name. The
	 * container argument must not be null, or a NullPointerException will be
	 * thrown. If the name argument is null, then the name is set to the empty
	 * string. Increment the version number of the workspace. Create the
	 * timeResolution parameter.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this director.
	 * @exception IllegalActionException
	 *                If the name has a period in it, or the director is not
	 *                compatible with the specified container, or if the time
	 *                resolution parameter is malformed.
	 * @exception NameDuplicationException
	 *                If the container already contains an entity with the
	 *                specified name.
	 */
	public PartialOrder(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

	}

	/**
	 * Construct a director in the workspace with an empty name. The director is
	 * added to the list of objects in the workspace. Increment the version
	 * number of the workspace.
	 * 
	 * @param workspace
	 *            The workspace of this object.
	 */
	public PartialOrder(Workspace workspace) {
		super(workspace);

	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	public void initialize() throws IllegalActionException {
		super.initialize();

		messagesPort_ = new Hashtable();
		mapperExists_ = false;
		fragmentTokens_ = new Hashtable();

		tmp_ = getPattern();

		try {

			mapper_ = (LifelineMapper) getMapper();

			// Using the mapper only if it exists, otherwise the application is
			// executed without a platform
			if (mapper_ != null) {

				mapperExists_ = true;

				if (container_ instanceof UMLSeqActor) {

					// mapper_.registerUMLSD((UMLSeqActor)container_);
					mapper_.registerUMLSD(this, ((UMLSeqActor) container_)
							.attributeList(Lifeline.class),
							((UMLSeqActor) container_)
									.attributeList(Message.class), _graph);

				}
			}

		} catch (NameDuplicationException foo) {
		}

		// If pipelined application execution, this initialises the hashtable
		// that keeps
		// track on the messages execution round.
		if (pipelining_) {
			initRound();
		}
	}

	/**
	 * Creates a new partial order precedence graph.
	 */
	public void createPrecedenceGraph() {

		container_ = getContainer();

		if (container_ instanceof UMLSeqActor) {

			List lifelines = ((UMLSeqActor) container_)
					.attributeList(Lifeline.class);
			List messages = ((UMLSeqActor) container_)
					.attributeList(Message.class);

			List fragments = ((UMLSeqActor) container_)
					.attributeList(CombinedFragment.class);

			_graph = new PartialOrderPrecedenceGraph(lifelines, messages,
					fragments);
		}
	}

	/**
	 * Return a new receiver of a type compatible with this director. In this
	 * base class, this returns an instance of Mailbox.
	 * 
	 * @return A new Mailbox.
	 */
	public Receiver newReceiver() {
		// return new Mailbox();
		// return new QueueReceiver();
		return super.newReceiver();
	}

	/**
	 * Transfer data from an input port of the container to the ports it is
	 * connected to on the inside. The implementation in this base class
	 * transfers at most one token. Derived classes may override this method to
	 * transfer a domain-specific number of tokens.
	 * 
	 * @exception IllegalActionException
	 *                If the port is not an opaque input port.
	 * @param port
	 *            The port to transfer tokens from.
	 * @return True if at least one data token is transferred.
	 */
	public boolean transferInputs(IOPort port) throws IllegalActionException {

		if (port instanceof CombinedFragmentPort) {

			Hashtable tokens = (Hashtable) getFragmentTokens_();

			if (tokens.size() != 0) {

				fragmentTokens_ = tokens;

			}
		}

		return _transferInputs(port);

	}

	public boolean transferOutputs(IOPort port) throws IllegalActionException {

		// If there is no mapper, the application executes without a platform.
		if (!mapperExists_) {

			boolean has;

			try {
				has = port.hasTokenInside(0);
			}

			catch (Exception e) {
				has = false;
			}

			if (port instanceof BehavioralPatternPort && has) {

				Message m = ((BehavioralPatternPort) port).getMessage();

				// Checking if there is tokens in the input ports of the
				// messages inside a combined fragment.
				_graph.setFragmentTokens(fragmentTokens_);

				boolean b = false;

				// The overloaded function (with the round_ variable) enables
				// the pipelined application execution
				if (pipelining_) {

					// Getting the correct execution round for the message.
					round_ = whichRound(m);

					b = _graph.precedenceSatisfied(m, round_);

				} else {

					b = _graph.precedenceSatisfied(m);

				}

				if (b) {

					_graph.notifyMessageFiring(m);

					if (pipelining_) {
						updateRound(m);
					}

					return _transferOutputs(port);
				}
			}

			return false;

			// There is a mapper, so the application is executed on a platform.
		} else {

			boolean has;

			try {

				has = port.hasTokenInside(0);
			}

			catch (Exception e) {
				has = false;
			}

			if (port instanceof BehavioralPatternPort && has) {

				Message m = ((BehavioralPatternPort) port).getMessage();

				// Checking if there are tokens in the input ports of the
				// messages inside a combined fragment.
				_graph.setFragmentTokens(fragmentTokens_);

				boolean b = false;

				// The overloaded function (with the round_ variable) enables
				// the pipelined application execution
				if (pipelining_) {

					// Getting the correct execution round for the message.
					round_ = whichRound(m);
					b = _graph.precedenceSatisfied(m, round_);

				} else {

					b = _graph.precedenceSatisfied(m);
				}

				if (b) {

					Token t = ((BehavioralPatternPort) port).peekToken(0);

					try {
						mapper_.sendMessage(m, t);

					} catch (NameDuplicationException nde) {
					}

					messagesPort_.put(m, port);
				}
			}
		}

		return true;
	}

	// If precedence is correct and the packet has been sent and received,
	// then notify firing and transfer outputs.

	private boolean callTransferOutputs_(Message m)
			throws IllegalActionException {

		_graph.notifyMessageFiring(m);

		IOPort p = (IOPort) messagesPort_.get(m);
		_transferOutputs(p);

		List outputPorts = tmp_.outputPortList();

		for (int i = 0; i < outputPorts.size(); i++) {

			IOPort io = (IOPort) outputPorts.get(i);

			if (io.hasTokenInside(0)) {

				transferOutputs(io);
			}
		}

		return true;
	}

	public void fireMessage(Message m) throws IllegalActionException {

		callTransferOutputs_(m);

		// Updating the hashtable indicating the message's round
		if (pipelining_) {
			updateRound(m);
		}
	}
}
