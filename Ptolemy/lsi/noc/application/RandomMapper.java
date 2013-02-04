/**
 * Implements a random mapping scheme. Each lifeline is randomly mapped to one of the producers.
 * There can be more than one lifeline mapped to same producer.
 */
package lsi.noc.application;

import java.util.List;
import java.util.Random;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.uml.Lifeline;

/**
 * @author Sanna M‰‰tt‰
 * 
 */

public class RandomMapper extends LifelineMapper {

	public RandomMapper(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		// Random number generator
		generator_ = new Random();

	}

	/**
	 * This is a simple random mapping, where each lifeline is mapped to a
	 * random producer This can map more than one lifeline to one producer
	 * 
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */

	protected void performMapping_() throws IllegalActionException,
			NameDuplicationException {

		// Getting all producers where lifelines can be mapped
		List producers = getproducers_();

		// producers.remove(3);
		// producers.remove(7);
		// producers.remove(11);
		int amountOfProducers = producers.size();

		// System.out.println("Producers: " + producers);

		// Going through all directors and getting their lifelines in the first
		// for-loop. Going through all lifelines of a director in the second
		// for-loop
		// Assigns randomly producer to each lifeline
		for (int i = 0; i < directors_.size(); i++) {

			List lifelines = (List) directorsLifelines_.get(directors_.get(i));

			for (int j = 0; j < lifelines.size(); j++) {

				Lifeline ll = (Lifeline) lifelines.get(j);
				// Getting a random number between zero and amount of Producers.
				int randomProducer = generator_.nextInt(amountOfProducers);
				// int randomProducer = generator_.nextInt(16);

				// Assigning a producer to a lifeline.
				lifelinesProducer_.put(ll, producers.get(randomProducer));
				// System.out.println("LIFELINE: " + ll + " PRODUCER: " +
				// randomProducer);

			}
		}

	}

	private Random generator_; // Random integers for mapping

}
