package lsi.noc.argus;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import lsi.noc.nocscope2.BufferScopeActor;
import lsi.noc.nocscope2.BufferSwitch;
import lsi.noc.nocscope2.EndToEndScopeActor;
import lsi.noc.nocscope2.HotSpotScope;
import lsi.noc.nocscope2.HotSpotScopeActor;
import lsi.noc.nocscope2.InputScopeActor;
import lsi.noc.nocscope2.OutputScopeActor;
import lsi.noc.nocscope2.PointToPointScopeActor;
import lsi.noc.nocscope2.PowerScopeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.*;
import ptolemy.kernel.util.ChangeRequest;

/**
 * 
 * Transaction-Level Model of Server. <br>
 * Receive source and target addresses from routers and compute which are being
 * used. <br>
 * Then, it optimizes the system according with this information by
 * reconfiguring the source-target pairs near to each other. <br>
 * Before triggering the reconfiguration the system must: end all current
 * communications and the producers must wait.<br>
 * After that, server must send the new routing table to the producers and
 * reactivate the producers.<br>
 * <br>
 * 
 * Copyright (c) 2008 - All rights reserved. <br>
 * 
 * @author Leandro Möller
 * @author Leandro Soares Indrusiak
 * @version Argus (Darmstadt, 29.08.2008)
 */
public class TLMServer extends TypedAtomicActor {

	private double timeInterval = 50;
	private NamedObj container;
	private HotSpotScopeActor hotspotScopeActor = null; // pointer to the
														// HotSpotScopeActor
	private EndToEndScopeActor endToEndScopeActor = null; // pointer to the
															// PointToPointScopeActor

	public TLMServer(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		this.container = container;
	}

	/**
	 * Method called once by the director when simulation starts; responsible to
	 * initialize variables, state machines and scopes.
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();

		// get the top level workspace
		// container = getContainer().getContainer();
		// get a list of actors in the top level workspace
		List l1 = ((CompositeEntity) container)
				.entityList(TypedCompositeActor.class);
		// scanning list of actors in the workspace
		for (int i = 0; i < l1.size(); i++) {
			// verify if it is the desired actor
			if (l1.get(i).toString().indexOf("NoCScope") != -1) {
				// get the NoCScope actor
				TypedCompositeActor nocscope = (TypedCompositeActor) l1.get(i);
				// get a list of actors in the NoCScope workspace
				List l2 = nocscope.deepEntityList();
				// scanning list of actors in the workspace
				for (int j = 0; j < l2.size(); j++) {
					// verify if it is the desired actor
					if (l2.get(j).toString().indexOf("HotSpotScopeActor") != -1) {
						// get the HotSpotScopeActor
						hotspotScopeActor = (HotSpotScopeActor) l2.get(j);
					}
					// verify if it is the desired actor
					else if (l2.get(j).toString().indexOf("EndToEndScopeActor") != -1) {
						// get the EndToEndScopeActor
						endToEndScopeActor = (EndToEndScopeActor) l2.get(j);
					}
				}
			}
		}
		System.out.println("inicializou server");

		// requests a "wake-up" from the director for the time it finishes
		// initializing
		getDirector().fireAt(this,
				getDirector().getModelTime().add(timeInterval));
	}

	/**
	 * Method called before every fire.
	 */
	public boolean prefire() throws IllegalActionException {
		if (_debugging)
			_debug("====================== Time = "
					+ getDirector().getModelTime() + " =======================");

		return super.prefire();
	}

	/**
	 * Implements the server functionality: <br>
	 */
	public void fire() throws IllegalActionException {
		super.fire();

		// get the XY address of the router that tried to send more packets and
		// was denied
		int bigX = hotspotScopeActor.getBiggestGlobalX();
		int bigY = hotspotScopeActor.getBiggestGlobalY();
		// get the number of attempts
		int bigValue = hotspotScopeActor.getSwitch(bigX, bigY).getBigValue();
		// get the port of this router that tried to send these packets
		int bigPort = hotspotScopeActor.getSwitch(bigX, bigY).getBigPort();

		Vector vector[][] = endToEndScopeActor.getSwitch(bigX, bigY)
				.getAllConnections();

		System.out.println("" + bigX + "" + bigY + " port=" + bigPort
				+ " value=" + bigValue);

		// check all targets
		for (int x = 0; x < vector.length; x++) {
			for (int y = 0; y < vector[x].length; y++) {
				if (vector[x][y].size() != 0) {
					System.out.println("  ->" + x + "" + y + " = "
							+ vector[x][y].size());
					if (vector[x][y].size() >= 10) {
						int distX = Math.abs(bigX - x);
						int distY = Math.abs(bigY - y);
						int distance = distX + distY;
						if (distance > 2)
							System.out
									.println("  Trigger reconfiguration, distance="
											+ distance);
					}
				}
			}
		}

		// requests a "wake-up" from the director for the time it finishes
		// initializing
		getDirector().fireAt(this,
				getDirector().getModelTime().add(timeInterval));

	}

	/**
	 * Last method called before finishing the simulation
	 */
	public void wrapup() throws IllegalActionException {
		super.wrapup();
	}

}
