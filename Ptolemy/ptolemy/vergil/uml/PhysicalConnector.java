package ptolemy.vergil.uml;

import ptolemy.kernel.Port;

// derived from what? or whom? => see also InternalStructures...
public class PhysicalConnector {

	private Port inputPort = null;
	private Port outputPort = null;

	public Port getInputPort() {
		return inputPort;
	}

	public void setInputPort(Port inputPort) {
		this.inputPort = inputPort;
	}

	public Port getOutputPort() {
		return outputPort;
	}

	public void setOutputPort(Port outputPort) {
		this.outputPort = outputPort;
	}

	public PhysicalConnector() {
		super();
		// TODO Auto-generated constructor stub
	}

}
