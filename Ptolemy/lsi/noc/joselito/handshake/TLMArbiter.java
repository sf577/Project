//Copyright (c) 2007 Luciano Ost & Leandro Möller
//All rights reserved.
//Version 3.0	Darmstadt, 13/02/2008.

package lsi.noc.joselito.handshake;

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
 * @author Luciano Ost, Leandro Möller, Leandro Indrusiak
 */

public class TLMArbiter extends TypedAtomicActor {

	// /////////////////////////////////////////////////////////////////
	// // ports and parameters ///

	// position of the switch on the 2D mesh
	public Parameter x;
	public Parameter y;

	// timing related variables
	protected Time atime;// timestamp used to record the starting time of the
							// last arbitration
	protected Time arbitrationtime;// model the execution time of the arbiter
	private double current_time; // stores the current time
	public Parameter acycles; // arbitration time (in cycles)
	public Parameter period; // cycle period

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
	protected int[] state; // current state of a given input port
	protected int[] muxin; // output port assigned to a given input port by the
							// router
	protected int[] muxout; // read port assigned to a given ack port by the
							// router
	protected double[] payloaddone; // stores time of last flit of a packet
	protected boolean[] ack_received; // stores if an ack/nack was received
	protected boolean[] sent; // stores if this packet was already sent
	protected boolean[] ack_value; // stores if the value of ack_received[i] is
									// true or false
	protected int[] number_nacks; // count the number of nacks received when
									// trying to send a HEADER of TRAILER away
	protected double[] wakeup_time; // stores the time to wakeup an input port
	protected double[] requesting_time; // stores the time an input port started
										// to request an output port
	protected double[] requestgranted_time; // stores the time an input port had
											// the arbitration granted
	protected boolean[] first_time; // stores if it is the first time you enter
									// in a specific state of a FSM

	// values for the port state variables
	static final int INACTIVE = 0; // initial state of the machine
	static final int REQUESTING = 1; // requesting arbiter
	static final int REQUESTGRANTED = 2; // arbitration done
	static final int TRAILER = 3; // sending trailer
	static final int PAYLOADDONE = 4; // packet sucessfully sent

	// arbiter state-holding variables
	protected int arbitersel = 0;// last port to receive arbitration, EAST at
									// startup
	protected int arbiterstate = 0;// arbiter state, ready at startup
	protected int arbiterreq;// port that currently has access to router
	protected int arbiterdir;// route calculated to the packet on the currently
								// arbitrated port
	protected RecordToken record;// currently arbitrated header flit

	// values for the arbiter state variables
	static final int AREADY = 0;
	static final int AROUTING = 1;
	static final int ACHECKPORT = 2;
	static final int ASEND = 3;

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

		// instantiates the state-holding arrays
		state = new int[5];
		muxin = new int[5];
		muxout = new int[5];
		freeoutput = new boolean[5];
		payloaddone = new double[5];
		ack_received = new boolean[5];
		sent = new boolean[5];
		ack_value = new boolean[5];
		number_nacks = new int[5];
		wakeup_time = new double[5];
		requesting_time = new double[5];
		requestgranted_time = new double[5];
		first_time = new boolean[5];
	}

	// overriding method from the Ptolemy superclass
	// called once by the director when simulation starts
	// initializes state-holding variables

	public void initialize() throws IllegalActionException {
		super.initialize();

		// all output ports are free upon start-up
		for (int i = 0; i < 5; i++) {
			freeoutput[i] = true;
			sent[i] = false;
			number_nacks[i] = 0;
			first_time[i] = true;
		}

		// check whether requesting ports are connected (hopefully to the
		// data_out of a buffer)
		// if positive, set state to REQUESTING, otherwise, INACTIVE
		for (int i = 0; i < 5; i++) {
			if (inputreq[i].getWidth() > 0)
				state[i] = REQUESTING;
			else
				state[i] = INACTIVE;
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

	public boolean prefire() throws IllegalActionException {
		if (_debugging)
			_debug("==================== Time = "
					+ getDirector().getModelTime() + " ====================");

		// pass from PAYLOADDONE state to REQUESTING in the same cycle
		for (int i = 0; i < 5; i++) {
			if (state[i] == PAYLOADDONE) {
				if (payloaddone[i] <= getDirector().getModelTime()
						.getDoubleValue()) {
					state[i] = REQUESTING;
				}
			}
		}

		for (int i = 0; i < 5; i++) {
			// verify if there are tokens in the input port in the RESQUESTING
			// state
			if (inputreq[i].hasToken(0)) {
				if (_debugging)
					_debug(getPortName(i) + " has token");
			}
		}

		for (int i = 0; i < 5; i++) {
			// verify if there are tokens in the input port in the RESQUESTING
			// state
			if (inputreq[i].hasToken(0) && state[i] == REQUESTING) {
				// if(_debugging) _debug(getPortName(i) + " has token");
				// if it is the first request attempt
				if (first_time[i]) {
					// store the time this request is being received by the
					// router
					requesting_time[i] = getDirector().getModelTime()
							.getDoubleValue();
					// set that it is not the first time anymore
					first_time[i] = false;
				}
			}
			// verify if there are tokens in the input ack port
			// if(ack[i].hasToken(0)){
			// if(_debugging) _debug(i + "<-" + muxout[i] +
			// " received ack/nack.");
			// }
		}
		return super.prefire();
	}

	// router functionality:
	// 1 - check if are there header tokens on any inputs
	// if positive, arbitrate, route, lock output port, set muxin and muxout
	// then send token to output
	// 2 - read ack token, forward them to read ports
	// wait for a trailer token, update states if needed
	// forward trailer token, wait for ack, unlock output port

	public void fire() throws IllegalActionException {
		super.fire();

		// *************************
		// FIRST: ARBITRATE REQUESTS
		// *************************

		// if arbiter is free and there is at least one port requesting
		if (arbiterstate == AREADY && hasRequest()) {
			// perform arbitration and get the index of the chosen input port
			arbiterreq = nextRequest();
			// get the token from the chosen input buffer
			record = (RecordToken) inputreq[arbiterreq].get(0);
			// calculate route information
			arbiterdir = route(record);
			// change state to routing
			arbiterstate = AROUTING;
			// stores the time when the arbitration started
			atime = getDirector().getModelTime();
			// debug message
			if (_debugging)
				_debug(getPortName(arbiterreq) + "->" + getPortName(arbiterdir)
						+ " attempting to establish this connection.");
			// requests a "wake-up" from the director for the time it finishes
			// arbitrating&routing
			getDirector().fireAt(this, atime.add(arbitrationtime));
		}
		// else if arbiter is routing
		else if (arbiterstate == AROUTING) {
			// check if routing has finished
			if (getDirector().getModelTime().equals(atime.add(arbitrationtime))) {
				// if so, send to the state to check if the chosen output port
				// is free
				arbiterstate = ACHECKPORT;
				if (_debugging)
					_debug(getPortName(arbiterreq)
							+ "->"
							+ getPortName(arbiterdir)
							+ " now have the permission to attempt this connection.");
			}
		}
		// checks if desired output port is free
		else if (arbiterstate == ACHECKPORT) {
			// debug message
			if (_debugging)
				_debug("Last sel was=" + getPortName(arbitersel)
						+ ", new sel is=" + getPortName(arbiterreq)
						+ ", current output selected is="
						+ getPortName(arbiterdir));
			// updates the latest arbitrated port
			arbitersel = arbiterreq;
			// checks if desired output port is free
			if (freeoutput[arbiterdir]) {
				// allocates output port
				freeoutput[arbiterdir] = false;
				// debug message
				if (_debugging)
					_debug(getPortName(arbiterreq) + "->"
							+ getPortName(arbiterdir) + " now established.");
				// updates switch matrix
				muxin[arbiterreq] = arbiterdir;
				muxout[arbiterdir] = arbiterreq;
				// go to send state
				arbiterstate = ASEND;
				// schedules a firing in one cycle
				atime = getDirector().getModelTime();
				// double dob =
				// (((DoubleToken)period.getToken()).doubleValue()*2);
				double dob = ((DoubleToken) period.getToken()).doubleValue() * 2;
				getDirector().fireAt(this,
						getDirector().getModelTime().add(dob));
			} else {
				// the chosen output port is busy, round the round-robin, so a
				// next request can be served
				arbiterstate = AREADY;
				// debug
				if (_debugging)
					_debug(getPortName(arbiterreq) + "->"
							+ getPortName(arbiterdir)
							+ " NOT established. Output is busy.");
			}
			// debug
			if (_debugging)
				_debug("Routing table:");
			for (int j = 0; j < 5; j++)
				if (_debugging && !freeoutput[j])
					_debug(getPortName(muxout[j]) + "->" + getPortName(j));

		}
		// output port is free, set the state to send the header out if 1 cycle
		// has passed
		else if (arbiterstate == ASEND) {
			// if the arbitration time has passed, it is possible to continue
			double dob = ((DoubleToken) period.getToken()).doubleValue() * 2;
			if (getDirector().getModelTime().equals(atime.add(dob))) {
				// now the chosen input port can start forwarding data
				state[arbiterreq] = REQUESTGRANTED;
				// set that it is the first time in the REQUESTGRANTED state
				first_time[arbiterreq] = true;
				// arbiter is now free again
				arbiterstate = AREADY;
			}
		}

		// discard all remaining requests
		clearRequests();

		// ***************************
		// SECOND: PROCESS ACKS
		// ***************************
		// reinitialize at every fire these ack variables
		for (int i = 0; i < 5; i++) {
			ack_value[i] = false;
			ack_received[muxout[i]] = false;
		}

		// correctly set the previous initialized variables with correct value
		// for this fire
		for (int i = 0; i < 5; i++) { // for each ack port
			// if a ack-nack has arrived
			if (ack[i].hasToken(0)) {
				// mark if an ack was received
				ack_received[muxout[i]] = true;
				// get the ack token
				BooleanToken boo = (BooleanToken) ack[i].get(0);
				// store in ack_value if an ACK or a NACK was received
				ack_value[muxout[i]] = boo.booleanValue();
				// debug
				if (_debugging)
					_debug(getPortName(muxout[i]) + "<-" + getPortName(i)
							+ " received ack " + boo);
			}
		}

		// ***************************
		// THIRD: PROCESS ROUTED FLITS
		// ***************************
		printPortsState();

		for (int i = 0; i < 5; i++) { // for each input port
			// connection established, it is now possible to try to forward data
			if (state[i] == REQUESTGRANTED) {
				// just in the first cycle it gets in this state
				if (first_time[i]) {
					// get the time
					requestgranted_time[i] = getDirector().getModelTime()
							.getDoubleValue();
					// set that it is not the first time anymore
					first_time[i] = false;
				}
				// if a token has arrived in the input data
				if (inputreq[i].hasToken(0)) {
					// if this token was not sent yet
					if (!sent[i]) {
						// send the header to the remote output buffer
						output[muxin[i]].send(0, inputreq[i].get(0));
						// set as sent
						sent[i] = true;
						// debug
						if (_debugging)
							_debug(getPortName(i) + "->"
									+ getPortName(muxin[i]) + " packet sent");
					}
					// if this packet was already sent but an ack-nack was
					// received
					else if (sent[i] && ack_received[i]) {
						// if a nack was received, send again
						if (!ack_value[i]) {
							// send the header to the remote output buffer again
							output[muxin[i]].send(0, inputreq[i].get(0));
							// increment the number of nacks received
							number_nacks[i]++;
							// debug
							if (_debugging)
								_debug(getPortName(i) + "->"
										+ getPortName(muxin[i])
										+ " sending packet again.");
							// reset variable that an ack-nack was received
							ack_received[i] = false;
						}
						// if an ack was received, go to TRAILER state
						else {
							// go to TRAILER state
							state[i] = TRAILER;
							// get the token again from the buffer
							RecordToken record = ((RecordToken) inputreq[i]
									.get(0));
							// get the token size
							int size = ((IntToken) record.get("size"))
									.intValue();
							// delete from the buffer
							read[i].send(0, new Token());
							// reset variables for next state
							sent[i] = false;
							ack_received[i] = false;
							first_time[i] = true;
							// wake up in the next fire
							getDirector().fireAt(this,
									getDirector().getModelTime().add(1));
							// get current time
							current_time = getDirector().getModelTime()
									.getDoubleValue();
							// calculate how long it was needed for the header
							// to be sent to the next router
							// double header_time = requestgranted_time[i] -
							// requesting_time[i];
							// double header_time = current_time -
							// requesting_time[i];
							// calculate time to wake up
							// wakeup_time[i] = current_time + number_nacks[i] +
							// 1 + header_time;
							// wakeup_time[i] = current_time + header_time;
							wakeup_time[i] = current_time + (size * 2);
							// debug
							if (_debugging)
								_debug(getPortName(i) + "->"
										+ getPortName(muxin[i])
										+ " packet sent after "
										+ number_nacks[i] + " nacks");
							// if(_debugging)
							// _debug("Time needed to forward the packet = " +
							// header_time);
							if (_debugging)
								_debug("Time to wake up = " + wakeup_time[i]);
						}
					} else {
						// flit discarded
						inputreq[i].get(0);
						if (_debugging)
							_debug("HEADER DISCARDED");
					}
				}
			}
			// wait for trailer, hold it if needed, forward it, close the
			// connection
			else if (state[i] == TRAILER) {
				// if a token has arrived in the input data
				if (inputreq[i].hasToken(0)) {
					// if this token was not sent yet
					if (!sent[i]) {
						// get the current time
						current_time = getDirector().getModelTime()
								.getDoubleValue();
						// if it is time to wake time
						if (current_time >= wakeup_time[i]) {
							// send the token to the output
							output[muxin[i]].send(0, inputreq[i].get(0));
							// debug
							if (_debugging)
								_debug(getPortName(i) + "->"
										+ getPortName(muxin[i])
										+ " Trailer sent.");
							// remember that the trailer was sent
							sent[i] = true;
						}
						// if it is not time to wake up
						else {
							// consume the token
							inputreq[i].get(0);
							// enqueue the correct time to be woke up
							// getDirector().fireAt(this,
							// getDirector().getModelTime().add(wakeup_time[i]));
							getDirector().fireAt(this, wakeup_time[i]);
						}
					}
					// if this packet was already sent but an ack-nack was
					// received
					else if (sent[i] && ack_received[i]) {
						// if a nack was received, go to the IF-THEN-ELSE above
						// to be sent again
						if (!ack_value[i]) {
							// if a nack was received, it means that the remote
							// buffer was still not able to
							// send the HEADER ahead. This is terrible in this
							// version of JOSELITO, because
							// the TRAILER of this same packet is only one hop
							// away. Depending on the size of
							// the packet it should be several hops away.

							// get the token again from the buffer
							RecordToken record = ((RecordToken) inputreq[i]
									.get(0));
							// get the token size
							int size = ((IntToken) record.get("size"))
									.intValue();
							// get the current time
							current_time = getDirector().getModelTime()
									.getDoubleValue();
							// calculate the minimum time to wake up again and
							// try
							wakeup_time[i] = current_time + (size * 2);
							// fire at the calculated time
							// getDirector().fireAt(this,
							// getDirector().getModelTime().add(wakeup_time[i]));
							getDirector().fireAt(this, wakeup_time[i]);
							// reset remember variables
							sent[i] = false;
							ack_received[i] = false;
							// debug
							if (_debugging)
								_debug(getPortName(i)
										+ "->"
										+ getPortName(muxin[i])
										+ "Nack received for trailer. Trailer will try to be sent at "
										+ wakeup_time[i]);
						}
						// if an ack was received, close the connection
						else {
							// updates the state of the switch, discard input
							// and notify buffer
							// close the connection
							freeoutput[muxin[i]] = true;
							// go to PAYLOADDONE state
							state[i] = PAYLOADDONE;
							// store the current time
							payloaddone[i] = getDirector().getModelTime()
									.getDoubleValue();
							// consume the token
							inputreq[i].get(0);
							// remove trailer from buffer
							read[i].send(0, new Token());
							// reset remember variables
							sent[i] = false;
							ack_received[i] = false;
							// reset the counted number of nacks
							number_nacks[i] = 0;
							// fire again in the next period
							double dob = ((DoubleToken) period.getToken())
									.doubleValue();
							getDirector().fireAt(this,
									getDirector().getModelTime().add(dob));

							// fire also all the input buffers in order to them
							// to concur fairly in the arbiter
							// getDirector().fireAt(,
							// getDirector().getModelTime().add(dob));

							// debug
							if (_debugging)
								_debug(getPortName(i) + "->"
										+ getPortName(muxin[i])
										+ " connection closed.");
						}
					} else {
						// flit discarded
						inputreq[i].get(0);
					}
				}
				// be CAREFULL: this makes this actor wake up every cycle when a
				// token is not
				// received in the trailer state
				else {
					double dob = ((DoubleToken) period.getToken())
							.doubleValue();
					getDirector().fireAt(this,
							getDirector().getModelTime().add(dob));
				}
			}
		}// end for
	}

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
									+ getPortName(i));
						return true;
					}
				}
			}
		}
		if (_debugging)
			_debug("There is no input port requesting arbitration!");
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
	 * Print the state of each input port of the router
	 */
	protected void printPortsState() {
		if (_debugging) {
			// printing input port information
			String print = "";
			for (int i = 0; i < 5; i++) {
				if (state[i] == INACTIVE)
					print = print + ("[" + getPortName(i) + "]=INAC ");
				else if (state[i] == REQUESTING)
					print = print + ("[" + getPortName(i) + "]=REQU ");
				else if (state[i] == REQUESTGRANTED)
					print = print + ("[" + getPortName(i) + "]=GRAN ");
				else if (state[i] == TRAILER)
					print = print + ("[" + getPortName(i) + "]=TRAI ");
			}
			_debug(print + " inputs info");

			// printing output port information
			print = "";
			for (int i = 0; i < 5; i++) {
				if (freeoutput[i] == true)
					print = print + ("[" + getPortName(i) + "]=FREE ");
				else
					print = print + ("[" + getPortName(i) + "]=BUSY ");
			}
			_debug(print + "outputs info");
		}
	}

	/**
	 * Print the state of each input port of the router
	 */
	protected String getPortName(int i) {
		if (i == EAST)
			return "E";
		else if (i == WEST)
			return "W";
		else if (i == NORTH)
			return "N";
		else if (i == SOUTH)
			return "S";
		else if (i == LOCAL)
			return "L";
		else
			return "ERROR";
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
