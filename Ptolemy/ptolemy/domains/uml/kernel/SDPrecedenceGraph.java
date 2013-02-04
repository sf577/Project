package ptolemy.domains.uml.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import lsi.noc.application.uml.directors.PrecedenceGraph;
import lsi.noc.application.uml.directors.PrecedenceGraphNode;

import ptolemy.data.Token;
import ptolemy.vergil.uml.CombinedFragment;
import ptolemy.vergil.uml.Message;

/**
 * @author Leandro Soares Indrusiak, Sanna M‰‰tt‰
 * @version $Id: SDPrecedenceGraph.java,v 2.0 2008/10/01 Added overloaded
 *          precedenceSatisfied due to the pipelined application execution
 *          2009/11/17 Moved a lot of functions from the child classes to here
 *          2009/11/10
 */

public abstract class SDPrecedenceGraph {

	public SDPrecedenceGraph() {

		fragmentTokens_ = new Hashtable();
		graph_ = new PrecedenceGraph();

	}

	public abstract boolean precedenceSatisfied(Message m);

	public abstract boolean precedenceSatisfied(Message m, int round);

	public abstract void notifyMessageFiring(Message m);

	// public abstract void setFragmentTokens(Hashtable fragmentTokens);

	/**
	 * Checking if a message m is inside a combined fragment cf. Returns true,
	 * if the message is inside the combined fragment, otherwise returns false.
	 * 
	 * @param m
	 * @param cf
	 * @return
	 */
	protected boolean ifFragmentMessage(Message m, CombinedFragment cf) {

		// Checking the message's and cf's location in the workspace.
		if ((m.getSendEvent().getLocTime()[1] <= (cf.getFragmentLocation()[1] + (cf
				.getHeigth() / 2)))
				&& m.getSendEvent().getLocTime()[1] >= (cf
						.getFragmentLocation()[1] - (cf.getHeigth() / 2))) {

			if ((m.getSendEvent().getLifeline().getLifelineLocation()[0] <= (cf
					.getFragmentLocation()[0] + (cf.getWidth() / 2)))
					&& m.getSendEvent().getLifeline().getLifelineLocation()[0] >= (cf
							.getFragmentLocation()[0] - (cf.getWidth() / 2))) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Getting the fragment tokens from the alt, opt, and loop inputs indicating
	 * the condition of the combined fragment Loop is executed if the condition
	 * is true, alt is executed if the condition is true, opt is executed if
	 * there is a token in the input.
	 */
	// FIXME: T‰m‰n olisi hyv‰ reagoida vain muutoksiin! opt, alt ja loop pysyy
	// siell‰ niin kauan kuin tulee muutos inputtiin.
	public void setFragmentTokens(Hashtable fragmentTokens) {

		fragmentTokens_ = fragmentTokens;

	}

	/**
	 * Checking if all nodes in the graph have been fired. This means that one
	 * execution round of the sequence diagram has finished.
	 * 
	 * @return true if all nodes in the graph have been fired are fired
	 */
	protected boolean allNodesFired_() {

		Collection col = graph_.nodes();
		Iterator it = col.iterator();

		while (it.hasNext()) {

			PrecedenceGraphNode nextNode = (PrecedenceGraphNode) it.next();

			if (!nextNode.getFired()) {
				return false;
			}

		}
		return true;
	}

	/**
	 * Checking if all nodes in the graph have been fired on that execution
	 * round. This means that one execution round of the sequence diagram has
	 * finished.
	 * 
	 * @return true if all nodes in the graph have been fired are fired
	 */
	protected boolean allNodesFired_(int round) {

		Collection col = graph_.nodes();
		Iterator it = col.iterator();

		while (it.hasNext()) {

			PrecedenceGraphNode nextNode = (PrecedenceGraphNode) it.next();

			if (!nextNode.getFired(round)) {
				return false;
			}

		}
		return true;
	}

	/**
	 * Checking, if all previous nodes have been fired, i.e. if the node in
	 * question can be fired or not. If opt or alt conditions are false (no
	 * token in opt input or false in alt input), the nodes inside opt or alt
	 * combined fragment do not need to be fired. Otherwise all preceding nodes
	 * need to be fired before the node in question can be fired. Note that
	 * nodes inside a parallel combined fragment are not preceding nodes in the
	 * graph, but parallel.
	 * 
	 * @param node
	 * @return
	 */
	protected boolean allPreviousNodesFired_(PrecedenceGraphNode node) {

		// A collection of all the nodes that can be reached backward from the
		// specified node.
		Collection col = graph_.backwardReachableNodes(node);

		Iterator it = col.iterator();

		CombinedFragment alt = null;
		CombinedFragment opt = null;

		while (it.hasNext()) {

			PrecedenceGraphNode nextNode = (PrecedenceGraphNode) it.next();
			CombinedFragment cf = nextNode.getFragment();

			// If one of the preceding nodes is not fired, the node in question
			// can be fired only if the non-fired node is
			// inside an alt or opt combined fragment and the condition for alt
			// or opt is false.
			if (!nextNode.getFired()) {

				// Node is not fired and is not inside an alt or opt combined
				// fragment. The node in question cannot be fired.
				if (cf == null || (cf.getOp()).equals("par")
						|| (cf.getOp()).equals("loop")) {

					return false;

				} else {

					// The node in question is inside an opt combined fragment.
					if ((cf.getOp()).equals("opt")) {

						// The node in question is not the first node inside an
						// opt combined fragment, the condition for executing
						// the opt is false, so all
						// nodes inside the opt will be set as fired.
						if (opt == cf) {

							nextNode.setFired(true);

						} else {
							// The node in question is the first node inside an
							// opt combined fragment, setting the opt parameter
							// to be the combined fragment
							opt = cf;

							// Checking the condition for the opt combined
							// fragment
							if (fragmentTokens_.containsKey(nextNode
									.getFragment())) {

								// Getting the next token that has arrived in
								// the opt fragment input (stored in the
								// fragmentTokens_ hashtable)
								Token t = (Token) fragmentTokens_.get(nextNode
										.getFragment());

								// If there is no token at the opt input, the
								// opt condition is not true and the nodes
								// inside the opt will be set fired
								// i.e., the nodes inside the opt will not be
								// executed.
								if (t == null) {

									nextNode.setFired(true);

									// The opt nodes should be executed, but are
									// not fired yet, so the node in question
									// should not be fired.
								} else {

									return false;

								}
								// There is no token at the opt input, all nodes
								// inside the opt combined fragment are set as
								// fired.
							} else {
								nextNode.setFired(true);

							}

						}
						// Node is inside an alt combined fragment
					} else {

						// The node in question is not the first node inside an
						// alt combined fragment, the condition for executing
						// the alt is false, so all
						// nodes inside the alt will be set as fired.
						if (alt == cf) {

							nextNode.setFired(true);

						} else {
							// The node in question is the first node inside an
							// alt combined fragment, setting the alt parameter
							// to be the combined fragment
							alt = cf;

							// Checking the condition for the alt combined
							// fragment
							if (fragmentTokens_.containsKey(nextNode
									.getFragment())) {

								// Getting the next token that has arrived in
								// the alt fragment input (stored in the
								// fragmentTokens_ hashtable)
								Token t = (Token) fragmentTokens_.get(nextNode
										.getFragment());

								// If the token at the alt input equals true,
								// the alt condition is true and the nodes
								// inside the alt should be fired
								// before the node in question is fired
								if ((t.toString()).equals("\"true\"")) {

									return false;

								} else {
									// The token in the alt input equals false,
									// therefore all the nodes inside the alt
									// combined fragment are set as fired
									// i.e. the alt condition is not true.
									nextNode.setFired(true);
								}

							} else {
								// There is no token at the alt input, so the
								// alt combined fragment condition is not true
								// and the nodes inside the fragment are
								// set as fired.
								nextNode.setFired(true);
							}
						}
					}
				}
			}
		}
		// All the preceding nodes are fired, the node in question can be fired
		return true;
	}

	/**
	 * Checking, if all previous nodes have been fired on that execution round,
	 * i.e. if the node in question can be fired or not. If opt or alt
	 * conditions are false (no token in opt input or false in alt input), the
	 * nodes inside opt or alt combined fragment do not need to be fired.
	 * Otherwise all preceding nodes need to be fired before the node in
	 * question can be fired. Note that nodes inside a parallel combined
	 * fragment are not preceding nodes in the graph, but parallel.
	 * 
	 * @param node
	 * @return
	 */
	protected boolean allPreviousNodesFired_(PrecedenceGraphNode node, int round) {

		// A collection of all the nodes that can be reached backward from the
		// specified node.
		Collection col = graph_.backwardReachableNodes(node);

		Iterator it = col.iterator();

		CombinedFragment alt = null;
		CombinedFragment opt = null;

		while (it.hasNext()) {

			PrecedenceGraphNode nextNode = (PrecedenceGraphNode) it.next();
			CombinedFragment cf = nextNode.getFragment();

			// If one of the preceding nodes is not fired, the node in question
			// can be fired only if the non-fired node is
			// inside an alt or opt combined fragment and the condition for alt
			// or opt is false.
			if (!nextNode.getFired(round)) {

				// Node is not fired and is not inside an alt or opt combined
				// fragment. The node in question cannot be fired.
				if (cf == null || (cf.getOp()).equals("par")
						|| (cf.getOp()).equals("loop")) {

					return false;

				} else {

					// The node in question is inside an opt combined fragment.
					if ((cf.getOp()).equals("opt")) {

						// The node in question is not the first node inside an
						// opt combined fragment, the condition for executing
						// the opt is false, so all
						// nodes inside the opt will be set as fired.
						if (opt == cf) {

							nextNode.setFired(true, round);

						} else {
							// The node in question is the first node inside an
							// opt combined fragment, setting the opt parameter
							// to be the combined fragment
							opt = cf;

							// Checking the condition for the opt combined
							// fragment
							if (fragmentTokens_.containsKey(nextNode
									.getFragment())) {

								// Getting the next token that has arrived in
								// the opt fragment input (stored in the
								// fragmentTokens_ hashtable)
								Token t = (Token) fragmentTokens_.get(nextNode
										.getFragment());

								// If there is no token at the opt input, the
								// opt condition is not true and the nodes
								// inside the opt will be set fired
								// i.e., the nodes inside the opt will not be
								// executed.
								if (t == null) {

									nextNode.setFired(true, round);

									// The opt nodes should be executed, but are
									// not fired yet, so the node in question
									// should not be fired.
								} else {

									return false;

								}
								// There is no token at the opt input, all nodes
								// inside the opt combined fragment are set as
								// fired.
							} else {
								nextNode.setFired(true, round);

							}

						}
						// Node is inside an alt combined fragment
					} else {

						// The node in question is not the first node inside an
						// alt combined fragment, the condition for executing
						// the alt is false, so all
						// nodes inside the alt will be set as fired.
						if (alt == cf) {

							nextNode.setFired(true, round);

						} else {
							// The node in question is the first node inside an
							// alt combined fragment, setting the alt parameter
							// to be the combined fragment
							alt = cf;

							// Checking the condition for the alt combined
							// fragment
							if (fragmentTokens_.containsKey(nextNode
									.getFragment())) {

								// Getting the next token that has arrived in
								// the alt fragment input (stored in the
								// fragmentTokens_ hashtable)
								Token t = (Token) fragmentTokens_.get(nextNode
										.getFragment());

								// If the token at the alt input equals true,
								// the alt condition is true and the nodes
								// inside the alt should be fired
								// before the node in question is fired
								if ((t.toString()).equals("\"true\"")) {

									return false;

								} else {
									// The token in the alt input equals false,
									// therefore all the nodes inside the alt
									// combined fragment are set as fired
									// i.e. the alt condition is not true.
									nextNode.setFired(true, round);
								}

							} else {
								// There is no token at the alt input, so the
								// alt combined fragment condition is not true
								// and the nodes inside the fragment are
								// set as fired.
								nextNode.setFired(true, round);
							}
						}
					}
				}
			}
		}
		// All the preceding nodes are fired, the node in question can be fired
		return true;
	}

	/**
	 * Counting the nodes inside a parallel combined fragment This information
	 * is used when checking whether all nodes inside a parallel fragment have
	 * been fired or not.
	 * 
	 * @param node
	 * @return
	 */
	protected int numberOfParallel_(PrecedenceGraphNode node) {

		// Getting the previous node(s)
		Collection col = graph_.predecessors(node);
		// If there are previous nodes to this parallel node
		if (!col.isEmpty()) {

			Iterator it = col.iterator();
			PrecedenceGraphNode n = (PrecedenceGraphNode) it.next();
			// Col2 contains all the nodes that can be reached from the
			// preceding nodes.
			Collection col2 = graph_.successors(n);
			return col2.size();

			// The parallel combined fragment is at the beginning of the graph,
			// so there are no preceding nodes.
		} else {
			col = graph_.successors(node);

			if (!col.isEmpty()) {

				Iterator it = col.iterator();
				PrecedenceGraphNode n = (PrecedenceGraphNode) it.next();
				Collection col2 = graph_.predecessors(n);
				return col2.size();

				// All nodes are inside a parallel fragment.
			} else {
				return graph_.nodeCount();
			}
		}
	}

	/**
	 * If the alt or opt fragment conditions are false, all nodes inside the
	 * combined fragment will be set as fired If a loop will be executed again,
	 * all nodes inside the combined fragment will be set as not fired.
	 * 
	 * @param cf
	 * @param node
	 * @param trueOrFalse
	 */
	protected void setArea_(CombinedFragment cf, PrecedenceGraphNode node,
			boolean trueOrFalse) {

		// Setting the node in question as either fired or not.
		node.setFired(trueOrFalse);

		// The precedence graph is a directed graph so the collection contains
		// only the succeeding nodes
		Collection col = graph_.reachableNodes(node);

		Iterator it = col.iterator();

		while (it.hasNext()) {

			PrecedenceGraphNode n = (PrecedenceGraphNode) it.next();

			// If the node is inside the same combined fragment, it is set as
			// either fired (true) or not fired (false).
			if (cf == n.getFragment()) {
				n.setFired(trueOrFalse);

			}
		}
	}

	/**
	 * If the alt or opt fragment conditions are false, all nodes inside the
	 * combined fragment will be set as fired on that execution round If a loop
	 * will be executed again, all nodes inside the combined fragment will be
	 * set as not fired.
	 * 
	 * @param cf
	 * @param node
	 * @param trueOrFalse
	 */
	protected void setArea_(CombinedFragment cf, PrecedenceGraphNode node,
			boolean trueOrFalse, int round) {

		// Setting the node in question as either fired or not.
		node.setFired(trueOrFalse, round);

		// The precedence graph is a directed graph so the collection contains
		// only the succeeding nodes
		Collection col = graph_.reachableNodes(node);

		Iterator it = col.iterator();

		while (it.hasNext()) {

			PrecedenceGraphNode n = (PrecedenceGraphNode) it.next();

			// If the node is inside the same combined fragment, it is set as
			// either fired (true) or not fired (false).
			if (cf == n.getFragment()) {
				n.setFired(trueOrFalse, round);

			}
		}
	}

	/**
	 * This method finds the node containing the message m Returns null, if the
	 * node cannot be found (this shouldn't really happen).
	 * 
	 * @param m
	 * @return
	 */
	protected PrecedenceGraphNode findNode_(Message m) {

		Collection col = graph_.nodes();
		Iterator it = col.iterator();

		// Going through all nodes in the precedence graph
		while (it.hasNext()) {

			PrecedenceGraphNode node = (PrecedenceGraphNode) it.next();

			// A correct node is found
			if (node.getNode(m) != null) {

				return node.getNode(m);
			}

		}

		return null;
	}

	/**
	 * This method returns the messages that are preceded by message m.
	 * 
	 * @param m
	 * @return
	 */

	public Collection successors(Message m) {

		ArrayList a = new ArrayList();

		Iterator i = graph_.successors(findNode_(m)).iterator();
		while (i.hasNext()) {

			PrecedenceGraphNode node = (PrecedenceGraphNode) i.next();
			a.add(node.getMessage());

		}

		return a;
	}

	/**
	 * Setting all nodes in the precedence graph not fired (used for
	 * initialisation and e.g. after one round of the sequence is executed)
	 */
	protected void resetGraph_() {

		Collection nodes = graph_.nodes();
		Iterator it = nodes.iterator();

		// Setting all nodes not fired
		while (it.hasNext()) {
			PrecedenceGraphNode next = (PrecedenceGraphNode) (it.next());
			next.setFired(false);
		}
	}

	/**
	 * Setting all nodes in the precedence graph not fired on that execution
	 * round (used for initialisation and e.g. after one round of the sequence
	 * is executed)
	 */
	protected void resetGraph_(int round) {

		Collection nodes = graph_.nodes();
		Iterator it = nodes.iterator();

		// Setting all nodes not fired
		while (it.hasNext()) {
			PrecedenceGraphNode next = (PrecedenceGraphNode) (it.next());
			next.setFired(false, round);
		}
	}

	protected Hashtable fragmentTokens_;
	protected PrecedenceGraph graph_;
}
