/* This class is an abstract super class for all producers (e.g. ArgusProducer, BocaProducer, JoselitoProducer, and RenatoProducer) */

package lsi.noc.application;

import java.util.Hashtable;
import java.util.Vector;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @version 1.02 (York, 31/03/2010)
 * @author Sanna Maatta, Leandro Soares Indrusiak
 * @version 1.03 (York, 10/08/2010) Osmar Marchi dos Santos
 * @info Extended producer to have a priority field in the packet (record token)
 */

public abstract class Producer extends PlatformCommunicationInterface {

	protected Producer(CompositeEntity container, String name)

	throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// Packet delay starts right after last packet's delay (true) or after
		// the last packet is sent completely (false)
		delayScheme = new Parameter(this, "delayScheme");
		delayScheme.setTypeEquals(BaseType.BOOLEAN);
		delayScheme.setExpression("true");
		delayScheme_ = ((BooleanToken) delayScheme.getToken()).booleanValue();

		// Port declarations
		ack_in = new TypedIOPort(this, "ack_in", true, false);
		data_out = new TypedIOPort(this, "data_out", false, true);

		buffer_ = new Vector();
		delays_ = new Hashtable();

		// Labels and types for a packet fields. A packet is a record token.
		labels_ = new String[13];
		Type[] types = new Type[13];

		labels_[0] = "x";
		labels_[1] = "y";
		labels_[2] = "size";
		labels_[3] = "payload";
		labels_[4] = "ID";
		labels_[5] = "sendTime";
		labels_[6] = "lastPacket";
		labels_[7] = "buffer_size";
		labels_[8] = "source_x";
		labels_[9] = "source_y";
		labels_[10] = "computation_delay";
		labels_[11] = "computation_releaseTime";
		labels_[12] = "priority";

		types[0] = BaseType.INT;
		types[1] = BaseType.INT;
		types[2] = BaseType.INT;
		types[3] = BaseType.INT;
		types[4] = BaseType.INT;
		types[5] = BaseType.DOUBLE;
		types[6] = BaseType.BOOLEAN;
		types[7] = BaseType.INT;
		types[8] = BaseType.INT;
		types[9] = BaseType.INT;
		types[10] = BaseType.DOUBLE;
		types[11] = BaseType.DOUBLE;
		types[12] = BaseType.INT;

		values_ = new Token[13];
		values_[0] = new IntToken(0);
		values_[1] = new IntToken(0);
		values_[2] = new IntToken(0);
		values_[3] = new IntToken(0);
		values_[4] = new IntToken(0);
		values_[5] = new DoubleToken(0);
		values_[6] = new BooleanToken(true); // A big packet split into smaller
												// ones, false, if not the last
												// packet
		values_[7] = new IntToken(0);
		values_[8] = new IntToken(0);
		values_[9] = new IntToken(0);
		values_[10] = new DoubleToken(0);
		values_[11] = new DoubleToken(0);
		values_[12] = new IntToken(0);

		RecordType declaredType = new RecordType(labels_, types);

		// Types for ports
		ack_in.setTypeEquals(BaseType.BOOLEAN);
		data_out.setTypeEquals(declaredType);

	}

	public TypedIOPort ack_in;
	public TypedIOPort data_out;
	public Parameter delayScheme;
	public Vector inst;

	/**
	 * Changes attribute values
	 */
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {

		if (attribute == delayScheme) {

			boolean newDelayScheme = ((BooleanToken) delayScheme.getToken())
					.booleanValue();
			delayScheme_ = newDelayScheme;

		} else {
			super.attributeChanged(attribute);
		}
	}

	/**
	 * Creates a new packet containing the x and y -coordinates of the address,
	 * packet ID, data token, packet size, and in case of a big packet split to
	 * smaller ones, last tells if this is the last subpacket or not. Method
	 * returns the packet that's just created.
	 * 
	 * @param x
	 * @param y
	 * @param ID
	 * @param data
	 * @param size
	 * @param last
	 * @return
	 * @throws IllegalActionException
	 */
	protected RecordToken createPacket(int x, int y, int ID, Token data,
			int size, boolean last, double preCompDelay)
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
		values_[10] = new DoubleToken(preCompDelay);

		RecordToken xy = new RecordToken(labels_, values_);
		return xy;

	}

	/**
	 * Creates a new packet containing the x and y -coordinates of the address,
	 * packet ID, data token, packet size, preCompDelay and the priority of the
	 * communication. Method returns the packet that's just created.
	 * 
	 * @param x
	 * @param y
	 * @param ID
	 * @param data
	 * @param size
	 * @param last
	 * @param preCompDelay
	 * @param priority
	 * @return
	 * @throws IllegalActionException
	 */
	protected RecordToken createPacket(int x, int y, int ID, Token data,
			int size, boolean last, double releaseTime, int priority)
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
		values_[12] = new IntToken(priority);

		RecordToken xy = new RecordToken(labels_, values_);
		return xy;

	}

	protected RecordToken createPacket(RecordToken packet, double sendTime,
			double preCompDelay) throws IllegalActionException {

		values_[0] = new IntToken(((IntToken) packet.get("x")).intValue());
		values_[1] = new IntToken(((IntToken) packet.get("y")).intValue());
		values_[2] = new IntToken(((IntToken) packet.get("size")).intValue());
		values_[3] = packet.get("payload");
		values_[4] = new IntToken(((IntToken) packet.get("ID")).intValue());
		values_[5] = new DoubleToken(sendTime);
		values_[6] = new BooleanToken(
				((BooleanToken) packet.get("lastPacket")).booleanValue());
		values_[8] = new IntToken(
				((IntToken) packet.get("source_x")).intValue());
		values_[9] = new IntToken(
				((IntToken) packet.get("source_y")).intValue());
		values_[10] = new DoubleToken(preCompDelay);

		RecordToken xy = new RecordToken(labels_, values_);
		return xy;

	}

	/**
	 * 
	 * @param token
	 * @param x
	 * @param y
	 * @param id
	 * @param totalPacketSize
	 * @param subPacketSize
	 * @param delay
	 * @param priority
	 * 
	 * @throws IllegalActionException
	 */
	public void sendPacket(Token token, int x, int y, int id,
			int totalPacketSize, int subPacketSize, Token delay, int priority)
			throws IllegalActionException {

		// simply ignores priority
		// must be overridden by subclasses that need to use priority
		sendPacket(token, x, y, id, totalPacketSize, subPacketSize, delay);
	}

	/**
	 * 
	 * @param token
	 * @param x
	 * @param y
	 * @param id
	 * @param totalPacketSize
	 * @param subPacketSize
	 * @param delay
	 * @param priority
	 * @param request
	 *            Whether this message is a request followed by a response
	 * @param response
	 *            The expected number of flits of the response
	 * 
	 * @throws IllegalActionException
	 */

	public void sendPacket(Token token, int x, int y, int id, int size,
			int subPacketSize, Token compDelay, int priority, boolean request,
			int response) throws IllegalActionException {

		// simply ignores request/response
		// must be overridden by subclasses that need to use request/response
		// optimization

		sendPacket(token, x, y, id, size, subPacketSize, compDelay, priority);

	}

	/**
	 * 
	 * @param token
	 * @param x
	 * @param y
	 * @param id
	 * @param totalPacketSize
	 * @param subPacketSize
	 * @param delay
	 * @throws IllegalActionException
	 */
	public abstract void sendPacket(Token token, int x, int y, int id,
			int totalPacketSize, int subPacketSize, Token delay)
			throws IllegalActionException;

	protected boolean delayScheme_;

	protected String[] labels_;
	protected Token[] values_;

	protected Vector buffer_;
	protected Hashtable delays_;

}
