/* Actor is sending packets according to the mapper's instructions. */

package lsi.noc.application.argus;

import java.util.Hashtable;

import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.RecordToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import lsi.noc.application.Producer;

/**
 * @author Sanna Maatta
 */

public class ArgusProducer extends Producer {

	static final int IDLE = 0;
	static final int DELAYING = 1;
	static final int SENDING = 2;
	static final int SENT = 3;

	@SuppressWarnings("unchecked")
	public ArgusProducer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		state_ = IDLE;
		totalPacketSize_ = 0;
		flitCounter_ = 0;
		lastPacketID_ = -1; // Mapper starts IDs from 0, this must be
							// initialized to something else, so that the packet
							// with id 0 gets delayed.

		packetID_ = -1;

		mapperTime_ = new Hashtable();

		getDirector().fireAtCurrentTime(this); // Needed for some reason. No
												// idea why.

	}

	// During prefire, a packet is sent out, if it's the first packet ever or an
	// ack has been received.

	public boolean prefire() throws IllegalActionException {

		// FIXME This if-clause shouldn't be needed, but it is needed anyway.
		// Quick'n dirty fix for a bug, whose location is unknown.
		if (state_ == DELAYING) {

			computationDelay_ = (getDirector().getModelTime()).getDoubleValue()
					- ((DoubleToken) mapperTime_.get(packetToSend_))
							.doubleValue();

			// This should not happen but it happens every now and then anyway.
			// No idea why.
			if (computationDelay_ < ((DoubleToken) packetToSend_
					.get("computation_delay")).doubleValue()) {
				Time currentTime = getDirector().getModelTime();
				getDirector().fireAt(
						this,
						currentTime.add(((DoubleToken) packetToSend_
								.get("computation_delay")).doubleValue()
								- computationDelay_));

			} else {

				state_ = SENDING;
			}

		}

		if (state_ == SENDING) {

			if (((IntToken) packetToSend_.get("ID")).intValue() != packetID_) {

				packetID_ = ((IntToken) packetToSend_.get("ID")).intValue();

				firstSubPacketSendTime_ = (getDirector().getModelTime())
						.getDoubleValue();
				computationDelay_ = (getDirector().getModelTime())
						.getDoubleValue()
						- ((DoubleToken) mapperTime_.get(packetToSend_))
								.doubleValue();

			}

			packetToSend_ = createPacket(packetToSend_,
					firstSubPacketSendTime_, computationDelay_);

			data_out.send(0, packetToSend_);

			state_ = SENT;
		}

		return true;
	}

	public void fire() throws IllegalActionException {

		// Ack_in has token

		if (ack_in.hasToken(0)) {

			BooleanToken ackin = (BooleanToken) ack_in.get(0);

			// Ack received

			if (ackin.booleanValue()) {

				flitCounter_ = flitCounter_ + 1;

				// More flits to send. A packet is sent flit by flit.
				if (flitCounter_ < (totalPacketSize_ + 2)) {

					state_ = SENDING;

					// All flits sent already
				} else {

					flitCounter_ = 0;

					state_ = IDLE;

				}

				Time currentTime = getDirector().getModelTime();
				getDirector().fireAt(this, currentTime.add(1.0));

			} else { // Nack received

				state_ = SENDING;

				Time currentTime = getDirector().getModelTime();
				getDirector().fireAt(this, currentTime.add(1.0));

			}
		}

		// Next packet's delay starts when current packets delay has ended
		if (delayScheme_) {

			if (buffer_.size() > 0 && state_ == IDLE) {

				state_ = DELAYING;
				packetToSend_ = getPacket_();

				totalPacketSize_ = ((IntToken) packetToSend_.get("size"))
						.intValue();

				Time currentTime = getDirector().getModelTime();

				int id = ((IntToken) packetToSend_.get("ID")).intValue();

				// Only the first subpacket gets delayed (if a large packet is
				// split into several smaller ones)
				if (id != lastPacketID_) {

					lastPacketID_ = id;
					double delay = ((DoubleToken) delays_.get(packetToSend_))
							.doubleValue();

					double timeDifference = currentTime.getDoubleValue()
							- ((DoubleToken) packetToSend_.get("sendTime"))
									.doubleValue();

					if (timeDifference < delay) { // Packet needs to be delayed.

						double delayLeft = delay - timeDifference;
						getDirector().fireAt(this, currentTime.add(delayLeft));

					} else { // Delay already gone, packet can be sent
								// immediately (packet has waited in the buffer
								// longer than it's computation delay)

						getDirector().fireAtCurrentTime(this);
						// getDirector().fireAt(this, currentTime.add(1.0));
						state_ = SENDING;
					}

				} else { // Packet is not the first subpacket, it won't get
							// delayed

					getDirector().fireAtCurrentTime(this);
					// getDirector().fireAt(this, currentTime.add(1.0));
					state_ = SENDING;

				}
			}

		} else { // Next packet's delay starts when current packet is sent
					// completely

			// There is a packet waiting for sending in the buffer.
			if (buffer_.size() > 0 && state_ == IDLE) {

				state_ = DELAYING;

				packetToSend_ = getPacket_();
				totalPacketSize_ = ((IntToken) packetToSend_.get("size"))
						.intValue();

				Time currentTime = getDirector().getModelTime();

				int id = ((IntToken) packetToSend_.get("ID")).intValue();

				// Only the first subpacket gets delayed (if a large packet is
				// split into several smaller ones)
				if (id != lastPacketID_) {

					lastPacketID_ = id;
					double delay = ((DoubleToken) delays_.get(packetToSend_))
							.doubleValue();
					getDirector().fireAt(this, currentTime.add(delay));

				} else { // Packet is not the first subpacket, it won't get
							// delayed

					getDirector().fireAtCurrentTime(this);
					// getDirector().fireAt(this, currentTime.add(1.0));

				}

				state_ = SENDING;
			}
		}
	}

	/**
	 * The Mapper is calling this to send a token. A packet, containing target
	 * address (x and y coordinates), packet size, packet id, token, and delay
	 * it causes to the processor, is created and stored into a buffer.
	 */
	public void sendPacket(Token token, int x, int y, int id,
			int totalPacketSize, int subPacketSize, Token preCompDelay)
			throws IllegalActionException {

		// Packet sizes are in bytes -> changed to flits (1 flit = 4 Bytes)
		totalPacketSize = totalPacketSize / 4;
		subPacketSize = subPacketSize / 4;

		// If total packet size is bigger than subPacketSize, it's split into
		// smaller packets.

		// System.out.println("TOKEN: " + token + " total: " + totalPacketSize +
		// " sub: " + subPacketSize);

		if (totalPacketSize > subPacketSize) {

			int packets = totalPacketSize / subPacketSize;

			int rest = totalPacketSize % subPacketSize;

			for (int i = 0; i < packets; i++) {

				if (rest == 0 && i == (packets - 1)) {

					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, true,
							((DoubleToken) preCompDelay).doubleValue());
					setBuffer_(tmpPacket, preCompDelay);

				} else {

					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, false,
							((DoubleToken) preCompDelay).doubleValue());
					setBuffer_(tmpPacket, preCompDelay);

				}

			}

			if (rest != 0) {

				RecordToken tmpPacket = createPacket(x, y, id, token, rest,
						true, ((DoubleToken) preCompDelay).doubleValue());
				setBuffer_(tmpPacket, preCompDelay);

			}

		} else { // Packet size was <= than sub packet size
			// Packet is created and put to a buffer waiting for sending
			RecordToken tmpPacket = createPacket(x, y, id, token,
					totalPacketSize, true,
					((DoubleToken) preCompDelay).doubleValue());
			setBuffer_(tmpPacket, preCompDelay);

		}

		if (state_ == IDLE) {

			if (_debugging)
				_debug("a token should be sent out: " + packetToSend_);

			getDirector().fireAtCurrentTime(this);
		}

	}

	public Time getProducerTime() {
		return getDirector().getModelTime();
	}

	// Add a packet to the buffer
	@SuppressWarnings("unchecked")
	private void setBuffer_(RecordToken packet, Token preCompDelay) {

		buffer_.addElement(packet);
		delays_.put(packet, preCompDelay);

		mapperTime_.put(packet, packet.get("sendTime"));

	}

	/**
	 * Returns the first packet in the buffer to be the next packet to send
	 * 
	 * @return
	 */
	private RecordToken getPacket_() {

		RecordToken tmp = (RecordToken) buffer_.firstElement();
		buffer_.remove(0);
		return tmp;

	}

	private int state_;
	private RecordToken packetToSend_;
	private int totalPacketSize_;
	private int flitCounter_;
	private int lastPacketID_;

	private int packetID_;
	private double firstSubPacketSendTime_;
	private double computationDelay_;
	private Hashtable mapperTime_;

}
