package util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class PowerSetEnum<T> implements Enumeration<Set<T>>{

	long currentPos;
	
	long lowerBound;
	long upperBound;
	
	long elementsConsumed;
	long maxElements;
	
	ArrayList<T> set;
	
	
	public PowerSetEnum(Set<T> originalSet, int lowerBound, int upperBound, int startingNumber){
		this.set = new ArrayList<T>(originalSet.size());
		this.set.addAll(originalSet);
		this.currentPos = 0;
		this.lowerBound= lowerBound;
		this.upperBound= upperBound;
		this.maxElements = 0; //the empty set
		for (int i = lowerBound; i <= upperBound; i++) {
			this.maxElements += Combinatory.comb(originalSet.size(), i).longValue();
		}
		elementsConsumed = startingNumber;
	}

	@Override
	public boolean hasMoreElements() {
		return (elementsConsumed < maxElements);
	}

	@Override
	public Set<T> nextElement() {
		Set<T> result;
		do {
			result = new HashSet<>();
			long tmpPos = currentPos;
			for (int i = 0; i < set.size(); i++) {
				if (tmpPos % 2 == 0) {
					result.add(set.get(i));
				}
				tmpPos = tmpPos/2;
			}
			currentPos++;
		} while (result.size() < lowerBound || result.size() > upperBound);
		elementsConsumed++;
		return result;
	}
	
}
