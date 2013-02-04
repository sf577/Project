package lsi.noc.application.uml.directors;

import java.util.Hashtable;

import ptolemy.data.BooleanToken;
import ptolemy.graph.Node;
import ptolemy.vergil.uml.CombinedFragment;
import ptolemy.vergil.uml.Message;

/**
 * 
 * @author Sanna Maatta
 * @version 2.0, pipelined application execution possible, hopefully.
 * 
 *          Fired -variable is not any more a boolean, but a hashtable, where
 *          each round has the fired value for the node
 * 
 */

public class PrecedenceGraphNode extends Node {

	public PrecedenceGraphNode() {
		super();

		roundFired_ = new Hashtable();
		fired_ = false;
		fragment_ = null;
	}

	/**
	 * Construct a node with a given node weight.
	 * 
	 * @exception IllegalArgumentException
	 *                If the specified weight is <code>null</code>.
	 * @param weight
	 *            The given weight.
	 */
	public PrecedenceGraphNode(Object weight) {
		super(weight);
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Returns the node that contains a certain message
	 */
	public PrecedenceGraphNode getNode(Message m) {

		if (m == message_) {
			return this;
		}

		return null;

	}

	/**
	 * A one-word description of the type of this graph element.
	 * 
	 * @return The description.
	 */
	public String descriptor() {
		return "precedenceGraphNode";
	}

	/**
	 * Returns whether the node (message) is already fired (true) or not (false)
	 * 
	 * @return
	 */
	public boolean getFired() {
		return fired_;
	}

	/**
	 * Returns whether the node (message) is already fired on a particular
	 * execution round
	 * 
	 * @param round
	 * @return
	 */
	public boolean getFired(int round) {

		if (roundFired_.containsKey(round)) {

			return ((Boolean) roundFired_.get(round)).booleanValue();

		} else {

			return false;
		}
	}

	/**
	 * Sets a node (message) either fired (true) or not fired (false)
	 * 
	 * @param fired
	 */
	public void setFired(boolean fired) {
		fired_ = fired;
	}

	/**
	 * Sets a node (message) either fired or not fired on a particular execution
	 * round
	 * 
	 * @param fired
	 * @param round
	 */
	public void setFired(boolean fired, int round) {

		roundFired_.put(round, fired);

	}

	/**
	 * Each node in the precedence graph contains (corresponds) one message
	 * 
	 * @param m
	 */
	public void setMessage(Message m) {
		message_ = m;
	}

	/**
	 * Returns the message that the node contains (corresponds)
	 * 
	 * @return
	 */
	public Message getMessage() {
		return message_;
	}

	/**
	 * If a message is inside a combined fragment, fragment it set for the node
	 * 
	 * @param fragment
	 */
	public void setFragment(CombinedFragment fragment) {
		fragment_ = fragment;
	}

	/**
	 * Returns the fragment the message belongs to
	 * 
	 * @return
	 */
	public CombinedFragment getFragment() {
		return fragment_;
	}

	private Hashtable roundFired_;
	private boolean fired_;
	private Message message_;
	private CombinedFragment fragment_;

}