/**
 * 
 */
package lsi.noc.application;

import java.util.List;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
//import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.vergil.uml.Lifeline;
import lsi.noc.application.Producer;
import lsi.noc.stats.BasicCommunicationLatencyAnalysis;

/**
 * @author Leandro Soares Indrusiak
 * @version 1.0 (York, 14/1/2009)
 * 
 * 
 *          This Mapper maps Lifelines onto producers/consumers in a SISD
 *          fashion (single input, single destination) according to the Open NoC
 *          Benchmarks classification.
 * 
 */
@SuppressWarnings("serial")
public class StaticMapper extends LifelineMapper {

	public StaticMapper(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		orderOfLifelines = new StringParameter(this, "lifelines");
		// orderOfLifelines.setTypeEquals(BaseType.STRING);

		this.addCommunicationLatencyAnalysis(new BasicCommunicationLatencyAnalysis());

	}

	/**
	 * This method overrides the simple random mapping defined by the superclass
	 * and implements a static mapping based on a the "lifelines" parameter.
	 * 
	 * The "lifelines" parameter (essentially a String) must contain a
	 * comma-separated list of names of lifelines available in the same
	 * container as the mapper. Lifeline name duplication should be prevented,
	 * so that the mapping can be deterministic (mapping is done based on the
	 * UML name of the lifeline, which is not protected by Ptolemy's
	 * NameDuplicationException, so the prevention of duplications is
	 * intentionally left to the designer of the model). The first lifeline on
	 * the list will be mapped to the producer attached to router 00, the second
	 * to 10, the third to 20 and so on, until the last router of that row is
	 * reached, then the next rows will be assigned lifelines: 01, 11, 21, etc.
	 * 
	 * 
	 * If the list contains more elements than the platform has routers, the
	 * remaining lifelines will be mapped sequentially to routers 00, 10, 20,
	 * etc. just as in the first round.
	 * 
	 * Any lifeline name which is not recognizable will be interpreted as a
	 * "no map" and the respective producer will be skipped.
	 * 
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

			/*
			 * System.out.println("checking producer"); System.out.println(x);
			 * System.out.println(y);
			 */

			if (x > xdimension)
				xdimension = x;
			if (y > ydimension)
				ydimension = y;

		}

		/*
		 * System.out.println("dimensions"); System.out.println(xdimension);
		 * System.out.println(ydimension);
		 */

		// Creates a hashtable indexing all lifelines by their UML names,
		// which is the name entered by the model designer

		Hashtable lifelinesByUMLName = new Hashtable();
		Enumeration lifelines = lifelinesDirector_.keys();

		while (lifelines.hasMoreElements()) {

			Lifeline l = (Lifeline) lifelines.nextElement();
			lifelinesByUMLName.put(l.getUMLName(), l);
			/*
			 * System.out.println("storing lifeline "+l.getUMLName());
			 */
		}

		// Reads the contents of the "lifelines" parameter and parses
		// the substrings separated by a comma

		String s = orderOfLifelines.stringValue();
		StringTokenizer st = new StringTokenizer(s, ",");

		// Fetches each lifeline of the list and maps it sequentially to a
		// producer
		while (st.hasMoreTokens()) {

			Object b = lifelinesByUMLName.get(st.nextToken());
			Producer p = nextProducer(); // must be called even if the lifeline
											// doesn't exist, to enable a
											// "no map"
			if (b instanceof Lifeline) {
				lifelinesProducer_.put(b, p);

				/*
				 * System.out.println("mapping");
				 * System.out.println(((Lifeline)b).getUMLName());
				 * System.out.println(p.getAddressX());
				 * System.out.println(p.getAddressY());
				 */

			}

		}

	}

	protected Producer nextProducer() throws IllegalActionException {

		// Goes through the list of producers and returns them sequentially,
		// one at each method call

		for (int i = 0; i < producers_.size(); i++) {

			Producer p = (Producer) producers_.get(i);
			int x = p.getAddressX();
			int y = p.getAddressY();

			/*
			 * System.out.println("checking producer"); System.out.println(x);
			 * System.out.println(y);
			 */

			if (x == lastx && y == lasty) {
				lastx++;
				if (lastx > xdimension) {
					lastx = 0;
					lasty++;
				}
				if (lasty > ydimension) {
					lastx = 0;
					lasty = 0;
				}
				// System.out.println("selected");
				return p;
			}

		}

		return null; // should only happen if platform routers are wrongly
						// numbered

	}

	protected List getproducers_() throws IllegalActionException,
			NameDuplicationException {

		Nameable container = getContainer();

		return ((CompositeEntity) container).entityList(Producer.class);

	}

	protected StringParameter orderOfLifelines;
	protected int xdimension = 0;
	protected int ydimension = 0;
	protected int lastx = 0;
	protected int lasty = 0;
	protected List producers_;
	protected List producers_;

}
