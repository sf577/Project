package ptolemy.vergil.uml;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.NamedObjNodeModel;
import diva.graph.modular.EdgeModel;
import diva.graph.modular.MutableEdgeModel;
import diva.graph.modular.NodeModel;

/**
 * This graph model is intended for use with the UML Sequence Diagram editor
 * within PtolemyII. The root of this graph is a TypedCompositeActor, to be more
 * specific, a UMLSeqActor.
 * 
 * The nodes of this graph are all Attributes.
 * 
 * A UMLGraphModel offers a NodeModel that contains the Lifelines of a Sequence
 * Diagram and an EdgeModel that contains Messages that are sent between
 * Lifelines. It should also be noted that Lifelines and Messages (nodes and
 * edges, respectively) are both derived from an Attribute-class.
 * 
 * @author Andreas Thuy
 * 
 *         last review: 09.09.06
 */
public class UMLGraphModel extends AbstractBasicGraphModel {
	// TODO see, if everything needed for an appropriate update of the visual
	// part has been done => _update()...
	public class LifelineModel extends NamedObjNodeModel {

		public LifelineModel() {
			super();
		}

		public String getDeleteNodeMoML(Object node) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see diva.graph.modular.NodeModel#getParent(java.lang.Object)
		 * 
		 * return an Interaction, that contains the Lifeline node
		 */
		public Object getParent(Object node) {
			if (node instanceof Lifeline) {
				return ((Lifeline) node).getContainer();
			}
			if (node instanceof Location) {
				if (((Location) node).getContainer() instanceof Lifeline) {
					return (((Location) node).getContainer().getContainer());
				}
			}
			// only Lifelines are allowed to be a node in this model!
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see diva.graph.modular.NodeModel#inEdges(java.lang.Object)
		 * 
		 * return all the messages that are sent to the given node (node must be
		 * a Lifeline)
		 */
		public Iterator inEdges(Object node) {
			List receivedMsgs = new Vector();
			Lifeline lifelineNode = null;
			if (node instanceof Location) {
				if (((Location) node).getContainer() instanceof Lifeline) {
					lifelineNode = (Lifeline) ((Location) node).getContainer();
				}
			} else if (node instanceof Lifeline) {
				lifelineNode = (Lifeline) node;
			}

			if (lifelineNode != null) {
				String lifelineName = lifelineNode.getRepresents();
				UMLSeqActor chief = (UMLSeqActor) lifelineNode.getContainer();
				if (chief != null) {
					Iterator msgs = chief.attributeList(Message.class)
							.iterator();
					while (msgs.hasNext()) {
						Message msg = (Message) msgs.next();
						try {
							MessageOccurrenceSpecification receiveEvent = (MessageOccurrenceSpecification) msg
									.getAttribute(
											"receiveEvent",
											MessageOccurrenceSpecification.class);
							if (receiveEvent != null) {
								if (receiveEvent.getLifeline() != null) {
									if (receiveEvent.getLifeline()
											.getRepresents()
											.equals(lifelineName)) {
										receivedMsgs.add(msg);
									}
								}
							}
						} catch (IllegalActionException iAE) {
							iAE.printStackTrace();
						}
					}
				}
			}
			return receivedMsgs.iterator();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see diva.graph.modular.NodeModel#outEdges(java.lang.Object)
		 * 
		 * return all the Messages that are sent from Lifeline node
		 */
		public Iterator outEdges(Object node) {
			List sentMsgs = new Vector();
			Lifeline lifelineNode = null;
			if (node instanceof Location) {
				if (((Location) node).getContainer() instanceof Lifeline) {
					lifelineNode = (Lifeline) ((Location) node).getContainer();
				}
			} else if (node instanceof Lifeline) {
				lifelineNode = (Lifeline) node;
			}

			if (lifelineNode != null) {
				lifelineNode = (Lifeline) ((Location) node).getContainer();
				String lifelineName = lifelineNode.getRepresents();
				UMLSeqActor chief = (UMLSeqActor) lifelineNode.getContainer();
				if (chief != null) {
					Iterator msgs = chief.attributeList(Message.class)
							.iterator();
					while (msgs.hasNext()) {
						Message msg = (Message) msgs.next();
						try {
							MessageOccurrenceSpecification sendEvent = (MessageOccurrenceSpecification) msg
									.getAttribute(
											"sendEvent",
											MessageOccurrenceSpecification.class);
							if (sendEvent != null) {
								if (sendEvent.getLifeline() != null) {
									if (sendEvent.getLifeline().getRepresents()
											.equals(lifelineName)) {
										sentMsgs.add(msg);
									}
								}
							}
						} catch (IllegalActionException iAE) {
							iAE.printStackTrace();
						}
					}
				}
			}
			return sentMsgs.iterator();
		}

		public void removeNode(Object eventSource, Object node) {
			// TODO Auto-generated method stub

		}
	}

	public class MessageModel implements MutableEdgeModel {

		public MessageModel() {
			super();
		}

		// I think acceptHead and acceptTail are obsolete, because this check
		// up is already done in the controller, where an appropriate target
		// must be found to create a Message...
		// Of course, it might be a good idea to have an additional test on
		// the model-side to ensure a consistent model structure. However,
		// this point is neglected here... TIME! => it is expected that the
		// EdgeController is a MessageController with a MessageTarget

		// always returns false if someone calls this method...
		public boolean acceptHead(Object edge, Object head) {
			return false;
		}

		// always returns false if someone calls this method...
		public boolean acceptTail(Object edge, Object tail) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see diva.graph.modular.EdgeModel#getHead(java.lang.Object)
		 * 
		 * return the MsgOccSpec of this Message at its head you can get the
		 * Lifeline name that is the HEAD by calling the appropriate method on
		 * the Lifeline
		 */
		public Object getHead(Object edge) {
			if (edge instanceof Message) {
				return ((Message) edge).getReceiveEvent();
			} else if (edge instanceof Location) {
				if (((Location) edge).getContainer() instanceof Message) {
					return (((Message) ((Location) edge).getContainer())
							.getReceiveEvent());
				}
			}
			return null;
		}

		public Iterator getMessages() {
			Iterator msgs = ((NamedObj) getRoot()).attributeList(Message.class)
					.iterator();
			return msgs;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see diva.graph.modular.EdgeModel#getTail(java.lang.Object)
		 * 
		 * return the MsgOccSpec of this Message at its tail you can get the
		 * Lifeline name that is the TAIL by calling the appropriate method on
		 * the Lifeline
		 */
		public Object getTail(Object edge) {
			if (edge instanceof Message) {
				return ((Message) edge).getSendEvent();
			} else if (edge instanceof Location) {
				if (((Location) edge).getContainer() instanceof Message) {
					return (((Message) ((Location) edge).getContainer())
							.getSendEvent());
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see diva.graph.modular.EdgeModel#isDirected(java.lang.Object)
		 * 
		 * the only edges we have are Messages, and they are always directed
		 */
		public boolean isDirected(Object edge) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see diva.graph.modular.MutableEdgeModel#setHead(java.lang.Object,
		 * java.lang.Object)
		 * 
		 * This method isn't useful in this context. This is because, Messages
		 * are only created if they are connected properly between two
		 * Lifelines. It doesn't make sense to disconnect a Message from one
		 * Lifeline and connect it to another instead. There are only limited
		 * situations where this is appropriate, therefore this feature is not
		 * supported in general.
		 * 
		 * Maybe it would be a good idea to divide the creation in two parts
		 * where these methods here do the work with the model, but that is only
		 * supposed for future releases :)
		 */
		public void setHead(Object edge, Object head) {
			// this part is done in UMLGraphController.
			// MessageCreator.mousePressed(...)
		}

		public void setTail(Object edge, Object tail) {
			// this part is done in UMLGraphController.
			// MessageCreator.mousePressed(...)
		}
	} // end of MessageModel

	// Models for nodes and edges within this model
	LifelineModel _lifelineModel = new LifelineModel();
	MessageModel _messageModel = new MessageModel();

	/**
	 * Create a new graph model with composite as its root (root is an
	 * Attribute). In this implementation it must be a UMLSeqActor
	 * 
	 * @param composite
	 *            - root of the model: only Attributes can be the root
	 */
	public UMLGraphModel(NamedObj composite) {
		super(composite);
	}

	// nothing is done here because, Messages cannot be disconnected.
	// in this implementation they can only be created and deleted
	public void disconnectEdge(Object eventSource, Object edge) {
	}

	public String getDeleteEdgeMoML(Object edge) {
		return getDeleteMoML(edge);
	}

	// see also AttributeNodeModel.getDeleteNodeMoML(...)
	protected String getDeleteMoML(Object toBeDeleted) {
		// toBeDeleted is eiter a Location contained by a Lifeline or a Message
		NamedObj attribute = null;
		if (toBeDeleted instanceof Location) {
			attribute = ((Locatable) toBeDeleted).getContainer();
		} else if (toBeDeleted instanceof CombinedFragment) {
			attribute = (CombinedFragment) toBeDeleted;
		} else if (toBeDeleted instanceof Lifeline) {
			attribute = (Lifeline) toBeDeleted;
		} else if (toBeDeleted instanceof Message) {
			attribute = (Message) toBeDeleted;
		}

		if (attribute != null) {
			// there are only properties in this model...
			return "<deleteProperty name=\"" + attribute.getName() + "\"/>\n";
		}
		return null;
	}

	public String getDeleteNodeMoML(Object node) {
		return getDeleteMoML(node);
	}

	public EdgeModel getEdgeModel(Object edge) {
		if (edge instanceof Message) {
			return this._messageModel;
		} else if (edge instanceof Location) {
			if (((Location) edge).getContainer() instanceof Message) {
				return this._messageModel;
			}
		}
		return null;
	}

	public LifelineModel getLifelineModel() {
		return this._lifelineModel;
	}

	public MessageModel getMessageModel() {
		return this._messageModel;
	}

	// there is only a LifelineModel and no model for CombinedFragments at the
	// moment... maybe this should be changed
	public NodeModel getNodeModel(Object node) {
		if (node instanceof Lifeline) {
			return this._lifelineModel;
		} else if (node instanceof Location) {
			if (((Location) node).getContainer() instanceof Lifeline) {
				return this._lifelineModel;
			}
		}
		return null;
	}

	@Override
	public Object getSemanticObject(Object element) {
		if (element instanceof Locatable) {
			return ((Locatable) element).getContainer();
		} else if (element instanceof Message) {
			return element;
		} else if (element instanceof Lifeline) {
			return element;
		} else if (element instanceof CombinedFragment) {
			return element;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diva.graph.GraphModel#isEdge(java.lang.Object)
	 * 
	 * In this model, Messages are edges.
	 */
	public boolean isEdge(Object edge) {
		if (edge instanceof Message) {
			return true;
		} else if (edge instanceof Location) {
			if (((Location) edge).getContainer() instanceof Message) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diva.graph.GraphModel#isNode(java.lang.Object)
	 * 
	 * In this model, Lifelines and CombinedFragments are nodes.
	 */
	public boolean isNode(Object node) {
		if (node instanceof Lifeline) {
			return true;
		} else if (node instanceof CombinedFragment) {
			return true;
		}
		// this is the normal case, node is a Location, and then we have to
		// see...
		else if (node instanceof Location) {
			if (((Location) node).getContainer() instanceof Lifeline) {
				return true;
			} else if (((Location) node).getContainer() instanceof CombinedFragment) {
				return true;
			}
		}
		return false;
	}

	// deletion is directly handled in the graphical user interface.
	// my getDeleteMoML-method in this class delivers the moml to remove nodes,
	// only the request must be done by the user...
	// therefore this method is seen as unnecessary, TIME, you know ;)
	public void removeNode(Object eventSource, Object node) {
		// TODO Auto-generated method stub
	}
}
