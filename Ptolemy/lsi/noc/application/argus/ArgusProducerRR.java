package lsi.noc.application.argus;

import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class ArgusProducerRR extends ArgusProducer {

	public ArgusProducerRR(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

	}

	public void sendPacket(Token token, int x, int y, int id, int size,
			int subPacketSize, Token compDelay, int priority, boolean request,
			int response) throws IllegalActionException {

		System.out.println("REQUEST DETECTED - RESPONSE SIZE: " + response);

		super.sendPacket(token, x, y, id, size, subPacketSize, compDelay,
				priority);

	}

}
