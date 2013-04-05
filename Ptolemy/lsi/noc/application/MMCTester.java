/**
 * 
 */
package lsi.noc.application;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Steven
 *
 */
public class MMCTester {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	
	@Test
	public void testcalculateXYpathtest(){
		int sourcex = 0;
		int sourcey = 0;
		int destx = 2;
		int desty = 3;
		testclass Mapper = new testclass();
		List<Integer> HIndexes = new ArrayList<Integer>();
		HIndexes.add(0);
		HIndexes.add(1);
		List<Integer> VIndexes = new ArrayList<Integer>();
		VIndexes.add(6);
		VIndexes.add(7);
		VIndexes.add(8);
		List<List<Integer>> actualIndexes = Mapper.calculateXYpathtest(sourcex, sourcey, destx, desty);
		List<List<Integer>> expectedIndexes = new ArrayList<List<Integer>>();
		expectedIndexes.add(HIndexes);
		expectedIndexes.add(VIndexes);
		assertEquals(actualIndexes, expectedIndexes);
	}
}