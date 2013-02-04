// Copyright (c) 2007 Leandro Soares Indrusiak & Luciano Ost
// All rights reserved.
// Darmstadt, 31/05/2007.
//Version 1.1	Darmstadt, 15/10/2007

package lsi.noc.renato;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.domains.de.lib.Queue;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.data.type.BaseType;

import ptolemy.data.RecordToken;

import ptolemy.data.BooleanToken;

//////////////////////////////////////////////////////////////////////////
////PeekableQueue

/**
 * This actor extends the original queue from Ptolemy by adding the capability
 * of checking the oldest token on the queue without removing it.
 * 
 * @author Leandro Indrusiak
 */

public class PeekableQueue extends Queue {

	private BooleanToken accept;

	public PeekableQueue(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		peektrigger = new TypedIOPort(this, "peek", true, false);
		peektrigger.setMultiport(true);
		// Leave peektrigger type undeclared.
		// Put it at the bottom of the icon by default.
		StringAttribute cardinality = new StringAttribute(peektrigger,
				"_cardinal");
		cardinality.setExpression("SOUTH");

		peekout = new TypedIOPort(this, "peekout", false, true);
		peekout.setMultiport(false);
		peekout.setTypeEquals(input.getType());

		hasRoom = new TypedIOPort(this, "hasRoom", true, false);
		ack = new TypedIOPort(this, "ack", false, true);

		ack.setTypeEquals(BaseType.BOOLEAN);

	}

	/**
	 * If there is a token on the peektrigger, send a copy of the oldest token
	 * on the queue to the <i>peekout</i> port. Then, fires the superclass to
	 * deal with reading and/or writing tokens on the queue. Notice that if read
	 * and a peek triggers arrive at the same time, the same token will be sent
	 * to peekout and to the output.
	 * 
	 * @exception IllegalActionException
	 *                If getting tokens from input and trigger ports or sending
	 *                token to output throws it.
	 */

	public void fire() throws IllegalActionException {

		super.fire();
		boolean gotPeekTrigger = false;
		boolean gotHasRoom = false;

		for (int i = 0; i < hasRoom.getWidth(); i++) {
			if (hasRoom.hasToken(i)) {
				// Consume the trigger token.
				hasRoom.get(i);
				gotHasRoom = true;
				// if(_debugging) {_debug("Peek Trigger token consumed");}
			}
		}
		if (gotHasRoom && _queue.size() < _queue.getCapacity()) {
			accept = BooleanToken.TRUE;
			ack.send(0, accept);

			if (_debugging) {
				_debug("Copy of oldest token sent");
			}
		} else {
			gotHasRoom = false;
			accept = BooleanToken.FALSE;
			ack.send(0, accept);
		}

		for (int i = 0; i < peektrigger.getWidth(); i++) {
			if (peektrigger.hasToken(i)) {
				// Consume the trigger token.
				peektrigger.get(i);
				gotPeekTrigger = true;
				if (_debugging) {
					_debug("Peek Trigger token consumed");
				}
			}
		}
		if (gotPeekTrigger && _queue.size() != 0) {
			peekout.send(0, (Token) _queue.get(0));
			if (_debugging) {
				_debug("Copy of oldest token sent");
			}
		} else
			gotPeekTrigger = false;

	}

	/**
	 * If there is no input on the <i>peektrigger</i> port, leaves to the
	 * superclass the decision whether the actor should fire or not. If there is
	 * an input on the <i>peektrigger</i> port, it returns true.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public boolean prefire() throws IllegalActionException {

		boolean hasPeekTrigger = false;
		boolean has_Room = false;

		if (peektrigger.getWidth() > 0) {
			hasPeekTrigger = (peektrigger.hasToken(0));
		}

		if (hasRoom.getWidth() > 0) {
			has_Room = (hasRoom.hasToken(0));
		}

		if (hasPeekTrigger || has_Room)
			return true;
		else
			return super.prefire();
	}

	/**
	 * Override the base class to declare that the <i>output</i> does not depend
	 * on the <i>input</i> in a firing.
	 */
	public void pruneDependencies() {
		super.pruneDependencies();
		removeDependency(input, ack);
		// removeDependency(hasRoom, ack);
		removeDependency(hasRoom, peekout);
	}

	/**
	 * The peek trigger port, which has undeclared type. If this port receives a
	 * token, then the oldest token in the queue will be emitted on the
	 * <i>output</i> port, but not removed from the queue.
	 */
	public TypedIOPort peektrigger, peekout, ack, hasRoom;
}
