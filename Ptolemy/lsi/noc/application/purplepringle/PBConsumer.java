/* Consumer receives packets and notifies the mapper when receiving them. */

package lsi.noc.application.purplepringle;

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
 * @author Leandro Soares Indrusiak
 */
public class PBConsumer extends Consumer {

	public PBConsumer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		try {

			mapper_ = (LifelineMapper) getMapper();

		} catch (NameDuplicationException foo) {
			System.out.println(foo);
		}

		getDirector().fireAtCurrentTime(this);

	}

	public void fire() throws IllegalActionException {

		if (data_in.hasToken(0)) {

			RecordToken tmp = (RecordToken) data_in.get(0);
			if (_debugging)
				_debug("packet received: " + tmp + " TIME: "
						+ getDirector().getModelTime());

			id_ = ((IntToken) tmp.get("ID")).intValue();

			Time currentTime_ = getDirector().getModelTime();
			double sendTime = ((DoubleToken) tmp.get("sendTime")).doubleValue();
			double compDelay = ((DoubleToken) tmp.get("computation_delay"))
					.doubleValue();
			double releaseTime = ((DoubleToken) tmp
					.get("computation_releaseTime")).doubleValue();

			// communication latency
			mapper_.notifyMessageReceipt(id_, sendTime, currentTime_, compDelay);

			// end-to-end latency
			// mapper_.notifyMessageReceipt(id_, releaseTime, currentTime_);
			if (_debugging)
				_debug("Mapper notified");

		}

	}

}
