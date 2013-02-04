/* Actor is sending packets according to the mapper's instructions. */

package lsi.noc.application.renato;

import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.RecordToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import lsi.noc.application.Producer;

/**
 * @author Sanna Maatta
 */

public class RenatoProducer extends Producer {

	public RenatoProducer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		ackReceived_ = true;
		sendFlit_ = false;
		flitCounter_ = 0;

		busy_ = false;
		first_ = true;
		again_ = false; // if receives a nack, sends again
		getDirector().fireAtCurrentTime(this);

	}

	public boolean prefire() throws IllegalActionException {

		if (sendFlit_ && (first_ || ackReceived_ || again_)) {

			data_out.send(0, packetToSend_);

			if (first_) {

				first_ = false;

				if (_debugging)
					_debug("first flit ever sent: " + packetToSend_ + " TIME: "
							+ getDirector().getModelTime());

			} else if (ackReceived_) {

				if (_debugging)
					_debug("flit after an ack sent: " + packetToSend_
							+ " TIME: " + getDirector().getModelTime());

			} else if (again_) {
				if (_debugging)
					_debug("sending flit again after receiving a nack: "
							+ packetToSend_ + " TIME: "
							+ getDirector().getModelTime());
			}

			ackReceived_ = false;
			again_ = false;

		}

		return true;
	}

	public void fire() throws IllegalActionException {

		// Ack_in has token, a new token can be sent

		if (ack_in.hasToken(0)) {

			BooleanToken ackin = (BooleanToken) ack_in.get(0);

			// Ack or Nack

			if (ackin.booleanValue()) {

				ackReceived_ = true;
				again_ = false;

				flitCounter_ = flitCounter_ + 1;

				if (flitCounter_ < (totalPacketSize_ + 2)) {

					busy_ = true;
					sendFlit_ = true;

				} else {

					busy_ = false;
					sendFlit_ = false;

					flitCounter_ = 0;

				}

				if (_debugging)
					_debug("ack received");

				currentTime_ = getDirector().getModelTime();
				getDirector().fireAt(this, currentTime_.add(1.0));

			} else { // A Nack received

				again_ = true;

				if (_debugging)
					_debug("nack received");

				currentTime_ = getDirector().getModelTime();
				getDirector().fireAt(this, currentTime_.add(1.0));

			}

		}

		if (!sendFlit_ && buffer_.size() > 0) {

			sendFlit_ = true;
			busy_ = true;

			packetToSend_ = getPacketToSend_();
			totalPacketSize_ = ((IntToken) packetToSend_.get("size"))
					.intValue();

			currentTime_ = getDirector().getModelTime();
			getDirector().fireAt(this, currentTime_.add(1.0));

		}

	}

	public void sendPacket(Token token, int x, int y, int id,
			int totalPacketSize, int subPacketSize, Token preCompDelay)
			throws IllegalActionException {

		if (totalPacketSize > subPacketSize) {

			int packets = totalPacketSize / subPacketSize;

			int rest = totalPacketSize % subPacketSize;

			for (int i = 0; i < packets; i++) {

				if (rest == 0 && i == (packets - 1)) {

					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, true,
							((DoubleToken) preCompDelay).doubleValue());
					setBuffer_(tmpPacket);

				} else {

					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, false,
							((DoubleToken) preCompDelay).doubleValue());
					setBuffer_(tmpPacket);

				}

			}

			if (rest != 0) {

				RecordToken tmpPacket = createPacket(x, y, id, token, rest,
						true, ((DoubleToken) preCompDelay).doubleValue());
				setBuffer_(tmpPacket);

			}

		} else {

			RecordToken tmpPacket = createPacket(x, y, id, token,
					totalPacketSize, true,
					((DoubleToken) preCompDelay).doubleValue());
			setBuffer_(tmpPacket);

		}

		if (!busy_) {

			if (_debugging)
				_debug("a token should be sent out: " + packetToSend_);

			getDirector().fireAtCurrentTime(this);
		}

	}

	public Time getProducerTime() {
		return getDirector().getModelTime();
	}

	// Add a packet to the buffer
	private void setBuffer_(RecordToken packet) {

		buffer_.addElement(packet);

	}

	private RecordToken getPacketToSend_() {

		RecordToken tmp = (RecordToken) buffer_.firstElement();
		buffer_.remove(0);
		return tmp;

	}

	private Time currentTime_;
	private boolean ackReceived_;
	private boolean sendFlit_;

	private boolean first_;
	private boolean again_;

	private int totalPacketSize_;
	private int flitCounter_;

	private boolean busy_;
	private RecordToken packetToSend_;

}
