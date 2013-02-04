//Copyright (c) 2007 Luciano Ost
//All rights reserved.
//Version 1.0	Porto Alegre, 17/09/2008.

package lsi.noc.nocscope;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.data.type.Type;
import ptolemy.data.type.RecordType;

public class RouterScope extends TypedAtomicActor {

	private String[] labels;
	private Token[] values;
	private double e, w, n, s, l;

	public TypedIOPort inputN, inputS, inputE, inputW, inputL, // receives
																// inputs from
																// buffer "size"
			output;

	protected RecordToken record;

	public RouterScope(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		inputE = new TypedIOPort(this, "inputE", true, false);
		inputW = new TypedIOPort(this, "inputW", true, false);
		inputN = new TypedIOPort(this, "inputN", true, false);
		inputS = new TypedIOPort(this, "inputS", true, false);
		inputL = new TypedIOPort(this, "inputL", true, false);

		output = new TypedIOPort(this, "output", false, true);

		// set the type constraints.
		labels = new String[5];
		Type[] types = new Type[5];

		labels[0] = "e";
		labels[1] = "w";
		labels[2] = "n";
		labels[3] = "s";
		labels[4] = "l";

		types[0] = BaseType.DOUBLE;
		types[1] = BaseType.DOUBLE;
		types[2] = BaseType.DOUBLE;
		types[3] = BaseType.DOUBLE;
		types[4] = BaseType.DOUBLE;

		RecordType declaredType = new RecordType(labels, types);
		output.setTypeEquals(declaredType);
	}

	// **********
	public void fire() throws IllegalActionException {
		super.fire();

		values = new Token[5];

		if (inputE.hasToken(0)) {
			e = ((DoubleToken) inputE.get(0)).doubleValue();
		}
		if (inputW.hasToken(0)) {
			w = ((DoubleToken) inputW.get(0)).doubleValue();
		}
		if (inputN.hasToken(0)) {
			n = ((DoubleToken) inputN.get(0)).doubleValue();
		}
		if (inputS.hasToken(0)) {
			s = ((DoubleToken) inputS.get(0)).doubleValue();
		}
		if (inputL.hasToken(0)) {
			l = ((DoubleToken) inputL.get(0)).doubleValue();
		}

		values[0] = new DoubleToken(e);
		values[1] = new DoubleToken(w);
		values[2] = new DoubleToken(n);
		values[3] = new DoubleToken(s);
		values[4] = new DoubleToken(l);

		output.send(0, new RecordToken(labels, values));
	}
}
