/* Actor is receiving packets and notifies the mapper when receiving them. */

package lsi.noc.application.greenpringle;

import lsi.noc.application.Consumer;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.expr.Parameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.RecordToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import lsi.noc.application.LifelineMapper;

/**
 * @author Leandro Indrusiak
 * @version 2.0 (York, 27/3/2009)
 */
public class VCConsumer extends Consumer {

	static final int RECEIVING_ADDRESS = 0;
	static final int RECEIVING_SIZE = 1;
	static final int RECEIVING_PAYLOAD = 2;
	protected int vcs;
	protected int[] state_;
	protected int[] flitCounter_;
	protected int[] id_;
	private Time currentTime_;

	public VCConsumer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

		ack_out.setMultiport(true);
		data_in.setMultiport(true);

	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		vcs = data_in.getWidth();
		state_ = new int[vcs];
		flitCounter_ = new int[vcs];
		id_ = new int[vcs];

		for (int i = 0; i < vcs; i++) {
			state_[i] = RECEIVING_ADDRESS;
			flitCounter_[i] = 0;
			id_[i] = 0;

		}

		try {

			mapper_ = (LifelineMapper) getMapper();

		} catch (NameDuplicationException foo) {
		}

		getDirector().fireAtCurrentTime(this);

	}

	public void fire() throws IllegalActionException {

		for (int i = 0; i < vcs; i++) {
			if (data_in.hasToken(i)) {

				if (state_[i] == RECEIVING_ADDRESS) {

					RecordToken tmp = (RecordToken) data_in.get(i);
					if (_debugging)
						_debug("vc: " + i + " - address received: " + tmp
								+ " TIME: " + getDirector().getModelTime());
					// Send an ack
					sendAck(i);
					state_[i] = RECEIVING_SIZE;
				}

				else if (state_[i] == RECEIVING_SIZE) {

					RecordToken tmp = (RecordToken) data_in.get(i);
					if (_debugging)
						_debug("vc: " + i + " - size received: " + tmp
								+ " TIME: " + getDirector().getModelTime());
					// Send an ack
					sendAck(i);

					flitCounter_[i] = ((IntToken) tmp.get("size")).intValue();
					id_[i] = ((IntToken) tmp.get("ID")).intValue();

					state_[i] = RECEIVING_PAYLOAD;

				}

				else if (state_[i] == RECEIVING_PAYLOAD) {

					RecordToken tmp = (RecordToken) data_in.get(i);
					if (_debugging)
						_debug("vc: " + i + " - payload received: " + tmp
								+ " TIME: " + getDirector().getModelTime());

					// Send an ack
					sendAck(i);
					flitCounter_[i]--;

					if (flitCounter_[i] == 0) {

						currentTime_ = getDirector().getModelTime();
						double sendTime = ((DoubleToken) tmp.get("sendTime"))
								.doubleValue();
						double releaseTime = ((DoubleToken) tmp
								.get("computation_releaseTime")).doubleValue();

						// If the packet is divided into smaller ones, only the
						// arrival of the last
						// should cause the notification of the Mapper about the
						// arriving packet
						boolean last = ((BooleanToken) tmp.get("lastPacket"))
								.booleanValue();

						if (last) {
							// only communication latency
							mapper_.notifyMessageReceipt(id_[i], sendTime,
									currentTime_);

							// end-to-end latency
							// mapper_.notifyMessageReceipt(id_[i], releaseTime,
							// currentTime_);

						}

						state_[i] = RECEIVING_ADDRESS;

					}

				}
			}
		}

	}

	protected void sendAck(int i) throws IllegalActionException {
		Token trueToken = BooleanToken.TRUE;
		ack_out.send(i, trueToken);

	}

}
