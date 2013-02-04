package lsi.noc.application.greenpringle;

import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class VCConsumerCB extends VCConsumer {

	public VCConsumerCB(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);
		ack_out.setTypeEquals(BaseType.INT);
	}

	protected void sendAck(int i) throws IllegalActionException {
		IntToken t = new IntToken(10); // consumer always has credit
		ack_out.send(i, t);

	}

	public void pruneDependencies() {
		super.pruneDependencies();

		removeDependency(ack_out, data_in);
		removeDependency(data_in, ack_out);
	}

}
