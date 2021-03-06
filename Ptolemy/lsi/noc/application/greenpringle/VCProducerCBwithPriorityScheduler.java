package lsi.noc.application.greenpringle;

import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.util.Time;

/**
 * 
 * @author Osmar M. dos Santos
 * @version 0.1 (York, 24/3/2010)
 * @version 0.2 (York, 31/3/2010) Leandro Soares Indrusiak
 */
public class VCProducerCBwithPriorityScheduler extends VCProducerCBwithPriority {

	// Used for the Basic Priority Scheduler
	protected BasicPriorityScheduler scheduler;

	// Used to generate an interrupt for the Basic Priority Scheduler
	protected Time interruptTime;

	public VCProducerCBwithPriorityScheduler(CompositeEntity container,
			String name) throws NameDuplicationException,
			IllegalActionException {
		super(container, name);
		// TODO Auto-generated constructor stub
	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		// Create the BasicPriortyScheduler
		// In this version, we use the number of virtual channels to define the
		// number o priority queues in the scheduler
		// Pass a reference to the producer (hardware)
		scheduler = new BasicPriorityScheduler(vcs, this);

		// Until the scheduler calls the setTimer method,
		// the interrupt time is considered to be the current time - 1
		interruptTime = getDirector().getModelTime();
		interruptTime = interruptTime.subtract(0.1);
	}

	public boolean prefire() throws IllegalActionException {

		if (_debugging)
			_debug("Pre fire: current time = "
					+ getDirector().getModelTime().getDoubleValue());
		if (_debugging)
			_debug("Pre fire: interrupt time = "
					+ interruptTime.getDoubleValue());
		if (_debugging)
			_debug("Producer = " + this.toString());

		// If the current time equals the interrupt time, interrupt the
		// scheduler
		// We can use equal since time is exact in the simulation
		if (getDirector().getModelTime().getDoubleValue() == interruptTime
				.getDoubleValue()) {
			scheduler.interrupt();
		}
		// update credits of each VC
		for (int i = 0; i < vcs; i++) {
			if (ack_in.hasToken(i)) {
				// Simply removes the ack from the port, credit value is never
				// used
				IntToken credit = (IntToken) ack_in.get(i);
				credits_[i]++;

				if (_debugging)
					_debug(getDirector().getModelTime() + " vc: " + i
							+ " - credit update received: " + credits_);

			}
		}
		return true;
	}

	public void fire() throws IllegalActionException {
		for (int i = 0; i < vcs; i++) {

			if (_debugging)
				_debug("for VC " + i);
			if (_debugging)
				_debug("    packs to send " + buffers_[i].size());
			if (_debugging)
				_debug("    credits on vc: " + credits_[i]);
			if (_debugging)
				_debug("    compare time? "
						+ currentTime_.compareTo(getDirector().getModelTime()));
			if (_debugging)
				_debug("    first? " + first_);

			if (buffers_[i].size() > 0
					&& credits_[i] > 0
					&& ((-1 == currentTime_.compareTo(getDirector()
							.getModelTime())) || first_)) {
				first_ = false;
				busy_ = true;
				packetToSend_ = (RecordToken) buffers_[i].firstElement();
				size_ = ((IntToken) packetToSend_.get("size")).intValue();

				if (_debugging)
					_debug(currentTime_
							+ " sending one flit of packet of size " + size_
							+ " via VC " + i);

				data_out.send(i, packetToSend_);
				credits_[i]--;
				currentTime_ = getDirector().getModelTime();
				getDirector().fireAt(this, currentTime_.add(period));

				if (_debugging)
					_debug("remaining credits: " + credits_[i]);

				flitCounter_[i]++;
				if (flitCounter_[i] == (size_ + 2)) {
					busy_ = false;
					flitCounter_[i] = 0;
					buffers_[i].remove(0);
				}
				break; // exits the loop once one flit is sent, so only the
						// highest priority flit is sent
			}
		}
	}

	public void sendPacket(Token token, int x, int y, int id, int size,
			int subPacketSize, Token compDelay, int priority) {

		if (_debugging)
			_debug("message request received from mapper: add process to scheduler");

		// Add process to the scheduler with all the paremeters passed from the
		// mapper
		scheduler.addProcess(token, x, y, id, size, subPacketSize,
				Double.valueOf(compDelay.toString()), priority);
	}

	public void sendPacket(Token token, int x, int y, int id, int size,
			int subPacketSize, int priority, double releaseTime)
			throws IllegalActionException {

		if (_debugging)
			_debug("message request received from scheduler");

		if (size > subPacketSize) {
			int packets = size / subPacketSize;
			int rest = size % subPacketSize;
			for (int i = 0; i < packets; i++) {
				if (rest == 0 && i == (packets - 1)) {
					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, true, releaseTime);
					addToBuffer_(tmpPacket, priority);
				} else {
					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, false, releaseTime);
					addToBuffer_(tmpPacket, priority);
				}
			}
			if (rest != 0) {
				RecordToken tmpPacket = createPacket(x, y, id, token, rest,
						true, releaseTime);
				addToBuffer_(tmpPacket, priority);
			}
		} else {
			RecordToken tmpPacket = createPacket(x, y, id, token, size, true,
					releaseTime);
			addToBuffer_(tmpPacket, priority);
		}
	}

	// Set the timer to interrupt the scheduler
	public void setTimer(double nextInterruptTime) {

		if (_debugging)
			_debug("Set Timer: nextInterruptTime = " + nextInterruptTime);

		interruptTime = getDirector().getModelTime();

		if (_debugging)
			_debug("Set Timer: current time = "
					+ getDirector().getModelTime().getDoubleValue());
		if (_debugging)
			_debug("Set Timer: interrupt time = "
					+ interruptTime.getDoubleValue());
		if (_debugging)
			_debug("Set Timer: created interrupt time (added value) = "
					+ interruptTime.add(nextInterruptTime).getDoubleValue());

		interruptTime = interruptTime.add(nextInterruptTime);

		try {
			getDirector().fireAt(this, interruptTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void addToBuffer_(RecordToken packet, int priority) {

		if (_debugging)
			_debug("a packed was added with priority " + priority);

		buffers_[priority].addElement(packet);
	}

	public void pruneDependencies() {
		super.pruneDependencies();

		removeDependency(data_out, ack_in);
	}
}