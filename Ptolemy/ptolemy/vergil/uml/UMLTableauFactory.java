package ptolemy.vergil.uml;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class UMLTableauFactory extends TableauFactory {

	/**
	 * Constructor.
	 * 
	 * @param container
	 *            - container of this Attribute
	 * @param name
	 *            - name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public UMLTableauFactory(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ptolemy.actor.gui.TableauFactory#createTableau(ptolemy.actor.gui.Effigy)
	 * 
	 * effigy is the container of the created UMLTableau
	 */
	public Tableau createTableau(Effigy effigy) throws Exception {
		UMLTableau tableau = new UMLTableau((PtolemyEffigy) effigy,
				"umlTableau");

		// TODO does this work with every Effigy? or must there be some
		// kind of a restriction? maybe null must be returned
		// => see TableauFactory.createTableau...
		return tableau;
	}
}
