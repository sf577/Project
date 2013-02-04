package lsi.noc.application.greenpringle;

import java.util.Vector;

import ptolemy.actor.util.Time;
import ptolemy.data.Token;

/**
 * 
 * @author Osmar M. dos Santos
 * @version 0.1 (York, 24/3/2010)
 * @version 0.2 (York, 31/3/2010) Leandro Soares Indrusiak
 */

public class ProcessControlBlock {

	// Enable debugging messages in this class
	private boolean _debug = false;

	// TODO: Process has only two states in this version
	public static final int waiting = 1;
	public static final int running = 2;

	// Info related to the execution of the process
	private int id;
	private int priority;
	private int state;
	private double usedComputation;
	private double totalComputation;
	private double releaseTime;

	// Info related to the message produced by the process
	// TODO: The ideal here would be to have a Packet type that contains
	// NOTE: Packet id and packet priority are equal to the process id and
	// priority
	// all the data for the packet that is sent by the process
	private Token packetToken;
	private int packetDstX;
	private int packetDstY;
	private int packetId;
	private int packetSize;
	private int subPacketSize;
	private int packetPriority;

	public ProcessControlBlock(Token packetToken, int packetDstX,
			int packetDstY, int id, int packetSize, int subPacketSize,
			Double totalComputation, int priority, double releaseTime) {
		// Set process data
		this.id = id;
		this.priority = priority;
		this.releaseTime = releaseTime;
		// Initially, the process is in the waiting state
		this.state = waiting;
		this.usedComputation = 0;
		this.totalComputation = totalComputation;

		// Set packet data
		this.packetToken = packetToken;
		this.packetDstX = packetDstX;
		this.packetDstY = packetDstY;
		// Same id as the process
		this.packetId = id;
		this.packetSize = packetSize;
		this.subPacketSize = subPacketSize;
		// Same priority as the process
		this.packetPriority = priority;
	}

	// Functions related to process data
	public int getId() {
		return id;
	}

	public int getPriority() {
		return priority;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public double getRemainingComputationTime() {
		double remainingTime = totalComputation - usedComputation;
		if (_debug) {
			if (usedComputation >= totalComputation)
				System.out.println("getRemainingTime (PROBLEM): Process id "
						+ this.id + " usedComputation >= totalComputation!");
			System.out.println("Process id " + this.id + " remainingTime = "
					+ remainingTime);
		}
		return remainingTime;
	}

	public Boolean hasFinished(double elapsedTime) {
		// Update the time the process has executed
		usedComputation = usedComputation + elapsedTime;
		double remainingTime = totalComputation - usedComputation;

		if (_debug)
			System.out.println("Process id " + this.id + " elapsedTime = "
					+ elapsedTime);
		if (_debug)
			System.out.println("Process id " + this.id + " usedComputation = "
					+ usedComputation);
		if (_debug)
			System.out.println("Process id " + this.id + " totalComputation = "
					+ totalComputation);

		// If remaining computation is lower or equal to zero, process has
		// finished
		if (remainingTime <= 0) {
			return true;
		} else {
			// However, oepending on the current precision of the simulation
			// (see the director),
			// the remaining time should be disconsidered, as Ptolemy will not
			// be able to
			// fire an event with such small precision (it will round it to 0)
			if (remainingTime < 0.00000001) {
				return true;
			} else {
				return false;
			}
		}
	}

	public double getReleaseTime() {
		return releaseTime;
	}

	// Functions related to packet data
	public Token getPacketToken() {
		return packetToken;
	}

	public int getPacketDstX() {
		return packetDstX;
	}

	public int getPacketDstY() {
		return packetDstY;
	}

	public int getPacketId() {
		return packetId;
	}

	public int getPacketSize() {
		return packetSize;
	}

	public int getSubPacketSize() {
		return subPacketSize;
	}

	public int getPacketPriority() {
		return packetPriority;
	}
}
