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

public class TLMRecordGen extends TypedAtomicActor {

	// **************** private variables **************** //
	String packet, address;
	StringTokenizer st;
	private BooleanToken request, req_buffer;
	private boolean end, acktx;
	private boolean request_sent = false;
	private boolean request_file = false;
	private double simulation_time;

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
	private int size, cont, cont2, x, y, timestamp_ini, timestamp_sent;
	private RecordToken xy;
	private double timestamp, compare_time;

	private Vector lala;
	// private BooleanToken ;

	private String[] labels;
	private Token[] values, value_temp;
	protected Time current_time, nextFlit;

	// timestamp;

	public TLMRecordGen(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		ack_tx = new TypedIOPort(this, "act_tx", true, false);
		data_out = new TypedIOPort(this, "data_out", false, true);
		data_in = new TypedIOPort(this, "data_in", true, false);

		time_out = new TypedIOPort(this, "time_out", false, true);

		// Packet Signals
		end_of_file = new TypedIOPort(this, "end_of_file", true, false);
		end_of_file.setTypeEquals(BaseType.BOOLEAN);

		read_packet = new TypedIOPort(this, "read_packet", false, true);
		read_packet.setTypeEquals(BaseType.BOOLEAN);

		// tx.setTypeEquals(BaseType.BOOLEAN);
		ack_tx.setTypeEquals(BaseType.BOOLEAN);

		// data_in.setTypeEquals(BaseType.STRING);

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
		types[6] = BaseType.DOUBLE;

		RecordType declaredType = new RecordType(labels, types);
		data_out.setTypeEquals(declaredType);
	}

	// **************** public TypedIOPort ****************//
	public TypedIOPort data_out, ack_tx, data_in, read_packet, end_of_file,
			time_out;

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
		time_out.send(0, new StringToken(" file request.."));
		request = BooleanToken.TRUE;
		read_packet.send(0, request);
		request_file = true;

		current_time = getDirector().getModelTime();

		state = REQUESTING;
	}

	/**
	 * According to an arbitration scheme, peeks the input buffers that are not
	 * currently assigned to an output port.
	 * 
	 * It also requests write permission on remote buffers. * @exception
	 * IllegalActionException If there is no director.
	 */

	public boolean prefire() throws IllegalActionException {

		if (state == REQUESTING) {
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
					// time cavalo = (time)l_stamp;
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
					values[2] = new IntToken(999);
					values[3] = new IntToken();
					values[4] = new IntToken(timestamp_ini);
					values[5] = new IntToken();
					values[6] = new DoubleToken(simulation_time);

					// getting packet size
					size = (Integer.parseInt(st.nextToken(), 16) + 4);

					// getting current Time
					current_time = getDirector().getModelTime();

					compare_time = current_time.getDoubleValue();

					if (compare_time >= timestamp) {
						time_out.send(0, new StringToken("TOKEN SENT AT: "
								+ getDirector().getModelTime()));

						timestamp_sent = (int) getDirector().getCurrentTime();
						values[5] = new IntToken(timestamp_sent);
						data_out.send(0, new RecordToken(labels, values));
						state = DISASSEMBLY;
					} else {
						// throw new InternalErrorException("Service time is "
						// + "reached, but output is not available.");
						state = WAITING_FIRE;
						time_out.send(
								0,
								new StringToken(" time fudeu " + timestamp
										+ " cur " + current_time.add(timestamp)));
						getDirector().fireAt(this, current_time.add(timestamp));
						time_out.send(
								0,
								new StringToken(" time fudeu2 " + timestamp
										+ " cur " + current_time.add(timestamp)));

					}
				}// if has data
				else
					time_out.send(0, new StringToken(" NO DATA SACO "));
			} // end_file.booleanValue()
			else {
				if (!request_file) {
					request = BooleanToken.TRUE;
					read_packet.send(0, request);
					request_file = true;
					time_out.send(0, new StringToken("vai caga "));
				}
				state = REQUESTING;
			}
			time_out.send(0, new StringToken("debu timestamp " + timestamp
					+ "current time " + getDirector().getModelTime()));
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
			time_out.send(0, new StringToken(" WAITING_FIRE FIRE "));

			if (compare_time >= timestamp) {
				time_out.send(0, new StringToken("TOKEN SENT AT: "
						+ getDirector().getModelTime()));

				timestamp_sent = (int) getDirector().getCurrentTime();
				values[5] = new IntToken(timestamp_sent);
				data_out.send(0, new RecordToken(labels, values));

				request_sent = true;
				state = DISASSEMBLY;
			} else {
				time_out.send(0, new StringToken(" WAITING_FIRE AQUI de "));
				state = WAITING_FIRE;
			}
		}

		else if (state == DISASSEMBLY) {
			if (ack_tx.hasToken(0)) {
				BooleanToken acktx = (BooleanToken) ack_tx.get(0);
				time_out.send(0, new StringToken(" DISASSEMBLY "));

				if (acktx.booleanValue()) {
					time_out.send(0, new StringToken(" ack_true xy at: "
							+ getDirector().getModelTime()));
					values[2] = new IntToken(size);
					time_out.send(0, new StringToken(" SENDING size " + size
							+ "AT: " + getDirector().getModelTime()));
					data_out.send(0, new RecordToken(labels, values));
					// cont++;
					state = SENDING;
				} else {
					time_out.send(0, new StringToken(" DISASSEMBLY ack false "));
					data_out.send(0, new RecordToken(labels, values));
					state = DISASSEMBLY;
				}
			} else {
				time_out.send(0, new StringToken(" DISASSEMBLY no ack "));
				// data_out.send (0, new RecordToken(labels,values));
				state = DISASSEMBLY;
			}
		} // end DISASSEMBLY

		else if (state == SENDING) {
			if (ack_tx.hasToken(0)) {
				BooleanToken acktx = (BooleanToken) ack_tx.get(0);
				// time_out.send(0, new StringToken(" SENDING fire"));
				if (acktx.booleanValue()) {
					// cont++;
					time_out.send(0, new StringToken(" ack_true "));
					if (cont < (size - 4)) {
						int payload = Integer.parseInt(st.nextToken(), 16);
						values[2] = new IntToken(999);
						values[3] = new IntToken(payload);
						data_out.send(0, new RecordToken(labels, values));
						cont++;
						time_out.send(0, new StringToken(
								" sent payload token add " + cont));
						state = SENDING;
					} else {
						if (cont < size) {
							values[2] = new IntToken(999);
							values[3] = new IntToken("0000");
							data_out.send(0, new RecordToken(labels, values));
							cont++;
							state = SENDING;
							time_out.send(0, new StringToken(
									" sent payload token else 0000 " + cont));
						}

						else if ((cont == size) && (end == false)) {
							time_out.send(0, new StringToken(" file " + cont));
							request = BooleanToken.TRUE;
							read_packet.send(0, request);
							request_file = true;
							state = REQUESTING;
							time_out.send(0, new StringToken(
									"Timestamp last token "
											+ getDirector().getModelTime()));

						}
						// if there is no traffic cabum
						else if ((cont == size) && (end == true)) {
							state = INACTIVE;
							time_out.send(0, new StringToken(" INACTIVE "));
						}
					}

				}
				// if ack = false
				else {
					time_out.send(0, new StringToken(" ack_false "));
					data_out.send(0, new RecordToken(labels, values));
					state = SENDING;
					// if (cont != 0)
					// cont--;
				}
			} else {
				time_out.send(0, new StringToken(" chute 1"));
				// data_out.send (0, new RecordToken(labels,values));
				// request_sent = true;
				state = SENDING;
			}
		}// sendi
	}// end public
}