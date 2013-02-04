package ptolemy.domains.uml.lib;

import ptolemy.actor.QueueReceiver;
import ptolemy.data.Token;

public class DebuggableQueueReceiver extends QueueReceiver {

	int max = 10;

	public void put(Token t) {

		super.put(t);
		if (this.size() > max)
			max = this.size();
		// System.out.println("port: "+this.getContainer()+" max buffer: "+max);
	}

}
