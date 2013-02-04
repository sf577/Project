/* Consumer receives packets and notifies the mapper when receiving them. */

package lsi.noc.application;

import java.util.Iterator;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import lsi.noc.application.LifelineMapper;

/**
 * 
 * @version 1.01 (York, 18/08/2009)
 * @author Sanna Maatta, Leandro Soares Indrusiak
 */
public abstract class Consumer extends PlatformCommunicationInterface {

	protected Consumer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// Signal declarations

		ack_out = new TypedIOPort(this, "ack_out", false, true);
		data_in = new TypedIOPort(this, "data_in", true, false);

		ack_out.setTypeEquals(BaseType.BOOLEAN);

	}

	public TypedIOPort ack_out;
	public TypedIOPort data_in;

	/**
	 * This function gets the mapper in order to call its functions
	 * (notifyMessageReceipt())
	 * 
	 * @return mapper
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	protected Attribute getMapper() throws IllegalActionException,
			NameDuplicationException {

		NamedObj container = getContainer();

		return container.getAttribute("LifelineMapper");

	}

	// returns a reference to a producer that is attached to the same NoC router
	// i.e. has the same values for addressX and addressY parameters

	protected Producer getProducer() throws IllegalActionException {

		NamedObj container = getContainer();

		Iterator it = container.containedObjectsIterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof Producer) {
				// return producer if it has the same X and Y router coordinates
				// of this consumer
				if (this.getAddressX() == ((Producer) o).getAddressX()
						& this.getAddressY() == ((Producer) o).getAddressY())
					return (Producer) o;

			}
		}

		return null;

	}

	protected void sendAck() throws IllegalActionException {
		Token trueToken = BooleanToken.TRUE;
		ack_out.send(0, trueToken);

	}

	protected int actorID_;
	protected LifelineMapper mapper_; // Instance of the mapper
	protected int state_; // Either receiving header or trailer
	protected int id_; // Packet id is used to tell the mapper, which packet
						// just arrived

}
