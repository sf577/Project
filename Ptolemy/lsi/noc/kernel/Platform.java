package lsi.noc.kernel;

import java.util.ArrayList;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 14/08/2010) 
 */
public class Platform {

	private ArrayList<ProcessingCore> cores;
	private Interconnect interconnect;

	public Platform(Interconnect i) {

		setCores(i.getCores());
		setInterconnect(i);

	}

	protected void setCores(ArrayList<ProcessingCore> cores) {
		this.cores = cores;
	}

	public ArrayList<ProcessingCore> getCores() {
		return cores;
	}

	protected void setInterconnect(Interconnect interconnect) {
		this.interconnect = interconnect;
	}

	public Interconnect getInterconnect() {
		return interconnect;
	}

	public ProcessingCore getCore(int i) {

		return cores.get(i);

	}

}
