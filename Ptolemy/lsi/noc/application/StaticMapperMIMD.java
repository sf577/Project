package lsi.noc.application;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.uml.Lifeline;

/**
 * @author Leandro Soares Indrusiak
 * @version 1.0 (York, 1/4/2009)
 * 
 * 
 *          This Mapper maps Lifelines onto producers/consumers in a MIMD
 *          fashion (multiple input, multiple destination) according to the Open
 *          NoC Benchmarks classification.
 * 
 */
@SuppressWarnings("serial")
public class StaticMapperMIMD extends StaticMapper {

	public StaticMapperMIMD(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

	}

	/**
	 * This method overrides the SISD mapping defined by the superclass and
	 * implements a static mapping that supports multiple inputs and multiple
	 * destinations.
	 * 
	 * The "lifelines" parameter follows the same idea implemented at the
	 * superclass, but multiple appearances of the same lifeline in different
	 * sequence diagrams is now supported. All of the appearances will be mapped
	 * onto the same producer/consumer pair , so their UML name should appear
	 * only once on the "lifelines" parameter.
	 * 
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */

	protected void performMapping_() throws IllegalActionException,
			NameDuplicationException {

		// Getting all producers where lifelines can be mapped
		producers_ = getproducers_();
		int amountOfProducers = producers_.size();

		// Goes through the list of producers and checks their positions on the
		// mesh to find out the NoC dimensions

		for (int i = 0; i < amountOfProducers; i++) {

			Producer p = (Producer) producers_.get(i);
			int x = p.getAddressX();
			int y = p.getAddressY();

			if (x > xdimension)
				xdimension = x;
			if (y > ydimension)
				ydimension = y;

		}

		// Creates a hashtable indexing all lifelines by their UML names,
		// which is the name entered by the model designer
		//

		Hashtable lifelinesByUMLName = new Hashtable();
		Enumeration lifelines = lifelinesDirector_.keys();

		while (lifelines.hasMoreElements()) {

			Lifeline l = (Lifeline) lifelines.nextElement();

			String name = l.getUMLName();
			if (!(lifelinesByUMLName.get(name) instanceof Vector))
				lifelinesByUMLName.put(name, new Vector());
			((Vector) lifelinesByUMLName.get(name)).addElement(l);
		}

		// Reads the contents of the "lifelines" parameter and parses
		// the substrings separated by a comma

		String s = orderOfLifelines.stringValue();
		StringTokenizer st = new StringTokenizer(s, ",");

		// Fetches each lifeline of the list and maps it sequentially to a
		// producer
		while (st.hasMoreTokens()) {

			Vector b = (Vector) lifelinesByUMLName.get(st.nextToken());
			Producer p = nextProducer(); // must be called even if the lifeline
											// doesn't exist, to enable a
											// "no map"
			if (b != null) {
				for (int i = 0; i < b.size(); i++) {

					Lifeline bl = (Lifeline) b.elementAt(i);
					lifelinesProducer_.put(bl, p);

				}
			}

		}

	}

}
