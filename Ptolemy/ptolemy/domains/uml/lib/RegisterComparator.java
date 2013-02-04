package ptolemy.domains.uml.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.util.ArrayList;

//////////////////////////////////////////////////////////////////////////
////RegisterComparator

/**
 * A slightly different logical equals operator. This operator has one input
 * multiport, one output port that is not a multiport and a register set of the
 * size of the input's width. It will consume at most one token from each input
 * channel per firing and store on a register (eventually overwriting previously
 * stored values). Once the register set is completed, it compares the tokens
 * using the isEqualTo() method of the Token class. If all tokens on the vector
 * are equal, then the output will be a true-valued boolean token. If the vector
 * is not completely full, then no output is produced. The type of the input
 * port is undeclared and will be resolved by the type resolution mechanism.
 * Note that all input channels must resolve to the same type. The type of the
 * output port is boolean.
 * 
 * @see Token#isEqualTo(Token)
 * @author Leandro Soares Indrusiak
 */

public class RegisterComparator extends Transformer {

	/**
	 * Construct an actor in the specified container with the specified name.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this actor within the container.
	 * @exception IllegalActionException
	 *                If the actor cannot be contained by the proposed
	 *                container.
	 * @exception NameDuplicationException
	 *                If the name coincides with an actor already in the
	 *                container.
	 */
	public RegisterComparator(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		input.setMultiport(true);
		output.setTypeEquals(BaseType.BOOLEAN);

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"-30\" y=\"-15\" " + "width=\"60\" height=\"30\" "
				+ "style=\"fill:white\"/>\n" + "<text x=\"-14\" y=\"8\""
				+ "style=\"font-size:24\">==</text>\n" + "</svg>\n");
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Consume at most one token from each input channel and store them on the
	 * state vector, eventually overwriting previously stored values. If the
	 * state vector is complete, output a boolean value - true if all elements
	 * of the state vector are equal (according to the equalTo() operator). If
	 * the input has width 1, then the output is always true. If the input has
	 * width 0, or there are no no input tokens available, then no output is
	 * produced.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public void fire() throws IllegalActionException {
		super.fire();
		BooleanToken result = BooleanToken.FALSE;
		for (int i = 0; i < input.getWidth(); i++) {
			if (input.hasToken(i)) {
				state.set(i, input.get(i));
			}
		}
		System.out.println(state);

		if (areAllThere()) {

			if (areAllEqual()) {
				result = BooleanToken.TRUE;
			} else {
				result = BooleanToken.FALSE;
			}
			output.send(0, result);
			resetState();
		}

	}

	public void initialize() {

		state = new ArrayList(input.getWidth());
		resetState();

	}

	public boolean areAllEqual() throws IllegalActionException {

		for (int i = 0; i < input.getWidth(); i++) {
			Token o = (Token) state.get(i);
			for (int j = 0; j < input.getWidth(); j++) {
				Token u = (Token) state.get(j);
				if (!(u.isEqualTo(o).booleanValue()))
					return false;
			}
		}
		return true;
	}

	public boolean areAllThere() {
		for (int i = 0; i < input.getWidth(); i++) {
			Object o = state.get(i);
			if (o == null)
				return false;
		}
		return true;
	}

	public void resetState() {

		state.clear();
		for (int i = 0; i < input.getWidth(); i++)
			state.add(i, null);

	}

	private ArrayList state;

}
