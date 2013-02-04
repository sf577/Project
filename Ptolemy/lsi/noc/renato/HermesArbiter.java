//Copyright (c) 2007 Leandro Soares Indrusiak
//All rights reserved.
//Version 1.0	Darmstadt, 31/05/2007.
//Version 1.1	Darmstadt, 12/10/2007
//Version 1.2	Darmstadt, 15/10/2007

package lsi.noc.renato;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.expr.Parameter;

////HermesArbiter
/**
 * Arbitrates the access to the router and switch matrix by the 5 input port
 * buffers.
 * 
 * @author Leandro Indrusiak
 */

public class HermesArbiter extends TypedAtomicActor {

	public HermesArbiter(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		x = new Parameter(this, "x");
		x.setTypeEquals(BaseType.INT);

		y = new Parameter(this, "y");
		y.setTypeEquals(BaseType.INT);

		wakeup = new TypedIOPort(this, "wakeup", true, false);

		ackE = new TypedIOPort(this, "ackE", true, false);
		ackW = new TypedIOPort(this, "ackW", true, false);
		ackN = new TypedIOPort(this, "ackN", true, false);
		ackS = new TypedIOPort(this, "ackS", true, false);
		ackL = new TypedIOPort(this, "ackL", true, false);
		ack = new TypedIOPort[5];
		ack[0] = ackE;
		ack[1] = ackW;
		ack[2] = ackN;
		ack[3] = ackS;
		ack[4] = ackL;

		inputreqE = new TypedIOPort(this, "inputreqE", true, false);
		inputreqW = new TypedIOPort(this, "inputreqW", true, false);
		inputreqN = new TypedIOPort(this, "inputreqN", true, false);
		inputreqS = new TypedIOPort(this, "inputreqS", true, false);
		inputreqL = new TypedIOPort(this, "inputreqL", true, false);
		inputreq = new TypedIOPort[5];
		inputreq[0] = inputreqE;
		inputreq[1] = inputreqW;
		inputreq[2] = inputreqN;
		inputreq[3] = inputreqS;
		inputreq[4] = inputreqL;

		peekE = new TypedIOPort(this, "peekE", false, true);
		peekW = new TypedIOPort(this, "peekW", false, true);
		peekN = new TypedIOPort(this, "peekN", false, true);
		peekS = new TypedIOPort(this, "peekS", false, true);
		peekL = new TypedIOPort(this, "peekL", false, true);
		peek = new TypedIOPort[5];
		peek[0] = peekE;
		peek[1] = peekW;
		peek[2] = peekN;
		peek[3] = peekS;
		peek[4] = peekL;

		readE = new TypedIOPort(this, "readE", false, true);
		readW = new TypedIOPort(this, "readW", false, true);
		readN = new TypedIOPort(this, "readN", false, true);
		readS = new TypedIOPort(this, "readS", false, true);
		readL = new TypedIOPort(this, "readL", false, true);
		read = new TypedIOPort[5];
		read[0] = readE;
		read[1] = readW;
		read[2] = readN;
		read[3] = readS;
		read[4] = readL;

		outputE = new TypedIOPort(this, "outputE", false, true);
		outputW = new TypedIOPort(this, "outputW", false, true);
		outputN = new TypedIOPort(this, "outputN", false, true);
		outputS = new TypedIOPort(this, "outputS", false, true);
		outputL = new TypedIOPort(this, "outputL", false, true);
		output = new TypedIOPort[5];
		output[0] = outputE;
		output[1] = outputW;
		output[2] = outputN;
		output[3] = outputS;
		output[4] = outputL;

		outputEtx = new TypedIOPort(this, "outputEtx", false, true);
		outputWtx = new TypedIOPort(this, "outputWtx", false, true);
		outputNtx = new TypedIOPort(this, "outputNtx", false, true);
		outputStx = new TypedIOPort(this, "outputStx", false, true);
		outputLtx = new TypedIOPort(this, "outputLtx", false, true);
		outputtx = new TypedIOPort[5];
		outputtx[0] = outputEtx;
		outputtx[1] = outputWtx;
		outputtx[2] = outputNtx;
		outputtx[3] = outputStx;
		outputtx[4] = outputLtx;

		packetsize = new int[5];
		state = new int[5];
		sel = 4;

		direction = new IntToken[5];
		freeoutput = new boolean[5];
		txack = new boolean[5];
		clean = new boolean[5];

	}

	// /////////////////////////////////////////////////////////////////
	// // ports and parameters ///

	public Parameter x;
	public Parameter y;

	public TypedIOPort inputreqN, inputreqS, inputreqE, inputreqW, inputreqL,
			peekE, peekW, peekN, peekS, peekL, readE, readW, readN, readS,
			readL, lastFired, outputN, outputS, outputE, outputW, outputL,
			outputNtx, outputStx, outputEtx, outputWtx, outputLtx, ackN, ackS,
			ackE, ackW, ackL, wakeup;
	IntToken lastFiredSize, currDirection;
	int[] packetsize, state;
	int sel;
	boolean[] freeoutput, txack, clean;
	IntToken[] direction;
	TypedIOPort[] peek;
	TypedIOPort[] read;
	TypedIOPort[] inputreq;
	TypedIOPort[] output;
	TypedIOPort[] outputtx;
	TypedIOPort[] ack;

	// values for the state variables
	static final int INACTIVE = 0;
	static final int REQUESTING = 1;
	static final int HEADER = 2;
	static final int SIZE = 3;
	static final int PAYLOAD = 4;

	boolean clocked = false;

	// /////////////////////////////////////////////////////////////////
	// // public methods ///

	public int getIndex(TypedIOPort in) {
		if (in == inputreq[2]) {
			return 0;
		} else if (in == inputreqW) {
			return 1;
		} else if (in == inputreqN) {
			return 2;
		} else if (in == inputreq[0]) {
			return 3;
		} else if (in == inputreqL) {
			return 4;
		} else
			return 5;

	}

	/**
	 * Initialize state-holding variables
	 * 
	 */

	public void initialize() throws IllegalActionException {
		super.initialize();

		// OST por aqui
		lastFired = inputreq[0];

		for (int i = 0; i < 5; i++) {
			state[i] = 0;
		}

		for (int i = 0; i < 5; i++) {
			freeoutput[i] = true;
		}

		for (int i = 0; i < 5; i++) {
			txack[i] = false;
		}

		// check whether requesting ports have connections
		// if positive, set state to REQUESTING

		for (int i = 0; i < 5; i++) {
			if (inputreq[i].getWidth() > 0) {
				state[i] = REQUESTING;
			}
		}

	}

	/**
	 * 
	 * Consumes packet header Consumes clock token Checks for buffers requesting
	 * arbitration
	 * 
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */

	public boolean prefire() throws IllegalActionException {
		super.prefire();

		// reads clock signal, if available

		if (wakeup.hasToken(0)) {
			wakeup.get(0);
			clocked = true;

		}

		return true;
	}

	public void fire() throws IllegalActionException {
		super.fire();

		// first, checks for acks from remote buffers, updating the
		// values within txack[].

		for (int i = 0; i < 5; i++) {
			if (ack[i].getWidth() > 0) {
				while (ack[i].hasToken(0)) {
					// consumes the token

					BooleanToken ackt = (BooleanToken) ack[i].get(0);
					if (_debugging)
						_debug("Consuming ack from " + ack[i]);

					if (ackt.booleanValue())
						txack[i] = true;
					else
						txack[i] = false;
					if (_debugging)
						_debug("ack: " + txack[i]);

				}
			}

		}

		// second, handle the current request from the arbiter

		if (hasRequest()) {
			// get the index of the port with the request

			int j = nextRequest();
			if (_debugging)
				_debug("Arbiter gives turn to input " + j);

			if (j != -1) {
				// packet header flit
				RecordToken record = (RecordToken) inputreq[j].get(0);
				// calculate route information
				int dir = route(record);
				// verify if the output port is free, otherwise
				// do nothing and wait for another request
				boolean isfree = freeoutput[dir];
				boolean hasspace = txack[dir];
				if (isfree & hasspace) {
					// allocate outputport
					direction[j] = new IntToken(dir);
					freeoutput[dir] = false;

					if (_debugging)
						_debug("Input " + j + " connected to output " + dir);

					// deletes original header flit from buffer
					read[j].send(0, new Token());
					// signalize state machine to prepare for size
					state[j] = SIZE;

					// sends copy of the header flit to output
					output[dir].send(0, record);
					txack[dir] = false;

					// updates the last granted input port
					sel = j;

				}
			}
		}

		// clear all unatended requests (they will be asked again next cycle)
		clearRequests();

		// third, handle tokens on the inputs

		for (int i = 0; i < 5; i++) {
			if (inputreq[i].getWidth() > 0 & inputreq[i].hasToken(0)) {
				if (state[i] == SIZE | state[i] == PAYLOAD) {
					if (_debugging)
						_debug(inputreq[i] + " has size/payload token");
					// payload flit

					// get the direction assigned to this input port
					int dir = direction[i].intValue();

					// check if the remote buffer in that direction is free
					if (txack[dir]) {

						// check if it is a size flit or a regular flit

						if (state[i] == SIZE) {
							// set the size of the packet
							RecordToken record = (RecordToken) inputreq[i]
									.get(0);
							packetsize[i] = ((IntToken) record.get("size"))
									.intValue();
							if (_debugging)
								_debug("Packet size " + packetsize[i]);

							// prepare for payload
							state[i] = PAYLOAD;

							// deletes original header flit from buffer
							read[i].send(0, new Token());

							// send the token to the remote output buffer
							output[dir].send(0, record);

							if (_debugging)
								_debug(inputreq[i] + " sent size token to "
										+ output[dir]);

							txack[dir] = false;

						}

						else if (state[i] == PAYLOAD) {

							// send the token to the remote output buffer
							output[dir].send(0, inputreq[i].get(0));
							txack[dir] = false;

							// deletes original header flit from buffer
							read[i].send(0, new Token());
							if (_debugging)
								_debug(inputreq[i] + " sent token to "
										+ output[dir]);

							packetsize[i]--;
							if (_debugging)
								_debug("packet size " + packetsize[i]);

							// if package is finished,
							// release output port and prepare for new header
							if (packetsize[i] == 0) {
								freeoutput[dir] = true;
								state[i] = REQUESTING;
								if (_debugging)
									_debug("packet is over");

							}
						}
					} // end if direction is free

					else {
						inputreq[i].get(0);
					}

				} // end if state
			}// end if width > 1
		}// end for

		if (clocked) {

			// fourth: requests write on all inputs with packet payload or size
			// and whose respective output ports received acks

			for (int i = 0; i < 5; i++) {
				if (state[i] == PAYLOAD | state[i] == SIZE) {
					if (txack[direction[i].intValue()])
						peek[i].send(0, new Token());
				}

			}

			// fifth: requests write on remote buffers
			for (int i = 0; i < 5; i++) {
				// if(!txack[i]){
				outputtx[i].send(0, new Token());
				// }
				txack[i] = false; // consumes unused acks

			}

			// requests copy of request from all requesting input buffers

			for (int i = 0; i < 5; i++) {
				if (state[i] == REQUESTING) {
					peek[i].send(0, new Token());
					if (_debugging) {
						_debug("Peek for request on direction " + i);
					}
				}
			}

			clocked = false;
		}

	}

	// /////////////////////////////////////////////////////////////////
	// // protected methods ///

	protected int nextRequest() throws IllegalActionException {

		if (_debugging)
			_debug("Request detected, arbiter activated");

		if (sel == 0) {
			if (state[1] == REQUESTING && inputreq[1].hasToken(0)) {
				return 1;
			} else if (state[2] == REQUESTING && inputreq[2].hasToken(0)) {
				return 2;
			} else if (state[3] == REQUESTING && inputreq[3].hasToken(0)) {
				return 3;
			} else if (state[4] == REQUESTING && inputreq[4].hasToken(0)) {
				return 4;
			} else if (state[0] == REQUESTING && inputreq[0].hasToken(0)) {
				return 0;
			}
		} else if (sel == 1) {
			if (state[2] == REQUESTING && inputreq[2].hasToken(0)) {
				return 2;
			} else if (state[3] == REQUESTING && inputreq[3].hasToken(0)) {
				return 3;
			} else if (state[4] == REQUESTING && inputreq[4].hasToken(0)) {
				return 4;
			} else if (state[0] == REQUESTING && inputreq[0].hasToken(0)) {
				return 0;
			} else if (state[1] == REQUESTING && inputreq[1].hasToken(0)) {
				return 1;
			}
		} else if (sel == 2) {
			if (state[3] == REQUESTING && inputreq[3].hasToken(0)) {
				return 3;
			} else if (state[4] == REQUESTING && inputreq[4].hasToken(0)) {
				return 4;
			} else if (state[0] == REQUESTING && inputreq[0].hasToken(0)) {
				return 0;
			} else if (state[1] == REQUESTING && inputreq[1].hasToken(0)) {
				return 1;
			} else if (state[2] == REQUESTING && inputreq[2].hasToken(0)) {
				return 2;
			}
		} else if (sel == 3) {
			if (state[4] == REQUESTING && inputreq[4].hasToken(0)) {
				return 4;
			} else if (state[0] == REQUESTING && inputreq[0].hasToken(0)) {
				return 0;
			} else if (state[1] == REQUESTING && inputreq[1].hasToken(0)) {
				return 1;
			} else if (state[2] == REQUESTING && inputreq[2].hasToken(0)) {
				return 2;
			} else if (state[3] == REQUESTING && inputreq[3].hasToken(0)) {
				return 3;
			}
		} else if (sel == 4) {
			if (state[0] == REQUESTING && inputreq[0].hasToken(0)) {
				return 0;
			} else if (state[1] == REQUESTING && inputreq[1].hasToken(0)) {
				return 1;
			} else if (state[2] == REQUESTING && inputreq[2].hasToken(0)) {
				return 2;
			} else if (state[3] == REQUESTING && inputreq[3].hasToken(0)) {
				return 3;
			} else if (state[4] == REQUESTING && inputreq[4].hasToken(0)) {
				return 4;
			}
		}

		if (_debugging)
			_debug("Fatal error: Arbitration problem");
		return -1;

	}

	protected void clearRequests() throws IllegalActionException {

		for (int i = 0; i < 5; i++) {
			if (inputreq[i].getWidth() > 0) {
				while (inputreq[i].hasToken(0) & state[i] == REQUESTING) {
					// consumes the token
					inputreq[i].get(0);
				}
			}
		}
	}

	/**
	 * Checks whether are there tokens on the requests.
	 */

	protected boolean hasRequest() {

		for (int i = 0; i < 5; i++) {
			// if(_debugging) _debug("state requesting? "+i);
			if (state[i] == REQUESTING) {
				// if(_debugging) _debug("state requesting! "+i);

				if (inputreq[i].getWidth() > 0) {
					try {

						if (inputreq[i].hasToken(0)) {
							if (_debugging)
								_debug("Detected request on input port "
										+ inputreq[i]);
							return true;
						}
					}

					catch (Exception e) {
						if (_debugging)
							_debug("problem! " + e);
						System.out.println(e);
					}
				}
			}
		}

		if (_debugging)
			_debug("no state requesting!");
		return false;

	}

	protected int route(RecordToken record) throws IllegalActionException {

		int tx = ((IntToken) record.get("x")).intValue();
		int ty = ((IntToken) record.get("y")).intValue();
		int lx = ((IntToken) x.getToken()).intValue();
		int ly = ((IntToken) y.getToken()).intValue();

		if (lx == tx && ly == ty)
			return Directions.LOCAL;
		else if (lx > tx)
			return Directions.WEST;
		else if (lx < tx)
			return Directions.EAST;
		else if (ly < ty)
			return Directions.NORTH;
		else
			return Directions.SOUTH;

	}

}
