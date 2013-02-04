package lsi.noc.application.uml.directors;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.List;
import ptolemy.vergil.uml.Lifeline;
import ptolemy.vergil.uml.Message;
import ptolemy.vergil.uml.MessageOccurrenceSpecification;
import ptolemy.vergil.uml.CombinedFragment;
import ptolemy.data.Token;

/**
 * 
 * @author Sanna Maatta
 * @version 2.2, Pipelined execution works. Some functions moved to superclass.
 *          This code creates a message precedence graph It creates a node for
 *          each message and edges to and from messages It also checks the
 *          precedence for each message, e.g. whether a message can be fired or
 *          not
 */
@SuppressWarnings("unchecked")
public class PartialOrderPrecedenceGraph extends
		ptolemy.domains.uml.kernel.SDPrecedenceGraph {

	public PartialOrderPrecedenceGraph(List lifelines, List messages,
			List fragments) {

		lifelineMessages_ = new Hashtable();

		addLifelines_(lifelines);
		addMessages_(messages);
		createGraph_(lifelines, messages, fragments);

		busy_ = false;
		parallel_ = false;

		cf_ = null;
		firstLoopNode_ = null;
		parallelNodes_ = 0;

	}

	/**
	 * Notifies that the message has been delivered to the destination and the
	 * next message can be sent.
	 */
	public void notifyMessageFiring(Message m) {

		// If the node is not inside a parallel combined fragment, the next
		// message can be fired (e.g. the precedence is not in the busy state
		// indicating
		// that a message delivery is not complete).
		if (!parallel_) {

			busy_ = false;

		} else {

			// If a node is inside a parallel combined fragment, another node
			// inside the same fragment can be fired
			// even if the previous one is not delivered yet.
			parallelNodes_ = parallelNodes_ - 1;

			// All nodes inside the parallel combined fragment are fired, so the
			// precedence is not in the busy state any more.
			if (parallelNodes_ <= 0) {

				busy_ = false;
				parallel_ = false;
			}
		}
	}

	/**
	 * Checking if the message's precedence is satisfied, i.e. it can be
	 * executed or not. The precedence is satisfied if all previous messages
	 * belonging to the same lifeline have fired before this message (false opt
	 * and alt conditions may have left some nodes not fired, which is ok).
	 */
	public boolean precedenceSatisfied(Message m) {

		if (allNodesFired_()) {

			resetGraph_();
			return false;
		}

		PrecedenceGraphNode node = findNode_(m);
		CombinedFragment cf = node.getFragment();

		// If the node containing the message in question has already fired or
		// all of the previous nodes have not been fired
		// the precedence is not satisfied.
		if (node.getFired() || !allPreviousNodesFired_(node)) {

			return false;

			// If the precedence graph is in a busy state, it indicates that a
			// message delivery is not complete so the precedence cannot be
			// satisfied unless the message is inside a parallel combined
			// fragment (messages inside a parallel combined fragment can be
			// delivered parallel, i.e. they do not have a precedence order).
		} else if (busy_) {

			// If the precedence graph is in the busy state and the node is not
			// inside a parallel combined fragment, the delivery of the
			// previous message is not complete and the precedence is not
			// satisfied for another message.
			if (!parallel_) {

				return false;

			} else {

				// If the node is inside the same parallel combined fragment, it
				// can be fired even if the previous one is not yet back from
				// the network
				if (cf_ != null && cf_ == cf && (cf.getOp()).equals("par")) {

					// The node can be fired and the precedence graphs remains
					// busy.
					node.setFired(true);
					busy_ = true;
					parallel_ = true;

				} else {

					// The node is not inside the same parallel combined
					// fragment (i.e. there are 2 adjacent parallel fragments
					// and the node
					// belongs to the next one).
					return false;
				}
			}

			// Precedence graph not busy and staying inside a same combined
			// fragment (or null, if not inside a fragment)
			// Therefore, precedence is satisfied, node can be fired.
		} else if (cf_ == cf) {

			busy_ = true;
			node.setFired(true);

			// Previous node's combined fragment is different than current
			// node's combined fragment, i.e. cf_ != cf.
			// Therefore, the condition for either firing the nodes inside the
			// fragment or skipping them needs to be checked.
		} else {

			// Node is inside a loop combined fragment
			if (cf_ != null && (cf_.getOp()).equals("loop")) {

				if (fragmentTokens_.containsKey(cf_)) {

					// Checking the loop condition
					Token t = (Token) fragmentTokens_.get(cf_);

					// A loop is looping until the condition is false
					if ((t.toString()).equals("\"true\"")) {

						// Setting the nodes inside a loop not fired so that
						// they will be fired again on the next round of the
						// loop.
						setArea_(cf_, firstLoopNode_, false);
						return false;
					}
				}

				// The new fragment is a parallel combined fragment
			} else if (cf_ != null && (cf_.getOp()).equals("par")) {

				parallel_ = false;
			}

			// The node is not inside any combined fragment, therefore the node
			// can be fired
			// The precedence graph is in busy state, until the message has been
			// delivered.
			if (cf == null) {

				busy_ = true;
				node.setFired(true);

				// The node is inside an alt combined fragment
			} else if ((cf.getOp()).equals("alt")) {

				// Checking the condition for the alt combined fragment
				if (fragmentTokens_.containsKey(node.getFragment())) {

					Token t = (Token) fragmentTokens_.get(node.getFragment());

					// The alt condition is false, so the nodes inside the alt
					// fragment are set fired and
					// the message's precedence is not satisfied
					if ((t.toString()).equals("\"false\"")) {

						setArea_(cf, node, true);
						return false;
					}

					// There is no token in the alt input, the nodes inside the
					// alt fragment are not executed and the message's
					// precedence is not satisfied.
				} else {

					setArea_(cf, node, true);
					return false;
				}

				// The node is inside an opt combined fragment
			} else if ((cf.getOp()).equals("opt")) {

				// Checking the opt condition
				if (fragmentTokens_.containsKey(node.getFragment())) {

					Token t = (Token) fragmentTokens_.get(node.getFragment());

					// The nodes inside the opt fragment are not executed and
					// the message's precedence is not satisfied.
					if (t == null) {

						setArea_(cf, node, true);
						return false;
					}

					// There is no token in the opt input, the nodes inside the
					// opt fragment are not executed and the
					// message's precedence is not satisfied.
				} else {

					setArea_(cf, node, true);
					return false;
				}

				// The message is inside a loop combined fragment
			} else if ((cf.getOp()).equals("loop")) {

				// Checking the loop condition
				if (fragmentTokens_.containsKey(node.getFragment())) {

					Token t = (Token) fragmentTokens_.get(node.getFragment());

					// Loop condition is false, so no more looping and message's
					// precedence is not satisfied.
					if ((t.toString()).equals("\"false\"")) {

						setArea_(cf, node, true);
						return false;

						// The loop should be executed, setting the first loop
						// node to be the node in question.
					} else {

						firstLoopNode_ = node;
					}

					// There is no condition for the loop, so the loop is not
					// executed and the
					// message's precedence is not satisfied.
				} else {

					setArea_(cf, node, true);
					return false;
				}

				// The node is inside a parallel combined fragment
				// Setting the number of nodes inside the fragment in order to
				// ease the checking whether
				// all the parallel nodes have been fired.
			} else if ((cf.getOp()).equals("par")) {

				parallel_ = true;
				parallelNodes_ = numberOfParallel_(node);
			}

			// Message's precedence is satisfied, node is set fired and the
			// precedence graph is in the busy
			// state until the message has been delivered.
			// Current combined fragment is the one the node in question is in
			// (or null, if it's not inside any combined fragment).
			busy_ = true;
			node.setFired(true);
			cf_ = cf;
		}

		return true;
	}

	/**
	 * Checking if the message's precedence is satisfied on that round, i.e. it
	 * can be executed or not. The precedence is satisfied if all previous
	 * messages belonging to the same lifeline on the same round have fired
	 * before this message (false opt and alt conditions may have left some
	 * nodes not fired, which is ok).
	 */
	public boolean precedenceSatisfied(Message m, int round) {

		PrecedenceGraphNode node = findNode_(m);
		CombinedFragment cf = node.getFragment();

		// If the node containing the message in question has already fired or
		// all of the previous nodes have not been fired
		// the precedence is not satisfied.
		if (node.getFired(round) || !allPreviousNodesFired_(node, round)) {

			return false;

			// If the precedence graph is in a busy state, it indicates that a
			// message delivery is not complete so the precedence cannot be
			// satisfied unless the message is inside a parallel combined
			// fragment (messages inside a parallel combined fragment can be
			// delivered parallel, i.e. they do not have a precedence order).
		} else if (busy_) {

			// If the precedence graph is in the busy state and the node is not
			// inside a parallel combined fragment, the delivery of the
			// previous message is not complete and the precedence is not
			// satisfied for another message.
			if (!parallel_) {

				return false;

			} else {

				// If the node is inside the same parallel combined fragment, it
				// can be fired even if the previous one is not yet back from
				// the network
				if (cf_ != null && cf_ == cf && (cf.getOp()).equals("par")) {

					// The node can be fired and the precedence graphs remains
					// busy.
					node.setFired(true, round);
					busy_ = true;
					parallel_ = true;

				} else {

					// The node is not inside the same parallel combined
					// fragment (i.e. there are 2 adjacent parallel fragments
					// and the node
					// belongs to the next one).
					return false;
				}
			}

			// Precedence graph not busy and staying inside a same combined
			// fragment (or null, if not inside a fragment)
			// Therefore, precedence is satisfied, node can be fired.
		} else if (cf_ == cf) {

			busy_ = true;
			node.setFired(true, round);

			// Previous node's combined fragment is different than current
			// node's combined fragment, i.e. cf_ != cf.
			// Therefore, the condition for either firing the nodes inside the
			// fragment or skipping them needs to be checked.
		} else {

			// Node is inside a loop combined fragment
			if (cf_ != null && (cf_.getOp()).equals("loop")) {

				if (fragmentTokens_.containsKey(cf_)) {

					// Checking the loop condition
					Token t = (Token) fragmentTokens_.get(cf_);

					// A loop is looping until the condition is false
					if ((t.toString()).equals("\"true\"")) {

						// Setting the nodes inside a loop not fired so that
						// they will be fired again on the next round of the
						// loop.
						setArea_(cf_, firstLoopNode_, false, round);
						return false;
					}
				}

				// The new fragment is a parallel combined fragment
			} else if (cf_ != null && (cf_.getOp()).equals("par")) {

				parallel_ = false;
			}

			// The node is not inside any combined fragment, therefore the node
			// can be fired
			// The precedence graph is in busy state, until the message has been
			// delivered.
			if (cf == null) {

				busy_ = true;
				node.setFired(true, round);

				// The node is inside an alt combined fragment
			} else if ((cf.getOp()).equals("alt")) {

				// Checking the condition for the alt combined fragment
				if (fragmentTokens_.containsKey(node.getFragment())) {

					Token t = (Token) fragmentTokens_.get(node.getFragment());

					// The alt condition is false, so the nodes inside the alt
					// fragment are set fired and
					// the message's precedence is not satisfied
					if ((t.toString()).equals("\"false\"")) {

						setArea_(cf, node, true, round);
						return false;
					}

					// There is no token in the alt input, the nodes inside the
					// alt fragment are not executed and the message's
					// precedence is not satisfied.
				} else {

					setArea_(cf, node, true, round);
					return false;
				}

				// The node is inside an opt combined fragment
			} else if ((cf.getOp()).equals("opt")) {

				// Checking the opt condition
				if (fragmentTokens_.containsKey(node.getFragment())) {

					Token t = (Token) fragmentTokens_.get(node.getFragment());

					// The nodes inside the opt fragment are not executed and
					// the message's precedence is not satisfied.
					if (t == null) {

						setArea_(cf, node, true, round);
						return false;
					}

					// There is no token in the opt input, the nodes inside the
					// opt fragment are not executed and the
					// message's precedence is not satisfied.
				} else {

					setArea_(cf, node, true, round);
					return false;
				}

				// The message is inside a loop combined fragment
			} else if ((cf.getOp()).equals("loop")) {

				// Checking the loop condition
				if (fragmentTokens_.containsKey(node.getFragment())) {

					Token t = (Token) fragmentTokens_.get(node.getFragment());

					// Loop condition is false, so no more looping and message's
					// precedence is not satisfied.
					if ((t.toString()).equals("\"false\"")) {

						setArea_(cf, node, true, round);
						return false;

						// The loop should be executed, setting the first loop
						// node to be the node in question.
					} else {

						firstLoopNode_ = node;
					}

					// There is no condition for the loop, so the loop is not
					// executed and the
					// message's precedence is not satisfied.
				} else {

					setArea_(cf, node, true, round);
					return false;
				}

				// The node is inside a parallel combined fragment
				// Setting the number of nodes inside the fragment in order to
				// ease the checking whether
				// all the parallel nodes have been fired.
			} else if ((cf.getOp()).equals("par")) {

				parallel_ = true;
				parallelNodes_ = numberOfParallel_(node);
			}

			// Message's precedence is satisfied, node is set fired and the
			// precedence graph is in the busy
			// state until the message has been delivered.
			// Current combined fragment is the one the node in question is in
			// (or null, if it's not inside any combined fragment).
			busy_ = true;
			node.setFired(true, round);
			cf_ = cf;
		}

		return true;
	}

	/**
	 * This method creates the message precedence graph. It adds all nodes (1
	 * message in sequence diagram corresponds 1 node) This method also adds
	 * edges between the nodes (by calling the addEdges-method).
	 * 
	 * @param lifelines
	 * @param messages
	 * @param fragments
	 */
	private void createGraph_(List lifelines, List messages, List fragments) {

		// Adds a node for each message in the graph
		for (int i = 0; i < messages.size(); i++) {

			PrecedenceGraphNode node = (PrecedenceGraphNode) graph_.addNode();

			// Each node contains one message
			node.setMessage((Message) messages.get(i));

			// If a message is inside a combined fragment, the fragment is set
			// for the node containing the message
			for (int j = 0; j < fragments.size(); j++) {

				// Checking whether the message is inside a combined fragment or
				// not.
				// FIXME: A really inefficient way to do the checking!!!
				// Indrusiak: please try to live with that for now. ;-)
				if (ifFragmentMessage((Message) messages.get(i),
						(CombinedFragment) fragments.get(j))) {

					// If the message is inside a combined fragment, setting the
					// fragment for the node containing the message
					node.setFragment((CombinedFragment) fragments.get(j));

					j = (fragments.size() + 1);
				}
			}
		}
		// Adds edges for the nodes
		addEdges_(lifelines);
	}

	/**
	 * This method adds edges for the nodes. Pretty much like the method in
	 * TotalOrderPrecedenceGraph, except every time an edge will be created the
	 * method checks that the edge does not exist between the two nodes already.
	 * The table below indicates in which situations an edge between the nodes p
	 * and c is created.
	 * 
	 * Line | p | c | Edge | Where | cf1 | cf2 |
	 * ----------------------------------------------------------- 1 | 00 | 00 |
	 * PRECEDENCE GRAPH IS EMTPY | X | X | 2 | 00 | 01 | No | | 0 | 0 | 3 | 00 |
	 * 10 | No | | 0->1 | 0 | 4 | 00 | 11 | NEVER CAN HAPPEN | X | X |
	 * ----------------------------------------------------------- 5 | 01 | 00 |
	 * No | | 0 | 0 | 6 | 01 | 01 | Yes | p->c | 0 | 0 | 7 | 01 | 10 | No | |
	 * 0->1 | 0 | 8 | 01 | 11 | NEVER CAN HAPPEN | X | X |
	 * ----------------------------------------------------------- 9 | 10 | 00 |
	 * Yes | ptp->All(T1) | 1->0 | 0 | 10 | 10 | 01 | Yes | ptp->All(T1)->c |
	 * 1->0 | 0 | 11 | 10 | 10 | No | | 1 | 0 | 12 | 10 | 11 | Yes |
	 * ptp->All(T1) | 1 | 0->1 |
	 * ----------------------------------------------------------- 13 | 11 | 00
	 * | Yes | All(T1)->All(T2) | 1->0 | 1->0 | 14 | 11 | 01 | Yes |
	 * All(T1)->All(T2)->c | 1->0 | 1->0 | 15 | 11 | 10 | No | | 1 | 1 | 16 | 11
	 * | 11 | Yes | All(H1)->All(H2) | 1 | 1 |
	 * ----------------------------------------------------------- p = previous
	 * node c = current node 00 = null (no node) (p == 00 means, that the
	 * current node c is the first one in the graph) 01 = normal node (i.e. not
	 * inside a parallel combined fragment) 10 = parallel node 11 = parallel
	 * node, but inside a different par fragment than the previous node -> = an
	 * edge between nodes (in cf1 and cf2 0->1 means a change in the parameter
	 * value) ptp = previous to parallel (i.e. the last normal node before a
	 * parallel combined fragment) T1 = TreeSet, where all the nodes inside a
	 * first parallel combined fragment are stored T2 = TreeSet, where all the
	 * nodes inside a second parallel combined fragment (adjacent to the first
	 * one) are stored E.g. All(H1)->All(H2) means that edges are created from
	 * all nodes in the H1 to all nodes in the H2. cf1 = first (parallel)
	 * fragment cf2 = second (parallel) fragment X = don't care
	 */
	private void addEdges_(List lifelines) {

		for (int i = 0; i < lifelines.size(); i++) {

			TreeSet messages = (TreeSet) lifelineMessages_
					.get(lifelines.get(i));
			Iterator it = messages.iterator();

			PrecedenceGraphNode previousNode = null;
			PrecedenceGraphNode currentNode = null;
			PrecedenceGraphNode previousToParallel = null;

			CombinedFragment firstFragment = null;
			CombinedFragment secondFragment = null;

			TreeSet parallelFirst = new TreeSet();
			TreeSet parallelSecond = new TreeSet();

			String op = null;

			// Going through all the nodes in the graph.
			while (it.hasNext()) {

				currentNode = findNode_((Message) it.next());

				// Checking if the current node is inside a combined fragment
				// No other combined fragments matter in the edge creation than
				// parallel fragments
				if ((CombinedFragment) currentNode.getFragment() != null) {

					op = ((CombinedFragment) currentNode.getFragment()).getOp();
				}

				// Lines 2, 3, 5, 6, 7: First fragment is null, so the previous
				// node is either a normal node or null
				if (firstFragment == null) {

					// Lines 3 and 7: no edge created, but setting the fragment
					// variables and the previousToParallel node for later use.
					if (op != null && op.equals("par")) {

						firstFragment = (CombinedFragment) currentNode
								.getFragment();
						parallelFirst.add(currentNode.getMessage());
						previousToParallel = previousNode;

						// Lines 2, 5, and 6: nothing to be done in case of
						// lines 2 or 5.
					} else {

						// Line 6: Edge created, previous and current nodes are
						// normal, not null, not inside a parallel combined
						// fragment
						if (previousNode != null) {

							if (!graph_.edgeExists(previousNode, currentNode)) {

								graph_.addEdge(previousNode, currentNode);
							}
						}
					}

					// Previous node is inside a par fragment
					// Lines 10, 11, and 12
				} else if (firstFragment != null && secondFragment == null) {

					if (op != null && op.equals("par")) {

						CombinedFragment fragment = (CombinedFragment) currentNode
								.getFragment();

						// Line, 11: Current node is inside the same parallel
						// fragment than the previous node
						if (firstFragment == fragment) {

							// Adding the node in the TreeSet holding the
							// parallel nodes, no edges created.
							parallelFirst.add(currentNode.getMessage());

							// Line 12: Current node is inside another parallel
							// fragment than the previous node
						} else {

							// If the previous to parallel node is not null, it
							// means that an edge is created from the ptp
							// node to all nodes in the first TreeSet.
							if (previousToParallel != null) {

								Iterator iter = parallelFirst.iterator();

								while (iter.hasNext()) {

									Message m = (Message) iter.next();
									PrecedenceGraphNode node = findNode_(m);

									if (!graph_.edgeExists(previousToParallel,
											node)) {

										graph_.addEdge(previousToParallel, node);

									}
								}
							}

							// Adding the current node inside the second TreeSet
							secondFragment = fragment;
							parallelSecond.add(currentNode.getMessage());

						}

						// Line 10: Next to parallel is not inside a par
						// combined fragment
					} else {

						firstFragment = null;

						Iterator iter = parallelFirst.iterator();

						while (iter.hasNext()) {

							Message m = (Message) iter.next();
							PrecedenceGraphNode node = findNode_(m);

							if (previousToParallel != null) {

								if (!graph_
										.edgeExists(previousToParallel, node)) {
									graph_.addEdge(previousToParallel, node);

								}
							}

							if (!graph_.edgeExists(node, currentNode)) {

								graph_.addEdge(node, currentNode);
							}
						}

						parallelFirst.clear();
					}

					// Lines 14, 15, and 16
				} else if (secondFragment != null) {

					if (op != null && op.equals("par")) {

						CombinedFragment fragment = (CombinedFragment) currentNode
								.getFragment();

						// Line 15: Current node is inside the same parallel
						// fragment than the previous node
						if (secondFragment == fragment) {

							// Adding the node in the second TreeSet holding the
							// parallel nodes, no edges created.
							parallelSecond.add(currentNode.getMessage());

							// Line 16: The current node is inside a 3rd
							// adjacent parallel combined fragment
						} else {

							Iterator iter1 = parallelFirst.iterator();

							while (iter1.hasNext()) {

								Message m1 = (Message) iter1.next();
								PrecedenceGraphNode node1 = findNode_(m1);

								Iterator iter2 = parallelSecond.iterator();

								// Creating edges from the nodes in the first
								// treeset to all of the nodes in the second
								// treeset
								while (iter2.hasNext()) {

									Message m2 = (Message) iter2.next();
									PrecedenceGraphNode node2 = findNode_(m2);

									if (!graph_.edgeExists(node1, node2)) {

										graph_.addEdge(node1, node2);
									}
								}
							}

							// The nodes in the first treeset are not needed any
							// more, clearing the set
							parallelFirst.clear();

							Iterator iter = parallelSecond.iterator();

							// Putting the nodes in the second treeset into the
							// first treeset, so that the nodes belonging
							// to the 3rd adjacent parallel fragment can be
							// placed into the second treeset.
							while (iter.hasNext()) {

								Message m = (Message) iter.next();
								parallelFirst.add(m);
							}

							parallelSecond.clear();

							parallelSecond.add(currentNode.getMessage());
							firstFragment = secondFragment;
							secondFragment = fragment;
						}

						// Line 14: Next to parallel is not inside a parallel
						// combined fragment
					} else {

						firstFragment = null;
						secondFragment = null;

						Iterator iter1 = parallelSecond.iterator();

						// Creating an edge from all the nodes in the second
						// tree set to the current node.
						while (iter1.hasNext()) {

							Message m1 = (Message) iter1.next();
							PrecedenceGraphNode node1 = findNode_(m1);

							if (!graph_.edgeExists(node1, currentNode)) {

								graph_.addEdge(node1, currentNode);
							}

							Iterator iter2 = parallelFirst.iterator();

							// Creating edges from the nodes in the first
							// treeset to all of the nodes in the second treeset
							while (iter2.hasNext()) {

								Message m2 = (Message) iter2.next();
								PrecedenceGraphNode node2 = findNode_(m2);

								if (!graph_.edgeExists(node2, node1)) {

									graph_.addEdge(node2, node1);
								}
							}
						}

						parallelFirst.clear();
						parallelSecond.clear();
					}

					// This should not happen (and has never happened)
				} else {

					System.out
							.println("BIG PROBLEM! PartialOrderPrecedenceGraph should never come here");
				}

				previousNode = currentNode;
				op = null;

			} // While ends here, no more nodes

			// Line 9: Current node is null, but previous has been inside a
			// parallel fragment
			if (firstFragment != null && secondFragment == null) {

				// Checking that there is a normal node before the parallel
				// fragment
				if (previousToParallel != null) {

					Iterator iter = parallelFirst.iterator();

					// Edge from ptp to all the nodes inside the first TreeSet
					while (iter.hasNext()) {

						Message m = (Message) iter.next();
						PrecedenceGraphNode node = findNode_(m);

						if (!graph_.edgeExists(previousToParallel, node)) {

							graph_.addEdge(previousToParallel, node);
						}
					}
				}

				// Line 13: Current node is null, but previous has been inside a
				// parallel fragment
			} else if (secondFragment != null) {

				Iterator iter1 = parallelFirst.iterator();

				// Edge from all nodes from the first TreeSet to all nodes in
				// the second TreeSet
				while (iter1.hasNext()) {

					Message m1 = (Message) iter1.next();
					PrecedenceGraphNode node1 = findNode_(m1);

					Iterator iter2 = parallelSecond.iterator();

					while (iter2.hasNext()) {

						Message m2 = (Message) iter2.next();
						PrecedenceGraphNode node2 = findNode_(m2);

						if (!graph_.edgeExists(node1, node2)) {

							graph_.addEdge(node1, node2);
						}
					}
				}
			}
		}
	}

	/**
	 * Adding all lifelines to a hashtable along with an empty treeset, where
	 * assignMessageToLifeline method adds all the lifelines messages to the
	 * treeset.
	 * 
	 * @param lifelines
	 */
	private void addLifelines_(List lifelines) {

		Iterator it = lifelines.iterator();

		while (it.hasNext()) {

			Lifeline ll = (Lifeline) it.next();
			lifelineMessages_.put(ll, new TreeSet());
		}
	}

	/**
	 * Assigning messages to both the sending and receiving lifeline in order to
	 * check the message's precedence.
	 * 
	 * @param messages
	 */
	private void addMessages_(List messages) {

		Iterator it = messages.iterator();

		while (it.hasNext()) {

			Message m = (Message) it.next();
			MessageOccurrenceSpecification send = m.getSendEvent();
			MessageOccurrenceSpecification receive = m.getReceiveEvent();
			assignMessageToLifeline_(send, m);
			assignMessageToLifeline_(receive, m);
		}
	}

	/**
	 * Adding the message into the treeset of each lifeline.
	 * 
	 * @param mos
	 * @param m
	 */
	private void assignMessageToLifeline_(MessageOccurrenceSpecification mos,
			Message m) {

		Lifeline l = mos.getLifeline();
		TreeSet ts = (TreeSet) lifelineMessages_.get(l);
		ts.add(m);
	}

	private Hashtable lifelineMessages_;
	private boolean busy_;
	private boolean parallel_;
	private CombinedFragment cf_;
	private PrecedenceGraphNode firstLoopNode_;
	private int parallelNodes_;

}