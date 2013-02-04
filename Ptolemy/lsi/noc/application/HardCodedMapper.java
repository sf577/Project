/**
 * Implements a hard coded mapping scheme. Each lifeline is mapped to one of the producers.
 * There can be more than one lifeline mapped to same producer.
 */
package lsi.noc.application;

import java.util.List;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.uml.Lifeline;

/**
 * @author Sanna M‰‰tt‰
 * 
 */

public class HardCodedMapper extends LifelineMapper {

	public HardCodedMapper(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

	}

	/**
	 * 
	 * This can map more than one lifeline to one producer
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

		// Mapping for
		// System.out.println("DIRS: " + directors_);
		for (int i = 0; i < directors_.size(); i++) {

			List lifelines = (List) directorsLifelines_.get(directors_.get(i));

			for (int j = 0; j < lifelines.size(); j++) {

				Lifeline ll = (Lifeline) lifelines.get(j);

				// VEHICLE
				if (i == 0) { // Ultrasonic sensing

					if (j == 0) {
						lifelinesProducer_.put(ll, producers.get(2));
					} else if (j == 1) {
						lifelinesProducer_.put(ll, producers.get(5));
					} else {
						System.out.println("BIG PROBLEM: ultrasonic");
					}

				} else if (i == 1) { // GPS sensing

					if (j == 0) {
						lifelinesProducer_.put(ll, producers.get(6));
					} else if (j == 1) {
						lifelinesProducer_.put(ll, producers.get(4));
					} else {
						System.out.println("BIG PROBLEM: gps");
					}

				} else if (i == 2) { // Speed sensing

					if (j == 0) {
						lifelinesProducer_.put(ll, producers.get(11));
					} else if (j == 1) {
						lifelinesProducer_.put(ll, producers.get(10));
					} else {
						System.out.println("BIG PROBLEM: speed sensing");
					}

				} else if (i == 3) { // Navigation controlling

					if (j == 0) {
						lifelinesProducer_.put(ll, producers.get(10));
					} else if (j == 1) {
						lifelinesProducer_.put(ll, producers.get(6));
					} else if (j == 2) {
						lifelinesProducer_.put(ll, producers.get(5));
					} else if (j == 3) {
						lifelinesProducer_.put(ll, producers.get(14));
					} else if (j == 4) {
						lifelinesProducer_.put(ll, producers.get(15));
					} else {

						System.out.println("BIG PROBLEM: nav");
					}

				} else if (i == 4) { // Speed controlling

					if (j == 0) {
						lifelinesProducer_.put(ll, producers.get(7));
					} else if (j == 1) {
						lifelinesProducer_.put(ll, producers.get(11));
					} else if (j == 2) {
						lifelinesProducer_.put(ll, producers.get(14));
					} else {

						System.out.println("BIG PROBLEM: sc");
					}

				} else if (i == 5) { // Vibration controlling

					if (j == 0) {
						lifelinesProducer_.put(ll, producers.get(13));
					} else if (j == 1) {
						lifelinesProducer_.put(ll, producers.get(7));
					} else {

						System.out.println("BIG PROBLEM: vc");
					}

				} else if (i == 6) { // Pressure controlling

					if (j == 0) {
						lifelinesProducer_.put(ll, producers.get(7));
					} else if (j == 1) {
						lifelinesProducer_.put(ll, producers.get(3));
					} else if (j == 2) {
						lifelinesProducer_.put(ll, producers.get(12));
					} else {

						System.out.println("BIG PROBLEM: pc");
					}

				} else if (i == 7) { // Obstacle recognition 1

					if (j == 0) {
						lifelinesProducer_.put(ll, producers.get(1));
					} else if (j == 1) {
						lifelinesProducer_.put(ll, producers.get(0));
					} else if (j == 2) {
						lifelinesProducer_.put(ll, producers.get(4));
					} else if (j == 3) {
						lifelinesProducer_.put(ll, producers.get(5));
					} else {

						System.out.println("BIG PROBLEM: or1");
					}

				} else if (i == 8) { // Obstacle recognition 2

					if (j == 0) {
						lifelinesProducer_.put(ll, producers.get(9));
					} else if (j == 1) {
						lifelinesProducer_.put(ll, producers.get(8));
					} else if (j == 2) {
						lifelinesProducer_.put(ll, producers.get(4));
					} else if (j == 3) {
						lifelinesProducer_.put(ll, producers.get(5));
					} else {

						System.out.println("BIG PROBLEM: or2");
					}

				} else {

					System.out
							.println("######################### this should not happen");
				}

			}

		}

	}

}
