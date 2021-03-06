/* A publisher that transparently tunnels messages to subscribers.

 Copyright (c) 2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.lib;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Publisher

/**
 * This actor publishes input tokens on a named channel. The tokens are
 * "tunneled" to any instance of Subscriber that names the same channel and that
 * is under the control of the same director. That is, it can be at a different
 * level of the hierarchy, or in an entirely different composite actor, as long
 * as the relevant composite actors are transparent (have no director).
 * <p>
 * It is an error to have two instances of Publisher using the same channel
 * under the control of the same director. When you create a new Publisher, by
 * default, it assigns a channel name that is unique. You can re-use channel
 * names within opaque composite actors.
 * <p>
 * This actor actually has a hidden output port that is connected to all
 * subcribers via hidden "liberal links" (links that are allowed to cross levels
 * of the hierarchy). Consequently, any data dependencies that the director
 * might assume on a regular "wired" connection will also be assumed across
 * Publisher-Subscriber pairs. Similarly, type constraints will propagate across
 * Publisher-Subscriber pairs. That is, the type of the Subscriber output will
 * match the type of the Publisher input.
 * 
 * @author Edward A. Lee, Raymond A. Cardillo
 * @version $Id: Publisher.java,v 1.11.4.1 2006/10/27 19:14:05 cxh Exp $
 * @since Ptolemy II 5.2
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Publisher extends TypedAtomicActor {

	/**
	 * Construct a publisher with the specified container and name.
	 * 
	 * @param container
	 *            The container actor.
	 * @param name
	 *            The name of the actor.
	 * @exception IllegalActionException
	 *                If the actor is not of an acceptable class for the
	 *                container.
	 * @exception NameDuplicationException
	 *                If the name coincides with an actor already in the
	 *                container.
	 */
	public Publisher(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		channel = new StringParameter(this, "channel");
		channel.setExpression(_uniqueChannelName());

		input = new TypedIOPort(this, "input", true, false);
		input.setMultiport(true);
		output = new TypedIOPort(this, "output", false, true);
		output.setMultiport(true);

		Parameter hide = new SingletonParameter(output, "_hide");
		hide.setToken(BooleanToken.TRUE);
		// hide = new SingletonParameter(this, "_hideName");
		// hide.setToken(BooleanToken.TRUE);
	}

	// /////////////////////////////////////////////////////////////////
	// // ports and parameters ////

	/**
	 * The name of the channel. Subscribers that reference this same channel
	 * will receive any transmissions to this port. This is a string that
	 * defaults to "channelX", where X is an integer that ensures that this
	 * channel name does not collide with a channel name already in use by
	 * another publisher.
	 */
	public StringParameter channel;

	/**
	 * The input port. This base class imposes no type constraints except that
	 * the type of the input cannot be greater than the type of the output.
	 */
	public TypedIOPort input;

	/**
	 * The output port. By default, the type of this output is constrained to be
	 * at least that of the input. This port is hidden by default and the actor
	 * handles creating connections to it.
	 */
	public TypedIOPort output;

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * If the attribute is the channel, increment the workspace version to force
	 * cached receiver lists to be updated, and invalidate the schedule and
	 * resolved types of the director, if there is one.
	 * 
	 * @param attribute
	 *            The attribute that changed.
	 * @exception IllegalActionException
	 *                If the change is not acceptable to this container.
	 */
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {
		if (attribute == channel) {
			String newValue = channel.stringValue();
			if (!newValue.equals(_channel)) {
				_channel = newValue;
				// If we are within a class definition, then we should
				// not create any links. The links should only exist
				// within instances. Otherwise, we could end up creating
				// a link between a class definition and an instance.
				if (!isWithinClassDefinition()) {
					_updateLinks();
				}
			}
		} else {
			super.attributeChanged(attribute);
		}
	}

	/**
	 * Override the base class to update the width of the hidden link.
	 * 
	 * @param port
	 *            The port that has connection changes.
	 */
	public void connectionsChanged(Port port) {
		super.connectionsChanged(port);
		if (port == input) {
			if (_relation != null && !_inConnectionsChanged) {
				try {
					_inConnectionsChanged = true;
					int width = input.getWidth();
					_relation.setWidth(width);
				} catch (IllegalActionException e) {
					throw new InternalErrorException(e);
				} finally {
					_inConnectionsChanged = false;
				}
			}
		}
	}

	/**
	 * Read at most one input token from each input channel and send it to the
	 * subscribers, if any.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public void fire() throws IllegalActionException {
		super.fire();
		for (int i = 0; i < input.getWidth(); i++) {
			if (input.hasToken(i)) {
				Token token = input.get(i);
				output.send(i, token);
			}
		}
	}

	/**
	 * Override the base class to ensure that links to subscribers have been
	 * updated.
	 * 
	 * @exception IllegalActionException
	 *                If there is already a publisher publishing on the same
	 *                channel.
	 */
	public void preinitialize() throws IllegalActionException {
		super.preinitialize();
		// If this was created by instantiating a container class,
		// then the links would not have been updated when setContainer()
		// was called, so we must do it now.
		if (!_updatedLinks) {
			_updateLinks();
		}
	}

	/**
	 * Override the base class to create an associated relation, and to remove
	 * any previous relation.
	 * 
	 * @param container
	 *            The proposed container.
	 * @exception IllegalActionException
	 *                If the action would result in a recursive containment
	 *                structure, or if this entity and container are not in the
	 *                same workspace.
	 * @exception NameDuplicationException
	 *                If the container already has an entity with the name of
	 *                this entity.
	 */
	public void setContainer(CompositeEntity container)
			throws IllegalActionException, NameDuplicationException {
		if (container != getContainer()) {
			super.setContainer(container);
			// Update the links.
			// If we are within a class definition, then we should
			// not create any links. The links should only exist
			// within instances. Otherwise, we could end up creating
			// a link between a class definition and an instance.
			// Note that if we are within an instantiate of a
			// containing class, then we will not update links
			// because this is (temporarily) within a class definition.
			if (!isWithinClassDefinition()) {
				_updateLinks();
			}
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // protected variables ////

	/** The relation used to link to subscribers. */
	protected TypedIORelation _relation;

	/** Cached channel name. */
	protected String _channel;

	// /////////////////////////////////////////////////////////////////
	// // private methods ////

	/**
	 * Find subscribers.
	 * 
	 * @return A list of subscribers.
	 * @exception IllegalActionException
	 *                If there is already a publisher using the same channel.
	 */
	private List _findSubscribers() throws IllegalActionException {
		LinkedList result = new LinkedList();
		// Find the nearest opaque container above in the hierarchy.
		CompositeEntity container = (CompositeEntity) getContainer();
		while (container != null && !container.isOpaque()) {
			container = (CompositeEntity) container.getContainer();
		}
		if (container != null) {
			Iterator actors = container.deepEntityList().iterator();
			while (actors.hasNext()) {
				Object actor = actors.next();
				if (actor instanceof Subscriber) {
					if (((Subscriber) actor).channelMatches(_channel)) {
						result.add(actor);
					}
				} else if (actor instanceof Publisher && actor != this) {
					// Throw an exception if there is another publisher
					// trying to publish on the same channel.
					if (_channel.equals(((Publisher) actor)._channel)) {
						throw new IllegalActionException(this,
								"There is already a publisher using channel "
										+ "\"_channel\": "
										+ ((NamedObj) actor).getFullName());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Return a channel name of the form "channelX", where X is an integer that
	 * ensures that this channel name is not already in use.
	 * 
	 * @return A unique channel name.
	 */
	private String _uniqueChannelName() {
		int suffix = 1;
		// Find the nearest opaque container above in the hierarchy.
		CompositeEntity container = (CompositeEntity) getContainer();
		while (container != null && !container.isOpaque()) {
			container = (CompositeEntity) container.getContainer();
		}
		if (container != null) {
			Iterator actors = container.deepEntityList().iterator();
			while (actors.hasNext()) {
				Object actor = actors.next();
				if (actor instanceof Publisher && actor != this) {
					String nameInUse = ((Publisher) actor)._channel;
					if (nameInUse != null && nameInUse.startsWith("channel")) {
						String suffixInUse = nameInUse.substring(7);
						try {
							int suffixAsInt = Integer.parseInt(suffixInUse);
							if (suffix <= suffixAsInt) {
								suffix = suffixAsInt + 1;
							}
						} catch (NumberFormatException ex) {
							// Not a number suffix, so it can't collide.
							continue;
						}
					}
				}
			}
		}
		return "channel" + suffix;
	}

	/**
	 * Update connections to subscribers.
	 * 
	 * @exception IllegalActionException
	 *                If there is already a publisher publishing on the same
	 *                channel.
	 */
	private void _updateLinks() throws IllegalActionException {
		// If the channel has not been set, then there is nothing
		// to do. This is probably the first setContainer() call,
		// before the object is fully constructed.
		if (_channel == null) {
			return;
		}
		// Do this before making any changes to the model in case
		// it throws an exception.
		Iterator subscribers = _findSubscribers().iterator();

		// Remove the previous relation, if necessary.
		if (_relation != null) {
			try {
				_relation.setContainer(null);
			} catch (NameDuplicationException e) {
				throw new InternalErrorException(e);
			}
			_relation = null;
		}
		// If the container is not a typed composite actor, then don't create
		// a relation. Probably the container is a library.
		NamedObj container = getContainer();
		if (container instanceof TypedCompositeActor) {
			try {
				_relation = new TypedIORelation(
						(TypedCompositeActor) container,
						container.uniqueName("publisherRelation"));
				// Prevent the relation and its links from being exported.
				_relation.setPersistent(false);
				// Prevent the relation from showing up in vergil.
				new Parameter(_relation, "_hide", BooleanToken.TRUE);
				// Set the width of the relation to match the
				// width of the input.
				_relation.setWidth(input.getWidth());
			} catch (NameDuplicationException e) {
				throw new InternalErrorException(e);
			}
			output.link(_relation);

			// Link to the subscribers.
			while (subscribers.hasNext()) {
				Subscriber subscriber = (Subscriber) subscribers.next();
				subscriber.input.liberalLink(_relation);
			}
		}
		Director director = getDirector();
		if (director != null) {
			director.invalidateSchedule();
			director.invalidateResolvedTypes();
		}
		_updatedLinks = true;
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/** An indicator that connectionsChanged() has been called. */
	private boolean _inConnectionsChanged = false;

	/** An indicator that _updateLinks has been called at least once. */
	private boolean _updatedLinks = false;
}
