// TLM Demo modified by Sanna Maatta 
// Modified the "hard coding of the year": packet size 999 was not the only option any more :-D
package lsi.noc.boca;

import java.util.Random;
import java.lang.String;
import java.lang.Math;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.IntToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class TLMDemo extends TypedAtomicActor {

	// array used to enumerate ports
	protected TypedIOPort[][] input;
	protected TypedIOPort[][] txack;
	protected TypedIOPort[][] output;
	protected TypedIOPort[][] rxack;

	// debug port
	protected TypedIOPort input_debug;
	protected StringAttribute posIndebug;
	protected SingletonParameter nameIndebug;
	protected TypedIOPort output_debug;
	protected StringAttribute posOutdebug;
	protected SingletonParameter nameOutdebug;

	// values for the port state description
	static final int INACTIVE = 0;
	static final int IDLE = 1;
	static final int READ = 2;
	static final int BUSY = 3;
	static final int SEND = 4;
	static final int WAIT = 5;
	static final int WRITE = 6;
	static final int ACK = 7;

	// delay model description
	static final int gaussian = 9;
	static final int uniform = 8;

	// packet related variables
	protected int[][] tx_size;// tx packet size
	protected int[][] tx_cont;// tx current size of the packet
	protected int[][] rx_size;// rx packet size
	protected int[][] rx_cont;// rx current size of the packet

	// buffer and states for ports
	protected int[][] input_state;// input port states
	protected int[][] output_state;// output port states
	protected RecordToken[][][] input_buffer;// current reading packet
	protected RecordToken[][][] output_buffer;// current writing packet
	protected RecordToken[][] input_header;// buffer for header

	// time related parameters
	protected double[][] input_delay;// ??
	protected double[][] output_delay;// ??
	protected double[][] link_delay;// ??

	// Nack timer and state
	protected double[][] nack_timer;// timer for sending Nack
	protected int[][] nack_state;// 1 for to be sent, 0 for already sent

	// current destination address
	protected int xAddress;
	protected int yAddress;

	// port direction parameters
	protected StringAttribute[][] posInput;
	protected StringAttribute[][] posOutput;
	protected StringAttribute[][] posTxack;
	protected StringAttribute[][] posRxack;

	// port name display parameters
	protected SingletonParameter[][] nameInput;
	protected SingletonParameter[][] nameOutput;
	protected SingletonParameter[][] nameTxack;
	protected SingletonParameter[][] nameRxack;

	// time related parameters
	public Parameter period;
	public Parameter delay_model;
	public Parameter delay_parameters;
	public Parameter Dimension;

	public double currenttime;

	// parameters instantiated
	protected int xdim;
	protected int ydim;
	protected double per;// length of a clock cycle
	protected double miu;
	protected double sigma;
	protected int rmode;

	// random object
	protected Random _random;

	public TLMDemo(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {

		super(container, name);

		// time related parameters
		period = new Parameter(this, "period");
		period.setExpression("2.0");
		period.setTypeEquals(BaseType.DOUBLE);

		delay_parameters = new Parameter(this, "delay_parameters");
		delay_parameters.setExpression("{2.0, 1.0}");
		delay_parameters.setTypeEquals(new ArrayType(BaseType.DOUBLE));

		delay_model = new Parameter(this, "delay_model");
		delay_model.setExpression("'Uniform'");
		delay_model.setTypeEquals(BaseType.STRING);

		Dimension = new Parameter(this, "Dimension");
		Dimension.setExpression("{5, 5}");
		Dimension.setTypeEquals(new ArrayType(BaseType.INT));

		// instantiate debug port;
		input_debug = new TypedIOPort(this, "input_debug", false, true);
		input_debug.setTypeEquals(BaseType.GENERAL);
		posIndebug = new StringAttribute(input_debug, "_cardinal");
		posIndebug.setExpression("WEST");
		nameIndebug = new SingletonParameter(input_debug, "_showName");
		nameIndebug.setExpression("true");

		output_debug = new TypedIOPort(this, "output_debug", false, true);
		output_debug.setTypeEquals(BaseType.GENERAL);
		posOutdebug = new StringAttribute(output_debug, "_cardinal");
		posOutdebug.setExpression("EAST");
		nameOutdebug = new SingletonParameter(output_debug, "_showName");
		nameOutdebug.setExpression("true");

		// set NoC dimension
		attributeChanged(Dimension);

		// create new random object
		_random = new Random(0);

		// icon description
		_attachText("_iconDescription", "<svg>\n" + "<rect x=\"0\" y=\"0\" "
				+ "width=\"320\" height=\"280\" "
				+ "style=\"fill:white; stroke:black\"/>\n"
				+ "<rect x=\"20\" y=\"20\" " + "width=\"280\" height=\"240\" "
				+ "style=\"fill:blue; stroke:black\"/>\n"
				+ "<polygon points=\"140,60 300,60 20,220 180,220\" "
				+ "style=\"fill:magenta; stroke:black\"/>\n"
				+ "<polygon points=\"140,60 20,220 300,60 180,220\" "
				+ "style=\"fill:white; stroke:black\"/>\n" + "</svg>\n");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.actor.AtomicActor#initialize()
	 */
	public void initialize() throws IllegalActionException {

		super.initialize();

		// initialize packet related variables
		tx_size = new int[xdim][ydim];
		tx_cont = new int[xdim][ydim];
		rx_size = new int[xdim][ydim];
		rx_cont = new int[xdim][ydim];

		// initialize storage elements
		input_state = new int[xdim][ydim];
		output_state = new int[xdim][ydim];
		input_buffer = new RecordToken[xdim][ydim][];
		output_buffer = new RecordToken[xdim][ydim][];
		input_header = new RecordToken[xdim][ydim];
		input_delay = new double[xdim][ydim];
		output_delay = new double[xdim][ydim];
		link_delay = new double[xdim][ydim];

		// for Nack
		nack_timer = new double[xdim][ydim];
		nack_state = new int[xdim][ydim];

		for (int i = 0; i < xdim; i++) {
			for (int j = 0; j < ydim; j++) {
				// initialize packet length buffer
				tx_size[i][j] = 0;
				tx_cont[i][j] = 0;
				rx_size[i][j] = 0;
				rx_cont[i][j] = 0;

				// initialize time variables
				input_delay[i][j] = 0;
				output_delay[i][j] = 0;
				link_delay[i][j] = 0;

				// initialize Nack variables
				nack_timer[i][j] = 0;
				nack_state[i][j] = 0;

				// check the connection of input port
				if (input[i][j].getWidth() > 0) {
					input_state[i][j] = IDLE;
				} else {
					input_state[i][j] = INACTIVE;
				}

				// check the connection of output port
				if (output[i][j].getWidth() > 0) {
					output_state[i][j] = IDLE;
				} else {
					output_state[i][j] = INACTIVE;
				}
			}// for j
		}// for i

	}

	/*
	 * combine with fire() to finish the packet transfer function
	 */
	public boolean prefire() throws IllegalActionException {
		if (_debugging)
			_debug("==================== Time = "
					+ getDirector().getModelTime() + " ====================");

		currenttime = getDirector().getModelTime().getDoubleValue();

		input_debug.send(0, new StringToken(Double.toString(currenttime)));

		for (int i = 0; i < xdim; i++) {
			for (int j = 0; j < ydim; j++) {
				// check if there's Nack to be sent
				if ((nack_state[i][j] == 1)
						&& (currenttime >= nack_timer[i][j])) {
					// set nack state back
					nack_state[i][j] = 0;

					// send Nack
					BooleanToken ack = new BooleanToken(false);
					txack[i][j].send(0, ack);
					// System.out.println("MATRIX NACK LINE " + 261);

					// if (_debugging) _debug("input"+Integer.toString(i)
					// +Integer.toString(j)+" ackToken "+Double.toString(currenttime));

					// debug send
					input_debug.send(0,
							new StringToken("input" + Integer.toString(i)
									+ Integer.toString(j) + " ackToken "
									+ Double.toString(currenttime)));
					input_debug.send(0, ack);
				}

				if (input_state[i][j] == READ) {
					// if ((input_delay[i][j] <= currenttime) && (tx_cont[i][j]
					// == 0)) {
					if ((tx_cont[i][j] == 0)) {
						// send header ack
						BooleanToken ack = new BooleanToken(true);
						txack[i][j].send(0, ack);

						// System.out.println("matrix header ack line 276");

						// increase flit count by 1
						tx_cont[i][j]++;

						// debug send
						input_debug.send(0,
								new StringToken("input" + Integer.toString(i)
										+ Integer.toString(j) + " ackToken "
										+ Double.toString(currenttime)));
						input_debug.send(0, ack);
					}

					// current packet finished

					// System.out.println("tx_cont: " + tx_cont[i][j]);
					// System.out.println("tx_size: " + tx_size[i][j]);

					if ((tx_cont[i][j] == tx_size[i][j])
							&& (tx_cont[i][j] != 0)) {
						// set variables
						input_state[i][j] = SEND;
						// System.out.println("TO SENDING STATE");
						// model input read delay
						// Time delay = new Time(getDirector());
						// input_delay[i][j] = getdelay();
						// input_delay[i][j] = input_delay[i][j] + currenttime;
						// getDirector().fireAt(this,
						// delay.add(input_delay[i][j]+0.01));
						// getDirector().fireAt(this,
						// delay.add(input_delay[i][j]+1.0));
						// debug send
						input_debug.send(
								0,
								new StringToken("input" + Integer.toString(i)
										+ Integer.toString(j)
										+ " set to SEND at "
										+ Double.toString(currenttime)));
					}
				} else if (input_state[i][j] == SEND) {

					if (input[i][j].hasToken(0)) {
						// get token
						input[i][j].get(0);

						// System.out.println("matrix get token");

						// set nack state
						nack_state[i][j] = 1;

						// start an Nack timer
						// Time delay = new Time(getDirector());
						// nack_timer[i][j] = getdelay();
						// nack_timer[i][j] = nack_timer[i][j] + currenttime;
						// getDirector().fireAt(this,
						// delay.add(nack_timer[i][j]+0.01));
						// getDirector().fireAt(this,
						// delay.add(nack_timer[i][j]+1.0));
						// debug send
						input_debug.send(
								0,
								new StringToken("input" + Integer.toString(i)
										+ Integer.toString(j)
										+ " Nack state set "
										+ Double.toString(currenttime)));

					}

				} // else if send

				// FIRE ADDED by Sanna
				// Time currentTime = getDirector().getModelTime();
				// getDirector().fireAt(this, currentTime.add(1.0));

				if (output_state[i][j] == WAIT) {

				} else if (output_state[i][j] == WRITE) {
					// current packet finished
					if ((rx_cont[i][j] == rx_size[i][j])
							&& (rx_cont[i][j] != 0)) {

						if (rxack[i][j].hasToken(0)) {
							// get rx ack token
							BooleanToken ack = (BooleanToken) rxack[i][j]
									.get(0);

							// debug send
							output_debug.send(
									0,
									new StringToken("output"
											+ Integer.toString(i)
											+ Integer.toString(j)
											+ " ackToken "
											+ Double.toString(currenttime)));
							output_debug.send(0, ack);

							// rx ack is true, then set state to IDLE
							if (ack.booleanValue()) {
								// set variables
								output_state[i][j] = IDLE;
								rx_cont[i][j] = 0;
								rx_size[i][j] = 0;
								// debug send
								output_debug
										.send(0,
												new StringToken(
														"output"
																+ Integer
																		.toString(i)
																+ Integer
																		.toString(j)
																+ " set to IDLE at "
																+ Double.toString(currenttime)));
							} else {

							}
						}// if has token
					}// if rx_cont==rx_size
				}// else if output state is write

			}// for j
		}// for i

		return super.prefire();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.actor.AtomicActor#fire()
	 */
	public void fire() throws IllegalActionException {

		super.fire();

		// get current time
		currenttime = getDirector().getModelTime().getDoubleValue();

		// check all routers
		for (int i = 0; i < xdim; i++) {
			for (int j = 0; j < ydim; j++) {
				// check each input port
				if (input_state[i][j] == IDLE) {
					// check for tokens
					if (input[i][j].hasToken(0)) {
						// get token, should be the header
						input_header[i][j] = (RecordToken) input[i][j].get(0);
						// get destination address
						xAddress = ((IntToken) input_header[i][j].get("x"))
								.intValue();
						yAddress = ((IntToken) input_header[i][j].get("y"))
								.intValue();
						// debug
						if (_debugging)
							_debug("" + i + "" + j + ": New header: "
									+ input_header[i][j]);
						// get the size of the packet
						if (((IntToken) input_header[i][j].get("size"))
								.intValue() > 0) {
							// model input read delay
							// Time delay = new Time(getDirector());
							// input_delay[i][j] = getdelay();
							// input_delay[i][j] = input_delay[i][j] +
							// currenttime;
							// getDirector().fireAt(this,
							// delay.add(input_delay[i][j]-0.01));
							// getDirector().fireAt(this,
							// delay.add(input_delay[i][j]+0.01));
							// change state to wait
							input_state[i][j] = READ;

							// debug send
							input_debug.send(
									0,
									new StringToken("input"
											+ Integer.toString(i)
											+ Integer.toString(j)
											+ " headToken "
											+ Double.toString(currenttime)));
							input_debug.send(0, input_header[i][j]);
							input_debug.send(
									0,
									new StringToken("input"
											+ Integer.toString(i)
											+ Integer.toString(j)
											+ " set to READ at "
											+ Double.toString(currenttime)));
						} else {
							// send Nack to token producer
							BooleanToken ack = new BooleanToken(false);
							txack[i][j].send(0, ack);
							// System.out.println("MATRIX NACK LINE " + 411);

							// debug send
							input_debug.send(0, ack);

							// stay in current state
							input_state[i][j] = IDLE;
						}
						getDirector().fireAt(this, currenttime + 1.0);
					} // if hastoken
					else {
						input_state[i][j] = IDLE;
					}
				} else if (input_state[i][j] == READ) {

					// if there's incoming token
					if (input[i][j].hasToken(0)) {
						if (tx_cont[i][j] == 1) {
							// get the size token
							RecordToken sizeToken = (RecordToken) input[i][j]
									.get(0);
							// size of the packet, including the size token
							// TODO: +2????????????????????
							tx_size[i][j] = ((IntToken) sizeToken.get("size"))
									.intValue() + 2;
							// System.out.println("getting size");

							input_buffer[i][j] = new RecordToken[tx_size[i][j]];
							input_buffer[i][j][0] = input_header[i][j];
							input_buffer[i][j][1] = sizeToken;

							// debug send
							input_debug.send(
									0,
									new StringToken("input"
											+ Integer.toString(i)
											+ Integer.toString(j)
											+ " sizeToken "
											+ Double.toString(currenttime)));
							input_debug.send(0, sizeToken);

							// increase the flit count by 1
							tx_cont[i][j]++;
						} else {
							// get the payload of the packet
							RecordToken bodyToken = (RecordToken) input[i][j]
									.get(0);
							input_buffer[i][j][tx_cont[i][j]] = bodyToken;
							// System.out.println("getting the rest");
							// debug send
							input_debug.send(
									0,
									new StringToken("input"
											+ Integer.toString(i)
											+ Integer.toString(j)
											+ " bodyToken "
											+ Double.toString(currenttime)));
							input_debug.send(0, bodyToken);

							// increase the flit count by 1
							tx_cont[i][j]++;
						}

						// send immediate ack
						BooleanToken ack = new BooleanToken(true);
						txack[i][j].send(0, ack);

						// System.out.println("matrix ack payload");

						// debug send
						input_debug.send(0,
								new StringToken("input" + Integer.toString(i)
										+ Integer.toString(j) + " ackToken "
										+ Double.toString(currenttime)));
						input_debug.send(0, ack);
					} else {
						input_state[i][j] = READ;
					}// if has token

					// FIRE ADDED by Sanna
					getDirector().fireAt(this, currenttime + 1.0);

				} else if (input_state[i][j] == SEND) {

					// if (currenttime >= input_delay[i][j]) {
					{

						// get destination address
						xAddress = ((IntToken) input_header[i][j].get("x"))
								.intValue();
						yAddress = ((IntToken) input_header[i][j].get("y"))
								.intValue();

						if (output_state[xAddress][yAddress] == IDLE) {
							// put the packet to output buffer
							output_buffer[xAddress][yAddress] = input_buffer[i][j];
							// set packet size
							rx_size[xAddress][yAddress] = tx_size[i][j];
							// set output state
							output_state[xAddress][yAddress] = WAIT;

							// set input state
							input_state[i][j] = IDLE;
							// set count and size
							tx_cont[i][j] = 0;
							tx_size[i][j] = 0;

							// model Link delay
							// int multiplier =
							// Math.abs(xAddress-i)+Math.abs(yAddress-j);
							// Time delay = new Time(getDirector());
							// link_delay[xAddress][yAddress] =
							// multiplier*getdelay();
							// link_delay[xAddress][yAddress] =
							// link_delay[xAddress][yAddress] + currenttime;
							// getDirector().fireAt(this,
							// delay.add(link_delay[xAddress][yAddress]+0.01));
							// getDirector().fireAt(this,
							// delay.add(link_delay[xAddress][yAddress]+1.0));
							// getDirector().fireAt(this, currenttime + 1.0 );

							// debug send
							input_debug.send(
									0,
									new StringToken("input"
											+ Integer.toString(i)
											+ Integer.toString(j)
											+ " set to IDLE at "
											+ Double.toString(currenttime)));
							input_debug.send(
									0,
									new StringToken("output"
											+ Integer.toString(xAddress)
											+ Integer.toString(yAddress)
											+ " set to WAIT at "
											+ Double.toString(currenttime)));
						} else {
							input_state[i][j] = SEND;
						}

					} // if time
					/*
					 * else {
					 * 
					 * //System.out.println("current: " + currenttime);
					 * //System.out.println("delay: " + input_delay[i][j]); }
					 */
					// MOLLER
					getDirector().fireAt(this, currenttime + 1.0);

				}// if send

				// else {
				// Time currentTime = getDirector().getModelTime();
				// getDirector().fireAt(this, currentTime.add(1.0));
				// }

				// check each output port
				if (output_state[i][j] == WAIT) {

					// if ((link_delay[i][j] <= currenttime) && (rx_cont[i][j]
					// == 0)) {
					if (rx_cont[i][j] == 0) {
						// set output state to WRITE
						output_state[i][j] = WRITE;

						// debug send
						output_debug.send(
								0,
								new StringToken("output" + Integer.toString(i)
										+ Integer.toString(j)
										+ " set to WRITE at "
										+ Double.toString(currenttime)));

						// model output header delay
						// Time delay = new Time(getDirector());
						// output_delay[i][j] = getdelay();
						// output_delay[i][j] = output_delay[i][j] +
						// currenttime;
						// getDirector().fireAt(this,
						// delay.add(output_delay[i][j]+0.01));
						// getDirector().fireAt(this,
						// delay.add(output_delay[i][j]+1.0));
					}
					// MOLLER
					getDirector().fireAt(this, currenttime + 1.0);
				} else if (output_state[i][j] == WRITE) {
					// if (currenttime >= output_delay[i][j]) {
					// check if this is header
					if (rx_cont[i][j] == 0) {
						// send packet header
						output[i][j].send(0, output_buffer[i][j][0]);
						// System.out.println("sending header out");
						// debug send
						output_debug.send(0,
								new StringToken("output" + Integer.toString(i)
										+ Integer.toString(j) + " headToken "
										+ Double.toString(currenttime)));
						output_debug.send(0, output_buffer[i][j][0]);

						rx_cont[i][j]++;
					} else if (rx_cont[i][j] < rx_size[i][j]) {
						if (rxack[i][j].hasToken(0)) {
							// get rx ack token
							BooleanToken ack = (BooleanToken) rxack[i][j]
									.get(0);

							// debug send
							output_debug.send(
									0,
									new StringToken("output"
											+ Integer.toString(i)
											+ Integer.toString(j)
											+ " ackToken "
											+ Double.toString(currenttime)));
							output_debug.send(0, ack);

							// rx ack is true, then send next flit
							if (ack.booleanValue()) {
								// send token and update transmitted packet
								// length
								output[i][j]
										.send(0,
												(RecordToken) output_buffer[i][j][rx_cont[i][j]]);
								// System.out.println("sending size out");
								rx_cont[i][j]++;

								output_debug
										.send(0,
												new StringToken(
														"output"
																+ Integer
																		.toString(i)
																+ Integer
																		.toString(j)
																+ " bodyToken "
																+ Double.toString(currenttime)));
								output_debug.send(0,
										output_buffer[i][j][rx_cont[i][j] - 1]);
							} else {
								// send last flit to receiver again
								output[i][j]
										.send(0,
												(RecordToken) output_buffer[i][j][rx_cont[i][j] - 1]);
								// System.out.println("sending payload");
							}
						}// if has token
					} else {
						// do nothing here, and this situation will never occur
					}// if rx_cont
					// }// if time

					// MOLLER
					getDirector().fireAt(this, currenttime + 1.0);
				}// output

			}// for j
		}// for i

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute
	 * )
	 */
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {

		if (attribute == Dimension) {
			// get new NOC dimension
			ArrayToken dim_tmp = (ArrayToken) Dimension.getToken();
			int xdim_tmp = ((IntToken) dim_tmp.getElement(0)).intValue();
			int ydim_tmp = ((IntToken) dim_tmp.getElement(1)).intValue();

			// check new dimension
			if ((xdim_tmp != xdim) || (ydim_tmp != ydim)) {
				// update the displayed ports
				portDisplay(xdim_tmp, ydim_tmp);
				// update the variables
				xdim = xdim_tmp;
				ydim = ydim_tmp;
				// reinitialize this object
				initialize();
			} else {

			}
		} else if (attribute == period) {
			per = ((DoubleToken) period.getToken()).doubleValue();
		} else if (attribute == delay_parameters) {
			ArrayToken tableValue = (ArrayToken) delay_parameters.getToken();

			miu = ((DoubleToken) tableValue.getElement(0)).doubleValue();
			sigma = ((DoubleToken) tableValue.getElement(1)).doubleValue();

		} else if (attribute == delay_model) {

			String s_rmode = ((StringToken) delay_model.getToken())
					.stringValue();

			if (s_rmode.equalsIgnoreCase("gaussian")) {
				rmode = gaussian;
			} else if (s_rmode.equalsIgnoreCase("uniform")) {
				rmode = uniform;
			} else {

			}
		} else {
			super.attributeChanged(attribute);
		}
	}

	/*
	 * change the number of ports depending on the parameter changes
	 */
	private void portDisplay(int new_xdim, int new_ydim)
			throws IllegalActionException {

		// delete unused ports
		for (int i = 0; i < xdim; i++) {
			for (int j = 0; j < ydim; j++) {
				if ((i >= new_xdim) || (j >= new_ydim)) {
					try {
						input[i][j].setContainer(null);
						txack[i][j].setContainer(null);
						output[i][j].setContainer(null);
						rxack[i][j].setContainer(null);
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {

				}
			}
		}

		// create array to orgnize all the ports and their parameters
		input = new TypedIOPort[new_xdim][new_ydim];
		output = new TypedIOPort[new_xdim][new_ydim];
		txack = new TypedIOPort[new_xdim][new_ydim];
		rxack = new TypedIOPort[new_xdim][new_ydim];

		posInput = new StringAttribute[new_xdim][new_ydim];
		posOutput = new StringAttribute[new_xdim][new_ydim];
		posTxack = new StringAttribute[new_xdim][new_ydim];
		posRxack = new StringAttribute[new_xdim][new_ydim];

		nameInput = new SingletonParameter[new_xdim][new_ydim];
		nameOutput = new SingletonParameter[new_xdim][new_ydim];
		nameTxack = new SingletonParameter[new_xdim][new_ydim];
		nameRxack = new SingletonParameter[new_xdim][new_ydim];

		// start filling all the arrays just created
		for (int i = 0; i < new_xdim; i++) {
			for (int j = 0; j < new_ydim; j++) {
				// check port name
				TypedIOPort temp_port = (TypedIOPort) getPort("input_"
						+ Integer.toString(i) + "_" + Integer.toString(j));
				if (temp_port == null) {
					// create a new port and add it to this object
					try {
						input[i][j] = new TypedIOPort(this, "input_"
								+ Integer.toString(i) + "_"
								+ Integer.toString(j), true, false);
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					input[i][j] = temp_port;
				}
				// set port type
				input[i][j].setTypeEquals(BaseType.GENERAL);
				input[i][j].setMultiport(true);

				// check position attribute
				StringAttribute temp_attribute = (StringAttribute) input[i][j]
						.getAttribute("_cardinal");
				if (temp_attribute == null) {
					// create new port position attribute
					try {
						posInput[i][j] = new StringAttribute(input[i][j],
								"_cardinal");
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					posInput[i][j] = temp_attribute;
				}
				// set position
				posInput[i][j].setExpression("WEST");

				// check name display attribute
				SingletonParameter temp_sparameter = (SingletonParameter) input[i][j]
						.getAttribute("_showName");
				if (temp_sparameter == null) {
					// create new port position attribute
					try {
						nameInput[i][j] = new SingletonParameter(input[i][j],
								"_showName");
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					nameInput[i][j] = temp_sparameter;
				}
				// set name to be displayed
				nameInput[i][j].setExpression("true");
				;
			}
		}

		for (int i = 0; i < new_xdim; i++) {
			for (int j = 0; j < new_ydim; j++) {
				// check port name
				TypedIOPort temp_port = (TypedIOPort) getPort("txack_"
						+ Integer.toString(i) + "_" + Integer.toString(j));
				if (temp_port == null) {
					// create a new port and add it to this object
					try {
						txack[i][j] = new TypedIOPort(this, "txack_"
								+ Integer.toString(i) + "_"
								+ Integer.toString(j), false, true);
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					txack[i][j] = temp_port;
				}
				// set port type
				txack[i][j].setTypeEquals(BaseType.BOOLEAN);
				txack[i][j].setMultiport(true);

				// check position attribute
				StringAttribute temp_attribute = (StringAttribute) txack[i][j]
						.getAttribute("_cardinal");
				if (temp_attribute == null) {
					// create new port position attribute
					try {
						posTxack[i][j] = new StringAttribute(txack[i][j],
								"_cardinal");
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					posTxack[i][j] = temp_attribute;
				}
				// set position
				posTxack[i][j].setExpression("SOUTH");

				// check name display attribute
				SingletonParameter temp_sparameter = (SingletonParameter) txack[i][j]
						.getAttribute("_showName");
				if (temp_sparameter == null) {
					// create new port position attribute
					try {
						nameTxack[i][j] = new SingletonParameter(txack[i][j],
								"_showName");
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					nameTxack[i][j] = temp_sparameter;
				}
				// set name to be displayed
				nameTxack[i][j].setExpression("true");
				;
			}
		}

		for (int i = 0; i < new_xdim; i++) {
			for (int j = 0; j < new_ydim; j++) {
				// check port name
				TypedIOPort temp_port = (TypedIOPort) getPort("output_"
						+ Integer.toString(i) + "_" + Integer.toString(j));
				if (temp_port == null) {
					// create a new port and add it to this object
					try {
						output[i][j] = new TypedIOPort(this, "output_"
								+ Integer.toString(i) + "_"
								+ Integer.toString(j), false, true);
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					output[i][j] = temp_port;
				}
				// set port type
				output[i][j].setTypeEquals(BaseType.GENERAL);
				output[i][j].setMultiport(true);

				// check position attribute
				StringAttribute temp_attribute = (StringAttribute) output[i][j]
						.getAttribute("_cardinal");
				if (temp_attribute == null) {
					// create new port position attribute
					try {
						posOutput[i][j] = new StringAttribute(output[i][j],
								"_cardinal");
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					posOutput[i][j] = temp_attribute;
				}
				// set position
				posOutput[i][j].setExpression("EAST");

				// check name display attribute
				SingletonParameter temp_sparameter = (SingletonParameter) output[i][j]
						.getAttribute("_showName");
				if (temp_sparameter == null) {
					// create new port position attribute
					try {
						nameOutput[i][j] = new SingletonParameter(output[i][j],
								"_showName");
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					nameOutput[i][j] = temp_sparameter;
				}
				// set name to be displayed
				nameOutput[i][j].setExpression("true");
				;
			}
		}

		for (int i = 0; i < new_xdim; i++) {
			for (int j = 0; j < new_ydim; j++) {
				// check port name
				TypedIOPort temp_port = (TypedIOPort) getPort("rxack_"
						+ Integer.toString(i) + "_" + Integer.toString(j));
				if (temp_port == null) {
					// create a new port and add it to this object
					try {
						rxack[i][j] = new TypedIOPort(this, "rxack_"
								+ Integer.toString(i) + "_"
								+ Integer.toString(j), true, false);
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					rxack[i][j] = temp_port;
				}
				// set port type
				rxack[i][j].setTypeEquals(BaseType.BOOLEAN);
				rxack[i][j].setMultiport(true);

				// check position attribute
				StringAttribute temp_attribute = (StringAttribute) rxack[i][j]
						.getAttribute("_cardinal");
				if (temp_attribute == null) {
					// create new port position attribute
					try {
						posRxack[i][j] = new StringAttribute(rxack[i][j],
								"_cardinal");
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					posRxack[i][j] = temp_attribute;
				}
				// set position
				posRxack[i][j].setExpression("NORTH");

				// check name display attribute
				SingletonParameter temp_sparameter = (SingletonParameter) rxack[i][j]
						.getAttribute("_showName");
				if (temp_sparameter == null) {
					// create new port position attribute
					try {
						nameRxack[i][j] = new SingletonParameter(rxack[i][j],
								"_showName");
					} catch (NameDuplicationException ex) {
						ex.fillInStackTrace();
					}
				} else {
					nameRxack[i][j] = temp_sparameter;
				}
				// set name to be displayed
				nameRxack[i][j].setExpression("true");
				;
			}
		}

	}

	protected double getdelay() throws IllegalActionException {

		double delay = 0;

		if (rmode == gaussian) {
			delay = (sigma * (_random.nextGaussian()) + miu) * per;

			while (delay <= 0) {
				delay = (sigma * (_random.nextGaussian()) + miu) * per;
			}
		} else if (rmode == uniform) {
			delay = (sigma * (_random.nextDouble()) + miu) * per;

			while (delay <= 0) {
				delay = (sigma * (_random.nextDouble()) + miu) * per;
			}
		}
		return delay;
	}

}
