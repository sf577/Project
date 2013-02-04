package ptolemy.vergil.uml;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * This class is an implementation of the class Lifeline specified in the UML
 * Superstructure specification within the PtolemyII environment.
 * 
 * This class is the starting point used in this implementation, this means that
 * the whole underlying hierarchy that is given by the UML specification is
 * simplified here. For example, NamedElement that is the super class of
 * MessageEnd is not implemented here.
 * 
 * It contains a Location to make the graphical appearience persistant and it
 * contains the name of the instance (name of the corresponding actor in the
 * model)that is represented by this Lifeline in the Sequence Diagram.
 * 
 * !!! Lifelines do not maintain references of the MsgOccSpec that belong to !!!
 * them. Even if this is specified in the UML Spec, it is not implemented !!!
 * here to reduce model size. Maybe this will be changed when 'StringMode' !!!
 * of the UML-Parameters is switched off... but this is left for future !!!
 * releases ;)
 * 
 * @author Leandro Soares Indrusiak, Andreas Thuy, Modified by Sanna Maatta
 * 
 *         last review: 11.09.08
 */
public class Lifeline extends UMLParameter implements ChangeListener {

	public LifelineIcon lifelineIcon = null;

	// position of the Lifeline on the screen
	public Location location = new Location(this, "location");

	/**
	 * @param container
	 * @param name
	 *            - name of the Lifeline
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public Lifeline(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		this.lifelineIcon = new LifelineIcon(this, "LifelineIcon");

		// the type of a Lifeline parameter is String
		// information is only stored to recover the model that was created
		// in an earlier session => Director won't complain about unknown
		// names in the model
		this.setStringMode(true);
	}

	// see comments of two argument constructor above
	public Lifeline(NamedObj container, String name, double[] position)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		this.lifelineIcon = new LifelineIcon(this, "LifelineIcon");

		this.setStringMode(true);

		this.location.setLocation(position);
	}

	// at the moment used for renaming stuff... this will be changed
	public void changeExecuted(ChangeRequest change) {
		String description = change.getDescription();
		System.out.println("change Lifeline: " + description);
		if (description.contains("rename")) {
			if (description.contains("<entity name=\"" + getRepresents()
					+ "\">")) {
				int startPos = description.lastIndexOf("name=\"");
				int endPos = description.lastIndexOf("\"");
				String newName = description.substring(startPos + 6, endPos);
				setRepresents(newName);
			}
		}
	}

	// we are not interested if something failes ;), has to be implemented
	public void changeFailed(ChangeRequest change, Exception exception) {

		System.out.println(exception);

	}

	/**
	 * @return - the position of the Lifeline on the screen
	 */
	public Location getLocation() {
		return this.location;
	}

	/**
	 * New public method added by Sanna Maatta
	 * 
	 * @return - the position of the Lifeline on the screen as a double table (x
	 *         and y coordinates can be extracted easily)
	 */

	public double[] getLifelineLocation() {
		return location.getLocation();
	}

	/**
	 * @return - Ptolemy name of the Lifeline
	 */
	public String getRepresents() {
		return getName();
	}

	/**
	 * @param represents
	 *            - set the Ptolemy name of the Lifeline
	 */
	public void setRepresents(String represents) {
		try {
			this.setName(represents);
		} catch (NameDuplicationException nDE) {
			nDE.printStackTrace();
		} catch (IllegalActionException iAE) {
			iAE.printStackTrace();
		}
	}
}
