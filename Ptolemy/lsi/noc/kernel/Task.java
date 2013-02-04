package lsi.noc.kernel;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 14/08/2010) 
 */

public abstract class Task {

	public abstract String getName();

	public abstract double getCost();

	public abstract ProcessingCore getCore();

	public abstract void setCore(ProcessingCore core);

}
