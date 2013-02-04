package lsi.noc.purplepringle;

/**

 * @author Leandro Indrusiak
 * @version 1.0 (York, 26/7/2010)

 */

import java.awt.Point;
import java.util.Hashtable;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import lsi.noc.kernel.priority.PriorityTrafficFlow;
import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DNoCFactory;
import lsi.noc.kernel.Mesh2DXYRouting;
import lsi.noc.kernel.ProcessingCore;
import lsi.noc.kernel.Route;

@SuppressWarnings("serial")
public class Interconnect extends TypedAtomicActor implements
		FlowTableController {

	protected TypedIOPort input;
	protected TypedIOPort output;

	// protected TypedIOPort txack;
	// protected TypedIOPort rxack;

	public Parameter dimension;
	public Parameter hopLatency;
	public Parameter routingLatency;

	private int xdim, ydim;
	private double hopL, routingL;

	protected FlowTable flowtable;
	protected Hashtable<PriorityTrafficFlow, RecordToken> tokentable;
	protected Mesh2DNoC network;
	protected Mesh2DXYRouting routingAlgorithm;

	public Interconnect(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {

		super(container, name);

		dimension = new Parameter(this, "dimension");
		dimension.setExpression("{5, 5}");
		dimension.setTypeEquals(new ArrayType(BaseType.INT));
		xdim = 5;
		ydim = 5;

		hopLatency = new Parameter(this, "hopLatency");
		hopLatency.setExpression("1.0");
		hopLatency.setTypeEquals(BaseType.DOUBLE);
		hopL = 1.0;

		routingLatency = new Parameter(this, "routingLatency");
		routingLatency.setExpression("3.0");
		routingLatency.setTypeEquals(BaseType.DOUBLE);
		routingL = 1.0;

		input = new TypedIOPort(this, "input", true, false);
		input.setTypeEquals(BaseType.GENERAL);
		input.setMultiport(true);
		StringAttribute posin = new StringAttribute(input, "_cardinal");
		posin.setExpression("WEST");
		SingletonParameter namein = new SingletonParameter(input, "_showName");
		namein.setExpression("true");

		/*
		 * txack = new TypedIOPort(this, "txack", false, true);
		 * txack.setTypeEquals(BaseType.GENERAL); txack.setMultiport(true);
		 * StringAttribute postxack = new StringAttribute(txack, "_cardinal");
		 * postxack.setExpression("WEST"); SingletonParameter nametxack = new
		 * SingletonParameter(txack, "_showName");
		 * nametxack.setExpression("true");
		 */

		output = new TypedIOPort(this, "output", false, true);
		output.setTypeEquals(BaseType.GENERAL);
		output.setMultiport(true);
		StringAttribute posout = new StringAttribute(output, "_cardinal");
		posout.setExpression("EAST");
		SingletonParameter nameout = new SingletonParameter(output, "_showName");
		nameout.setExpression("true");

		/*
		 * rxack = new TypedIOPort(this, "rxack", true, false);
		 * rxack.setTypeEquals(BaseType.BOOLEAN); rxack.setMultiport(true);
		 * StringAttribute posrxack = new StringAttribute(rxack, "_cardinal");
		 * posrxack.setExpression("EAST"); SingletonParameter namerxack = new
		 * SingletonParameter(rxack, "_showName");
		 * namerxack.setExpression("true");
		 */

	}

	public void initialize() throws IllegalActionException {

		flowtable = new FlowTable(this);
		tokentable = new Hashtable<PriorityTrafficFlow, RecordToken>();

		ArrayToken dim_tmp = (ArrayToken) dimension.getToken();
		xdim = ((IntToken) dim_tmp.getElement(0)).intValue();
		ydim = ((IntToken) dim_tmp.getElement(1)).intValue();

		DoubleToken hop = (DoubleToken) hopLatency.getToken();
		hopL = hop.doubleValue();

		DoubleToken rout = (DoubleToken) routingLatency.getToken();
		routingL = rout.doubleValue();

		Mesh2DNoCFactory factory = new Mesh2DNoCFactory();
		network = (Mesh2DNoC) factory.createInterconnect(xdim, ydim);

		routingAlgorithm = new Mesh2DXYRouting();

	}

	public void fire() throws IllegalActionException {
		super.fire();
		processHeaders();
		flowtable.update(getDirector().getModelTime());
	}

	protected void processHeaders() throws IllegalActionException {
		/***********************************
		 ********** PROCESS HEADERS**********
		 ***********************************/

		int indmax = input.getWidth();
		for (int i = 0; i < indmax; i++) {

			if (input.hasToken(i)) {
				RecordToken record = (RecordToken) input.get(i);

				// get flow attributes from header token
				int priority = ((IntToken) record.get("priority")).intValue();

				// get x,y coords of the source
				int sx = ((IntToken) record.get("source_x")).intValue();
				int sy = ((IntToken) record.get("source_y")).intValue();

				// get x,y coords of the destination
				int dx = ((IntToken) record.get("x")).intValue();
				int dy = ((IntToken) record.get("y")).intValue();

				// get payload size plus 2 (header and size flits)
				int payload = ((IntToken) record.get("size")).intValue() + 2;

				// if packet is for the actual sender
				// send token out immediately
				// otherwise, create flow and add to table
				if (sx == dx & sy == dy) {

					output.send(getIndex(new Point(dx, dy)), record);

				}

				else {

					ProcessingCore src = network.getCore(sx, sy);
					ProcessingCore dst = network.getCore(dx, dy);

					Route route = routingAlgorithm.route(src, dst, network);

					PriorityTrafficFlow flow = new PriorityTrafficFlow(route,
							priority);
					flow.setPayload(payload);

					flowtable.addFlow(
							flow,
							getDirector().getModelTime(),
							new Time(getDirector(), flow.getBasicLatency(
									getHopLatency(), getRoutingLatency())));

					tokentable.put(flow, record);
				}

				if (_debugging)
					_debug("=====PROCESS HEADERS=========");
				if (_debugging)
					_debug("Time: " + getDirector().getModelTime());
				if (_debugging)
					_debug("source: " + sx + "," + sy);
				if (_debugging)
					_debug("dest: " + dx + "," + dy);
				if (_debugging)
					_debug("payload: " + payload);
				if (_debugging)
					_debug("=====/HEADERS===================");
			}
		}

	}

	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {

		if (attribute == dimension) {
			// get new NOC dimension
			ArrayToken dim_tmp = (ArrayToken) dimension.getToken();
			xdim = ((IntToken) dim_tmp.getElement(0)).intValue();
			ydim = ((IntToken) dim_tmp.getElement(1)).intValue();
		} else if (attribute == hopLatency) {
			// get new hop latency
			DoubleToken hop = (DoubleToken) hopLatency.getToken();
			hopL = hop.doubleValue();
		} else if (attribute == routingLatency) {
			// get new routing latency
			DoubleToken rout = (DoubleToken) routingLatency.getToken();
			routingL = rout.doubleValue();
		}

	}

	public int getIndex(Point p) {

		return p.y * xdim + p.x;
	}

	public Point getPosition(int i) {
		int py = i / xdim;
		int px = i % xdim;
		return new Point(px, py);

	}

	public void pruneDependencies() {
		super.pruneDependencies();
		removeDependency(input, output);
	}

	public boolean addFiringRequest(Time time) {

		try {
			getDirector().fireAt(this, getDirector().getModelTime().add(time));
			if (_debugging)
				_debug("Flow table requested firing at: "
						+ getDirector().getModelTime().add(time)
								.getDoubleValue());
			return true;
		} catch (Exception e) {
			if (_debugging)
				_debug("Denied flow table firing request for: "
						+ getDirector().getModelTime().add(time)
								.getDoubleValue());
			if (_debugging)
				_debug("Exception: " + e);

			return false;
		}

	}

	public FlowTable getFlowTable() {
		return flowtable;
	}

	public void setFlowTable(FlowTable t) {
		this.flowtable = t;
	}

	@Override
	public double getHopLatency() {

		return hopL;
	}

	@Override
	public double getRoutingLatency() {
		return routingL;
	}

	@Override
	public void notifyFlowCompletion(PriorityTrafficFlow flow) {

		RecordToken record = tokentable.get(flow);
		// get x,y coords of the destination
		int dx = ((IntToken) record.get("x")).intValue();
		int dy = ((IntToken) record.get("y")).intValue();
		int index = getIndex(new Point(dx, dy));
		if (_debugging)
			_debug("Flow completion notified: " + record + " at time: "
					+ getDirector().getModelTime());

		try {
			output.send(index, record);
		} catch (Exception e) {
			if (_debugging)
				_debug("Problem when releasing the token of a completed message: "
						+ record);
			if (_debugging)
				_debug("Message destination: " + dx + "," + dy);
			if (_debugging)
				_debug("Exception: " + e);
		}

	}

}