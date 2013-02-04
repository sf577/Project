package lsi.noc.joselito.handshake;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.lib.Queue;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

////TLMBuffer

/**
 * This actor implements a FIFO Buffer. When a token is received on the
 * <i>input</i> port, it is stored in the queue. The oldest token on the FIFO is
 * sent to the <i>output</i> port periodically according to the <i>period</i>
 * parameter. When the <i>read</i> port receives a token, the oldest element in
 * the buffer is discarded.
 * <p>
 * The inputs can be of any token type, and the output is constrained to be of a
 * type at least that of the input. If the <i>capacity</i> parameter is negative
 * or zero (the default), then the capacity is infinite. Otherwise, the capacity
 * is given by that parameter, and inputs received when the queue is full are
 * discarded. Whenever the size of the queue changes, the new size is produced
 * on the <i>size</i> output port. If an input arrives at the same time that an
 * output is produced, then the <i>size</i> port gets two events at the same
 * time.
 * 
 * The implementation was inspired on the ptolemy.domains.de.lib.Queue class by
 * Lee and Neuendorffer. The major difference is the fact that this buffer has
 * an implicit trigger.
 * 
 * @author Leandro Soares Indrusiak
 * @version 1.0 (Darmstadt, 19.11.2007) @ Modified by Luciano Ost
 * @version 2.0 (Porto Alegre, 13.09.2008) @ Modified by Luciano Ost
 */

public class TLMBuffer extends Transformer {

	public TLMBuffer(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		output.setTypeAtLeast(input);
		read = new TypedIOPort(this, "read", true, false);
		ack = new TypedIOPort(this, "ack", false, true);
		size = new TypedIOPort(this, "size", false, true);
		size.setTypeEquals(BaseType.DOUBLE);

		_queue = new FIFOQueue();

		capacity = new Parameter(this, "capacity");
		capacity.setTypeEquals(BaseType.INT);
		capacity.setExpression("0");

		// instantiates the parameter that define the period of a cycle
		period = new Parameter(this, "period");
		period.setTypeEquals(BaseType.DOUBLE);

		// Power Comsumption Parameters
		noc_frequency = new Parameter(this, "noc_frequency");
		noc_frequency.setTypeEquals(BaseType.DOUBLE);
		noc_frequency.setExpression("50");

		phit_size = new Parameter(this, "phit_size");
		phit_size.setTypeEquals(BaseType.INT);
		phit_size.setExpression("16");

		simulation_window = new Parameter(this, "simulation_window");
		simulation_window.setTypeEquals(BaseType.INT);
		simulation_window.setExpression("1000");
	}

	// /////////////////////////////////////////////////////////////////
	// // ports and parameters ////

	/**
	 * The capacity of the buffer. If the value is positive, then it specifies
	 * the capacity of the queue. If it is negative or 0, then it specifies that
	 * the capacity is infinite. This is an integer with default 0.
	 */
	public Parameter capacity;

	/**
	 * The current occupation of the buffer. This port produces an output
	 * whenever the internal FIFO changes. It has type int.
	 */
	public TypedIOPort size;

	/**
	 * The read port, which has undeclared type. If this port receives a token,
	 * then the oldest token in the queue will be discarded.
	 */
	public TypedIOPort read;

	/**
	 * The ack port, which has undeclared type. If an input token is added to
	 * the FIFO, a <i>true</i> BooleanToken is sent out through this port. If
	 * the FIFO is full, the input token is discarded and a <i>false</i>
	 * BooleanToken is sent out through this port.
	 */
	public TypedIOPort ack;

	// cycle period
	public Parameter period;

	/**
	 * Parameters used for power consumption analysis
	 */
	public Parameter noc_frequency;
	public Parameter phit_size;
	public Parameter simulation_window;

	/**
	 * React to a change in an attribute. If the attribute is <i>capacity</i>,
	 * then change the capacity of the queue. If the size of the queue currently
	 * exceeds the specified capacity, then throw an exception.
	 * 
	 * @param attribute
	 *            The attribute that changed.
	 * @exception IllegalActionException
	 *                If the current size of the queue exceeds the specified
	 *                capacity.
	 */
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {
		if (attribute == capacity) {
			int newCapacity = ((IntToken) capacity.getToken()).intValue();
			if (newCapacity <= 0) {
				if (_queue.getCapacity() != FIFOQueue.INFINITE_CAPACITY) {
					_queue.setCapacity(FIFOQueue.INFINITE_CAPACITY);
				}
			} else {
				if (newCapacity < _queue.size()) {
					throw new IllegalActionException(this, "Queue size ("
							+ _queue.size() + ") exceed requested capacity "
							+ newCapacity + ").");
				}
				_queue.setCapacity(newCapacity);
			}
		} else if (attribute == simulation_window) {
			int simu = ((IntToken) simulation_window.getToken()).intValue();
			if (simu <= 0) {
				throw new IllegalActionException(this,
						"simulation_window is required to be positive. simulation_window given: "
								+ simu);
			}
		} else {
			super.attributeChanged(attribute);
		}
	}

	/**
	 * Clone the actor into the specified workspace. This calls the base class
	 * and then sets the ports.
	 * 
	 * @param workspace
	 *            The workspace for the new object.
	 * @return A new actor.
	 * @exception CloneNotSupportedException
	 *                If a derived class has has an attribute that cannot be
	 *                cloned.
	 */
	public Object clone(Workspace workspace) throws CloneNotSupportedException {
		TLMBuffer newObject = (TLMBuffer) super.clone(workspace);
		newObject._queue = new FIFOQueue();
		newObject.output.setTypeAtLeast(newObject.input);
		return newObject;
	}

	/**
	 * Clear the cached input tokens.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public void initialize() throws IllegalActionException {
		_queue.clear();
		nextFlit = getDirector().getModelTime();

		freq = ((DoubleToken) noc_frequency.getToken()).doubleValue();
		phit = ((IntToken) phit_size.getToken()).intValue();
		simu_window = ((IntToken) simulation_window.getToken()).intValue();

		sw = simu_window;

		// The Buffer Requests to the director to be fire() at the window sample
		getDirector().fireAt(this, sw);

		// Get the simulation Stop time
		DEstop_time = getDirector().getModelStopTime();
		stop_time = DEstop_time.getDoubleValue();

		super.initialize();
	}

	/**
	 * If there is no input on the <i>trigger</i> port, return false, indicating
	 * that this actor does not want to fire. This has the effect of leaving
	 * input values in the input ports, if there are any.
	 * 
	 * @exception IllegalActionException
	 *                If there is no director.
	 */
	public boolean prefire() throws IllegalActionException {

		boolean hasReadTrigger = false;

		if (read.getWidth() > 0) {
			hasReadTrigger = (read.hasToken(0));
		}

		// if read notification is received, remove the oldest token
		// from the queue
		if (hasReadTrigger) {
			// Consume the trigger token.
			read.get(0);
			_queue.take();
			if (state == HEADER) {
				state = WAITING_TRAILER;
				HFT = (getDirector().getModelTime().getDoubleValue()) - 1;
			} else if (state == TRAILER) {
				state = EMPTY;
			}
		}
		return true;
	}

	/**
	 * If there is a new token on the <i>input</i> port, then put it on the
	 * FIFO.
	 * 
	 * If the queue is not empty, then send the oldest token on the queue to the
	 * <i>output</i> port. Send the resulting FIFO size to the <i>size</i>
	 * output port.
	 * 
	 * @exception IllegalActionException
	 *                If getting tokens from input and read ports or sending
	 *                token to output throws it.
	 */
	public void fire() throws IllegalActionException {
		super.fire();

		// sends copy of the oldest token to the output
		int comp = getDirector().getModelTime().compareTo(nextFlit);
		if (_queue.size() != 0 && (comp == 0 | comp == 1)) {
			output.send(0, (RecordToken) _queue.get(0));
			// if(_debugging) _debug("Data sent at: " +
			// getDirector().getModelTime());
		}

		// reads input port
		// stores token on the FIFO if there is room
		// sends ack or nack regarding the storage of the token
		if (input.hasToken(0)) {

			RecordToken data_token = (RecordToken) input.get(0);
			packet_size = ((IntToken) data_token.get("size")).intValue();

			// if(_debugging) _debug("Packet size: " + packet_size);

			if (_queue.size() < _queue.getCapacity()) {
				_queue.put(data_token);
				if (state == EMPTY) {
					state = HEADER;
					ack.send(0, new BooleanToken(true));

				} else if (state == WAITING_TRAILER) {
					state = TRAILER;
					ack.send(0, new BooleanToken(true));
					// packet received
					received_packets++;
				}
			} else {
				ack.send(0, new BooleanToken(false));
			}
		}

		if (_queue.size() != 0) {
			// requests new firing after period
			nextFlit = getDirector().getModelTime();
			DoubleToken per = (DoubleToken) period.getToken();
			nextFlit = nextFlit.add(per.doubleValue());

			getDirector().fireAt(this, nextFlit);
		}

		// getting current Time
		current_time = getDirector().getModelTime();
		compare_time = current_time.getDoubleValue();

		if (compare_time == sw) {
			AvPC(received_packets);

			if (sw <= stop_time) {
				sw = sw + 1000;
				getDirector().fireAt(this, sw);
			}
			/*
			 * else if(sw != stop_time && received_packets != 0){ sw = sw +
			 * 1000; getDirector().fireAt(this,sw); } else if (sw != stop_time
			 * && received_packets == 0 && no_rec_packet <=2){
			 * getDirector().fireAt(this,sw); no_rec_packet++; }
			 */
			// sends the Average Power Consumption to the "size" port
			size.send(0, new DoubleToken(rate));
			received_packets = 0;
		}
		// sends the FIFO size to the size port
		// size.send(0, new IntToken(_queue.size()));
	}

	/**
	 * Override the base class to declare that the <i>output</i> does not depend
	 * on the <i>input</i> in a firing.
	 */
	public void pruneDependencies() {
		super.pruneDependencies();
		removeDependency(input, output);
		removeDependency(read, output);
	}

	/**
	 * Rate Function
	 */
	private double AvPC(int received_packets) {
		rate = ((received_packets * (packet_size + 2)) * phit)
				/ ((1 / freq) * simu_window);
		return rate;
	}

	// /////////////////////////////////////////////////////////////////
	// // protected variables ////

	/** The FIFOQueue. */
	protected FIFOQueue _queue;
	protected Time nextFlit, current_time, DEstop_time;

	/** Values for the State Variables */
	static final int EMPTY = 0;
	static final int HEADER = 1;
	static final int WAITING_TRAILER = 2;
	static final int TRAILER = 3;

	/** Initial State */
	private int state = EMPTY;

	/** Variables */
	private RecordToken data_token;
	private double compare_time, pw_buffer, rate, stop_time;
	private int received_packets = 0;
	private int cont = 0, simu_window, sw, packet_size, no_rec_packet = 0;
	private double freq, phit, HFT = 0;

}
