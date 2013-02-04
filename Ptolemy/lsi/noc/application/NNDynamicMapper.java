/**
 * 
 */
package lsi.noc.application;

import lsi.noc.stats.BasicCommunicationLatencyAnalysis;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @author Steven Fisher
 *
 */
@SuppressWarnings("serial")
public class NNDynamicMapper extends DynamicMapper{

	public NNDynamicMapper(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		this.addCommunicationLatencyAnalysis(new BasicCommunicationLatencyAnalysis());
	}

	@Override
	protected void performMapping_(){
		
	}
}
