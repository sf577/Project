package lsi.noc.renato;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class BlockingQueue extends PeekableQueue {

	public BlockingQueue(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		hasRoom = new TypedIOPort(this, "hasRoom", true, false);
		hasRoom.setMultiport(true);

		hasRoomAck = new TypedIOPort(this, "ack", false, true);
		hasRoomAck.setMultiport(false);

		// Leave types undeclared.

	}

	/**
	 * Reads the parameter defining the max size of the queue.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public void initialize() throws IllegalActionException {

		// TO DO: read the parameter from the container and store it
		// in the maxsize variable;

		super.initialize();
	}

	/**
	 * If there is no input on the <i>hasRoom</i> port, leaves to the superclass
	 * the decision whether the actor should fire or not. If there is an input
	 * on the <i>hasRoom</i> port, it returns true.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public boolean prefire() throws IllegalActionException {

		boolean hasRoomQuery = false;

		if (hasRoom.getWidth() > 0) {
			hasRoomQuery = (hasRoom.hasToken(0));
		}

		if (hasRoomQuery)
			return true;
		else
			return super.prefire();
	}

	/**
	 * First, fire the superclass to deal with peeking, reading and/or writing
	 * tokens on the queue. Then, if there is a token on the <i>hasRoom</i>
	 * port, a token is sent to the <i>ack</i> port if the queue is not full.
	 * 
	 * @exception IllegalActionException
	 * 
	 */

	public void fire() throws IllegalActionException {

		super.fire();

		boolean hasRoomQuery = false;
		for (int i = 0; i < hasRoom.getWidth(); i++) {
			if (hasRoom.hasToken(i)) {
				// Consume the query token.
				hasRoom.get(i);
				hasRoomQuery = true;
				if (_debugging) {
					_debug("hasRoom token consumed");
				}
			}
		}
		if (hasRoomQuery && sizeOutput < maxsize) {
			hasRoomAck.send(0, new BooleanToken(true));
			if (_debugging) {
				_debug("hasRoom query acknowledged");
			}
		} else {
			hasRoomAck.send(0, new BooleanToken(false));
		}

	}

	/**
	 * The hasRoom and ack ports. If hasRoom receives a token, a token is sent
	 * to the ack port if the queue is not full.
	 */
	public TypedIOPort hasRoom, hasRoomAck;
	int maxsize = 8;

}
