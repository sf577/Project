package lsi.noc.application;

import java.util.ArrayList;
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
			switch (lastassignedcluster){
			case 0: sourcex = 0;
					sourcey = ydimension;
					lastassignedcluster = 1;
					break;
			case 1: sourcex = xdimension;
					sourcey = ydimension;
					lastassignedcluster = 2;
					break;
			case 2: sourcex = xdimension;
					sourcey = 0;
					lastassignedcluster = 3;
					break;
			case 3: sourcex = 0;
					sourcey = 0;
					lastassignedcluster = 0;
					break;
			}
		}
		
		boolean mapped = false;
		//get the list of possible producers
		producers_ = getproducers_();
		int amountOfProducers = producers_.size();
		while (!mapped){
			for (int hopdistance = 0; hopdistance <= 6 && !mapped; hopdistance ++)	
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
					int minx = 3;
					int miny = 3;
					int mapping = 0;
					for (int i = 0; i < possiblemappings.size(); i++) {
						Producer p = possiblemappings.get(i);
						int px = p.getAddressX();
						int py = p.getAddressY();
						if ((px + py) < (minx + miny)){
							minx = px;
							miny = py;
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