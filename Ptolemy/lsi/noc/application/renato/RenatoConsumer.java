/* Actor is receiving packets and notifies the mapper when receiving them. */

package lsi.noc.application.renato;

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
 * @author Sanna Maatta
 */

public class RenatoConsumer extends Consumer {

	static final int RECEIVING_ADDRESS = 0;
	static final int RECEIVING_SIZE = 1;
	static final int RECEIVING_PAYLOAD = 2;

	public RenatoConsumer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		state_ = RECEIVING_ADDRESS;
		flitCounter_ = 0;

		try {

			mapper_ = (LifelineMapper) getMapper();

		} catch (NameDuplicationException foo) {
		}

		getDirector().fireAtCurrentTime(this);

	}

	public void fire() throws IllegalActionException {

		if (state_ == RECEIVING_ADDRESS) {

			if (data_in.hasToken(0)) {

				RecordToken tmp = (RecordToken) data_in.get(0);
				if (_debugging)
					_debug("address received: " + tmp + " TIME: "
							+ getDirector().getModelTime());

				// Send an ack
				Token trueToken = BooleanToken.TRUE;
				ack_out.send(0, trueToken);

				flitCounter_ = 0;

				state_ = RECEIVING_SIZE;
			}

		} else if (state_ == RECEIVING_SIZE) {

			if (data_in.hasToken(0)) {

				RecordToken tmp = (RecordToken) data_in.get(0);
				if (_debugging)
					_debug("size received: " + tmp + " TIME: "
							+ getDirector().getModelTime());

				// Send an ack
				Token trueToken = BooleanToken.TRUE;
				ack_out.send(0, trueToken);

				size_ = ((IntToken) tmp.get("size")).intValue();
				id_ = ((IntToken) tmp.get("ID")).intValue();

				state_ = RECEIVING_PAYLOAD;

			}

		} else if (state_ == RECEIVING_PAYLOAD) {

			if (data_in.hasToken(0)) {

				RecordToken tmp = (RecordToken) data_in.get(0);
				if (_debugging)
					_debug("payload received: " + tmp + " TIME: "
							+ getDirector().getModelTime());

				// Send an ack
				Token trueToken = BooleanToken.TRUE;
				ack_out.send(0, trueToken);

				flitCounter_ = flitCounter_ + 1;

				if (flitCounter_ < size_) {

					state_ = RECEIVING_PAYLOAD;

				} else {

					currentTime_ = getDirector().getModelTime();
					double sendTime = ((DoubleToken) tmp.get("sendTime"))
							.doubleValue();
					double compDelay = ((DoubleToken) tmp
							.get("computation_delay")).doubleValue();

					// If the packet is divided into smaller ones, only the
					// arrival of the last
					// should cause the notification of the Mapper about the
					// arriving packet
					boolean last = ((BooleanToken) tmp.get("lastPacket"))
							.booleanValue();

					if (last) {

						mapper_.notifyMessageReceipt(id_, sendTime,
								currentTime_, compDelay);

					}

					state_ = RECEIVING_ADDRESS;

				}
			}
		}

	}

	private int flitCounter_;
	private Time currentTime_;
	private int size_;

}
