package lsi.noc.kernel;

public class WeightedCommunication extends SimpleCommunication {

	protected double weight;

	public WeightedCommunication(Task s, Task r) {
		super(s, r);

	}

	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

}
