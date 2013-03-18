package lsi.noc.application;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class PNNDynamicMapper extends DynamicMapperNN {

	public PNNDynamicMapper(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}
	
	protected void performMapping(Task newTask, Task Source)
			throws IllegalActionException, NameDuplicationException {
		List<Producer> possiblemappings = new ArrayList<Producer>();
		int sourcex = 0;
		int sourcey = 0;
		
		if (!(Source == null)){
			Producer SourceP = TaskProducer_.get(Source);
			sourcex = SourceP.getAddressX();
			sourcey = SourceP.getAddressY();
			
		} else {
			applicationClusters.remove(newTask.applicationid);
			switch (lastassignedcluster){
			case 0: sourcex = 0;
					sourcey = ydimension;
					lastassignedcluster = 1;
					applicationClusters.put(newTask.applicationid, 1);
					break;
			case 1: sourcex = xdimension;
					sourcey = ydimension;
					lastassignedcluster = 2;
					applicationClusters.put(newTask.applicationid, 2);
					break;
			case 2: sourcex = xdimension;
					sourcey = 0;
					lastassignedcluster = 3;
					applicationClusters.put(newTask.applicationid, 3);
					break;
			case 3: sourcex = 0;
					sourcey = 0;
					lastassignedcluster = 0;
					applicationClusters.put(newTask.applicationid, 0);
					break;
			}
		}
		
		boolean mapped = false;
		//get the list of possible producers
		producers_ = getproducers_();
		int amountOfProducers = producers_.size();
		while (!mapped){
			for (int hopdistance = 0; hopdistance <= 6 && !mapped; hopdistance ++){	
				for (int i = 0; i < amountOfProducers; i++) {
					Producer p = (Producer) producers_.get(i);
					int px = p.getAddressX();
					int py = p.getAddressY();
					if ((Math.abs(px-sourcex) + Math.abs(py - sourcey)) == hopdistance){
						//check if producer is mapped
						if(!(TaskProducer_.containsValue(p))){
							//add to list of possible neighbours
							possiblemappings.add(p);
						}
					}
				}
				if (!(possiblemappings.isEmpty())){
					int minhopdistance = xdimension + ydimension;
					int mapping = 0;
					for (int i = 0; i < possiblemappings.size(); i++) {
						if(hopdistanceFromClusterCorner(possiblemappings.get(i), newTask.applicationid) < minhopdistance){
							mapping = i;
						}
					}
					Producer p = possiblemappings.get(mapping);
					TaskProducer_.put(newTask, p);
					mapped = true;
				}
			}
	}
}
	
	protected int hopdistanceFromClusterCorner(Producer PE, int Appid) throws IllegalActionException{
		int cluster = applicationClusters.get(Appid);
		int x = 0,y = 0;
		switch(cluster){
			case 0: x = 0;
					y = 0;
					break;
			case 1: x = 0;
					y = ydimension;
					break;
			case 2: x = xdimension;
					y = ydimension;
					break;
			case 3: x = xdimension;
					y = 0;
					break;
		}
		int px = PE.getAddressX();
		int py = PE.getAddressY();
		return (Math.abs(px-x) + Math.abs(py - y));
	}
	
	protected Hashtable<Integer, Integer> applicationClusters = new Hashtable<Integer,Integer>();
}	