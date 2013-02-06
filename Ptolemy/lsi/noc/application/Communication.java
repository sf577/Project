package lsi.noc.application;

import ptolemy.data.expr.Parameter;

/**
 * 
 * @author Steven Fisher
 *
 *
 * This Class represents the communication between two tasks.
 * 
 */
public class Communication {

	/**
	 * Sets the information about the communication
	 * 
	 * @param Source Where the communication originated  
	 * @param Destination where the communication is headed
	 * @param Packet, Size of the packet
	 * @param Subpacket
	 * @param computationtime, Computational delay before Communication occurs
	 */
	
	public void setParameters(Task Source, Task Destination, int Packet, int Subpacket, double computationtime){
		SourceTask = Source;
		DestTask = Destination;
		TotalPacketSize = Packet;
		SubPacketSize = Subpacket;
		PreComptime = computationtime;
	}
	
	public Task getSource(){
		return SourceTask;
	}
	
	public Task getDest(){
		return DestTask;
	}
	
	Task SourceTask;
	Task DestTask;
	int TotalPacketSize;
	int SubPacketSize;
	Parameter PreComptime;
}
