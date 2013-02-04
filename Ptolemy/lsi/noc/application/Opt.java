package lsi.noc.application;

import ptolemy.vergil.uml.InteractionOperator;

/**
 * Interaction operator for optional execution of messages inside opt combined
 * fragment
 * 
 * @author Sanna Maatta
 * 
 */

public class Opt extends InteractionOperator {

	public Opt() {
		super();
		operator = "opt";
	}
}
