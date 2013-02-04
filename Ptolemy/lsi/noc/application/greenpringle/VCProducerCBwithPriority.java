package lsi.noc.application.greenpringle;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.util.Vector;

/**
 * 
 * @author Leandro Indrusiak
 * @version 1.0 (York, 08/4/2009)
 */
public class VCProducerCBwithPriority extends VCProducerCB {

	protected int[] credits_; // stores the credits available on remote buffer
	protected Vector[] buffers_; // one queue per priority level
	protected int[] flitCounter_; // amount of flits to be transferred, for each
									// virtual channel
	protected int vcs; // number of virtual channels

	public VCProducerCBwithPriority(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		// TODO Auto-generated constructor stub
	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		// all output ports and vcs have full credits upon initialization

		vcs = data_out.getWidth();
		credits_ = new int[vcs];
		flitCounter_ = new int[vcs];
		buffers_ = new Vector[vcs];
		int fullcredits = ((IntToken) cred.getToken()).intValue();
		first_ = true;
		busy_ = false;

		if (_debugging)
			_debug("initializing producer with " + vcs
					+ " VCs, each with credits: " + fullcredits);

		for (int i = 0; i < vcs; i++) {
			credits_[i] = fullcredits;
			buffers_[i] = new Vector();
			flitCounter_[i] = 0;
		}

	}

	public boolean prefire() throws IllegalActionException {

		// update credits of each VC

		for (int i = 0; i < vcs; i++) {

			if (ack_in.hasToken(i)) {

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
			int subPacketSize, Token compDelay, int priority)
			throws IllegalActionException {

		if (_debugging)
			_debug("message request received from mapper");

		if (size > subPacketSize) {

			int packets = size / subPacketSize;

			int rest = size % subPacketSize;

			for (int i = 0; i < packets; i++) {

				if (rest == 0 && i == (packets - 1)) {

					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, true);
					addToBuffer_(tmpPacket, priority);

				} else {

					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, false);
					addToBuffer_(tmpPacket, priority);

				}

			}

			if (rest != 0) {

				RecordToken tmpPacket = createPacket(x, y, id, token, rest,
						true);
				addToBuffer_(tmpPacket, priority);

			}

		} else {

			RecordToken tmpPacket = createPacket(x, y, id, token, size, true);
			addToBuffer_(tmpPacket, priority);

		}

		if (!busy_) {

			if (_debugging)
				_debug("message processed, will be sent after one period");

			getDirector()
					.fireAt(this, getDirector().getModelTime().add(period));
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
