//Copyright (c) 2008 Luciano Ost
//All rights reserved.
//Version 2.0	Porto Alegre, 03/08/2008.

package lsi.noc.joselito.handshake;

import java.io.*;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.BooleanToken;
import ptolemy.actor.util.Time;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.kernel.util.Attribute;

public class TLMConsumer extends TypedAtomicActor {

	// PRIVATE VARIABLES //
	private String packet, size_s, payload, output_file, s_xy,
			s_timestamp_rede, s_timestamp_ini, newFileName;
	private BooleanToken accept;
	private RecordToken record, data_token;
	private int simulation_time;
	private FileOutputStream fileOut;
	private DataOutputStream dataOutputSan;

	// TIMESTAP_FF of first flit received by the consumer
	// TIMESTAP_LF of last flit of the packet received by the consumer
	private int x, y, xy, timestamp_ini, timestamp_rede, timestamp_ff,
			timestamp_lf, current_time;

	// VALUES FOR THE STATE VARIABLES
	static final int INACTIVE = 0;
	static final int RECEIVING_PACKET = 1;
	static final int RECEIVING_SIZE = 2;
	static final int SENDING = 3;

	// STATE MACHINE DEFINITION
	private int state = INACTIVE;
	private int size, cont;

	public TLMConsumer(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		fileName = new FilePortParameter(this, "fileName");
		fileName.setExpression("System.out");

		// NOC SIGNALS DECLARATION
		ack = new TypedIOPort(this, "ack", false, true);
		data_in = new TypedIOPort(this, "data_in", true, false);
		ack.setTypeEquals(BaseType.BOOLEAN);

		data_in.setTypeEquals(BaseType.GENERAL);

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"0\" y=\"0\" width=\"46\" "
				+ "height=\"80\" style=\"fill:blue\"/>\n" + "</svg>\n");
	}

	// **************** public TypedIOPort ****************//
	public TypedIOPort data_out, ack, data_in;

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
		state = RECEIVING_PACKET;

		try {
			// Set Path
			String path = System.getProperties().getProperty("user.dir")
					+ "/lsi/noc/joselito/handshake/";

			// Write to temp file
			fileOut = new FileOutputStream(path + newFileName);
			dataOutputSan = new DataOutputStream(fileOut);
		} catch (IOException e) {
		}
	}

	/**
	 * According to an arbitration scheme, peeks the It also requests write
	 * permission on file.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public boolean prefire() throws IllegalActionException {
		// reads data_in signal, if available an state is selected
		if (data_in.getWidth() > 0) {
			if (_debugging)
				_debug("newFileName: " + newFileName);
			if (state == RECEIVING_PACKET) {
				accept = BooleanToken.TRUE;
				ack.send(0, accept);
			} else if (state == RECEIVING_SIZE) {
				accept = BooleanToken.TRUE;
				ack.send(0, accept);
			}
		}
		return true;
	}

	/**
	 * State machine responsable by receiving the traffic from the data_in port,
	 * disassembly the package and send its flits to the output ports (file). If
	 * the data_in does not have a token, suspend firing and return.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public void fire() throws IllegalActionException {
		accept = BooleanToken.FALSE;

		// **************Receiving file**************************** //
		if (state == RECEIVING_PACKET) {
			if (data_in.hasToken(0)) {
				cont = 0;
				// get the timestamp of the first flit received from the NoC
				timestamp_ff = (int) getDirector().getCurrentTime();

				// get the packet
				record = ((RecordToken) data_in.get(0));

				// get the xy adress
				x = ((IntToken) record.get("x")).intValue();
				y = ((IntToken) record.get("y")).intValue();
				s_xy = (Integer.toString(x)) + (Integer.toString(y));
				for (int i = s_xy.length(); i < 4; i++)
					s_xy = "0" + s_xy;

				size = ((IntToken) record.get("size")).intValue();
				size_s = (Integer.toHexString(size).toUpperCase());
				for (int i = size_s.length(); i < 4; i++)
					size_s = "0" + size_s;

				payload = ((StringToken) record.get("payload")).stringValue();
				packet = s_xy + " " + size_s + payload;

				// set new state
				state = RECEIVING_SIZE;
			} else {
				state = RECEIVING_PACKET;
			}
		}// end if RECEIVING_PACKET

		else if (state == RECEIVING_SIZE) {
			if (data_in.hasToken(0)) {
				record = ((RecordToken) data_in.get(0));

				for (int i = 0; i < 4; i++)
					packet = packet + " 0000";

				timestamp_ini = ((IntToken) record.get("timestamp_ini"))
						.intValue();
				timestamp_rede = ((IntToken) record.get("timestamp_sent"))
						.intValue();

				// timestamp of the last flit of the packet
				timestamp_lf = (int) getDirector().getCurrentTime();

				int latency = timestamp_lf - timestamp_rede;

				double ini_simulation_time = ((DoubleToken) record
						.get("simulation_time")).doubleValue();
				double simu = (new java.util.Date()).getTime();
				simulation_time = (int) ((simu - ini_simulation_time) / 1000);

				try {
					// Write to file
					dataOutputSan.writeBytes(packet + " " + timestamp_ini + " "
							+ timestamp_rede + " " + timestamp_lf + " "
							+ latency + " " + simulation_time + "\n");
				} catch (IOException e) {
				}
				state = RECEIVING_PACKET;
			} else {
				state = RECEIVING_SIZE;
			}
		}// end if RECEIVING_PACKET
	}// end public

	/**
	 * @Function: void wrapup()
	 */
	public void wrapup() throws IllegalActionException {
		try {
			dataOutputSan.close();
		} catch (IOException e) {
		}
		System.out
				.println("Simulation over, get your results and enjoy it :-)");
		super.wrapup();
	}
}// end class