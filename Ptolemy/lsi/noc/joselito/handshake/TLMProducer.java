package lsi.noc.joselito.handshake;

import java.io.*;
import java.lang.Integer;
import java.util.StringTokenizer;

import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.kernel.util.Attribute;
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

/**
 * 
 * Transaction-Level Model Producer. <br>
 * Read input files of the NoC (these files are usually generated by the Atlas
 * tool <link> http://www.inf.pucrs.br/~gaph/AtlasHtml/AtlasIndex_us.html
 * </link> ) and is responsible to transform the packets into tokens. These
 * tokens are sent to the NoC model.<br>
 * <br>
 * 
 * Copyright (c) 2007 - All rights reserved. <br>
 * 
 * @author Luciano Ost
 * @author Leandro M�ller
 * @version Joselito (Porto Alegre, 12.03.2009)
 */

public class TLMProducer extends TypedAtomicActor {

	// public parameters of the producer
	public Parameter prodX, prodY; // position of the producer on the 2D mesh
	private int sourceX, sourceY; // position of the producer on the 2D mesh in
									// Integer

	// **************** private variables **************** //
	String payload, packet, address, newFileName, line;
	StringTokenizer st;
	private BooleanToken request, req_buffer;
	private boolean end, acktx;
	private boolean packet_forward = false;
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
	private int size, x, y, timestamp_ini, timestamp_sent;
	private RecordToken xy;
	private double timestamp, compare_time, last_flit_time;
	// store the input file
	BufferedReader buff;

	private String[] labels;
	private Token[] values, value_temp;
	protected Time current_time;

	public TLMProducer(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// actor ports
		ack_tx = new TypedIOPort(this, "act_tx", true, false);
		data_out = new TypedIOPort(this, "data_out", false, true);
		ack_tx.setTypeEquals(BaseType.BOOLEAN);

		fileName = new FilePortParameter(this, "fileName");
		fileName.setExpression("System.out");

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"0\" y=\"0\" width=\"46\" "
				+ "height=\"80\" style=\"fill:blue\"/>\n" + "</svg>\n");

		// token contents
		labels = new String[7];
		Type[] types = new Type[7];

		// label fields of the token
		labels[0] = "x";
		labels[1] = "y";
		labels[2] = "size";
		labels[3] = "payload";
		labels[4] = "timestamp_ini";
		labels[5] = "timestamp_sent";
		labels[6] = "simulation_time";

		// type fields of the token
		types[0] = BaseType.INT;
		types[1] = BaseType.INT;
		types[2] = BaseType.INT;
		types[3] = BaseType.STRING;
		types[4] = BaseType.INT;
		types[5] = BaseType.INT;
		types[6] = BaseType.DOUBLE;

		// setting the token type accepted by the output port of the producer
		RecordType declaredType = new RecordType(labels, types);
		data_out.setTypeEquals(declaredType);
	}

	// **************** public TypedIOPort ****************//
	public TypedIOPort data_out, ack_tx;

	/**
	 * The file name to which to write. This is a string with any form accepted
	 * by FilePortParameter. The default value is "System.out".
	 * 
	 * @see FilePortParameter
	 */
	public FilePortParameter fileName;

	// **************************PUBLIC METHODS***********************//

	/**
	 * If the specified attribute is <i>fileName</i> and there is an open file
	 * being written, then close that file. The new file will be opened or
	 * created when it is next written to.
	 * 
	 * @param attribute
	 *            The attribute that has changed.
	 * @exception IllegalActionException
	 *                If the specified attribute is <i>fileName</i> and the
	 *                previously opened file cannot be closed.
	 */
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {
		if (attribute == fileName) {
			// Do not close the file if it is the same file.
			newFileName = ((StringToken) fileName.getToken()).stringValue();
		} else {
			super.attributeChanged(attribute);
		}
	}

	/**
	 * Set the incial state
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();
		getDirector().fireAt(this, 0.0);
		simulation_time = (new java.util.Date()).getTime();

		try {
			// Set Path
			String path = System.getProperties().getProperty("user.dir")
					+ "/lsi/noc/joselito/handshake/";
			buff = new BufferedReader(new InputStreamReader(
					new FileInputStream(path + newFileName)));
		} catch (IOException e) {
		}

		current_time = getDirector().getModelTime();

		state = HEADER_REQUEST;
	}

	/**
	 * Reads lines from input files (Atlas), prepare Recordtokens and send it to
	 * the ouput port.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */

	public boolean prefire() throws IllegalActionException {
		if (_debugging)
			_debug("==================== Time = "
					+ getDirector().getModelTime() + " ====================");

		if (state == HEADER_REQUEST) {
			try {
				// Read one packet of the input traffic file
				packet = buff.readLine();
				if (packet != null) {

					st = new StringTokenizer(packet, " ");
					packet_forward = true;
				} else
					packet_forward = false;
			} catch (IOException e) {
				System.out.println("Input Error");
			}

			if (packet_forward) {
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
				// add four flits to be compatible with the Atlas tool and
				// Hermes simulations.
				// This four flits are used in Hermes to send the timestamp that
				// packet entered in the NoC.
				// We are not using it here because it is easier just to insert
				// this information
				// in another field of the record token.
				size = (Integer.parseInt(st.nextToken(), 16) + 4);
				payload = "";
				try {
					while (true)
						payload = payload + " " + st.nextToken();
				} catch (Exception e) {
				}
				;

				// type fields of the token
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
					state = WAITING_FIRE;
					getDirector().fireAt(this, timestamp);
				}
			}// if has data
		} else {
			if (_debugging)
				_debug("There is something wrong, at: "
						+ getDirector().getModelTime());
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

				// if(_debugging) _debug("HEADER sent at: " +
				// getDirector().getModelTime());
				state = HEADER_ACK;
			} else {
				state = WAITING_FIRE;
			}
		} else if (state == HEADER_ACK) {
			if (ack_tx.hasToken(0)) {
				BooleanToken acktx = (BooleanToken) ack_tx.get(0);
				if (acktx.booleanValue()) {
					state = TRAILER_REQUEST;
					last_flit_time = compare_time + (size * 2);
					getDirector().fireAt(this, last_flit_time);
				} else {
					data_out.send(0, new RecordToken(labels, values));
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

				data_out.send(0, new RecordToken(labels, values));
				state = TRAILER_ACK;
			} else {
				getDirector().fireAt(this, current_time.add(1));
			}
		} // end TRAILER_REQUEST

		else if (state == TRAILER_ACK) {
			if (ack_tx.hasToken(0)) {
				BooleanToken acktx = (BooleanToken) ack_tx.get(0);
				if ((acktx.booleanValue()) && (packet != null)) {
					state = HEADER_REQUEST;
					getDirector().fireAt(this, current_time.add(1));
				} else {
					last_flit_time = compare_time + (size * 2);
					getDirector().fireAt(this, last_flit_time);

					state = TRAILER_REQUEST;
				}
			}// no received ack
			else {
				getDirector().fireAt(this, current_time.add(1));
			}

		} // end HEADER_ACK
	}// end public

	/**
	 * @Function: void wrapup()
	 */
	public void wrapup() throws IllegalActionException {
		try {
			buff.close();
		} catch (IOException e) {
		}
		super.wrapup();
	}

	/**
	 * @return the X address of this producer
	 * @throws IllegalActionException
	 */
	public int getX() throws IllegalActionException {
		return sourceX;
	}

	/**
	 * @return the y address of this producer
	 * @throws IllegalActionException
	 */
	public int getY() throws IllegalActionException {
		return sourceY;
	}
}