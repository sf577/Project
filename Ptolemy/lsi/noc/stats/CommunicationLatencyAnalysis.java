/**
 * 
 */
package lsi.noc.stats;

/**
 * @author Leandro Soares Indrusiak
 * @version 1.0 (York, 17/08/2009)
 * 
 * 
 *          This is an abstract class for all classes doing analysis of
 *          communication latency results. It specifies the methods used by the
 *          mapper to notify the latency of each message once it is completely
 *          received by the respective consumer.
 * 
 */
public abstract class CommunicationLatencyAnalysis {

	/*
	 * @param id The message id, which is unique for every instance of every
	 * message (unique within a mapper).
	 * 
	 * @param name The message name.
	 * 
	 * @param latency The communication latency.
	 */
	public abstract void notifyReceipt(int id, String name, double latency);

	/*
	 * @param name The message name.
	 * 
	 * @param latency The communication latency.
	 */
	public abstract void notifyReceipt(String name, double latency);

	/*
	 * @param name The message name.
	 * 
	 * @param receiptTime The time when the message was completely received by
	 * the consumer.
	 * 
	 * @param latency The communication latency.
	 */
	public abstract void notifyReceipt(String name, double receiptTime,
			double latency);

	public abstract void reset();

	public abstract String[] report();

}
