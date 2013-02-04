package ptolemy.domains.uml.kernel;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.List;
import java.util.Vector;

import ptolemy.vergil.uml.Lifeline;
import ptolemy.vergil.uml.Message;
import ptolemy.vergil.uml.MessageOccurrenceSpecification;
import ptolemy.vergil.uml.UMLSeqActor;

/**
 * @author Leandro Soares Indrusiak
 * @version $Id: SDPrecedenceGraph.java,v 1.0 2007/04/03
 */

public class SDLinearizedPrecedenceGraph extends SDPrecedenceGraph {

	TreeSet _messages;
	Message _next;

	public SDLinearizedPrecedenceGraph(List messages) {

		_messages = new TreeSet(messages);
		_next = (Message) _messages.first();

	}

	public boolean precedenceSatisfied(Message m) {

		if (m == _next)
			return true;
		return false;

	}

	public void notifyMessageFiring(Message m) {

		Iterator i = _messages.tailSet(m).iterator();
		i.next();
		if (i.hasNext()) {
			_next = (Message) i.next();
		} else {
			_next = (Message) _messages.first();
		}

	}

	@Override
	public void setFragmentTokens(Hashtable fragmentTokens) {
		// TODO Auto-generated method stub

	}
}
