package lsi.noc.application.joselito;

import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.RecordToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import lsi.noc.application.Consumer;
import lsi.noc.application.LifelineMapper;

/**
 * Joselito consumer receives packets and informs the mapper about them
 * 
 * @author Sanna Maatta
 */
public class JoselitoConsumer extends Consumer {

	// State declarations
	static final int RECEIVING_HEADER = 0;
	static final int RECEIVING_TRAILER = 1;

	public JoselitoConsumer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		state_ = RECEIVING_HEADER;

		try { // Trying the get grip on the mapper

			mapper_ = (LifelineMapper) getMapper();

		} catch (NameDuplicationException foo) {
		}

		// Obviously needed, don't know why
		getDirector().fireAtCurrentTime(this);

	}

	public void fire() throws IllegalActionException {

		// If a packet is arriving
		if (data_in.hasToken(0)) {

			// Getting the packet header
			if (state_ == RECEIVING_HEADER) {

				RecordToken tmp = (RecordToken) data_in.get(0);

				if (_debugging)
					_debug("address received: " + tmp + " TIME: "
							+ getDirector().getModelTime());

				// Send an ack
				sendAck();

				// Trailer should arrive next
				state_ = RECEIVING_TRAILER;

				// Getting the packet trailer
			} else if (state_ == RECEIVING_TRAILER) {

				RecordToken tmp = (RecordToken) data_in.get(0);

				if (_debugging)
					_debug("size received: " + tmp + " TIME: "
							+ getDirector().getModelTime());

				// Send an ack
				sendAck();

				// Mapper will be notified about the packet using its id
				id_ = ((IntToken) tmp.get("ID")).intValue();

				// If a big packet is split into several smaller ones, trailer
				// needs to know
				// which is the last one in order to notify the mapper
				boolean last = ((BooleanToken) tmp.get("lastPacket"))
						.booleanValue();

				if (last) {

					Time currentTime = getDirector().getModelTime();
					double sendTime = ((DoubleToken) tmp.get("sendTime"))
							.doubleValue();
					double compDelay = ((DoubleToken) tmp
							.get("computation_delay")).doubleValue();
					mapper_.notifyMessageReceipt(id_, sendTime, currentTime,
							compDelay);

				}
				state_ = RECEIVING_HEADER;

			}
		}

	}

}
