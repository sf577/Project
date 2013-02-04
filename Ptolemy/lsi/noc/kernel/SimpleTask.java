package lsi.noc.kernel;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 14/08/2010) 
 */

public class SimpleTask extends Task {

	protected String name;
	protected ProcessingCore core;
	protected double cost;

	public SimpleTask() {

		super();
	}

	public SimpleTask(String s) {

		name = s;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost
	 *            the cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void setCore(ProcessingCore core) {
		if (this.core != null) {
			core.unmapTask(this);
		}
		this.core = core;
		core.mapTask(this);
	}

	public ProcessingCore getCore() {
		return core;
	}

	public void mapTask(ProcessingCore p) {
		this.setCore(p);

	}

	public void unmapTask() {
		if (this.core != null) {
			core.unmapTask(this);
		}
		this.core = null;
	}
}
