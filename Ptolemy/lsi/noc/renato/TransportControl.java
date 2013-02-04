// Copyright (c) 2007 Leandro Soares Indrusiak
// All rights reserved.
// Darmstadt, 30/05/2007.

package lsi.noc.renato;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.IntToken;

/**
 * Controls the proper handling of packet header and payload.
 * 
 * @author Leandro Indrusiak
 */

public class TransportControl extends TypedAtomicActor {

	public TransportControl(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		inputE = new TypedIOPort(this, "inputE", true, false);
		inputW = new TypedIOPort(this, "inputW", true, false);
		inputN = new TypedIOPort(this, "inputN", true, false);
		inputS = new TypedIOPort(this, "inputS", true, false);
		inputL = new TypedIOPort(this, "inputL", true, false);
		inputDirection = new TypedIOPort(this, "inputDirection", true, false);
		outputRouter = new TypedIOPort(this, "outputRouter", false, true);
		outputSwitchMatrix = new TypedIOPort(this, "outputSwitchMatrix", false,
				true);
		outputDirection = new TypedIOPort(this, "outputDirection", false, true);
		sizeN = 0;
		sizeS = 0;
		sizeE = 0;
		sizeW = 0;
		sizeL = 0;

	}

	// /////////////////////////////////////////////////////////////////
	// // ports and parameters ///

	public TypedIOPort inputN, inputS, inputE, inputW, inputL, inputDirection,
			outputDirection, outputSwitchMatrix, outputRouter;
	public IntToken directionN, directionS, directionE, directionW, directionL,
			currDirection;
	public int sizeN, sizeS, sizeE, sizeW, sizeL;

	// /////////////////////////////////////////////////////////////////
	// // public methods ///

	/**
	 * Reads a header, stores the packet size and forwards the header to the
	 * router. The direction calculated by the router is stored accordingly.
	 * Sends all remaining flits of the package directly to the switch matrix
	 * using the stored direction for each input buffer.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */

	public void fire() throws IllegalActionException {

		RecordToken record;
		if (inputE.hasToken(0)) {
			if (sizeE == 0) {
				record = (RecordToken) inputE.get(0);
				sizeE = ((IntToken) record.get("size")).intValue();
				currDirection = directionE;
			} else {
				sizeE--;
				outputSwitchMatrix.send(0, inputE.get(0));
				outputDirection.send(0, directionE);
			}
		} else if (inputW.hasToken(0)) {
			if (sizeW == 0) {
				record = (RecordToken) inputW.get(0);
				sizeW = ((IntToken) record.get("size")).intValue();
				currDirection = directionW;
			} else {
				sizeW--;
				outputSwitchMatrix.send(0, inputW.get(0));
				outputDirection.send(0, directionW);
			}
		} else if (inputN.hasToken(0)) {
			if (sizeN == 0) {
				record = (RecordToken) inputN.get(0);
				sizeN = ((IntToken) record.get("size")).intValue();
				currDirection = directionN;
			} else {
				sizeN--;
				outputSwitchMatrix.send(0, inputN.get(0));
				outputDirection.send(0, directionN);
			}
		} else if (inputS.hasToken(0)) {
			if (sizeS == 0) {
				record = (RecordToken) inputS.get(0);
				sizeS = ((IntToken) record.get("size")).intValue();
				currDirection = directionS;
			} else {
				sizeS--;
				outputSwitchMatrix.send(0, inputS.get(0));
				outputDirection.send(0, directionS);
			}
		} else if (inputL.hasToken(0)) {
			if (sizeL == 0) {
				record = (RecordToken) inputL.get(0);
				sizeL = ((IntToken) record.get("size")).intValue();
				currDirection = directionL;
			} else {
				sizeL--;
				outputSwitchMatrix.send(0, inputL.get(0));
				outputDirection.send(0, directionL);
			}
		}

		else if (inputDirection.hasToken(0)) {
			currDirection = (IntToken) inputDirection.get(0);

		}

	}

}
