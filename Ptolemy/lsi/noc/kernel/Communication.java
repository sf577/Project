package lsi.noc.kernel;

public abstract class Communication {

	public abstract Task getSender();

	public abstract Task getReceiver();

	public abstract Flow getFlow();

	public abstract long getVolume();

	public abstract void setVolume(long vol);

}
