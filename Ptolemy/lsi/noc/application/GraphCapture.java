package lsi.noc.application;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;

import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.IntToken;
import ptolemy.kernel.util.Attribute;
import ptolemy.vergil.uml.Lifeline;
import ptolemy.vergil.uml.Message;
import ptolemy.vergil.uml.UMLSeqActor;
import lsi.noc.application.cafes.WindowPrincipal;

public class GraphCapture extends TypedAtomicActor {

	public GraphCapture(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		fileName = new FilePortParameter(this, "fileName");
		fileName.setExpression("application.cwg");

		x = new Parameter(this, "NoC Size (X)");
		x.setTypeEquals(BaseType.INT);
		x.setExpression("4");

		y = new Parameter(this, "NoC Size (Y)");
		y.setTypeEquals(BaseType.INT);
		y.setExpression("4");

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"0\" y=\"0\" width=\"46\" "
				+ "height=\"80\" style=\"fill:blue\"/>\n" + "</svg>\n");
	}

	// **************** public TypedIOPort ****************//

	/**
	 * The file name to which to write. This is a string with any form accepted
	 * by FilePortParameter. The default value is "System.out".
	 * 
	 * @see FilePortParameter
	 */
	public FilePortParameter fileName;
	public Parameter x, y; // 2D mesh dimension

	// **************************PUBLIC METHODS***********************//
	/**
	 * If the specified attribute is <i>fileName</i> and there is an open file
	 * being written, then close that file. The new file will be opened or
	 * created when it is next written to.
	 * 
	 * @param attribute
	 *            The attribute that has changed.
	 * @exception IllegalActionException
	 *                If the specified attribute is <i>fileName</i> and the
	 *                previously opened file cannot be closed.
	 */
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {
		if (attribute == fileName) {
			// Do not close the file if it is the same file.
			newFileName = ((StringToken) fileName.getToken()).stringValue();
		} else {
			super.attributeChanged(attribute);
		}
	}

	public void initialize() throws IllegalActionException {
		super.initialize();

		xdimension = ((IntToken) x.getToken()).intValue();
		ydimension = ((IntToken) y.getToken()).intValue();

		// Stores all Application Actors used to model the application
		application_actors = new Vector();
		// Stores the lifeline pair communication name
		// (SendingLifelineName_ReceivingLifeline_Name)
		communication_names = new Vector();
		lifelines_communication = new Hashtable();
		// Stores the task par communication name (e.g. Task A_TaskB)
		source_target = new Hashtable();

		// Getting the list of all Actor in the model
		// SelfFiringActors/DependentActors/Producers/UMLSeqActor
		try {
			selfFiringActors_ = getSelfFiringActors_();
			dependentActors_ = getDependentActors_();
			producers_ = getproducers_();
			UMLSeqActor_ = getUMLSeqActor_();
		} catch (NameDuplicationException e3) {
			e3.printStackTrace();
		}

		if (selfFiringActors_.size() < 1) {
			application_actors.add(self_actor.getActorName());
		} else {
			for (int i = 0; i < selfFiringActors_.size(); i++) {
				self_actor = (SelfFiringActor) selfFiringActors_.get(i);
				// System.out.println("Self Firing Actors " +
				// depen.getActorName());
				application_actors.add(self_actor.getActorName());
			}
		}

		for (int i = 0; i < dependentActors_.size(); i++) {
			DependentActor depen = (DependentActor) dependentActors_.get(i);
			// System.out.println("Depedence Actors " + depen.getActorName());
			application_actors.add(depen.getActorName());
		}

		for (int i = 0; i < producers_.size(); i++) {

			Producer p = (Producer) producers_.get(i);
			int x = p.getAddressX();
			int y = p.getAddressY();

			if (x > xdimension)
				xdimension = x;
			if (y > ydimension)
				ydimension = y;
		}

		// ************** CAFES OUTPUT FILE ***************
		// Generates an input file to the CAFES framework,
		// which describes the graph representation of the application
		try {
			// Set the initial Path
			String path = System.getProperties().getProperty("user.dir")
					+ "/lsi/noc/application/";

			// Write to temp file
			fileOut = new FileOutputStream(path + "application.cwg");
			dataOutput = new DataOutputStream(fileOut);
			dataOutput.writeBytes("#_NoC_Size (lines columns) \n");
			dataOutput.writeBytes(getX() + " " + getY() + "\n\n");

			dataOutput.writeBytes("#_CWG_Graphic (list of: core x y) \n");
			for (int s = 0; s < application_actors.size(); s++) {
				int x = 100 + (s * 40);
				int y = 630 - (s * 40);
				dataOutput.writeBytes((String) application_actors.elementAt(s)
						+ " " + x + " " + y + " \n");
			}
			dataOutput.writeBytes("\n #_CWG_Vertices (list of: vertices) \n");
			for (int s = 0; s < application_actors.size(); s++) {
				String actor_name = (String) application_actors.elementAt(s);
				dataOutput.writeBytes(actor_name + " ");
			}
			dataOutput
					.writeBytes("\n\n#_CWG_Edges (list of: sourceVertex - targetVertex numberOfPhitsTransmited) \n");
		} catch (IOException ex) {
		}

		// get the UMLSeqActors
		for (int i = 0; i < UMLSeqActor_.size(); i++) {
			UMLSeqActor uml_seqActors = (UMLSeqActor) UMLSeqActor_.get(i);

			// Vector with all UML Sequence Actors
			Vector SD_messages = uml_seqActors.getUMLSeqActor();

			// Capture the messages' names of each Sequence diagram
			for (int j = 0; j < SD_messages.size(); j++) {
				Message m = (Message) SD_messages.elementAt(j);

				int totalPacketSize = ((IntToken) m.getTotalPacketSize())
						.intValue();

				// Getting sending and receiving lifeline due to the message
				Lifeline sendingLifeline = m.getSendEvent().getLifeline();
				Lifeline receivingLifeline = m.getReceiveEvent().getLifeline();

				String comunication_name = sendingLifeline.getUMLName() + "_"
						+ receivingLifeline.getUMLName();
				if (communication_names.size() == 0) {
					communication_names.add(comunication_name);
					lifelines_communication.put(comunication_name,
							totalPacketSize);

					new_name = false;
				} else {
					for (int s = 0; s < communication_names.size(); s++) {
						String temp_name = (String) communication_names.get(s);
						if (!new_name) {
							if (comunication_name.compareTo(temp_name) == 0) {
								Integer numberFlits = (Integer) lifelines_communication
										.get(comunication_name);
								if (totalPacketSize > numberFlits) {
									lifelines_communication.put(
											comunication_name, new Integer(
													totalPacketSize));
									new_name = true;
								} else {
									// System.out.println("Equal Comunication Name  ********  "
									// + comunication_name + " ss " +
									// numberFlits);
									new_name = true;
								}
							}
						}
					}
					if (!new_name) {
						communication_names.add(comunication_name);
						lifelines_communication.put(comunication_name,
								totalPacketSize);
						// System.out.println("****Different Comunication Name Added***  "
						// + comunication_name + " value 1");
					}
				}
				for (int s = 0; s < communication_names.size(); s++) {
					StringTokenizer st = new StringTokenizer(
							(String) communication_names.get(s), "_");
					// fix it up (used to discard the sequence diagram name)
					st.nextToken();

					String sourceVertex = st.nextToken();

					// fix it up (used to discard the sequence diagram name)
					st.nextToken();

					String targetVertex = st.nextToken();

					Integer n_flits = (Integer) lifelines_communication
							.get((String) communication_names.get(s));

					String sourc_targt = sourceVertex + " - " + targetVertex;

					Boolean verify = source_target.containsKey(sourc_targt);
					if (!verify) {
						source_target.put(sourc_targt, n_flits);
					} else {
						Integer number_flits = (Integer) source_target
								.get(sourc_targt);
						if (n_flits > number_flits)
							source_target.remove(sourc_targt);
						source_target.put(sourc_targt, n_flits);
					}
				}
			}
			// System.out.println("lifelines_communication " +
			// lifelines_communication );
			communication_names.removeAllElements();
		}

		try {
			for (Enumeration e = source_target.keys(); e.hasMoreElements();) {
				String se_target = (String) e.nextElement();
				Integer number_flits = (Integer) source_target.get(se_target);
				dataOutput.writeBytes(se_target + " " + number_flits + " \n");
			}
			dataOutput.close();
		} catch (IOException e) {
		}

		String titulo = "CAFES - Communication Analysis For Embedded Systems - V 3.9.3";
		try {
			new WindowPrincipal(titulo);
		} catch (NameDuplicationException e) {
			e.printStackTrace();
		}
	}

	protected List getproducers_() throws IllegalActionException,
			NameDuplicationException {

		Nameable container = getContainer();

		return ((CompositeEntity) container).entityList(Producer.class);
	}

	protected List getSelfFiringActors_() throws IllegalActionException,
			NameDuplicationException {

		Nameable container = getContainer();

		return ((CompositeEntity) container).entityList(SelfFiringActor.class);

	}

	protected List getDependentActors_() throws IllegalActionException,
			NameDuplicationException {

		Nameable container = getContainer();

		return ((CompositeEntity) container).entityList(DependentActor.class);

	}

	protected List getUMLSeqActor_() throws IllegalActionException,
			NameDuplicationException {

		Nameable container = getContainer();

		return ((CompositeEntity) container).entityList(UMLSeqActor.class);
	}

	/**
	 * @return the number of hops in X
	 * @throws IllegalActionException
	 */
	public int getX() throws IllegalActionException {
		return xdimension;
	}

	/**
	 * @return the number of hops in Y
	 * @throws IllegalActionException
	 */
	public int getY() throws IllegalActionException {
		return ydimension;
	}

	protected String newFileName;
	protected int xdimension = 0;
	protected int ydimension = 0;
	protected int lastx = 0;
	protected int lasty = 0;
	protected List producers_, selfFiringActors_, dependentActors_,
			UMLSeqActor_;

	protected Hashtable appliActors_position, lifelines_communication,
			source_target;

	protected JFrame _frame;
	protected String _mapperName, producer_name;
	protected boolean MT_visible, new_name;
	protected Vector application_actors, communication_names;
	protected SelfFiringActor self_actor;
	protected Lifeline l;
	protected Producer p;

	private FileOutputStream fileOut;
	private DataOutputStream dataOutput;

}// end class
