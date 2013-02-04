//Copyright (c) 2010 Luciano Ost
//All rights reserved.
//Version 1.0	Porto Alegre, 22/01/2010.

package lsi.noc.application;

import java.io.*;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.BooleanToken;
import ptolemy.actor.util.Time;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.kernel.util.Attribute;

public class ApplicationInitActor extends TypedAtomicActor {

	private double sending_msg, compare_time;
	private double simulation_time, stop_time;
	protected Time current_time, DEstop_time, nextSend, firstSend;
	private double init_tim, new_send;

	public ApplicationInitActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// instantiates the parameter that define the period of a cycle
		interval = new Parameter(this, "interval");
		interval.setTypeEquals(BaseType.DOUBLE);

		// instantiates the parameter that define the period of a cycle
		initial_time = new Parameter(this, "initial_time");
		initial_time.setTypeEquals(BaseType.DOUBLE);
		// initial_time.setExpression("0.0");

		// NOC SIGNALS DECLARATION
		send_msg = new TypedIOPort(this, "send_msg", false, true);
		// send_msg.setTypeEquals(BaseType.BOOLEAN);

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"0\" y=\"0\" width=\"46\" "
				+ "height=\"80\" style=\"fill:blue\"/>\n" + "</svg>\n");
	}

	// **************** public TypedIOPort ****************//
	public TypedIOPort send_msg;

	// cycle period
	public Parameter interval, initial_time;

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

		if (attribute == interval) {
			double simu = ((DoubleToken) interval.getToken()).doubleValue();
			if (simu <= 0.0) {
				throw new IllegalActionException(this,
						"simulation_window is required to be positive. simulation_window given: "
								+ simu);
			}
			if (attribute == initial_time) {
				double init = ((DoubleToken) initial_time.getToken())
						.doubleValue();
				if (init <= 0.0) {
					throw new IllegalActionException(this,
							"simulation_window is required to be positive. simulation_window given: "
									+ simu);
				} else {
					super.attributeChanged(attribute);
				}
			}
		}

	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		init_tim = ((DoubleToken) initial_time.getToken()).doubleValue();

		current_time = getDirector().getModelTime();

		firstSend = current_time.add(init_tim);

		getDirector().fireAt(this, firstSend);

		// Get the simulation Stop time
		DEstop_time = getDirector().getModelStopTime();
		stop_time = DEstop_time.getDoubleValue();

		if (_debugging)
			_debug("GET STOP TIME: ");
	}

	/**
	 * State machine responsable by receiving the traffic from the data_in port,
	 * disassembly the pcredit_outage and send its flits to the output ports
	 * (file). If the data_in does not have a token, suspend firing and return.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public void fire() throws IllegalActionException {
		super.fire();
		if (_debugging)
			_debug("fire current_time: " + getDirector().getModelTime());

		// getting current Time
		current_time = getDirector().getModelTime();
		compare_time = current_time.getDoubleValue();

		// if(firstSend.compareTo(compare_time) == 0.0){
		if (compare_time == init_tim) {
			send_msg.send(0, new IntToken("1"));

			if (_debugging)
				_debug("FIRST sent at: " + compare_time);
			if (compare_time <= stop_time) {
				new_send = ((DoubleToken) interval.getToken()).doubleValue();

				nextSend = current_time.add(new_send);

				getDirector().fireAt(this, nextSend);

				if (_debugging)
					_debug("after first send at: " + nextSend);
				// Requests to the director to be fire() at the window sample
			}
		} else if (compare_time < init_tim) {
			firstSend = current_time.add(init_tim);

			getDirector().fireAt(this, firstSend);

			if (_debugging)
				_debug("init_tim at: " + init_tim + " first send at "
						+ firstSend);
		}

		else if (current_time.getDoubleValue() == nextSend.getDoubleValue()) {
			nextSend = nextSend.add(new_send);
			send_msg.send(0, new IntToken("2"));

			if (_debugging)
				_debug("msg sent at: " + compare_time);
			if (_debugging)
				_debug("NEXT SEND AT: " + nextSend);

			getDirector().fireAt(this, nextSend);

		} else if (current_time.getDoubleValue() != nextSend.getDoubleValue()) {
			if (_debugging)
				_debug("NAO ROLOU " + compare_time);
		}

	}// end public

}// end class