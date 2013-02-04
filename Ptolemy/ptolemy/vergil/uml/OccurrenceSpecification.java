package ptolemy.vergil.uml;

import java.util.Iterator;

/**
 * This interface describes the functionality offered by an
 * OccurrenceSpecification specified in the UML Superstructure specification.
 * Whereas it is specified there as a class, it is implemented as an interface
 * within the PtolemyII environment because multiple inheritance is not
 * available in Java and MessageOccurrenceSpecification is derived from both,
 * MessageEnd and OccurrenceSpecification. This interface is the way out of this
 * problem.
 * 
 * @author Andreas Thuy
 */
public interface OccurrenceSpecification {

	public Iterator getAfter();

	public Iterator getBefore();

	public Event getEvent();

	public void setPredecessor(GeneralOrdering predecessor);

	public void setSuccessor(GeneralOrdering successor);

}
