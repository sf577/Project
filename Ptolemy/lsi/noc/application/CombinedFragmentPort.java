package lsi.noc.application;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.Attribute;
import ptolemy.moml.MoMLChangeRequest;

// Added for the peekToken()-function
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.QueueReceiver;
import ptolemy.data.Token;
import ptolemy.data.RecordToken;

import ptolemy.vergil.uml.Message;
import ptolemy.vergil.uml.CombinedFragment;
import ptolemy.vergil.uml.UMLSeqActor;

//////////////////////////////////////////////////////////////////////////
////UMLPort

/**
 This class is a TypedIOPort with extended capabilities to handle token
 forwarding according to a UML Sequence Diagram.
 Modified from the BehavioralPatternPort.java -file

 @author Leandro Soares Indrusiak, Modified by Sanna Maatta
 @version 
 */

/**
 * Modified by Sanna Maatta Added public Token peekToken(int channelIndex)
 * method for peeking the oldest value of the (queue)receiver-fifo without
 * removing it
 */

public class CombinedFragmentPort extends TypedIOPort implements ChangeListener {

	private CombinedFragment cf_;

	/**
	 * Construct a port with a container and a name that is either an input, an
	 * output, or both, depending on the third and fourth arguments. The
	 * specified container must implement the Actor interface or an exception
	 * will be thrown.
	 * 
	 * @param container
	 *            The container actor.
	 * @param name
	 *            The name of the port.
	 * @param isInput
	 *            True if this is to be an input port.
	 * @param isOutput
	 *            True if this is to be an output port.
	 * @exception IllegalActionException
	 *                If the port is not of an acceptable class for the
	 *                container, or if the container does not implement the
	 *                Actor interface.
	 * @exception NameDuplicationException
	 *                If the name coincides with a port already in the
	 *                container.
	 */
	public CombinedFragmentPort(ComponentEntity container, String name,
			boolean isInput, boolean isOutput, CombinedFragment cf)
			throws IllegalActionException, NameDuplicationException {
		super(container, name, isInput, isOutput);

		cf_ = cf;

	}

	public CombinedFragmentPort(ComponentEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		refreshFragment();

	}

	public CombinedFragment getFragment() {

		if (cf_ == null) {
			refreshFragment();
		}
		return cf_;

	}

	private void refreshFragment() {

		Attribute fragment = getFragmentAttribute();

		// gets the actual fragment, which is an attribute of
		// this port's container (an UMLSeqActor), by querying for the name

		if (fragment != null) {

			String fragmentName = fragment.getName();

			UMLSeqActor root = (UMLSeqActor) getContainer();

			CombinedFragment frag = null;

			try {
				frag = (CombinedFragment) root.getAttribute(fragmentName,
						CombinedFragment.class);
			} catch (IllegalActionException iAE) {
				iAE.printStackTrace();
			}

			frag.addChangeListener(this);
			cf_ = frag;

		}
	}

	public Attribute getFragmentAttribute() {

		// reads the name of this port's related message, stored as an attribute

		Attribute fragment = null;
		List fragmentList = this.attributeList(CombinedFragment.class);
		if (fragmentList.size() == 1) {
			if (fragmentList.get(0) != null) {
				fragment = (CombinedFragment) fragmentList.get(0);
			}
		}
		return fragment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ptolemy.kernel.util.ChangeListener#changeExecuted(ptolemy.kernel.util
	 * .ChangeRequest)
	 */
	public void changeExecuted(ChangeRequest change) {
		// TODO

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.kernel.util.ChangeListener#changeFailed(ptolemy.kernel.util.
	 * ChangeRequest, java.lang.Exception)
	 */
	public void changeFailed(ChangeRequest change, Exception exception) {
		// TODO Auto-generated method stub

	}

	// A method, that returns the oldes value of the receiver-fifo without
	// removing it.
	// Almost an identical copy of the get() method in the IOPort.java
	// There must be a better way to do this???

	// ChannelIndex is always 0, because the oldest token of the fifo is wanted

	public Token peekToken(int channelIndex) throws NoTokenException,
			IllegalActionException {

		Receiver[][] localReceivers;

		try {
			_workspace.getReadAccess();

			// Note that the getInsideReceivers() method might throw an
			// IllegalActionException if there's no director.
			localReceivers = getInsideReceivers();

			if (channelIndex >= localReceivers.length) {
				if (!isOutput()) {
					throw new IllegalActionException(this,
							"Port is not an output port!");
				} else {
					throw new IllegalActionException(this, "Channel index "
							+ channelIndex
							+ " is out of range, because inside width is only "
							+ getWidthInside() + ".");
				}
			}

			if (localReceivers[channelIndex] == null) {
				throw new IllegalActionException(this,
						"No receiver at inside index: " + channelIndex + ".");
			}
		} finally {
			_workspace.doneReading();
		}

		Token token = null;

		// localReceiver changed to QueueReceiver to use the get(int index)
		// -method, which does not
		// remove the token from the fifo

		for (int j = 0; j < localReceivers[channelIndex].length; j++) {
			token = ((QueueReceiver) localReceivers[channelIndex][j]).get(0);

		}

		if (token == null) {
			throw new NoTokenException(this, "No token to return.");
		}

		if (_debugging) {
			_debug("get from inside channel " + channelIndex + ": " + token);
		}

		return token;

	}

}
