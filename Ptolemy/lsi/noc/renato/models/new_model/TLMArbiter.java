//Copyright (c) 2007 Leandro Soares Indrusiak
// Modified by Luciano Ost
//All rights reserved.
//Version 1.0	Darmstadt, 06/11/2007.

package lsi.noc.renato.models.new_model;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.IntToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

////TLMArbiter
/**
 * Transaction-level model of the Hermes NoC Arbiter + Router + Switch matrix
 * 
 * Arbitrates the access to the router and switch matrix by the 5 input port
 * buffers. Implements the XY routing algorithm. Implements the switch matrix.
 * 
 * @author Leandro Indrusiak
 */

public class TLMArbiter extends TypedAtomicActor {

	// /////////////////////////////////////////////////////////////////
	// // ports and parameters ///

	// position of the switch on the 2D mesh
	public Parameter x;
	public Parameter y;

	// arbitration time (in cycles)
	public Parameter acycles;

	// cycle period
	public Parameter period;

	// input and output ports
	public TypedIOPort inputreqN, inputreqS, inputreqE, inputreqW, inputreqL, // receives
																				// inputs
																				// from
																				// buffer
																				// data_out
			readE, readW, readN, readS, readL, // acknowledges the buffer that
												// data was read
			outputN, outputS, outputE, outputW, outputL,// writes data to remote
														// buffers
			ackN, ackS, ackE, ackW, ackL;// receives acknowledgement from remote
											// buffers

	// arrays used to index ports
	protected TypedIOPort[] read;
	protected TypedIOPort[] inputreq;
	protected TypedIOPort[] output;
	protected TypedIOPort[] ack;

	// port directions
	static final int EAST = 0;
	static final int WEST = 1;
	static final int NORTH = 2;
	static final int SOUTH = 3;
	static final int LOCAL = 4;

	// port state-holding arrays, indexed by direction
	protected boolean[] freeoutput; // true if a given output port is free (i.e.
									// not assigned to an input)
	protected int[] packetsize; // amount of the flits yet to be transfer in the
								// packet currently received by an input port
	protected int[] state; // current state of a given input port
	protected int[] muxin; // output port assigned to a given input port by the
							// router
	protected int[] muxout; // read port assigned to a given ack port by the
							// router
	protected double[] payloaddone; // stores time of last flit of a packet
	protected boolean[] next_sent; //
	protected boolean[] sent; //

	// values for the port state variables
	static final int INACTIVE = 0;
	static final int REQUESTING = 1;
	static final int REQUESTGRANTED = 5;
	static final int HEADER = 2;
	static final int SIZE = 3;
	static final int PAYLOAD = 4;
	static final int PAYLOADDONE = 6;

	// arbiter state-holding variables
	protected int arbitersel = 0;// last port to receive arbitration, LOCAL at
									// startup
	protected int arbiterstate = 0;// arbiter state, ready at startup
	protected int arbiterreq;// port that currently has access to router
	protected int arbiterdir;// route calculated to the packet on the currently
								// arbitrated port
	protected RecordToken record;// currently arbitrated header flit
	protected Time atime;// timestamp used to record the starting time of the
							// last arbitration
	protected Time arbitrationtime;// model the execution time of the arbiter

	// values for the arbiter state variables
	static final int AREADY = 0;
	static final int AROUTING = 1;
	static final int ACHECKPORT = 2;
	static final int ASEND = 3;

	private boolean run = false;
	private boolean flit_sent;
	private int cont;

	// /////////////////////////////////////////////////////////////////
	// // constructors ///

	public TLMArbiter(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// instantiates the parameters that hold the grid position of this
		// instance
		// on the 2D mesh
		x = new Parameter(this, "x");
		x.setTypeEquals(BaseType.INT);
		y = new Parameter(this, "y");
		y.setTypeEquals(BaseType.INT);

		// instantiates the parameter that define the number of cycles the
		// arbiter needs to select a port and route its packet
		acycles = new Parameter(this, "arbitration cycles");
		acycles.setTypeEquals(BaseType.INT);

		// instantiates the parameter that define the period of a cycles
		period = new Parameter(this, "period");
		period.setTypeEquals(BaseType.DOUBLE);

		// instantiates ports and adds them to indexing arrays
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
		outputE = new TypedIOPort(this, "outputE", false, true);
		outputW = new TypedIOPort(this, "outputW", false, true);
		outputN = new TypedIOPort(this, "outputN", false, true);
		outputS = new TypedIOPort(this, "outputS", false, true);
		outputL = new TypedIOPort(this, "outputL", false, true);

		// bla bla bla
		outputE.setTypeAtLeast(inputreqE);
		outputW.setTypeAtLeast(inputreqW);
		outputN.setTypeAtLeast(inputreqN);
		outputS.setTypeAtLeast(inputreqS);
		outputL.setTypeAtLeast(inputreqL);

		output = new TypedIOPort[5];

		output[0] = outputE;
		output[1] = outputW;
		output[2] = outputN;
		output[3] = outputS;
		output[4] = outputL;

		output[0].setTypeAtLeast(inputreqE);
		output[1].setTypeAtLeast(inputreqW);
		output[2].setTypeAtLeast(inputreqN);
		output[3].setTypeAtLeast(inputreqS);
		output[4].setTypeAtLeast(inputreqL);

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

		read[0].setTypeAtLeast(ackE);
		read[1].setTypeAtLeast(ackW);
		read[2].setTypeAtLeast(ackN);
		read[3].setTypeAtLeast(ackS);
		read[4].setTypeAtLeast(ackL);

		// output.setTypeAtLeast(input);

		// instantiates the state-holding arrays
		packetsize = new int[5];
		state = new int[5];
		muxin = new int[5];
		muxout = new int[5];
		freeoutput = new boolean[5];
		payloaddone = new double[5];
		next_sent = new boolean[5];
		sent = new boolean[5];

	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ///

	// overriding method from the Ptolemy superclass
	// called once by the director when simulation starts
	// initializes state-holding variables

	public void initialize() throws IllegalActionException {
		super.initialize();

		// all output ports are free upon start-up
		for (int i = 0; i < 5; i++) {
			freeoutput[i] = true;
			sent[i] = false;
		}

		// check whether requesting ports are connected (hopefully to the
		// data_out of a buffer)
		// if positive, set state to REQUESTING
		// otherwise, INACTIVE
		for (int i = 0; i < 5; i++) {
			if (inputreq[i].getWidth() > 0) {
				state[i] = REQUESTING;
			} else {
				state[i] = INACTIVE;
			}
		}

		// calculate arbitration time by multiplying the acycles by the period
		IntToken cyc = (IntToken) acycles.getToken();
		DoubleToken per = (DoubleToken) period.getToken();

		// instantiates arbitrationtime object
		arbitrationtime = new Time(getDirector());

		for (int i = 0; i < cyc.intValue(); i++) {
			arbitrationtime = arbitrationtime.add(per.doubleValue());
		}
		if (_debugging)
			_debug("Cycles: " + cyc.intValue() + "Period: " + per.doubleValue()
					+ "Arbitration time: " + arbitrationtime);

	}

	// wakes up ports that are in payloaddone state

	public boolean prefire() throws IllegalActionException {
		for (int i = 0; i < 5; i++) {
			if (state[i] == PAYLOADDONE) {
				if (payloaddone[i] <= getDirector().getModelTime()
						.getDoubleValue()) {
					state[i] = REQUESTING;
				}
			}
		}
		return super.prefire();
	}

	// overriding method from the Ptolemy superclass
	// implements the actual functionality of the arbiter
	// overall functionality:
	// 1 - check if are there header tokens on any inputs
	// if positive, arbitrate, route, lock output port, set muxin and muxout
	// then send token to output
	// 2 - check if are there size tokens on any inputs
	// if positive, reinitialize size state variable
	// then send token to output
	// 3 - send payload tokens to output port
	// 4 - read ack tokens, forward them to read ports
	// update states if needed
	// decrement size state variable

	public void fire() throws IllegalActionException {
		super.fire();

		// *************************
		// FIRST: ARBITRATE REQUESTS
		// *************************

		// if arbiter is ready,
		// reads tokens from the inputs on requesting state, if any
		if (arbiterstate == AREADY && hasRequest()) {
			// perform arbitration and
			// get the index of the port with the request
			// freeoutput[arbiterdir]=false;
			arbiterreq = nextRequest();

			// packet header flit
			record = (RecordToken) inputreq[arbiterreq].get(0);

			// calculate route information
			arbiterdir = route(record);

			// change state to routing and stores the time when it started
			// arbitrating
			// requests a "wake-up" from the director for the time it finishes
			// arbitrating&routing
			arbiterstate = AROUTING;
			atime = getDirector().getModelTime();
			if (_debugging)
				_debug(arbiterreq + "->" + arbiterdir
						+ " will be established. Time " + atime);
			getDirector().fireAt(this, atime.add(arbitrationtime));
		}
		// else if arbiter is routing, check if it finished routing
		else if (arbiterstate == AROUTING) {
			if (getDirector().getModelTime().equals(atime.add(arbitrationtime))) {
				arbiterstate = ACHECKPORT;
				if (_debugging)
					_debug(arbiterreq
							+ "->"
							+ arbiterdir
							+ " now have the permission to be established. Time "
							+ getDirector().getModelTime());
			}
		}

		// checks if desired output port is free
		if (arbiterstate == ACHECKPORT) {

			// updates the latest arbitrated port
			arbitersel = arbiterreq;

			if (freeoutput[arbiterdir]) {
				// allocates output port
				freeoutput[arbiterdir] = false;
				if (_debugging)
					_debug(arbiterreq + "->" + arbiterdir
							+ " now established. Time "
							+ getDirector().getModelTime());
				// updates switch matrix
				muxin[arbiterreq] = arbiterdir;
				muxout[arbiterdir] = arbiterreq;
				arbiterstate = ASEND;
				if (_debugging)
					_debug("Last sel=" + arbitersel + ", new sel=" + arbiterreq);
				// schedules a firing in one cycle

				atime = getDirector().getModelTime();
				// double dob =
				// (((DoubleToken)period.getToken()).doubleValue()*2);
				double dob = ((DoubleToken) period.getToken()).doubleValue() * 2;
				getDirector().fireAt(this,
						getDirector().getModelTime().add(dob));

			} else {
				// updates the arbiter state to initial state, so a next request
				// can be served
				arbiterstate = AREADY;
			}

		}
		// output port is free, set the state to send the header out if 1 cycle
		// has passed
		else if (arbiterstate == ASEND) {
			double dob = ((DoubleToken) period.getToken()).doubleValue() * 2;
			if (getDirector().getModelTime().equals(atime.add(dob))) {
				// updates port state
				state[arbiterreq] = REQUESTGRANTED;
				arbiterstate = AREADY;
			}
		}

		// discard all remaining requests
		clearRequests();

		// ***************************
		// SECOND: PROCESS ACKS
		// ***************************
		boolean[] acks = new boolean[5];
		for (int i = 0; i < 5; i++) {
			acks[i] = false;
			next_sent[muxout[i]] = false;
		}

		for (int i = 0; i < 5; i++) { // for each ack port
			if (ack[i].hasToken(0)) {
				next_sent[muxout[i]] = true;
				BooleanToken boo = (BooleanToken) ack[i].get(0);
				if (_debugging)
					_debug(i + "<-" + muxin[i] + " received ack " + boo
							+ "at: " + getDirector().getModelTime());
				acks[muxout[i]] = boo.booleanValue();
			}

		}

		// ***************************
		// THIRD: PROCESS ROUTED FLITS
		// ***************************
		for (int i = 0; i < 5; i++) { // for each input port
			// check if the corresponding ack port received an ack or nack
			boolean hasAck = acks[i];
			boolean has_ack = next_sent[i];
			boolean flit_sent = sent[i];

			// ////////////////
			// handle granted requests (header already sent)
			// ////////////////
			if (state[i] == REQUESTGRANTED) {
				if (inputreq[i].hasToken(0)) {
					if (!flit_sent) {
						// send the header to the remote output buffer
						output[muxin[i]].send(0, inputreq[i].get(0));
						sent[i] = true;
						if (_debugging)
							_debug(i + "->" + muxout[i]
									+ " target flit sent at AAAA: "
									+ getDirector().getModelTime());
						if (_debugging)
							_debug("Arbiter status: arbiterreq=" + arbiterreq
									+ ", arbitersel=" + arbitersel
									+ ", arbiterdir=" + arbiterdir
									+ ". Routing table:");
						for (int j = 0; j < 5; j++) {
							if (_debugging && !freeoutput[j])
								_debug(j + "->" + muxin[j]);
						}
					} else if (flit_sent && has_ack) {
						if (!hasAck) {
							// send the header to the remote output buffer
							output[muxin[i]].send(0, inputreq[i].get(0));
							if (_debugging)
								_debug(i + "->" + muxout[i]
										+ " target flit sent at: "
										+ getDirector().getModelTime());
							if (_debugging)
								_debug("Arbiter status: arbiterreq="
										+ arbiterreq + ", arbitersel="
										+ arbitersel + ", arbiterdir="
										+ arbiterdir + ". Routing table:");
							for (int j = 0; j < 5; j++) {
								if (_debugging && !freeoutput[j])
									_debug(j + "->" + muxin[j]);
							}
							next_sent[i] = false;
						} else {
							// updates the state of the switch, discard input
							// and notify buffer
							state[i] = SIZE;
							if (_debugging)
								_debug(i + "->" + muxin[i]
										+ " going to size state");
							inputreq[i].get(0);
							read[i].send(0, new Token());
							sent[i] = false;
							next_sent[i] = false;
						}
					} else {
						// flit discated
						inputreq[i].get(0);
					}
				}
			}

			// ////////////////
			// update the size
			// ////////////////
			else if (state[i] == SIZE) {
				if (inputreq[i].hasToken(0)) {
					if (!flit_sent) {
						// update the size and send the token (again, if there
						// was a nack)
						RecordToken rec = (RecordToken) inputreq[i].get(0);
						packetsize[i] = ((IntToken) rec.get("size")).intValue();
						if (_debugging)
							_debug(i + "->" + muxin[i] + " size "
									+ packetsize[i]);
						// send the token to the remote output buffer
						output[muxin[i]].send(0, rec);
						if (_debugging)
							_debug(i + "->" + muxout[i] + " size sent at AAA: "
									+ getDirector().getModelTime());
						sent[i] = true;
					} else if (flit_sent && has_ack) {
						if (!hasAck) {
							// update the size and send the token (again, if
							// there was a nack)
							RecordToken rec = (RecordToken) inputreq[i].get(0);
							packetsize[i] = ((IntToken) rec.get("size"))
									.intValue();
							if (_debugging)
								_debug(i + "->" + muxin[i] + " size "
										+ packetsize[i]);
							// send the token to the remote output buffer
							if (_debugging)
								_debug(i + "->" + muxout[i]
										+ " size sent AGAIN NACK: "
										+ getDirector().getModelTime());
							output[muxin[i]].send(0, rec);
							next_sent[i] = false;
						} else {
							// updates the state of the switch, discard input
							// and notify buffer
							inputreq[i].get(0);
							state[i] = PAYLOAD;
							read[i].send(0, new Token());
							sent[i] = false;
							next_sent[i] = false;
						}
					} else {
						// flit discated
						inputreq[i].get(0);
					}
				}
			}
			// ////////////
			// send payload
			// ////////////
			else if (state[i] == PAYLOAD) {
				if (inputreq[i].hasToken(0)) {
					if (!flit_sent) {
						// send the token to the remote output buffer
						output[muxin[i]].send(0, inputreq[i].get(0));
						if (_debugging)
							_debug("PAYLOAD sent at AAA"
									+ getDirector().getModelTime());
						sent[i] = true;
					} else if (flit_sent && has_ack) {
						if (!hasAck) {
							// send the token to the remote output buffer
							output[muxin[i]].send(0, inputreq[i].get(0));
							// next_sent[i] = false;
							next_sent[i] = false;
						} else {
							inputreq[i].get(0);
							read[i].send(0, new Token());
							// decrement packet size
							sent[i] = false;
							packetsize[i]--;
							if (_debugging)
								_debug(i + "->" + muxin[i] + " size "
										+ packetsize[i]);
							if (_debugging)
								_debug("PAYLOAD -- "
										+ getDirector().getModelTime());
							// if package is finished,
							// release output port and prepare for new header
							if (packetsize[i] == 0) {
								freeoutput[muxin[i]] = true;
								run = false;
								state[i] = PAYLOADDONE;
								payloaddone[i] = getDirector().getModelTime()
										.getDoubleValue();
								if (_debugging)
									_debug(i + "->" + muxin[i]
											+ " packet is over. Time "
											+ getDirector().getModelTime());
								sent[i] = false;
								next_sent[i] = false;
							}
						}
					} else {
						// flit discated
						inputreq[i].get(0);
					}

				}
			}// end else
		}// end for

	}

	// /////////////////////////////////////////////////////////////////
	// // protected methods ///

	/**
	 * Performs arbitration among all requesting ports. May be overriden by
	 * subclasses to implement different arbitration algorithms. Current
	 * implementation mimics Hermes arbitration algorithm, based on if-chains.
	 */
	protected int nextRequest() throws IllegalActionException {
		if (_debugging)
			_debug("Request detected, arbiter activated");
		if (arbitersel == 0) {
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
		} else if (arbitersel == 1) {
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
		} else if (arbitersel == 2) {
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
		} else if (arbitersel == 3) {
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
		} else if (arbitersel == 4) {
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

	/**
	 * Checks whether are there header tokens on the inputs.
	 */
	protected boolean hasRequest() throws IllegalActionException {
		for (int i = 0; i < 5; i++) {
			if (state[i] == REQUESTING) {
				if (inputreq[i].getWidth() > 0) {
					if (inputreq[i].hasToken(0)) {
						if (_debugging)
							_debug("Detected request on input port "
									+ inputreq[i]);
						return true;
					}
				}
			}
		}
		if (_debugging)
			_debug("no state requesting!");
		return false;

	}

	/**
	 * Consumes and discards all requests.
	 */
	protected void clearRequests() throws IllegalActionException {

		for (int i = 0; i < 5; i++) {
			if (inputreq[i].getWidth() > 0) {
				while (inputreq[i].hasToken(0)
						&& (state[i] == REQUESTING || state[i] == PAYLOADDONE)) {
					// consumes the token
					inputreq[i].get(0);
				}
			}
		}
	}

	/**
	 * Routes a given package based on its destination X and Y coordinates.
	 * Currently implements XY routing algorithm. Can be overriden by
	 * subclasses.
	 */
	protected int route(RecordToken record) throws IllegalActionException {
		int tx = ((IntToken) record.get("x")).intValue();
		int ty = ((IntToken) record.get("y")).intValue();
		int lx = ((IntToken) x.getToken()).intValue();
		int ly = ((IntToken) y.getToken()).intValue();
		if (lx == tx && ly == ty)
			return LOCAL;
		else if (lx > tx)
			return WEST;
		else if (lx < tx)
			return EAST;
		else if (ly < ty)
			return NORTH;
		else
			return SOUTH;
	}

}
