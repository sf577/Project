/*
Copyright (c) 2007 Luciano C. Ost
All rights reserved.
Eu to com medo.
30/05/07 DIA DE GREMIO X SANTOS*/

package lsi.noc.renato;

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

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;
import java.util.Vector;

public class RecordGen extends TypedAtomicActor {

	// **************** private variables **************** //
	String packet, address;
	StringTokenizer st;
	private BooleanToken request, req_buffer;
	private boolean end, acktx;
	private boolean request_sent = false;
	private boolean request_file = false;
	private long simulation_time;

	// State machine definition
	static final int INACTIVE = 0;
	static final int REQUESTING = 1;
	static final int DISASSEMBLY = 2;
	static final int SENDING = 3;
	static final int WAITING = 4;
	static final int WAITING_FIRE = 5;

	// Values for the state variables
	private int state = INACTIVE;
	private int last_state;
	private int size, cont, x, y, timestamp_ini, timestamp_sent;
	private RecordToken xy;
	private double current_time, timestamp;

	private Vector lala;
	// private BooleanToken ;

	private String[] labels;
	private Token[] values;

	public RecordGen(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// NoC Signals declaration
		clock = new TypedIOPort(this, "clock", true, false);
		tx = new TypedIOPort(this, "tx", false, true);
		ack_tx = new TypedIOPort(this, "act_tx", true, false);

		data_out = new TypedIOPort(this, "data_out", false, true);
		data_in = new TypedIOPort(this, "data_in", true, false);

		time_out = new TypedIOPort(this, "time_out", false, true);

		// Packet Signals
		end_of_file = new TypedIOPort(this, "end_of_file", true, false);
		end_of_file.setTypeEquals(BaseType.BOOLEAN);

		read_packet = new TypedIOPort(this, "read_packet", false, true);
		read_packet.setTypeEquals(BaseType.BOOLEAN);

		tx.setTypeEquals(BaseType.BOOLEAN);
		ack_tx.setTypeEquals(BaseType.BOOLEAN);

		data_in.setTypeEquals(BaseType.STRING);

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
		types[3] = BaseType.INT;
		types[4] = BaseType.INT;
		types[5] = BaseType.INT;
		types[6] = BaseType.LONG;

		RecordType declaredType = new RecordType(labels, types);
		data_out.setTypeEquals(declaredType);
	}

	// **************** public TypedIOPort ****************//
	public TypedIOPort data_out, tx, ack_tx, data_in, clock, read_packet,
			end_of_file, time_out;

	// // public methods ////
	/**
	 * Set the incial state
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();
		getDirector().fireAt(this, 0.0);
		simulation_time = (new java.util.Date()).getTime();
		state = REQUESTING;
		lala = new Vector();
	}

	/**
	 * According to an arbitration scheme, peeks the input buffers that are not
	 * currently assigned to an output port.
	 * 
	 * It also requests write permission on remote buffers. * @exception
	 * IllegalActionException If there is no director.
	 */

	public boolean prefire() throws IllegalActionException {

		// reads clock signal, if available
		if (clock.hasToken(0)) {
			clock.get(0);
			if (state == WAITING_FIRE) {
				time_out.send(0, new StringToken(" WAITING_FIRE "));
				current_time = getDirector().getCurrentTime();
				if (current_time >= timestamp) {
					time_out.send(0, new StringToken("pos timestamp "
							+ timestamp + "cur " + current_time));

					req_buffer = BooleanToken.TRUE;
					tx.send(0, req_buffer);
					request_sent = true;
					state = DISASSEMBLY;
				} else {
					state = WAITING_FIRE;
				}
			} else if (state == REQUESTING) {
				if (!request_file) {
					// request a packet from the LineReader Actor
					request = BooleanToken.TRUE;
					read_packet.send(0, request);
					request_file = true;
				}
			} else if (state == DISASSEMBLY) {
				time_out.send(0, new StringToken(" DISASSEMBLY "));
				if (ack_tx.getWidth() > 0) {
					// time_out.send(0, new StringToken(" merda "));
					acktx = ack_tx.hasToken(0);
					time_out.send(0, new StringToken(" DISASSEMBLY "));
				}

				else if (!request_sent) {
					req_buffer = BooleanToken.TRUE;
					tx.send(0, req_buffer);
					request_sent = true;
				} else
					time_out.send(0, new StringToken(" aula leandro "));
			}

			else if (state == SENDING) {
				if (!request_sent) {
					req_buffer = BooleanToken.TRUE;
					tx.send(0, req_buffer);
					request_sent = true;
				}
			}
		}
		if (acktx)
			return true;
		else
			return super.prefire();

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

		// ************* Receiving file *************************** //
		if (state == REQUESTING) {
			time_out.send(0, new StringToken(" REQUESTING "));
			if (end_of_file.hasToken(0)) {
				end = ((BooleanToken) end_of_file.get(0)).booleanValue();
				// time_out.send(0, new StringToken(" bfdfdasfaosta "));
				request_file = false;
				time_out.send(0, new StringToken(" REQUESTING_FILE "));
				if (data_in.hasToken(0)) {
					// time_out.send(0, new StringToken(" bostas "));
					packet = ((StringToken) data_in.get(0)).stringValue();
					st = new StringTokenizer(packet, " ");
					cont = 0;

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

					values = new Token[7];
					values[0] = new IntToken(x);
					values[1] = new IntToken(y);
					values[2] = new IntToken();
					values[3] = new IntToken();
					values[4] = new IntToken(timestamp_ini);
					values[5] = new IntToken();
					values[6] = new LongToken(simulation_time);

					// getting packet size
					size = (Integer.parseInt(st.nextToken(), 16) + 4);

					xy = new RecordToken(labels, values);

					// getting current Time
					current_time = getDirector().getCurrentTime();
					if (current_time >= timestamp) {
						time_out.send(0, new StringToken("pos timestamp "
								+ timestamp + "cur " + current_time));

						req_buffer = BooleanToken.TRUE;
						tx.send(0, req_buffer);
						request_sent = true;
						state = DISASSEMBLY;
					} else {
						// throw new InternalErrorException("Service time is "
						// + "reached, but output is not available.");
						getDirector().fireAt(this, timestamp);
						state = WAITING_FIRE;
					}
				}// if has data
				else
					state = REQUESTING;
			} // end_file.booleanValue()
			else
				state = REQUESTING;
		}

		else if (state == DISASSEMBLY) {
			if (ack_tx.hasToken(0)) {
				time_out.send(0, new StringToken(" bfdfdasfaosta "));
				// if (acktx){
				BooleanToken vai = (BooleanToken) ack_tx.get(0);
				timestamp_sent = (int) getDirector().getCurrentTime();
				values[5] = new IntToken(timestamp_sent);
				data_out.send(0, xy);
				request_sent = false;
				state = SENDING;
			} else {
				req_buffer = BooleanToken.TRUE;
				tx.send(0, req_buffer);
				request_sent = true;
				state = DISASSEMBLY;
			}
			/*
			 * } else{ req_buffer= BooleanToken.TRUE; tx.send(0, req_buffer);
			 * request_sent = true; state = DISASSEMBLY; }
			 */
		} // end DISASSEMBLY

		else if (state == SENDING) {
			if (ack_tx.hasToken(0)) {
				// ack_tx.get(0);
				BooleanToken acktx = (BooleanToken) ack_tx.get(0);
				// time_out.send(0, new StringToken(" SENDING "));

				if (acktx.booleanValue()) {
					if (cont == 0) {
						values[2] = new IntToken(size);
						data_out.send(0, new RecordToken(labels, values));
						cont++;
						state = SENDING;
						request_sent = false;
					} else if (cont > 0 && cont <= (size - 4)) {
						int payload = Integer.parseInt(st.nextToken(), 16);
						values[3] = new IntToken(payload);
						data_out.send(0, new RecordToken(labels, values));

						lala.addElement(new RecordToken(labels, values));
						time_out.send(0,
								new StringToken("LALA IS NICE" + lala.get(0)));

						cont++;
						if (cont == (size + 1) && (end == false))
							state = REQUESTING;
						// if there is no traffic cabum
						else if (cont == (size + 1) && (end == true)) {
							state = INACTIVE;
							time_out.send(0, new StringToken(" INACTIVE "));
						} else
							state = SENDING;
					} else {
						values[3] = new IntToken("0000");
						data_out.send(0, new RecordToken(labels, values));
						cont++;
						if (cont == (size + 1) && (end == false))
							state = REQUESTING;
						// if there is no traffic cabum
						else if (cont == (size + 1) && (end == true)) {
							state = INACTIVE;
							time_out.send(0, new StringToken(" INACTIVE "));
						} else
							state = SENDING;
					}

				} else {
					req_buffer = BooleanToken.TRUE;
					tx.send(0, req_buffer);
					request_sent = true;
					state = SENDING;
				}
			} else {
				req_buffer = BooleanToken.TRUE;
				tx.send(0, req_buffer);
				request_sent = true;
				state = SENDING;
			}
		}
	}// end public
}