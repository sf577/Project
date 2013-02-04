package lsi.noc.argus;

import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;
import lsi.noc.nocscope2.BufferScopeActor;
import lsi.noc.nocscope2.OutputScopeActor;
import lsi.noc.nocscope2.InputScopeActor;
import lsi.noc.nocscope2.HotSpotScopeActor;
import lsi.noc.nocscope2.PowerScopeActor;
import lsi.noc.nocscope2.EndToEndScopeActor;
import lsi.noc.nocscope2.PointToPointScopeActor;
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

/**
 * 
 * Transaction-Level Model of the Hermes NoC Arbiter + Router + Switch matrix. <br>
 * Arbitrates the access to the router and switch matrix by the 5 input port
 * buffers. <br>
 * Implements the XY routing algorithm. <br>
 * Implements the switch matrix. <br>
 * <br>
 * 
 * Copyright (c) 2007 - All rights reserved. <br>
 * 
 * @author Leandro Soares Indrusiak
 * @author Luciano Ost
 * @author Leandro Möller
 * @version Argus (Darmstadt, 12.08.2008)
 */
public class TLMArbiter extends TypedAtomicActor {

	private NamedObj container;

	// public parameters of the router
	public Parameter x, y; // position of the switch on the 2D mesh
	public Parameter acycles; // arbitration time (in cycles)
	public Parameter period; // cycle period

	// arrays used to index ports
	private TypedIOPort[] read, inputreq, output, ack;

	// port constant directions
	static final int EAST = 0, WEST = 1, NORTH = 2, SOUTH = 3, LOCAL = 4;

	// port state-holding arrays, indexed by direction
	private boolean[] freeoutput; // true if a given output port is free (i.e.
									// not assigned to an input)
	private int[] packetsize; // amount of the flits yet to be transfer in the
								// packet currently received by an input port
	private int[] state; // current state of a given input port
	private int[] muxin; // output port assigned to a given input port by the
							// router
	private int[] muxout; // read port assigned to a given ack port by the
							// router
	private double[] payloaddone; // stores time of last flit of a packet
	private boolean[] ack_value; // stores if an ACK or a NACK was received
	private boolean[] next_sent; // set a flit to be sent again
	private boolean[] sent; // stores if a flit has already been sent

	// values for the port state variables
	static final int INACTIVE = 0; // initial state of the machine
	static final int REQUESTING = 1; // requesting arbiter
	static final int REQUESTGRANTED = 2; // arbitration done, first flit
											// resolved
	static final int SIZE = 3; // getting second flit
	static final int PAYLOAD = 4; // getting payload flits
	static final int PAYLOADDONE = 5; // closing connection

	// arbiter state-holding variables
	private int arbitersel = 0;// last port to receive arbitration, EAST at
								// startup
	private int arbiterstate = 0;// arbiter state, ready at startup
	private int arbiterreq;// port that currently has access to router
	private int arbiterdir;// route calculated to the packet on the currently
							// arbitrated port
	private RecordToken record;// currently arbitrated header flit
	private Time atime;// timestamp used to record the starting time of the last
						// arbitration
	private Time arbitrationtime;// model the execution time of the arbiter

	// constant values for the arbiter state variables
	static final int AREADY = 0, AROUTING = 1, ACHECKPORT = 2, ASEND = 3;

	// monitor variables
	private int windowSize; // number fires info that are maintained in the
							// vector times below
	private int windowCounter = 1; // increment until windowSize, then reset

	private EndToEndScopeActor endToEndScopeActor = null; // pointer to the
															// EndToEndScopeActor

	private InputScopeActor inputScopeActor = null; // pointer to the
													// OutputScopeActor
	private Vector[] input_time; // store times of sent packets
	private boolean input_update; // true if the OutputScope GUI must be updated
									// in this fire
	private int[] in_flits; // store the number of sent flits
	private int[] in_packets; // store the number of sent packets
	private int[] input_counter; // store the number of sent packets of the
									// current window
	private int[] input_counter_old; // store the number of sent packets of the
										// previous window

	private OutputScopeActor outputScopeActor = null; // pointer to the
														// InputScopeActor
	private Vector[] output_time; // store times of sent packets
	private boolean output_update; // true if the InputScope GUI must be updated
									// in this fire
	private int[] out_flits; // store the number of sent flits
	private int[] out_packets; // store the number of sent packets
	private int[] output_counter; // store the number of sent packets of the
									// current window
	private int[] output_counter_old; // store the number of sent packets of the
										// previous window

	private HotSpotScopeActor hotspotScopeActor = null; // pointer to the
														// HotSpotScopeActor
	private Vector[] hotspot_time; // store times of received nacks
	private boolean hotspot_update; // true if the HotSpotScope GUI must be
									// updated in this fire
	private int[] hotspot_collector; // store the total number of nacks received
	private int[] hotspot_counter; // store the hotspots of the current window
	private int[] hotspot_counter_old; // store the hotspots of the previous
										// window

	private BufferScopeActor bufferScopeActor = null; // pointer to the
														// BufferScopeActor
	private int[] bufferSize; // store buffer size received from buffers
	private boolean buffer_update; // true if the BufferScope GUI must be
									// updated in this fire

	private PowerScopeActor powerScopeActor = null; // pointer to the
													// PowerScopeActor
	private Vector[] power_time; // store one time for each changed bit!
	private boolean power_update; // true if the PowerScope GUI must be updated
									// in this fire
	private int[] power_collector; // store the total number of bit transitions
	private int[] actual_flit; // store the flit that was received in this fire
	private int[] last_flit; // store the flit that was received in the previous
								// fire
	private int[] power_counter; // store the number of transitions of the
									// current window
	private int[] power_counter_old; // store the number of transitions of the
										// previous window

	private PointToPointScopeActor pointToPointScopeActor = null; // pointer to
																	// the
																	// PointToPointScopeActor
	private int[] point_status; // store the status of each input port
	private int[] sourceX, targetX, sourceY, targetY; // store the source and
														// target of each packet
														// passing through this
														// router

	private CompositeEntity workspace;

	public TLMArbiter(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		workspace = container;

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
		ack = new TypedIOPort[5];
		ack[0] = new TypedIOPort(this, "ackE", true, false);
		ack[1] = new TypedIOPort(this, "ackW", true, false);
		ack[2] = new TypedIOPort(this, "ackN", true, false);
		ack[3] = new TypedIOPort(this, "ackS", true, false);
		ack[4] = new TypedIOPort(this, "ackL", true, false);

		inputreq = new TypedIOPort[5];
		inputreq[0] = new TypedIOPort(this, "inputreqE", true, false);
		inputreq[1] = new TypedIOPort(this, "inputreqW", true, false);
		inputreq[2] = new TypedIOPort(this, "inputreqN", true, false);
		inputreq[3] = new TypedIOPort(this, "inputreqS", true, false);
		inputreq[4] = new TypedIOPort(this, "inputreqL", true, false);

		output = new TypedIOPort[5];
		output[0] = new TypedIOPort(this, "outputE", false, true);
		output[1] = new TypedIOPort(this, "outputW", false, true);
		output[2] = new TypedIOPort(this, "outputN", false, true);
		output[3] = new TypedIOPort(this, "outputS", false, true);
		output[4] = new TypedIOPort(this, "outputL", false, true);

		read = new TypedIOPort[5];
		read[0] = new TypedIOPort(this, "readE", false, true);
		read[1] = new TypedIOPort(this, "readW", false, true);
		read[2] = new TypedIOPort(this, "readN", false, true);
		read[3] = new TypedIOPort(this, "readS", false, true);
		read[4] = new TypedIOPort(this, "readL", false, true);

		output[0].setTypeAtLeast(inputreq[0]);
		output[1].setTypeAtLeast(inputreq[1]);
		output[2].setTypeAtLeast(inputreq[2]);
		output[3].setTypeAtLeast(inputreq[3]);
		output[4].setTypeAtLeast(inputreq[4]);

		read[0].setTypeAtLeast(ack[0]);
		read[1].setTypeAtLeast(ack[1]);
		read[2].setTypeAtLeast(ack[2]);
		read[3].setTypeAtLeast(ack[3]);
		read[4].setTypeAtLeast(ack[4]);

		// instantiates the state-holding arrays
		packetsize = new int[5];
		state = new int[5];
		muxin = new int[5];
		muxout = new int[5];
		freeoutput = new boolean[5];
		payloaddone = new double[5];
		ack_value = new boolean[5];
		next_sent = new boolean[5];
		sent = new boolean[5];

		// instantiates the scope arrays
		// InputScope
		in_flits = new int[5];
		in_packets = new int[5];
		input_time = new Vector[5];
		input_counter = new int[5];
		input_counter_old = new int[5];
		// OutputScope
		out_flits = new int[5];
		out_packets = new int[5];
		output_time = new Vector[5];
		output_counter = new int[5];
		output_counter_old = new int[5];
		// PowerScope
		actual_flit = new int[5];
		last_flit = new int[5];
		power_collector = new int[5];
		power_counter = new int[5];
		power_counter_old = new int[5];
		power_time = new Vector[5];
		// HotSpotScope
		hotspot_collector = new int[5];
		hotspot_counter = new int[5];
		hotspot_counter_old = new int[5];
		hotspot_time = new Vector[5];
		// BufferScope
		bufferSize = new int[5];
		// PointToPointScope
		sourceX = new int[5];
		targetX = new int[5];
		sourceY = new int[5];
		targetY = new int[5];
	}

	/**
	 * Method called once by the director when simulation starts; responsible to
	 * initialize variables, state machines and scopes.
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();

		// all output ports are free upon start-up
		for (int i = 0; i < 5; i++) {
			freeoutput[i] = true;
			sent[i] = false;
			muxin[i] = -1;
			// InputScope
			in_flits[i] = 0;
			in_packets[i] = 0;
			input_counter[i] = 0;
			input_counter_old[i] = 0;
			input_time[i] = new Vector();
			// OutputScope
			out_flits[i] = 0;
			out_packets[i] = 0;
			output_counter[i] = 0;
			output_counter_old[i] = 0;
			output_time[i] = new Vector();
			// PowerScope
			actual_flit[i] = 0;
			last_flit[i] = 0;
			power_collector[i] = 0;
			power_counter[i] = 0;
			power_counter_old[i] = 0;
			power_time[i] = new Vector();
			// HotSpotScope
			hotspot_collector[i] = 0;
			hotspot_counter[i] = 0;
			hotspot_counter_old[i] = 0;
			hotspot_time[i] = new Vector();
			// BufferScope
			bufferSize[i] = 0;
			// PointToPointScope
			sourceX[i] = 0;
			targetX[i] = 0;
			sourceY[i] = 0;
			targetY[i] = 0;
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

		// get the top level workspace
		container = getContainer().getContainer();
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
					if (l2.get(j).toString().indexOf("BufferScopeActor") != -1) {
						// get the BufferScopeActor
						bufferScopeActor = (BufferScopeActor) l2.get(j);
					}
					// verify if it is the desired actor
					else if (l2.get(j).toString().indexOf("OutputScopeActor") != -1) {
						// get the BufferScopeActor
						outputScopeActor = (OutputScopeActor) l2.get(j);
					}
					// verify if it is the desired actor
					else if (l2.get(j).toString().indexOf("InputScopeActor") != -1) {
						// get the BufferScopeActor
						inputScopeActor = (InputScopeActor) l2.get(j);
					}
					// verify if it is the desired actor
					else if (l2.get(j).toString().indexOf("HotSpotScopeActor") != -1) {
						// get the HotSpotScopeActor
						hotspotScopeActor = (HotSpotScopeActor) l2.get(j);
					}
					// verify if it is the desired actor
					else if (l2.get(j).toString().indexOf("PowerScopeActor") != -1) {
						// get the HotSpotScopeActor
						powerScopeActor = (PowerScopeActor) l2.get(j);
					}
					// verify if it is the desired actor
					else if (l2.get(j).toString().indexOf("EndToEndScopeActor") != -1) {
						// get the EndToEndScopeActor
						endToEndScopeActor = (EndToEndScopeActor) l2.get(j);
					}
					// verify if it is the desired actor
					else if (l2.get(j).toString()
							.indexOf("PointToPointScopeActor") != -1) {
						// get the PointToPointScopeActor
						pointToPointScopeActor = (PointToPointScopeActor) l2
								.get(j);
					}
				}
			}
		}

		if (bufferScopeActor == null)
			System.out.println("BufferScopeActor not found.");
		if (outputScopeActor == null)
			System.out.println("OutputScopeActor not found.");
		if (inputScopeActor == null)
			System.out.println("InputScopeActor not found.");
		if (hotspotScopeActor == null)
			System.out.println("HotspotScopeActor not found.");
		if (powerScopeActor == null)
			System.out.println("PowerScopeActor not found.");
		if (endToEndScopeActor == null)
			System.out.println("EndToEndScopeActor not found.");
		if (pointToPointScopeActor == null)
			System.out.println("PointToPointScopeActor not found.");

		// get the measuring window size available in the NoCScopeActor and
		// reachable by any scope
		try {
			windowSize = endToEndScopeActor.getWindowSize();
		} catch (NullPointerException npe) {
		}

		// getting values from workspace
		IntToken cyc = (IntToken) acycles.getToken();
		DoubleToken per = (DoubleToken) period.getToken();

		// instantiates arbitrationtime object
		arbitrationtime = new Time(getDirector());

		// calculate arbitration time by multiplying the acycles by the period
		for (int i = 0; i < cyc.intValue(); i++)
			arbitrationtime = arbitrationtime.add(per.doubleValue());

		// debug
		if (_debugging)
			_debug("Cycles: " + cyc.intValue() + "Period: " + per.doubleValue()
					+ "Arbitration time: " + arbitrationtime);
	}

	/**
	 * Method called before every fire; responsible to update the state machine
	 * of the router.
	 */
	public boolean prefire() throws IllegalActionException {
		if (_debugging)
			_debug("====================== Time = "
					+ getDirector().getModelTime() + " =======================");

		// pass from PAYLOADDONE state to REQUESTING in the same cycle
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

	/**
	 * Implements most of the arbiter functionality: <br>
	 * 1 - check if are there header tokens on any inputs; if so, arbitrate,
	 * route, lock output port, set muxin and muxout, then send token to output <br>
	 * 2 - check if are there size tokens on any inputs; if so, reinitialize
	 * size state variable then send token to output <br>
	 * 3 - send payload tokens to output port <br>
	 * 4 - read ack tokens, forward them to the buffer, update states if needed,
	 * decrement size state variable <br>
	 */
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
						+ " attempting to establish connection.");
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
		if (arbiterstate == ACHECKPORT) {
			// updates the latest arbitrated port. This has to be HERE because
			// the arbiter
			// always rotates even though the output port for the arbitrated
			// input port is used.
			// WARNING: changing this may lead to DEADLOCK
			arbitersel = arbiterreq;
			// checks if desired output port is free
			if (freeoutput[arbiterdir]) {
				// allocates output port
				freeoutput[arbiterdir] = false;
				// debug message
				if (_debugging)
					_debug(getPortName(arbitersel) + "->"
							+ getPortName(arbiterdir) + " now established.");
				// updates switch matrix
				muxin[arbitersel] = arbiterdir;
				muxout[arbiterdir] = arbitersel;
				// go to send state
				arbiterstate = ASEND;
				// schedules a firing in one cycle
				atime = getDirector().getModelTime();
				double dob = ((DoubleToken) period.getToken()).doubleValue() * 2;
				getDirector().fireAt(this,
						getDirector().getModelTime().add(dob));
			} else {
				// the chosen output port is busy, round the round-robin, so a
				// next request can be served
				arbiterstate = AREADY;
				// debug
				if (_debugging)
					_debug(getPortName(arbitersel) + "->"
							+ getPortName(arbiterdir)
							+ " NOT established. Output is busy.");
			}
			// debug
			printRoutingTable();
		}
		// output port is free, set the state to send the header out if 1 cycle
		// has passed
		else if (arbiterstate == ASEND) {
			// if the arbitration time has passed, it is possible to continue
			double dob = ((DoubleToken) period.getToken()).doubleValue() * 2;
			if (getDirector().getModelTime().equals(atime.add(dob))) {
				// now the chosen input port can start forwarding data
				state[arbitersel] = REQUESTGRANTED;
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
			next_sent[muxout[i]] = false;
		}

		// correctly set the previous initialized variables with correct value
		// for this fire
		for (int i = 0; i < 5; i++) { // for each ack port
			// if a ack-nack has arrived
			if (ack[i].hasToken(0)) {
				// set a flit to be sent again
				next_sent[muxout[i]] = true;
				// store in ack_value if an ACK or a NACK was received
				ack_value[muxout[i]] = ((BooleanToken) ack[i].get(0))
						.booleanValue();
				// if it is a ack
				if (ack_value[muxout[i]]) {
					// OUTPUTSCOPE: increment the number of flits sent by this
					// output port
					out_flits[i]++;
					// BUFFERSCOPE: reduce the buffer queue size if the flit was
					// successfully sent
					bufferSize[muxout[i]]--;
					// BUFFERSCOPE: update the BufferScope window
					buffer_update = true;
					// POWERSCOPE
					powerUpdate(i);
					// debug
					if (_debugging)
						_debug(getPortName(muxout[i]) + "<-" + getPortName(i)
								+ " received ack");
				}
				// if it is a nack
				else {
					// HOTSPOTSCOPE
					hotspotScopeUpdate(i);
					// debug
					if (_debugging)
						_debug(getPortName(muxout[i]) + "<-" + getPortName(i)
								+ " received nack");
				}
			}
		}

		// ***************************
		// THIRD: PROCESS ROUTED FLITS
		// ***************************
		String payload_s = "";

		for (int i = 0; i < 5; i++) { // for each input port
			// ///////////////////////////
			// TARGET FLIT //
			// ///////////////////////////
			if (state[i] == REQUESTGRANTED) {
				// if a token has arrived in the input data
				if (inputreq[i].hasToken(0)) {
					// verify if this flit has not been sent yet
					if (!sent[i]) {
						// get the size flit token
						RecordToken rec = (RecordToken) inputreq[i].get(0);
						// send the token to the remote output buffer
						output[muxin[i]].send(0, rec);
						// set flit as sent
						sent[i] = true;
						// debug: getting the payload value
						int payload = ((IntToken) rec.get("payload"))
								.intValue();
						// POWERSCOPE: capturing the data in the output channel
						// to be measured
						actual_flit[i] = payload;
						// BUFFERSCOPE
						bufferScopeUpdate(i, rec);
						// INPUTSCOPE: increment the number of packets received
						// by this input port
						inputScopeUpdate(i);
						// INPUTSCOPE: increment the number of flits received by
						// this input port
						in_flits[i]++;
						// ENDTOENDSCOPE: detect new connections (only the ones
						// coming from input port)
						if (i == LOCAL) {
							// ENDTOENDSCOPE: get source coordinates of the
							// packet to be sent
							int sourceX = ((IntToken) x.getToken()).intValue();
							int sourceY = ((IntToken) y.getToken()).intValue();
							// ENDTOENDSCOPE: get target coordinates of the
							// packet to be sent
							int targetX = ((IntToken) rec.get("x")).intValue();
							int targetY = ((IntToken) rec.get("y")).intValue();
							// ENDTOENDSCOPE: get the time that the packet is
							// entering the NoC
							double timeIn = new Double(getDirector()
									.getModelTime().getDoubleValue());
							// ENDTOENDSCOPE: create a new connection from
							// source switch to target switch
							try {
								endToEndScopeActor
										.getSwitch(sourceX, sourceY)
										.addConnection(targetX, targetY, timeIn);
							} catch (NullPointerException npe) {
							}
						}
						sourceX[i] = ((IntToken) record.get("source_x"))
								.intValue();
						targetX[i] = ((IntToken) record.get("x")).intValue();
						sourceY[i] = ((IntToken) record.get("source_y"))
								.intValue();
						targetY[i] = ((IntToken) record.get("y")).intValue();
						// debug: changing payload to a hexadecimal value
						payload_s = (Integer.toHexString(payload).toUpperCase());
						// debug: formating the payload flit
						for (int j = payload_s.length(); j < 4; j++)
							payload_s = "0" + payload_s;
						// debug
						if (_debugging)
							_debug(getPortName(i) + "->"
									+ getPortName(muxin[i])
									+ " sending header to target " + payload_s);
					}
					// if flit has already been sent but needs to be sent again
					else if (next_sent[i]) {
						// if a nack was received, send again
						if (!ack_value[i]) {
							// get the size flit token
							RecordToken rec = (RecordToken) inputreq[i].get(0);
							// send the token to the remote output buffer again
							output[muxin[i]].send(0, rec);
							// debug: getting the payload value
							int payload = ((IntToken) rec.get("payload"))
									.intValue();
							// debug: changing payload to a hexadecimal value
							payload_s = (Integer.toHexString(payload)
									.toUpperCase());
							// debug: formating the payload flit
							for (int j = payload_s.length(); j < 4; j++)
								payload_s = "0" + payload_s;
							// debug
							if (_debugging)
								_debug(getPortName(i) + "->"
										+ getPortName(muxin[i])
										+ " sending header to target "
										+ payload_s + " again");
						}
						// if an ack was received, go to next state
						else {
							// go to SIZE state
							state[i] = SIZE;
							// discard input
							inputreq[i].get(0);
							// delete flit from buffer
							read[i].send(0, new Token());
							// reset flit sent information
							sent[i] = false;
							// debug
							if (_debugging)
								_debug(getPortName(i) + "->"
										+ getPortName(muxin[i])
										+ " going to size state");
						}
					} else {
						// flit discarded
						inputreq[i].get(0);
					}
				}
			}
			// ///////////////////////////
			// SIZE FLIT //
			// ///////////////////////////
			else if (state[i] == SIZE) {
				// if a token has arrived in the input data
				if (inputreq[i].hasToken(0)) {
					// verify if this flit has not been sent yet
					if (!sent[i]) {
						// get the size flit token
						RecordToken rec = (RecordToken) inputreq[i].get(0);
						// send the token to the remote output buffer
						output[muxin[i]].send(0, rec);
						// update the size array information
						packetsize[i] = ((IntToken) rec.get("size")).intValue();
						// POWERSCOPE: capturing the data in the output channel
						// to be measured
						actual_flit[i] = packetsize[i];
						// BUFFERSCOPE
						bufferScopeUpdate(i, rec);
						// INPUTSCOPE: increment the number of flits received by
						// this input port
						in_flits[i]++;
						// set flit as sent
						sent[i] = true;
						// debug
						if (_debugging)
							_debug(getPortName(i) + "->"
									+ getPortName(muxin[i]) + " size "
									+ packetsize[i]);
					}
					// if flit has already been sent but needs to be sent again
					else if (next_sent[i]) {
						// if a nack was received, send again
						if (!ack_value[i]) {
							// get the size flit token (again, if there was a
							// nack)
							RecordToken rec = (RecordToken) inputreq[i].get(0);
							// send the token to the remote output buffer again
							output[muxin[i]].send(0, rec);
							// update the size array information again
							packetsize[i] = ((IntToken) rec.get("size"))
									.intValue();
							// debug
							if (_debugging)
								_debug(getPortName(i) + "->"
										+ getPortName(muxin[i]) + " size "
										+ packetsize[i] + " again");
						}
						// if an ack was received, go to next state
						else {
							// go to PAYLOAD state
							state[i] = PAYLOAD;
							// discard input
							inputreq[i].get(0);
							// delete flit from buffer
							read[i].send(0, new Token());
							// reset flit sent information
							sent[i] = false;
							// debug
							if (_debugging)
								_debug(getPortName(i) + "->"
										+ getPortName(muxin[i])
										+ " going to payload state");
						}
					} else {
						// flit discarded
						inputreq[i].get(0);
					}
				}
			}
			// ///////////////////////////
			// PAYLOAD FLITS //
			// ///////////////////////////
			else if (state[i] == PAYLOAD) {
				// if a token has arrived in the input data
				if (inputreq[i].hasToken(0)) {
					// verify if this flit has not been sent yet
					if (!sent[i]) {
						// get the size flit token
						RecordToken rec = (RecordToken) inputreq[i].get(0);
						// send the token to the remote output buffer
						output[muxin[i]].send(0, rec);
						// set flit as sent
						sent[i] = true;
						// debug: getting the payload value
						int payload = ((IntToken) rec.get("payload"))
								.intValue();
						// POWERSCOPE: capturing the data in the output channel
						// to be measured
						actual_flit[i] = payload;
						// BUFFERSCOPE
						bufferScopeUpdate(i, rec);
						// INPUTSCOPE: increment the number of flits received by
						// this input port
						in_flits[i]++;
						// debug: changing payload to a hexadecimal value
						payload_s = (Integer.toHexString(payload).toUpperCase());
						// debug: formating the payload flit
						for (int j = payload_s.length(); j < 4; j++)
							payload_s = "0" + payload_s;
						// debug
						if (_debugging)
							_debug(getPortName(i) + "->"
									+ getPortName(muxin[i]) + " flit("
									+ packetsize[i] + ")=" + payload_s);
					}
					// if flit has already been sent but needs to be sent again
					else if (next_sent[i]) {
						// if a nack was received, send again
						if (!ack_value[i]) {
							// get the size flit token
							RecordToken rec = (RecordToken) inputreq[i].get(0);
							// send the token to the remote output buffer
							output[muxin[i]].send(0, rec);
							// debug: getting the payload value
							int payload = ((IntToken) rec.get("payload"))
									.intValue();
							// debug: changing payload to a hexadecimal value
							payload_s = (Integer.toHexString(payload)
									.toUpperCase());
							// debug: formating the payload flit
							for (int j = payload_s.length(); j < 4; j++)
								payload_s = "0" + payload_s;
							// debug
							if (_debugging)
								_debug(getPortName(i) + "->"
										+ getPortName(muxin[i]) + " flit("
										+ packetsize[i] + ")=" + payload_s
										+ " again");
						}
						// if an ack was received
						else {
							// get the size flit token
							inputreq[i].get(0);
							// delete flit from buffer
							read[i].send(0, new Token());
							// reset flit sent information
							sent[i] = false;
							// decrement packet size
							packetsize[i]--;
							// if packet is finished,
							if (packetsize[i] == 0) {
								// closing the connection
								state[i] = PAYLOADDONE;
								// get the time the packet has finished
								payloaddone[i] = getDirector().getModelTime()
										.getDoubleValue();
								// OUTPUTSCOPE
								outputScopeUpdate(i);
								// debug
								if (_debugging)
									_debug(getPortName(i) + "->"
											+ getPortName(muxin[i])
											+ " packet over.");
								// release output port
								freeoutput[muxin[i]] = true;
								// POINTTOPOINTSCOPE: release output port
								muxin[i] = -1;
							}
						}
					} else {
						// flit discarded
						inputreq[i].get(0);
					}
				} // end CURRENT STATE (PAYLOAD)
			} // end STATE (HEADER, SIZE, PAYLOAD)
		} // end FOR for 5 ports

		// SCOPE
		scopeUpdate();
		scopeWindowUpdate();
		// DEBUG
		debugPrints();

	}

	/**
	 * Performs arbitration among all requesting ports, currently following the
	 * Round-Robin arbitration algorithm implemented on Hermes.
	 * 
	 * @return the input port selected to be routed
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
	 * Checks whether are there header tokens on the input ports of the router.
	 * 
	 * @return <code> true </code> if there is at least one input port
	 *         requesting arbitration
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
	 * Consumes and discards all requests arriving to the arbiter.
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
	 * Routes a given packet based on its XY destination coordinates; currently
	 * implements the XY routing algorithm of Hermes.
	 * 
	 * @param record
	 *            the header token that contains the destination of the packet
	 * @return the output port resulted from the routing algorithm
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

	/**
	 * Update the power consumption information
	 * 
	 * @param i
	 *            the port to be updated
	 */
	protected void powerUpdate(int i) {
		// compare the previous transitions of the data in the output port with
		// actual
		int dif = last_flit[i] ^ actual_flit[i];
		// debug: changing flits to binary values
		String diff = (Integer.toBinaryString(dif));
		String last = (Integer.toBinaryString(last_flit[i]));
		String actual = (Integer.toBinaryString(actual_flit[i]));
		// reset dif to zero
		dif = 0;
		// count the number of "ones" in the difference
		for (int j = 0; j < diff.length(); j++) {
			// store the number of ones in dif
			if (diff.charAt(j) == '1')
				dif++;
			// for each one, put the time in a Vector (Vector is cleaned in
			// updateScopeTime)
			power_time[i].add(new Double(getDirector().getModelTime()
					.getDoubleValue()));
			// increment one bit transition when using window based scope
			power_counter[i]++;
			// repaint the PowerScope in this fire
			power_update = true;
		}
		// debug
		if (_debugging)
			_debug("last=" + last + " actual=" + actual + " dif=" + dif);
		// old flit is now the new flit
		last_flit[i] = actual_flit[i];
		// sum all the data transitions of an output port
		power_collector[i] = power_collector[i] + dif;
		// debug
		if (_debugging)
			_debug("power_collector=" + power_collector[i]);
	}

	/**
	 * Capture the buffer occupation from inside the flit and update the value
	 * to be presented in the bufferScope window
	 * 
	 * @param i
	 *            index of the router to be updated
	 * @param rec
	 *            recordToken (flit) received that contains the information
	 *            about the buffer occupation
	 */
	protected void bufferScopeUpdate(int i, RecordToken rec) {
		// BUFFERSCOPE: capture the buffer size information stored inside the
		// token by the buffer
		int bufSize = ((IntToken) rec.get("buffer_size")).intValue();
		// BUFFERSCOPE: test if the buffer size is different from before, if so
		// update it
		if (bufferSize[i] != bufSize) {
			bufferSize[i] = bufSize;
			buffer_update = true;
		}
	}

	/**
	 * Store the time a NACK was received, update a global counter of received
	 * NACKS, update the local counter of received NACKS of the current window
	 * and set the hotspotScope to be repainted in this fire.
	 * 
	 * @param i
	 *            index of the router to be updated
	 */
	protected void hotspotScopeUpdate(int i) {
		// HOTSPOTSCOPE: capture the time a packet received a nack
		hotspot_time[i].add(new Double(getDirector().getModelTime()
				.getDoubleValue()));
		// HOTSPOTSCOPE: increment the number of received nacks
		hotspot_collector[i]++;
		// HOTSPOTSCOPE: increment the number of received nacks when using
		// window based scope
		hotspot_counter[i]++;
		// HOTSPOTSCOPE: repaint HotSpotScope in this fire
		hotspot_update = true;
	}

	/**
	 * Increment the number of sent packets by a specific output port of the
	 * router, update the counter of sent packets inside the current window and
	 * set the outputScope to be repainted in this fire.
	 * 
	 * @param i
	 *            index of the router to be updated
	 */
	protected void outputScopeUpdate(int i) {
		// OUTPUTSCOPE: count the number of sent packets
		out_packets[muxin[i]]++;
		// OUTPUTSCOPE: capture the time a packet has finished to be sent and
		// put it into a vector
		output_time[muxin[i]].add(new Double(getDirector().getModelTime()
				.getDoubleValue()));
		// OUTPUTSCOPE: increment the number of sent packets when using window
		// based scopes
		output_counter[i]++;
		// OUTPUTSCOPE: repaint the OutputScope in this fire
		output_update = true;
	}

	/**
	 * Increment the number of sent packets by a specific output port of the
	 * router, update the counter of sent packets inside the current window and
	 * set the outputScope to be repainted in this fire.
	 * 
	 * @param i
	 *            index of the router to be updated
	 */
	protected void inputScopeUpdate(int i) {
		// INPUTSCOPE: count the number of received packets
		in_packets[i]++;
		// INPUTSCOPE: capture the time a packet has started to be received and
		// put it into a vector
		input_time[i].add(new Double(getDirector().getModelTime()
				.getDoubleValue()));
		// INPUTSCOPE: increment the number of received packets when using
		// window based scopes
		input_counter[i]++;
		// INPUTSCOPE: repaint the InputScope in this fire
		input_update = true;
	}

	/**
	 * Update the Vector times of a given scope, paint the new scope and print
	 * info in the router debug screen
	 * 
	 * @param scope_time
	 *            contains the times that a data of this scope was capture
	 * @param scope_name
	 *            name of this scope
	 */
	protected void scopeTimeUpdate(Vector[] scope_time, String scope_name)
			throws IllegalActionException {
		String print = "";
		double timeNow = getDirector().getModelTime().getDoubleValue();
		double timeTemp;

		// printing output port information
		print = "";
		// check all ports of the router
		for (int i = 0; i < 5; i++) {
			// check all scope times
			for (int j = 0; j < scope_time[i].size(); j++) {
				try {
					// get a time from the vector
					Object obj = scope_time[i].get(j);
					String str = obj.toString();
					timeTemp = Double.valueOf(str).doubleValue();
					// verify if the time in the scope vector was received a
					// long time ago
					if (timeNow - timeTemp > windowSize)
						// remove the old value of the beginning of the vector
						scope_time[i].removeElementAt(0);
					// the packet has been sent a short time ago
					else {
						// stop the measuring of this port in this moment
						break;
					}
				} catch (NullPointerException npe) {
					System.out
							.println("Unexpected error while acquiring data to Scope");
					break;
				}
			}
			print = print
					+ ("[" + getPortName(i) + "]=" + scope_time[i].size() + " ");
		}

		if (scope_name.equals("HotSpotScope")) {
			print = print + "nacks received recently";
			hotspotScopeActor.paint(((IntToken) x.getToken()).intValue(),
					((IntToken) y.getToken()).intValue(), scope_time[0].size(),
					scope_time[1].size(), scope_time[2].size(),
					scope_time[3].size(), scope_time[4].size());
			// hotspotScopeActor.paint(((IntToken)x.getToken()).intValue(),((IntToken)y.getToken()).intValue(),hotspot_counter[0],hotspot_counter[1],hotspot_counter[2],hotspot_counter[3],hotspot_counter[4]);
		} else if (scope_name.equals("PowerScope")) {
			print = print + "bit transitions received recently";
			powerScopeActor.paint(((IntToken) x.getToken()).intValue(),
					((IntToken) y.getToken()).intValue(), scope_time[0].size(),
					scope_time[1].size(), scope_time[2].size(),
					scope_time[3].size(), scope_time[4].size());
			// powerScopeActor.paint(((IntToken)x.getToken()).intValue(),((IntToken)y.getToken()).intValue(),power_counter[0],power_counter[1],power_counter[2],power_counter[3],power_counter[4]);
		} else if (scope_name.equals("OutputScope")) {
			print = print + "packets sent recently";
			outputScopeActor.paint(((IntToken) x.getToken()).intValue(),
					((IntToken) y.getToken()).intValue(), scope_time[0].size(),
					scope_time[1].size(), scope_time[2].size(),
					scope_time[3].size(), scope_time[4].size());
			// outputScopeActor.paint(((IntToken)x.getToken()).intValue(),((IntToken)y.getToken()).intValue(),output_counter[0],output_counter[1],output_counter[2],output_counter[3],output_counter[4]);
			endToEndScopeActor.paintOutput(
					((IntToken) x.getToken()).intValue(),
					((IntToken) y.getToken()).intValue(),
					outputScopeActor.getBiggestLocalPort(),
					output_time[LOCAL].size());
		} else if (scope_name.equals("InputScope")) {
			print = print + "packets received recently";
			inputScopeActor.paint(((IntToken) x.getToken()).intValue(),
					((IntToken) y.getToken()).intValue(), scope_time[0].size(),
					scope_time[1].size(), scope_time[2].size(),
					scope_time[3].size(), scope_time[4].size());
			// inputScopeActor.paint(((IntToken)x.getToken()).intValue(),((IntToken)y.getToken()).intValue(),input_counter[0],input_counter[1],input_counter[2],input_counter[3],input_counter[4]);
			endToEndScopeActor.paintInput(((IntToken) x.getToken()).intValue(),
					((IntToken) y.getToken()).intValue(),
					inputScopeActor.getBiggestLocalPort(),
					input_time[LOCAL].size());
		} else if (scope_name.equals("BufferScope")) {
			print = print + "current buffer status";
		}
		if (_debugging)
			_debug(print);
	}

	/**
	 * Test which scopes needed to update the GUI and call the respective
	 * repaint method.
	 * 
	 * @throws IllegalActionException
	 */
	protected void scopeUpdate() throws IllegalActionException {
		// repaint only the scopes needed to be repainted
		if (buffer_update)
			try {
				bufferScopeActor.paint(((IntToken) x.getToken()).intValue(),
						((IntToken) y.getToken()).intValue(), bufferSize[0],
						bufferSize[1], bufferSize[2], bufferSize[3],
						bufferSize[4]);
			} catch (NullPointerException npe) {
			}
		if (hotspot_update)
			try {
				scopeTimeUpdate(hotspot_time, "HotSpotScope");
				// hotspotScopeActor.paint(((IntToken)x.getToken()).intValue(),((IntToken)y.getToken()).intValue(),hotspot_collector[0],hotspot_collector[1],hotspot_collector[2],hotspot_collector[3],hotspot_collector[4]);
			} catch (NullPointerException npe) {
			}
		if (power_update)
			try {
				scopeTimeUpdate(power_time, "PowerScope");
				// powerScopeActor.paint(((IntToken)x.getToken()).intValue(),((IntToken)y.getToken()).intValue(),power_collector[0],power_collector[1],power_collector[2],power_collector[3],power_collector[4]);
			} catch (NullPointerException npe) {
			}
		if (output_update) {
			try {
				// scopeTimeUpdate(output_time,"OutputScope");
				outputScopeActor.paint(((IntToken) x.getToken()).intValue(),
						((IntToken) y.getToken()).intValue(), out_packets[0],
						out_packets[1], out_packets[2], out_packets[3],
						out_packets[4]);
				// outputScopeActor.paint(((IntToken)x.getToken()).intValue(),((IntToken)y.getToken()).intValue(),out_flits[0],out_flits[1],out_flits[2],out_flits[3],out_flits[4]);
			} catch (NullPointerException npe) {
			}
		}
		if (input_update) {
			try {
				// scopeTimeUpdate(input_time,"InputScope");
				inputScopeActor.paint(((IntToken) x.getToken()).intValue(),
						((IntToken) y.getToken()).intValue(), in_packets[0],
						in_packets[1], in_packets[2], in_packets[3],
						in_packets[4]);
				// inputScopeActor.paint(((IntToken)x.getToken()).intValue(),((IntToken)y.getToken()).intValue(),in_flits[0],in_flits[1],in_flits[2],in_flits[3],in_flits[4]);
			} catch (NullPointerException npe) {
			}
		}
		if (input_update || output_update) {
			try {
				pointToPointUpdate();
			} catch (NullPointerException npe) {
			}
		}

		// reinitialize update variable for the next cycle
		input_update = output_update = hotspot_update = power_update = buffer_update = false;
	}

	/**
	 * Update the window time of gathering information used by the scopes
	 */
	protected void scopeWindowUpdate() throws IllegalActionException {
		// if the window counter has reached the window size
		if (windowCounter == windowSize) {
			// do for all router ports
			for (int i = 0; i < 5; i++) {
				// copy the actual value to the old value
				hotspot_counter_old[i] = hotspot_counter[i];
				power_counter_old[i] = power_counter[i];
				output_counter_old[i] = output_counter[i];
				input_counter_old[i] = input_counter[i];
				// reset the actual values in order to be ready to the following
				// window that will start
				hotspot_counter[i] = power_counter[i] = output_counter[i] = input_counter[i] = 0;
			}
			// reset the window counter
			windowCounter = 0;

			// reinitialize update variable for the next cycle
			input_update = output_update = hotspot_update = power_update = buffer_update = true;
			// force at least one call per window to repaint every scope
			scopeUpdate();
			// force repaint
			endToEndScopeActor.update(getDirector().getModelTime()
					.getDoubleValue());
		}

		// increment the window counter
		windowCounter++;
	}

	public void pointToPointUpdate() throws IllegalActionException {
		int addressX = ((IntToken) x.getToken()).intValue();
		int addressY = ((IntToken) y.getToken()).intValue();
		pointToPointScopeActor.setColorE(addressX, addressY, sourceX[0],
				sourceY[0], targetX[0], targetY[0]);
		pointToPointScopeActor.setColorW(addressX, addressY, sourceX[1],
				sourceY[1], targetX[1], targetY[1]);
		pointToPointScopeActor.setColorN(addressX, addressY, sourceX[2],
				sourceY[2], targetX[2], targetY[2]);
		pointToPointScopeActor.setColorS(addressX, addressY, sourceX[3],
				sourceY[3], targetX[3], targetY[3]);
		pointToPointScopeActor.setColorL(addressX, addressY, sourceX[4],
				sourceY[4], targetX[4], targetY[4], arbitersel);
		pointToPointScopeActor.paint(addressX, addressY, muxin[0], muxin[1],
				muxin[2], muxin[3], muxin[4]);
	}

	/**
	 * Call methods to print several informations in the router debugging window
	 */
	public void debugPrints() {
		// printing information to the router debugging window
		printRoutingTable();
		printPortsState();
		printOutFlits();
		printOutPackets();
	}

	/**
	 * Print the routing table in the router debug screen
	 */
	public void printRoutingTable() {
		if (_debugging) {
			// printing routing table
			String print = "";
			for (int j = 0; j < 5; j++)
				if (!freeoutput[j])
					print = print + getPortName(muxout[j]) + "->"
							+ getPortName(j) + " ";
			_debug("Routing table: " + print);
		}
	}

	/**
	 * Print the state of each input port of the router in the router debug
	 * screen
	 */
	public void printPortsState() {
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
				else if (state[i] == SIZE)
					print = print + ("[" + getPortName(i) + "]=SIZE ");
				else if (state[i] == PAYLOAD)
					print = print + ("[" + getPortName(i) + "]=PAYL ");
				else if (state[i] == PAYLOADDONE)
					print = print + ("[" + getPortName(i) + "]=DONE ");
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
	 * Print the output flits counter of each port of the router in the router
	 * debug screen
	 */
	public void printOutFlits() {
		if (_debugging) {
			// printing output port information
			String print = "";
			for (int i = 0; i < 5; i++) {
				print = print
						+ ("[" + getPortName(i) + "]=" + out_flits[i] + " ");
			}
			_debug(print + "output flits counter");
		}
	}

	/**
	 * Print the output packets counter of each port of the router in the router
	 * debug screen
	 */
	public void printOutPackets() {
		if (_debugging) {
			// printing output port information
			String print = "";
			for (int i = 0; i < 5; i++) {
				print = print
						+ ("[" + getPortName(i) + "]=" + out_packets[i] + " ");
			}
			_debug(print + "output packets counter");
		}
	}

	/**
	 * Get the first letter of a port given its number
	 * 
	 * @param i
	 *            the port number
	 * @return the first letter of the port
	 */
	public String getPortName(int i) {
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
	 * Get the current queue size of all buffers of this router
	 * 
	 * @return an <code> int[5] </code> with the queue size of each buffer
	 */
	public int[] getCurrentBuffer() {
		return bufferSize;
	}

	/**
	 * Get the current power consumption of all ports of this router
	 * 
	 * @return an <code> int[5] </code> with the power consumption of each
	 *         buffer
	 */
	public int[] getCurrentPower() {
		return power_counter;
	}

	/**
	 * Get the current number of packets sent by all output ports of this router
	 * 
	 * @return an <code> int[5] </code> with the number of packets sent by each
	 *         output port
	 */
	public int[] getCurrentOutput() {
		return output_counter;
	}

	/**
	 * Get the current number of packets received by all input ports of this
	 * router
	 * 
	 * @return an <code> int[5] </code> with the number of packets sent by each
	 *         input port
	 */
	public int[] getCurrentInput() {
		return input_counter;
	}

	/**
	 * Get the current number of nacks received by all input ports of this
	 * router
	 * 
	 * @return an <code> int[5] </code> with the number of nacks received by
	 *         each input port
	 */
	public int[] getCurrentHotSpot() {
		return hotspot_counter;
	}

	/**
	 * Get the previous power consumption of all ports of this router
	 * 
	 * @return an <code> int[5] </code> with the power consumption of each
	 *         buffer
	 */
	public int[] getPreviousPower() {
		return power_counter_old;
	}

	/**
	 * Get the previous number of packets sent by all output ports of this
	 * router
	 * 
	 * @return an <code> int[5] </code> with the number of packets sent by each
	 *         output port
	 */
	public int[] getPreviousOutput() {
		return output_counter_old;
	}

	/**
	 * Get the previous number of packets received by all input ports of this
	 * router
	 * 
	 * @return an <code> int[5] </code> with the number of packets sent by each
	 *         input port
	 */
	public int[] getPreviousInput() {
		return input_counter_old;
	}

	/**
	 * Get the previous number of nacks received by all input ports of this
	 * router
	 * 
	 * @return an <code> int[5] </code> with the number of nacks received by
	 *         each input port
	 */
	public int[] getPreviousHotSpot() {
		return hotspot_counter_old;
	}

	/**
	 * Last method called before finishing the simulation
	 */
	public void wrapup() throws IllegalActionException {
		super.wrapup();
	}

}
