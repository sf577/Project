package lsi.noc.argus;

import java.lang.Integer;
import ptolemy.data.StringToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.BooleanToken;

/**
 * 
 * Transaction-Level Model Consumer. <br>
 * Receive tokens from the NoC model, transform them in Strings and write in a
 * file.<br>
 * <br>
 * 
 * Copyright (c) 2007 - All rights reserved. <br>
 * 
 * @author Leandro Soares Indrusiak
 * @author Luciano Ost
 * @author Leandro Möller
 * @version Argus (Darmstadt, 12.08.2008)
 */
public class TLMConsumer extends TypedAtomicActor {

	// public parameters of the consumer
	public Parameter consX, consY; // position of the consumer on the 2D mesh

	// time variables
	private long timestamp_set, timestamp_sent, timestamp_current, start_time,
			simulation_time;

	// packet variables
	private int x, y, size, flitCounter, payload;

	// state machine
	static final int HEADER = 1;
	static final int SIZE = 2;
	static final int PAYLOAD = 3;
	private int state = HEADER;

	// general variables
	private String size_s, payload_s, output_file, xy;
	private RecordToken record;

	// port variables
	public TypedIOPort data_out, ack, data_in;

	public TLMConsumer(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// instantiates the parameters that hold the grid position of this
		// instance
		// on the 2D mesh
		consX = new Parameter(this, "prodX");
		consX.setTypeEquals(BaseType.INT);
		consY = new Parameter(this, "prodY");
		consY.setTypeEquals(BaseType.INT);

		// NoC Signals declaration
		data_in = new TypedIOPort(this, "data_in", true, false);
		data_out = new TypedIOPort(this, "data_out", false, true);
		data_out.setTypeAtLeast(data_in);
		ack = new TypedIOPort(this, "act_rx", false, true);
		ack.setTypeEquals(BaseType.BOOLEAN);

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"0\" y=\"0\" width=\"46\" "
				+ "height=\"80\" style=\"fill:blue\"/>\n" + "</svg>\n");
	}

	/**
	 * Set the initial state of the consumer
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();

		// get real time information (not from Ptolemy simulation)
		start_time = (new java.util.Date()).getTime();
		// start the state machine in the header state
		state = HEADER;
	}

	/**
	 * State machine responsible to receive tokens from the output data port of
	 * the NoC model, send an ack back, disassembly the packet and send its
	 * flits to the file. If the input does not have a token, do nothing.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public void fire() throws IllegalActionException {

		if (state == HEADER) {
			// if there is incoming data
			if (data_in.hasToken(0)) {
				// send ack back to the NoC router
				ack.send(0, BooleanToken.TRUE);
				// get the incoming token
				record = ((RecordToken) data_in.get(0));
				// get the xy address
				x = ((IntToken) record.get("x")).intValue();
				y = ((IntToken) record.get("y")).intValue();
				// transform the xy address in string
				xy = x + "" + y;
				// complete with zeros if needed
				for (int i = xy.length(); i < 4; i++)
					xy = "0" + xy;
				// store string to be written in the file later
				output_file = xy + " ";
				// go to size state
				state = SIZE;
			}
		}
		if (state == SIZE) {
			// if there is incoming data
			if (data_in.hasToken(0)) {
				// send ack back to the NoC router
				ack.send(0, BooleanToken.TRUE);
				// get the incoming token
				record = ((RecordToken) data_in.get(0));
				// get the size
				size = ((IntToken) record.get("size")).intValue();
				// transform into hexadecimal
				size_s = (Integer.toHexString(size).toUpperCase());
				// complete with zeros if needed
				for (int i = size_s.length(); i < 4; i++)
					size_s = "0" + size_s;
				// store string to be written in the file later
				output_file = output_file + size_s + " ";
				// initialize the number of flits received from this packet
				flitCounter = 0;
				// go to payload state
				state = PAYLOAD;
			}
		} else if (state == PAYLOAD) {
			// if there is incoming data
			if (data_in.hasToken(0)) {
				// send ack back to the NoC router
				ack.send(0, BooleanToken.TRUE);
				// get the incoming token
				record = ((RecordToken) data_in.get(0));
				// get one flit
				payload = ((IntToken) record.get("payload")).intValue();
				// transform into hexadecimal
				payload_s = (Integer.toHexString(payload).toUpperCase());
				// complete with zeros if needed
				for (int i = payload_s.length(); i < 4; i++)
					payload_s = "0" + payload_s;
				// increment the number of flits received
				flitCounter++;
				// store string to be written in the file later
				output_file = output_file + payload_s + " ";
				// if it is the last flit of the packet
				if (flitCounter == size) {
					// get from the packet the timestamp when the packet was
					// expected to be sent by the producer
					timestamp_set = ((LongToken) record.get("timestamp_ini"))
							.longValue();
					// get from the packet the timestamp when the packet was
					// really sent by the producer
					timestamp_sent = ((LongToken) record.get("timestamp_sent"))
							.longValue();
					// the direct usage of getLongValue() does NOT work, use
					// like this to take the current timestamp
					timestamp_current = (long) getDirector().getModelTime()
							.getDoubleValue();
					// latency from source to target
					long latency = timestamp_current - timestamp_sent;
					// get the real clock time
					long current_time = (long) (new java.util.Date()).getTime();
					// get the time in ms that the packet arrived since the
					// beginning of the simulation
					simulation_time = ((current_time - start_time) / 1000);
					// write in the file
					data_out.send(0, new StringToken(output_file
							+ timestamp_set + " " + timestamp_sent + " "
							+ timestamp_current + " " + latency + " "
							+ simulation_time));
					// go back to header state
					state = HEADER;
				}
			} // end has incoming data
		} // end state machine
	} // end fire

	/**
	 * Last method called before finishing the simulation
	 */
	public void wrapup() throws IllegalActionException {
		super.wrapup();
		System.out
				.println("Simulation over, get your results and enjoy it :-)");
	}

	/**
	 * 
	 * @return the X address of this producer
	 * @throws IllegalActionException
	 */
	public int getX() throws IllegalActionException {
		return ((IntToken) consX.getToken()).intValue();
	}

	/**
	 * 
	 * @return the y address of this producer
	 * @throws IllegalActionException
	 */
	public int getY() throws IllegalActionException {
		return ((IntToken) consY.getToken()).intValue();
	}

}// end class