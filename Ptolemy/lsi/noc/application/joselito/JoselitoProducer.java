package lsi.noc.application.joselito;

import java.util.Hashtable;

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
 * Joselito producer sends packets according to the mapper's instructions.
 * 
 * @author Sanna Maatta
 */

public class JoselitoProducer extends Producer {

	// State declarations

	static final int IDLE = 0;
	static final int DELAYING = 1;
	static final int SENDING_HEADER = 2;
	static final int HEADER_SENT = 3;
	static final int SENDING_TRAILER = 4;
	static final int TRAILER_SENT = 5;

	public JoselitoProducer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		state_ = IDLE;
		totalPacketSize_ = 0;
		lastPacketID_ = -1; // Mapper starts IDs from 0, this must be
							// initialized to something else, so that the packet
							// with id 0 gets delayed.
		packetID_ = -1;
		mapperTime_ = new Hashtable();

		getDirector().fireAtCurrentTime(this);

	}

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

				state_ = SENDING_HEADER;
			}

		}

		if (state_ == SENDING_HEADER) {

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
			// System.out.println("SENDING PACKET: " + packetToSend_);

			state_ = HEADER_SENT;

		} else if (state_ == SENDING_TRAILER) {

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

			state_ = TRAILER_SENT;
			// System.out.println("SENDING TRAILER: " + packetToSend_);

		}

		return true;
	}

	public void fire() throws IllegalActionException {

		if (ack_in.hasToken(0)) {

			BooleanToken ackin = (BooleanToken) ack_in.get(0);

			// Ack or Nack

			if (ackin.booleanValue()) { // Ack received

				// System.out.println("GOT ACK");
				if (state_ == HEADER_SENT) {
					// System.out.println("GOT ACK, HEADER SENT");
					boolean last = ((BooleanToken) packetToSend_
							.get("lastPacket")).booleanValue();

					state_ = SENDING_TRAILER;

					/*
					 * if (last) {
					 * 
					 * state_ = SENDING_TRAILER; packetToSend_ = getPacket_();
					 * 
					 * } else { state_ = SENDING_HEADER; packetToSend_ =
					 * getPacket_();
					 * 
					 * }
					 */

					// int size =
					// ((IntToken)packetToSend_.get("size")).intValue();

					// Sending trailer after time (size_ * 2) has passed
					// System.out.println("SHOULD FIRE");
					currentTime_ = getDirector().getModelTime();
					getDirector().fireAt(this,
							currentTime_.add(totalPacketSize_ * 2));
					// System.out.println("FIRED");

				} else if (state_ == TRAILER_SENT) {

					state_ = IDLE;

				}

			} else { // Nack received

				if (state_ == HEADER_SENT) {

					state_ = SENDING_HEADER;

				} else if (state_ == TRAILER_SENT) {

					state_ = SENDING_TRAILER;

				} else {
					System.out
							.println("########THIS CANNOT HAPPEN: joselito producer lin 193");
				}

				currentTime_ = getDirector().getModelTime();
				getDirector().fireAt(this, currentTime_.add(1.0));

			}

		}

		// Next packet's delay starts when current packets delay has ended
		if (delayScheme_) {

			if (buffer_.size() > 0 && state_ == IDLE) {
				// System.out.println("IDLE");
				state_ = DELAYING;
				packetToSend_ = getPacket_();

				totalPacketSize_ = ((IntToken) packetToSend_.get("size"))
						.intValue();
				// System.out.println("Total: " + totalPacketSize_);

				Time currentTime = getDirector().getModelTime();

				int id = ((IntToken) packetToSend_.get("ID")).intValue();

				// Only the first subpacket gets delayed (if a large packet is
				// split into several smaller ones)
				if (id != lastPacketID_) {

					// System.out.println("PRODUCER SENT FIRST: " + id);

					lastPacketID_ = id;
					double delay = ((DoubleToken) delays_.get(packetToSend_))
							.doubleValue();

					double timeDifference = currentTime.getDoubleValue()
							- ((DoubleToken) packetToSend_.get("sendTime"))
									.doubleValue();

					if (timeDifference < delay) { // Packet needs to be delayed.

						double delayLeft = delay - timeDifference;
						getDirector().fireAt(this, currentTime.add(delayLeft));
						// System.out.println("DELAYING!!!");

					} else { // Delay already gone, packet can be sent
								// immediately (packet has waited in the buffer
								// longer than it's computation delay)

						getDirector().fireAtCurrentTime(this);
						// getDirector().fireAt(this, currentTime.add(1.0));
						// System.out.println("SHOULD SEND HEADER, DELAY GONE");
						state_ = SENDING_HEADER;
					}

				} else { // Packet is not the first subpacket, it won't get
							// delayed

					getDirector().fireAtCurrentTime(this);
					// getDirector().fireAt(this, currentTime.add(1.0));
					// System.out.println("SHOULD SEND HEADER, NOT DELAYED");
					state_ = SENDING_HEADER;

				}
			}
		}

	}

	public void sendPacket(Token token, int x, int y, int id,
			int totalPacketSize, int subPacketSize, Token preCompDelay)
			throws IllegalActionException {

		totalPacketSize = totalPacketSize / 4;
		subPacketSize = subPacketSize / 4;

		if (totalPacketSize > subPacketSize) {

			int packets = totalPacketSize / subPacketSize;

			int rest = totalPacketSize % subPacketSize;

			for (int i = 0; i < packets; i++) {

				if (rest == 0 && i == (packets - 1)) {
					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, false,
							((DoubleToken) preCompDelay).doubleValue()); // last
																			// header
					setBuffer_(tmpPacket, preCompDelay);

					tmpPacket = createPacket(x, y, id, token, 0, true,
							((DoubleToken) preCompDelay).doubleValue()); // last
																			// trailer
					setBuffer_(tmpPacket, preCompDelay);

				} else {

					RecordToken tmpPacket = createPacket(x, y, id, token,
							subPacketSize, false,
							((DoubleToken) preCompDelay).doubleValue()); // not
																			// last
																			// header
					setBuffer_(tmpPacket, preCompDelay);

					tmpPacket = createPacket(x, y, id, token, 0, false,
							((DoubleToken) preCompDelay).doubleValue()); // not
																			// last
																			// trailer
					setBuffer_(tmpPacket, preCompDelay);

				}

			}

			if (rest != 0) {
				RecordToken tmpPacket = createPacket(x, y, id, token, rest,
						false, ((DoubleToken) preCompDelay).doubleValue()); // last
																			// header
				setBuffer_(tmpPacket, preCompDelay);

				tmpPacket = createPacket(x, y, id, token, 0, true,
						((DoubleToken) preCompDelay).doubleValue()); // trailer
				setBuffer_(tmpPacket, preCompDelay);

			}

		} else {

			RecordToken tmpPacket = createPacket(x, y, id, token,
					totalPacketSize, false,
					((DoubleToken) preCompDelay).doubleValue()); // only header
			setBuffer_(tmpPacket, preCompDelay);

			tmpPacket = createPacket(x, y, id, token, 0, true,
					((DoubleToken) preCompDelay).doubleValue()); // trailer
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

	/**
	 * Add a packet to the buffer
	 */
	@SuppressWarnings("unchecked")
	private void setBuffer_(RecordToken packet, Token preCompDelay) {

		buffer_.addElement(packet);
		delays_.put(packet, preCompDelay);
		// System.out.println("SET BUFFER");
		mapperTime_.put(packet, packet.get("sendTime"));
	}

	/**
	 * Gets the next packet from the (fifo)buffer
	 * 
	 * @return next packet
	 */

	private RecordToken getPacket_() {

		// System.out.println("BUFFER SIZE: " + buffer_.size());
		RecordToken tmp = (RecordToken) buffer_.firstElement();
		buffer_.remove(0);
		return tmp;

	}

	private int state_; // Either sending header or trailer
	private int totalPacketSize_;
	private int lastPacketID_;
	private int packetID_;
	private double firstSubPacketSendTime_;
	private double computationDelay_;
	private Hashtable mapperTime_;
	private Time currentTime_;
	private RecordToken packetToSend_;

}
