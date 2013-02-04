package lsi.noc.kernel.sdf;

import lsi.noc.kernel.SimpleCommunication;

public class SDFCommunication extends SimpleCommunication {

	private int currentTokens;
	private int maxTokens;

	public SDFCommunication(SDFTask s, SDFTask r) {
		super(s, r);
		currentTokens = 0;
		maxTokens = 0;

	}

	public void writeTokens(int i) {

		currentTokens = currentTokens + i;
		if (currentTokens > maxTokens) {
			maxTokens = currentTokens;
		}

	}

	public boolean readTokens(int i) {

		if (currentTokens - i >= 0) {
			currentTokens = currentTokens - i;
			return true;
		} else
			return false;

	}

	public int getCurrentTokens() {
		return currentTokens;
	}

	public int getMaxTokens() {
		return maxTokens;
	}

}
