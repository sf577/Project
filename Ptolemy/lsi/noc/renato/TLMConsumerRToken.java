/*
Copyright (c) 2007 Luciano C. Ost
All rights reserved.
Prazer final de semana.
03/08/07 Lets play a game...*/

package lsi.noc.renato;

import java.io.*;
import java.lang.Integer;

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
import ptolemy.data.DoubleToken;
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

/*
 1 - timestamp de criao do pacote
 2 - timestamp em que o pacote entra na rede
 3 -  na verdade o timestamp de chegada do ltimo flit do pacote no roteador destino
 4 - latncia do pacote considerando o momento de entrada na rede
 5 - tempo de corrido de simulao at o momento em ns*/

public class TLMConsumerRToken extends TypedAtomicActor {

	// **************** private variables ****************//
	private String size_s, address, payload_s, output_file, zero, s_xy,
			s_timestamp_rede, s_timestamp_ini;
	private BooleanToken accept;
	private RecordToken record, data_token;
	private int simulation_time;
	/*
	 * TIMESTAP_FF of first flit received by the consumer TIMESTAP_LF of last
	 * flit of the packet received by the consumer
	 */
	private int x, y, xy, timestamp_ini, timestamp_rede, timestamp_ff,
			timestamp_lf, current_time;

	// values for the state variables
	static final int INACTIVE = 0;
	static final int RECEIVING_ADD = 1;
	static final int RECEIVING_SIZE = 2;
	static final int SENDING = 3;

	// State machine definition
	private int state = INACTIVE;
	private int size, cont, payload;

	public TLMConsumerRToken(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// NoC Signals declaration
		debug = new TypedIOPort(this, "debug", false, true);
		ack = new TypedIOPort(this, "act_rx", false, true);
		data_out = new TypedIOPort(this, "data_out", false, true);
		data_in = new TypedIOPort(this, "data_in", true, false);

		data_out.setTypeAtLeast(data_in);

		ack.setTypeEquals(BaseType.BOOLEAN);
		debug.setTypeEquals(BaseType.STRING);

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"0\" y=\"0\" width=\"46\" "
				+ "height=\"80\" style=\"fill:blue\"/>\n" + "</svg>\n");
	}

	// **************** public TypedIOPort ****************//
	public TypedIOPort data_out, ack, data_in, debug;

	// // public methods ////
	/**
	 * Set the incial state
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();
		state = RECEIVING_ADD;
	}

	/**
	 * According to an arbitration scheme, peeks the It also requests write
	 * permission on file. * @exception IllegalActionException If there is no
	 * director.
	 */

	public boolean prefire() throws IllegalActionException {

		// reads rx signal, if available an state is selected

		if (data_in.getWidth() > 0) {
			// boolean ata_token = data_in.hasToken(0);
			// data_token = data_in.hasToken(0);
			if (state == RECEIVING_ADD) {
				accept = BooleanToken.TRUE;
				ack.send(0, accept);
			} else if (state == RECEIVING_SIZE) {
				accept = BooleanToken.TRUE;
				ack.send(0, accept);
			} else if (state == SENDING) {
				accept = BooleanToken.TRUE;
				ack.send(0, accept);
			}
		}
		return true;
	}

	/**
	 * State machine responsable by receiving the traffic from the input port,
	 * disassembly the package and send its flits to the output ports (file). If
	 * the input does not have a token, suspend firing and return.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public void fire() throws IllegalActionException {
		accept = BooleanToken.FALSE;

		// **************Receiving file**************************** //
		if (state == RECEIVING_ADD) {
			if (data_in.hasToken(0)) {

				cont = 0;
				// get the timestamp of the first flit received from the NoC
				timestamp_ff = (int) getDirector().getCurrentTime();
				// timestap_f_flit = timestamp_ff;

				// get the xy adress
				record = ((RecordToken) data_in.get(0));
				x = ((IntToken) record.get("x")).intValue();
				y = ((IntToken) record.get("y")).intValue();
				s_xy = (Integer.toString(x)) + (Integer.toString(y));
				for (int i = s_xy.length(); i < 4; i++)
					s_xy = "0" + s_xy;

				printFlit(s_xy);

				debug.send(0, new StringToken("RECEIVING_ADD " + s_xy
						+ " AT TIME: " + getDirector().getModelTime()));

				// set new state
				state = RECEIVING_SIZE;
			} else {
				state = RECEIVING_ADD;
			}
		}// end if RECEIVING_ADD

		if (state == RECEIVING_SIZE) {

			if (data_in.hasToken(0)) {

				record = ((RecordToken) data_in.get(0));
				size = ((IntToken) record.get("size")).intValue();
				size_s = (Integer.toHexString(size).toUpperCase());
				for (int i = size_s.length(); i < 4; i++)
					size_s = "0" + size_s;
				cont++;
				printFlit(size_s);

				debug.send(0, new StringToken("RECEIVING_SIZE " + size
						+ " AT TIME: " + getDirector().getModelTime()));
				state = SENDING;
				cont = 0;
			} else {
				state = RECEIVING_SIZE;
			}
		}// end if RECEIVING_ADD

		else if (state == SENDING) {
			debug.send(0, new StringToken("SENDING "));
			if (data_in.hasToken(0)) {
				record = ((RecordToken) data_in.get(0));
				payload = ((IntToken) record.get("payload")).intValue();
				payload_s = (Integer.toHexString(payload).toUpperCase());
				for (int i = payload_s.length(); i < 4; i++)
					payload_s = "0" + payload_s;

				cont++;
				if (cont != size) {
					debug.send(0, new StringToken("payload_s " + payload
							+ " AT TIME: " + getDirector().getModelTime()));
					printFlit(payload_s);
					state = SENDING;
				} else {
					printFlit(payload_s);
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

					debug.send(0, new StringToken("foi "));

					data_out.send(0, new StringToken(readOutput_file()
							+ timestamp_ini + " " + timestamp_rede + " "
							+ timestamp_lf + " " + latency + " "
							+ simulation_time));
					state = RECEIVING_ADD;
					// wrapup();
				}
			} else {
				state = SENDING;
			}
		}
	}// end public

	public void printFlit(String string) {
		if (cont == 0) {
			zero = new String(" ");
			output_file = new String(string + zero);
		} else {
			output_file = output_file + string + zero;
		}
	}// end printPacket

	public String readOutput_file() {
		return output_file;
	}

	/**
	 * @Function: void wrapup() ---------------------------
	 * @Input: none
	 * @Output: none
	 * 
	 * @Description:
	 * 
	 */
	public void wrapup() throws IllegalActionException {
		super.wrapup();
		System.out
				.println("Simulation over, get your results and enjoy it :-)");
	}

}// end class