/*
Copyright (c) 2007 Luciano C. Ost
All rights reserved.
Eu to com medo.
30/05/07 DIA DE GREMIO X SANTOS*/

package lsi.noc.joselito;

import java.io.*;
import java.lang.Integer;
import java.util.StringTokenizer;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.Type;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.BooleanToken;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.actor.util.Time;
import ptolemy.data.*;
import ptolemy.data.expr.*;

public class TLMProducer extends TypedAtomicActor {

	// **************** private variables **************** //
	String payload, packet, address;
	StringTokenizer st;
	private BooleanToken request, req_buffer;
	private boolean end, acktx;
	private boolean request_sent = false;
	private boolean request_packet = false;
	private double simulation_time;

	// State machine definition
	static final int INACTIVE = 0;
	static final int HEADER_REQUEST = 1;
	static final int HEADER_ACK = 2;
	static final int TRAILER_REQUEST = 3;
	static final int TRAILER_ACK = 4;
	static final int WAITING_FIRE = 5;

	// Values for the state variables
	private int state = INACTIVE;
	private int number_nack;
	private int size, cont, cont2, x, y, timestamp_ini, timestamp_sent;
	private RecordToken xy;
	private double timestamp, compare_time, last_flit_time;

	private String[] labels;
	private Token[] values, value_temp;
	protected Time current_time;

	public TLMProducer(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		ack_tx = new TypedIOPort(this, "act_tx", true, false);
		data_out = new TypedIOPort(this, "data_out", false, true);
		data_in = new TypedIOPort(this, "data_in", true, false);

		// Packet Signals
		end_of_file = new TypedIOPort(this, "end_of_file", true, false);
		end_of_file.setTypeEquals(BaseType.BOOLEAN);

		read_packet = new TypedIOPort(this, "read_packet", false, true);
		read_packet.setTypeEquals(BaseType.BOOLEAN);

		ack_tx.setTypeEquals(BaseType.BOOLEAN);

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"0\" y=\"0\" width=\"46\" "
				+ "height=\"80\" style=\"fill:blue\"/>\n" + "</svg>\n");

		// set the type constraints.
		labels = new String[7];
		Type[] types = new Type[7];

		labels[0] = "x";
		labels[1] = "y";
		labels[2] = "size";
		labels[3] = "payload";
		labels[4] = "timestamp_ini";
		labels[5] = "timestamp_sent";
		labels[6] = "simulation_time";

		types[0] = BaseType.INT;
		types[1] = BaseType.INT;
		types[2] = BaseType.INT;
		types[3] = BaseType.STRING;
		types[4] = BaseType.INT;
		types[5] = BaseType.INT;
		types[6] = BaseType.DOUBLE;

		RecordType declaredType = new RecordType(labels, types);
		data_out.setTypeEquals(declaredType);
	}

	// **************** public TypedIOPort ****************//
	public TypedIOPort data_out, ack_tx, data_in, read_packet, end_of_file;

	// // public methods ////
	/**
	 * Set the incial state
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();
		getDirector().fireAt(this, 0.0);
		simulation_time = (new java.util.Date()).getTime();
		// getRealStartTimeMillis

		// request a packet from the LineReader Actor
		request = BooleanToken.TRUE;
		read_packet.send(0, request);
		request_packet = true;

		current_time = getDirector().getModelTime();

		state = HEADER_REQUEST;
	}

	/**
	 * According to an arbitration scheme, peeks the input buffers that are not
	 * currently assigned to an output port.
	 * 
	 * It also requests write permission on remote buffers. * @exception
	 * IllegalActionException If there is no director.
	 */

	public boolean prefire() throws IllegalActionException {
		if (_debugging)
			_debug("==================== Time = "
					+ getDirector().getModelTime() + " ====================");

		if (state == HEADER_REQUEST) {
			if (end_of_file.hasToken(0)) {
				end = ((BooleanToken) end_of_file.get(0)).booleanValue();
				request_packet = false;
				if (data_in.hasToken(0)) {
					packet = ((StringToken) data_in.get(0)).stringValue();
					st = new StringTokenizer(packet, " ");
					cont = 0;
					number_nack = 0;

					// getting timestamp
					long l_stamp = Long.parseLong(st.nextToken(), 16);
					timestamp = (double) l_stamp;
					timestamp_ini = (int) timestamp;

					// getting address
					address = st.nextToken();
					if (address.length() > 2) {
						x = Integer.valueOf(address.substring(2, 3)).intValue();
						y = Integer.valueOf(address.substring(3, 4)).intValue();
					} else {
						x = (int) address.charAt(0) - (int) '0';
						y = (int) address.charAt(1) - (int) '0';
					}

					// getting packet size
					size = (Integer.parseInt(st.nextToken(), 16) + 4);
					payload = "";
					try {
						while (true)
							payload = payload + " " + st.nextToken();
					} catch (Exception e) {
					}
					;

					values = new Token[7];
					values[0] = new IntToken(x);
					values[1] = new IntToken(y);
					values[2] = new IntToken(size);
					values[3] = new StringToken(payload);
					values[4] = new IntToken(timestamp_ini);
					values[5] = new IntToken();
					values[6] = new DoubleToken(simulation_time);

					// getting current Time
					current_time = getDirector().getModelTime();
					compare_time = current_time.getDoubleValue();

					if (compare_time >= timestamp) {
						timestamp_sent = (int) getDirector().getCurrentTime();
						values[5] = new IntToken(timestamp_sent);
						data_out.send(0, new RecordToken(labels, values));
						state = HEADER_ACK;

						if (_debugging)
							_debug("HEADER sent at: "
									+ getDirector().getModelTime());
					} else {
						// throw new InternalErrorException("Service time is "
						// + "reached, but output is not available.");
						state = WAITING_FIRE;
						// getDirector().fireAt(this,
						// current_time.add(timestamp));
						getDirector().fireAt(this, timestamp);
					}
				}// if has data

			} // end_file.booleanValue()
			else {
				if (!request_packet) {
					request = BooleanToken.TRUE;
					read_packet.send(0, request);
					request_packet = true;
				}
				state = HEADER_REQUEST;
			}
		}
		return true;

	}

	/**
	 * Read one traffic file from the input port, disassembly the package and
	 * send its flits to the output ports. If the input does not have a token,
	 * suspend firing and return.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public void fire() throws IllegalActionException {
		req_buffer = BooleanToken.FALSE;
		request = BooleanToken.FALSE;

		// getting current Time
		current_time = getDirector().getModelTime();
		compare_time = current_time.getDoubleValue();

		if (state == WAITING_FIRE) {
			if (compare_time >= timestamp) {

				timestamp_sent = (int) getDirector().getCurrentTime();
				values[5] = new IntToken(timestamp_sent);
				data_out.send(0, new RecordToken(labels, values));

				if (_debugging)
					_debug("HEADER sent at: " + getDirector().getModelTime());
				state = HEADER_ACK;
			} else {
				state = WAITING_FIRE;
			}
		} else if (state == HEADER_ACK) {
			if (ack_tx.hasToken(0)) {
				if (_debugging)
					_debug("HEADER_ACK entrei: ");
				BooleanToken acktx = (BooleanToken) ack_tx.get(0);
				if (acktx.booleanValue()) {
					state = TRAILER_REQUEST;
					if (_debugging)
						_debug("HEADER_ACK = TRUE: ");
					last_flit_time = compare_time + (size * 2);
					getDirector().fireAt(this, last_flit_time);
				} else {
					data_out.send(0, new RecordToken(labels, values));
					number_nack = number_nack + 1;
					if (_debugging)
						_debug("HEADER_ACK = FALSE: ");
				}
			}// no ack
			else {
				if (_debugging)
					_debug("NO HEADER_ACK RECEIVED: ");
			}
		} // end HEADER_ACK
		else if (state == TRAILER_REQUEST) {
			if (compare_time >= last_flit_time) {
				values[3] = new StringToken("end");

				// timestamp_sent = (int)getDirector().getCurrentTime();
				// values[5]=new IntToken(timestamp_sent);

				data_out.send(0, new RecordToken(labels, values));
				state = TRAILER_ACK;
				if (_debugging)
					_debug("TRAILER SENT AT: " + getDirector().getModelTime());
			} else {
				if (_debugging)
					_debug("TRAILER_REQUEST: " + "compare_time: "
							+ compare_time + "last_flit_time: "
							+ last_flit_time);
				getDirector().fireAt(this, current_time.add(1));
			}
		} // end TRAILER_REQUEST

		else if (state == TRAILER_ACK) {
			if (ack_tx.hasToken(0)) {
				if (_debugging)
					_debug("TRAILER_ACK entrei: ");
				BooleanToken acktx = (BooleanToken) ack_tx.get(0);
				if (acktx.booleanValue()) {
					state = HEADER_REQUEST;
					request = BooleanToken.TRUE;
					read_packet.send(0, request);
					request_packet = true;
					if (_debugging)
						_debug("ack_tx TRUE: ");
				} else {
					last_flit_time = compare_time + (size * 2);
					getDirector().fireAt(this, last_flit_time);

					// data_out.send (0, new RecordToken(labels,values));
					if (_debugging)
						_debug("ack_tx FALSE: ");
					state = TRAILER_REQUEST;
				}
			}// no ack
			else {
				getDirector().fireAt(this, current_time.add(1));
			}

		} // end HEADER_ACK
	}// end public

	/**
	 * Override the base class to declare that the <i>output</i> does not depend
	 * on the <i>input</i> in a firing.
	 */
	public void pruneDependencies() {
		super.pruneDependencies();
		removeDependency(end_of_file, read_packet);
		removeDependency(data_in, read_packet);
	}
}