/* A SDirector governs the execution of an UML Sequence Diagram 
 * implemented as a CompositeActor.

 */
package ptolemy.domains.uml.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import ptolemy.actor.*;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.uml.MessageOccurrenceSpecification;
import ptolemy.vergil.uml.UMLSeqActor;
import ptolemy.vergil.uml.Lifeline;
import ptolemy.vergil.uml.BehavioralPatternPort;
import ptolemy.vergil.uml.Message;
import ptolemy.domains.uml.kernel.*;

/**
 * @author Leandro Soares Indrusiak
 * @version $Id: SDLinDirector.java,v 1.0 2007/04/11 07:19:50
 */
public class SDLinDirector extends SDDirector {
	/**
	 * Construct a director in the default workspace with an empty string as its
	 * name. The director is added to the list of objects in the workspace.
	 * Increment the version number of the workspace.
	 */
	public SDLinDirector() {
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
	public SDLinDirector(CompositeEntity container, String name)
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
	public SDLinDirector(Workspace workspace) {
		super(workspace);

	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	public void createPrecedenceGraph() {

		Nameable container = getContainer();
		if (container instanceof UMLSeqActor) {

			List messages = ((UMLSeqActor) container)
					.attributeList(Message.class);
			_graph = new SDLinearizedPrecedenceGraph(messages);

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

		return _transferInputs(port);

	}

	public boolean transferOutputs(IOPort port) throws IllegalActionException {

		boolean has;
		try {
			has = port.hasTokenInside(0);
		} catch (Exception e) {
			has = false;
		}

		if (port instanceof BehavioralPatternPort && has) {
			// System.out.println("port: "+port);
			Message m = ((BehavioralPatternPort) port).getMessage();
			boolean b = _graph.precedenceSatisfied(m);
			// System.out.println("message: "+m+"  precedence: "+b);
			if (b) {
				_graph.notifyMessageFiring(m);
				return _transferOutputs(port);
			}

		}
		return false;
	}

}
