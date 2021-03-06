package ptolemy.vergil.uml;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * This class is an implementation of the class MessageEnd specified in the UML
 * Superstructure specification within the PtolemyII environment.
 * 
 * It is derived from Parameter, so that information can be made persistant. The
 * main purpose of this class is to keep consistency with the UML specification,
 * where derived classes from MessageEnd specify what happens at the end of a
 * message.
 * 
 * This class is the starting point used in this implementation, this means that
 * the whole underlying hierarchy that is given by the UML specification is
 * simplified here. For example, NamedElement that is the super class of
 * MessageEnd is not implemented here.
 * 
 * @author Andreas Thuy
 * 
 *         last review: 28.08.06
 */
public class MessageEnd extends Parameter {

	// this is the only constructor that is in use => nothing with workspace...
	public MessageEnd(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		// TODO Auto-generated constructor stub
	}

	public MessageEnd() {
		super();
		// TODO Auto-generated constructor stub
	}

}
