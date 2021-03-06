// Abstract super class for directors that communicate
//with a mapper (e.g. Total Order and Partial Order)

package lsi.noc.application.uml.directors;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import lsi.noc.application.CombinedFragmentPort;
import lsi.noc.application.LifelineMapper;

import ptolemy.actor.IOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.uml.kernel.SDDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.uml.CombinedFragment;
import ptolemy.vergil.uml.Message;
import ptolemy.vergil.uml.UMLSeqActor;

/**
 * @author Sanna Maatta Added functions for pipelined application execution
 */

public abstract class MappableSDDirector extends SDDirector {
	/**
	 * Construct a director in the default workspace with an empty string as its
	 * name. The director is added to the list of objects in the workspace.
	 * Increment the version number of the workspace.
	 */
	public MappableSDDirector() {
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
	public MappableSDDirector(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		messagesRound_ = new Hashtable();
		round_ = 0;

		pipelining = new Parameter(this, "Pipelined");
		pipelining.setTypeEquals(BaseType.BOOLEAN);
		pipelining.setToken("true");

	}

	/**
	 * Construct a director in the workspace with an empty name. The director is
	 * added to the list of objects in the workspace. Increment the version
	 * number of the workspace.
	 * 
	 * @param workspace
	 *            The workspace of this object.
	 */
	public MappableSDDirector(Workspace workspace) {
		super(workspace);

	}

	public abstract void createPrecedenceGraph();

	public abstract void fireMessage(Message m) throws IllegalActionException;

	/**
	 * Changes attribute values
	 */
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {

		if (attribute == pipelining) {

			boolean newPipelining = ((BooleanToken) pipelining.getToken())
					.booleanValue();
			pipelining_ = newPipelining;

		} else {
			super.attributeChanged(attribute);
		}
	}

	/**
	 * Getting the mapper from the workspace, so that the director can call its
	 * methods
	 * 
	 * @return
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	protected Attribute getMapper() throws IllegalActionException,
			NameDuplicationException {

		NamedObj container = getContainer().getContainer();

		return container.getAttribute("LifelineMapper");

	}

	/**
	 * Getting the pattern from the workspace (i.e. sequence diagram) that the
	 * director executes
	 * 
	 * @return
	 * @throws IllegalActionException
	 */

	protected UMLSeqActor getPattern() throws IllegalActionException {

		Nameable container = getContainer().getContainer();
		List patterns = ((CompositeEntity) container)
				.entityList(UMLSeqActor.class);
		// System.out.println("PATTERNs: " + patterns);
		Iterator it = patterns.iterator();

		while (it.hasNext()) {

			UMLSeqActor seqActor = (UMLSeqActor) it.next();

			List l = seqActor.attributeList(MappableSDDirector.class);

			if ((MappableSDDirector) l.get(0) == this) {
				tmp_ = seqActor;
			}
		}
		return tmp_;
	}

	/**
	 * 
	 * @return
	 * @throws IllegalActionException
	 */

	protected Hashtable getFragmentTokens_() throws IllegalActionException {

		List inputPorts = tmp_.inputPortList();

		Hashtable ht = new Hashtable();

		for (int i = 0; i < inputPorts.size(); i++) {

			IOPort io = (IOPort) inputPorts.get(i);

			if (io instanceof CombinedFragmentPort) {

				if (io.hasToken(0)) {

					Token t = io.get(0);
					CombinedFragmentPort cfp = (CombinedFragmentPort) io;
					CombinedFragment cf = (CombinedFragment) cfp.getFragment();

					ht.put(cf, t);

				}
			}

		}
		return ht;

	}

	/**
	 * Initialises the hashtable, where the information about the messages'
	 * round is stored
	 */
	protected void initRound() {

		container_ = getContainer();

		if (container_ instanceof UMLSeqActor) {

			List messages = ((UMLSeqActor) container_)
					.attributeList(Message.class);

			for (int i = 0; i < messages.size(); i++) {

				messagesRound_.put(messages.get(i), 0);

			}
		}
	}

	/**
	 * Returns the message's execution round
	 * 
	 * @param m
	 * @return
	 */
	protected int whichRound(Message m) {

		return ((Integer) messagesRound_.get(m)).intValue();

	}

	/**
	 * When the message is fired (in case of no mapper) or received from the
	 * network (executing with the platform), the execution round is updated
	 * 
	 * @param m
	 */
	protected void updateRound(Message m) {

		int round = ((Integer) messagesRound_.get(m)).intValue();
		round = round + 1;
		messagesRound_.put(m, round);
	}

	public Parameter pipelining;
	protected boolean pipelining_;

	protected UMLSeqActor tmp_;

	protected LifelineMapper mapper_;
	protected boolean mapperExists_;

	protected Nameable container_;

	protected Hashtable messagesPort_;
	protected Hashtable fragmentTokens_;

	protected int round_;
	protected Hashtable messagesRound_;

}