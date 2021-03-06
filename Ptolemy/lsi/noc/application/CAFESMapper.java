/**
 * 
 */
package lsi.noc.application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;

import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.Attribute;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.vergil.uml.Lifeline;
import lsi.noc.application.Producer;

//lsi.noc.application.cafes.ModelosAplicacao.CWM.CWM_MappingCost.CWM_MappingCost

//(WindowPrincipal WP, int IniX, int IniY, int numPixelColuna, int numPixelLinha)
import lsi.noc.mapperscope.TableMapper;
import lsi.noc.stats.BasicCommunicationLatencyAnalysis;

/**
 * @author Leandro Soares Indrusiak
 * @version 1.0 (York, 14/1/2009)
 * @author Luciano Ost
 * @version 2.0 (Porto Alegre, 18/05/2009)
 * 
 *          This Mapper maps Lifelines onto producers/consumers according to the
 *          mapping file genereted by CAFES.
 */
@SuppressWarnings("serial")
public class CAFESMapper extends LifelineMapper {

	public Parameter mapper_name, mapperTable_Visible;
	public FilePortParameter fileName;

	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {
		if (attribute == fileName) {
			// Do not close the file if it is the same file.
			newFileName = ((StringToken) fileName.getToken()).stringValue();
		} else {
			super.attributeChanged(attribute);
		}
	}

	public CAFESMapper(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		fileName = new FilePortParameter(this, "fileName");
		fileName.setExpression("mapping.txt");

		mapperTable_Visible = new Parameter(this, "mapperTable_Visible");
		mapperTable_Visible.setTypeEquals(BaseType.BOOLEAN);
		mapperTable_Visible.setExpression("true");

		this.addCommunicationLatencyAnalysis(new BasicCommunicationLatencyAnalysis());
	}

	protected void performMapping_() throws IllegalActionException,
			NameDuplicationException {

		System.out.println("Starting Mapping");

		MT_visible = ((BooleanToken) mapperTable_Visible.getToken())
				.booleanValue();

		producers_position = new Hashtable();
		lName_position = new Vector();

		// Get the lifelines
		lifelinesByUMLName = new Hashtable();
		Enumeration lifelines = lifelinesDirector_.keys();
		while (lifelines.hasMoreElements()) {

			l = (Lifeline) lifelines.nextElement();
			lifelinesByUMLName.put(l.getUMLName(), l);

			lName_position.add(l.getUMLName());

		}

		// Goes through the list of producers and checks their positions on the
		// mesh to find out the NoC dimensions
		// Getting all producers where lifelines can be mapped and add them to
		// the HashTable called producers_position
		try {
			producers_ = getproducers_();
		} catch (NameDuplicationException e2) {
			e2.printStackTrace();
		}

		for (int i = 0; i < producers_.size(); i++) {

			p = (Producer) producers_.get(i);
			int x = p.getAddressX();
			int y = p.getAddressY();

			// Stores Producers due to the XY position
			producer_position = String.valueOf(x) + String.valueOf(y);
			producers_position.put(producer_position, p);

			if (x > xdimension)
				xdimension = x;
			if (y > ydimension)
				ydimension = y;
		}

		try {
			// Pre-defined path
			String path = System.getProperties().getProperty("user.dir")
					+ "/lsi/noc/";

			// Creates a BufferedReader to read the mapping file generated by
			// CAFES.
			buff = new BufferedReader(new InputStreamReader(
					new FileInputStream(path + newFileName)));

			do {
				// Read one line of the input mapping file
				mapper_file = buff.readLine();
				if (mapper_file != null) {
					StringTokenizer st = new StringTokenizer(mapper_file, " ");

					for (int i = 0; i < getX(); i++) {
						task_name = st.nextToken();
						p = (Producer) producers_position.get(String.valueOf(i)
								+ String.valueOf((getY() - 1) - num_lines));

						// Checks
						if (!task_name.equals("-")) {
							for (int j = 0; j < lifelinesByUMLName.size(); j++) {

								String lifeline_name = (String) lName_position
										.get(j);
								// System.out.println("lName_position " +
								// lName_position.size());

								StringTokenizer ln = new StringTokenizer(
										lifeline_name, "_");

								// fix it up
								ln.nextToken();

								String life_name = ln.nextToken();

								if (life_name.compareTo(task_name) == 0) {
									l = (Lifeline) lifelinesByUMLName
											.get(lifeline_name);
									lifelinesProducer_.put(l, p);
								}
								// else
								// System.out.println("Caboom  " + life_name +
								// " TAsk name " + task_name);
							}// end for
						} else
							System.out.println("No task at Producer "
									+ p.getAddressX() + p.getAddressY());
					}
					num_lines++;
				} else if (mapper_file == null)
					break;
			} while (true);
			buff.close();
		} catch (IOException e) {
		}

		// ****************************************************
		// If TRUE a Table with mapping information is created
		// ****************************************************
		if (MT_visible) {
			// Create and set up the window.
			_frame = new JFrame("TableMapper");
			// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// Create and set up the content pane.
			TableMapper newContentPane = new TableMapper(lifelinesProducer_);
			newContentPane.setOpaque(true); // content panes must be opaque
			_frame.setContentPane(newContentPane);

			// Display the window.
			_frame.pack();
			_frame.setVisible(true);
		}

	}

	protected List getproducers_() throws IllegalActionException,
			NameDuplicationException {

		Nameable container = getContainer();

		return ((CompositeEntity) container).entityList(Producer.class);
	}

	public int getX() {
		return (xdimension + 1);
	}

	public int getY() {
		return (ydimension + 1);
	}

	protected StringParameter orderOfLifelines;
	protected int xdimension = 0;
	protected int ydimension = 0;
	protected int lastx = 0;
	protected int lasty = 0;
	protected int num_lines = 0;
	protected int x = 0;
	protected int y = 0;
	protected List producers_;

	protected Hashtable producers_position, lifelines_comunication,
			lifelinesByUMLName;

	protected JFrame _frame;
	protected String _mapperName, mapper_file, newFileName, producer_position,
			task_name;
	protected boolean MT_visible, new_name;
	protected Vector application_actors, comunication_names, lName_position;
	protected Lifeline l;
	protected Producer p;

	protected BufferedReader buff;

}
