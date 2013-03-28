/**
 * 
 */
package lsi.noc.application;

import java.util.ArrayList;
import java.util.List;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @author Steven
 *
 */
@SuppressWarnings("serial")
public class DynamicMapperPL extends DynamicMapperMAC {

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public DynamicMapperPL(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		// TODO Auto-generated constructor stub
	}
	
	protected int getCongestion(List<List<Integer>> Indexes, List<Integer> HEL, List<Integer> VEL){
	int sum = 0;
	List<Integer> Hindexes = Indexes.get(0);
	List<Integer> Vindexes = Indexes.get(1);
		for(int i = 0; i < Hindexes.size(); i++){
			sum = sum + HEL.get(Hindexes.get(i));
		}
		for(int i = 0; i < Vindexes.size(); i++){
			sum = sum + VEL.get(Vindexes.get(i));
		}
	return sum;
	}
	
	@Override
	protected int possibleMappingCost(Communication com, Producer perspectivemapping) throws IllegalActionException{
		List<List<Integer>> indexes = calculateXYpath(TaskProducer_.get(com.SourceTask), perspectivemapping);
		List<Integer> Hindexes = indexes.get(0);
		List<Integer> Vindexes = indexes.get(1);
		List<Integer> newHELs = new ArrayList<Integer>(HorizontalEdgeLoads);
		List<Integer> newVELs = new ArrayList<Integer>(VerticalEdgeLoads);
		for (int i = 0; i < Hindexes.size(); i++) {
			int edge = Hindexes.get(i);
			int edgeweight = newHELs.get(edge) + com.TotalPacketSize;
			newHELs.set(edge, edgeweight);
		}
		for (int i = 0; i < Vindexes.size(); i++) {
			int edge = Vindexes.get(i);
			int edgeweight = newVELs.get(edge);
			edgeweight = edgeweight + com.TotalPacketSize;
			newVELs.set(edge, edgeweight);
		}		
		int congestion = getCongestion(indexes, newHELs, newVELs);
		return congestion;
	}

}
