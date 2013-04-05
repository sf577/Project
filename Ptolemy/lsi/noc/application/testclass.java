package lsi.noc.application;

import java.util.ArrayList;
import java.util.List;

import ptolemy.kernel.util.IllegalActionException;

public class testclass {

	public testclass() {
		// TODO Auto-generated constructor stub
	}
	
	public List<List<Integer>> calculateXYpathtest(int sourcex, int sourcey , int destx, int desty){
		List<Integer> HIndexes = new ArrayList<Integer>();
		List<Integer> VIndexes = new ArrayList<Integer>();
		List<List<Integer>> Indexes = new ArrayList<List<Integer>>();
		int currentx = sourcex;
		int currenty = sourcey;
		while(currentx != destx){
			if (currentx < destx){
				HIndexes.add(currenty*3 + currentx);
				currentx ++;
			} else {
				currentx --;
				HIndexes.add(currenty*3 + currentx);
			}
		}
		while(currenty != desty){
			if (currenty < desty){
				VIndexes.add(currentx*3 + currenty);
				currenty ++;
			} else {
				currenty --;
				VIndexes.add(currentx*3 + currenty);
			}
		}
		Indexes.add(HIndexes);
		Indexes.add(VIndexes);
		return Indexes;
	}

}
