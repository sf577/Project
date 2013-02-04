/* Actor is sending packets according to the mapper's instructions. */

package lsi.noc.application.greenpringle;

import java.util.Vector;
import lsi.noc.application.Producer;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.expr.Parameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.RecordToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @author Leandro Indrusiak
 * @version 1.01 (York, 31/3/2010)
 */

public class VCProducer extends Producer {

	public VCProducer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

		virtualChannel = new Parameter(this, "VC");
		virtualChannel.setTypeEquals(BaseType.INT);
		virtualChannel.setExpression("0");

		ack_in.setMultiport(true);
		data_out.setMultiport(true);

	}

	protected Parameter virtualChannel;
	protected int vc;

	public void initialize() throws IllegalActionException {
		super.initialize();

		vc = ((IntToken) virtualChannel.getToken()).intValue();

		packetToSend_ = null;
		buffer_ = new Vector();

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

			data_out.send(vc, packetToSend_);

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

		if (ack_in.hasToken(vc)) {

			BooleanToken ackin = (BooleanToken) ack_in.get(vc);

			// Ack or Nack

			if (ackin.booleanValue()) {

				ackReceived_ = true;
				again_ = false;

				flitCounter_ = flitCounter_ + 1;

				if (flitCounter_ < (size_ + 2)) {

					busy_ = true;
					sendFlit_ = true;

				} else {

					busy_ = false;
					sendFlit_ = false;

					// System.out.println("SENT: " + packetToSend_);

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
			size_ = ((IntToken) packetToSend_.get("size")).intValue();

			currentTime_ = getDirector().getModelTime();
			getDirector().fireAt(this, currentTime_.add(1.0));

		}

	}

	public void sendPacket(Token token, int x, int y, int id,
			int totalPacketSize, int subPacketSize, Token delay)
			throws IllegalActionException {

		// ignores computation delay
		sendPacket(token, x, y, id, totalPacketSize, subPacketSize);

	}

	public void sendPacket(Token token, int x, int y, int id, int size,
			int subPacketSize) throws IllegalActionException {

		if (size > subPacketSize) {

			int packets = size / subPacketSize;

			int rest = size % subPacketSize;

			for (int i = 0; i < packets; i++) {

				if (rest == 0 && i == (packets - 1)) {

					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, true);
					addToBuffer_(tmpPacket);

				} else {

					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, false);
					addToBuffer_(tmpPacket);

				}

			}

			if (rest != 0) {

				RecordToken tmpPacket = createPacket(x, y, id, token, rest,
						true);
				addToBuffer_(tmpPacket);

			}

		} else {

			RecordToken tmpPacket = createPacket(x, y, id, token, size, true);
			addToBuffer_(tmpPacket);

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

	protected RecordToken createPacket(int x, int y, int ID, Token data,
			int size, boolean last) throws IllegalActionException {

		values_[0] = new IntToken(x);
		values_[1] = new IntToken(y);
		values_[2] = new IntToken(size);
		values_[3] = data;
		values_[4] = new IntToken(ID);
		values_[5] = new DoubleToken(
				(getDirector().getModelTime()).getDoubleValue());
		values_[6] = new BooleanToken(last);
		values_[8] = addressX.getToken();
		values_[9] = addressY.getToken();

		RecordToken xy = new RecordToken(labels_, values_);
		return xy;

	}

	protected RecordToken createPacket(int x, int y, int ID, Token data,
			int size, boolean last, double releaseTime)
			throws IllegalActionException {

		values_[0] = new IntToken(x);
		values_[1] = new IntToken(y);
		values_[2] = new IntToken(size);
		values_[3] = data;
		values_[4] = new IntToken(ID);
		values_[5] = new DoubleToken(
				(getDirector().getModelTime()).getDoubleValue());
		values_[6] = new BooleanToken(last);
		values_[8] = addressX.getToken();
		values_[9] = addressY.getToken();
		values_[11] = new DoubleToken(releaseTime);

		RecordToken xy = new RecordToken(labels_, values_);
		return xy;

	}

	// Add a packet to the buffer
	protected void addToBuffer_(RecordToken packet) {

		buffer_.addElement(packet);

	}

	protected RecordToken getPacketToSend_() {

		RecordToken tmp = (RecordToken) buffer_.firstElement();
		buffer_.remove(0);
		return tmp;

	}

	/**
	 * Override the base class to declare that the <i>output</i> does not depend
	 * on the <i>input</i> in a firing.
	 */
	public void pruneDependencies() {
		super.pruneDependencies();
		removeDependency(ack_in, data_out);
	}

	protected Time currentTime_;
	protected boolean ackReceived_;
	protected boolean sendFlit_;
	protected boolean first_;
	protected boolean again_;
	protected int size_;
	protected int flitCounter_;
	protected boolean busy_;
	protected RecordToken packetToSend_;

}