//Copyright (c) 2007 Leandro Soares Indrusiak
//All rights reserved.
//Version 2.0	Darmstadt, 07/12/2007.

package lsi.noc.nocscope;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Iterator;
import javax.swing.JFrame;
import java.util.List;

////NoCScopeActor
/**
 * Base class for all NoCScopeActors. Handles its input ports in a bidimentional
 * array. It contains a concrete Scope, which is the GUI element displaying
 * pertinent information about particular NoC elements. A Scope is a subclass of
 * JPanel and is added to a JFrame, which is displayed permanently from the
 * first initialization of the NoCScopeActor. Thus, visual results are
 * overwritten upon each new simulation.
 * 
 * 
 * @author Leandro Indrusiak
 */

public abstract class NoCScopeActor extends TypedAtomicActor {

	public NoCScopeActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		_frame = new JFrame("NoCScope");

	}

	public void initialize() throws IllegalActionException {

		super.initialize();
		nports = 0;
		maxwidth = 0;

		List l = portList();

		Iterator ite = l.iterator();
		while (ite.hasNext()) {
			IOPort p = (IOPort) ite.next();
			if (p.isInput()) {
				nports++;
				if (p.getWidth() > maxwidth)
					maxwidth = p.getWidth();
			}

		}

		_nocscope = createScope();

		_frame.getContentPane().add(_nocscope);
		_frame.setSize(maxwidth * 100 + 10, nports * 100 + 30);
		_frame.setVisible(true);

	}

	public abstract NoCScope createScope();

	JFrame _frame;
	NoCScope _nocscope;
	int nports;
	int maxwidth;

}
