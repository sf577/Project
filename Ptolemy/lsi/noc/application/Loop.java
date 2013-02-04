package lsi.noc.application;

import ptolemy.vergil.uml.InteractionOperator;

/**
 * Interaction operator for looped execution of messages inside loop combined
 * fragment
 * 
 * @author Sanna Maatta
 * 
 */

public class Loop extends InteractionOperator {

	public Loop() {
		super();
		operator = "loop";
	}
}
