package lsi.noc.application;

import java.util.ArrayList;

public class Task {

	public Task() {
		ComputationTime = 250;
		CommunicationVolume = 30;
	}
	
	public void StartComputation(){
	
		DynamicMapper.sendMessage();
	}
	

	int ComputationTime;
	int CommunicationVolume;
	ArrayList<Task> CommunicatesWith = new ArrayList<Task>();
}
