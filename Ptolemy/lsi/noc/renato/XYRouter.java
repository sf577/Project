/* 
 

Copyright (c) 2004 Leandro Soares Indrusiak, Marcio Kreutz
All rights reserved.
Stockstad am Rhein, 27/11/2004.


 */

package lsi.noc.renato;

import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.Type;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;

//////////////////////////////////////////////////////////////////////////
//// XYRouter
/**
 * Routes tokens which contain a record of two integers
 * 
 * @author Leandro Indrusiak
 */

public class XYRouter extends TypedAtomicActor {

	public XYRouter(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		input = new TypedIOPort(this, "input", true, false);
		outputN = new TypedIOPort(this, "outputN", false, true);
		outputE = new TypedIOPort(this, "outputE", false, true);
		outputS = new TypedIOPort(this, "outputS", false, true);
		outputW = new TypedIOPort(this, "outputW", false, true);
		outputL = new TypedIOPort(this, "outputL", false, true);

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"0\" y=\"0\" width=\"26\" "
				+ "height=\"40\" style=\"fill:red\"/>\n" + "</svg>\n");

		// set the type constraints.
		labels = new String[2];
		Type[] types = new Type[2];

		labels[0] = "x";
		labels[1] = "y";
		types[0] = BaseType.INT;
		types[1] = BaseType.INT;

		RecordType declaredType = new RecordType(labels, types);
		input.setTypeEquals(declaredType);
		outputN.setTypeSameAs(input);
		outputS.setTypeSameAs(input);
		outputE.setTypeSameAs(input);
		outputW.setTypeSameAs(input);
		outputL.setTypeSameAs(input);

	}

	// /////////////////////////////////////////////////////////////////
	// // ports and parameters ////

	public TypedIOPort input, outputN, outputE, outputS, outputW, outputL;

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Read one RecordToken from the input port and send its fields to the
	 * output ports. If the input does not have a token, suspend firing and
	 * return.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */

	public void fire() throws IllegalActionException {

		if (input.hasToken(0)) {
			RecordToken record = (RecordToken) input.get(0);
			int x = ((IntToken) record.get("x")).intValue();
			int y = ((IntToken) record.get("y")).intValue();
			Token[] values = new Token[2];

			if (x > 0) {
				x--;
				values[0] = new IntToken(x);
				values[1] = new IntToken(y);
				RecordToken newrecord = new RecordToken(labels, values);
				outputE.send(0, newrecord);

			} else if (x < 0) {
				x++;
				values[0] = new IntToken(x);
				values[1] = new IntToken(y);
				RecordToken newrecord = new RecordToken(labels, values);
				outputW.send(0, newrecord);

			} else if (y > 0) {
				y--;
				values[0] = new IntToken(x);
				values[1] = new IntToken(y);
				RecordToken newrecord = new RecordToken(labels, values);
				outputS.send(0, newrecord);

			} else if (y < 0) {
				y++;
				values[0] = new IntToken(x);
				values[1] = new IntToken(y);
				RecordToken newrecord = new RecordToken(labels, values);
				outputN.send(0, newrecord);

			} else {

				outputL.send(0, record);

			}
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	private String[] labels;

}
