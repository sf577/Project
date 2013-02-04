package lsi.noc.kernel;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 25/09/2009) 
 */

public abstract class InterconnectFactory {

	public abstract Interconnect createInterconnect(int procs);

}
