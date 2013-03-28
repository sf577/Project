package lsi.noc.application;

import java.util.List;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class DynamicMapperMAC extends DynamicMapperMMC {

	public DynamicMapperMAC(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}
	
	protected int getCongestion(List<Integer> HEL, List<Integer> VEL){
		int sum = 0;
		for(int i = 0; i < HEL.size(); i++){
			sum = sum + HEL.get(i);
		}
		for(int i = 0; i < VEL.size(); i++){
			sum = sum + VEL.get(i);
		}
		int average = sum/(VEL.size() + HEL.size());
		return average;
	}
}
