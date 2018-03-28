package org.jgrapht.alg.util;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * Bla bla bla
 *
 * @author Rolando Trujillo-Rasua
 * @since Feb 15, 2012
 */
public class VertexPowerSetIterator<V> implements Iterator<Set<V>>{

	protected V[] vertexSet;
	private BigInteger binaryForm;
	protected BigInteger base;
	
	public VertexPowerSetIterator(Set<V> vertexSet){
		this.vertexSet = (V[])vertexSet.toArray();
		binaryForm = BigInteger.ZERO;
		base = new BigInteger("2");
		base = base.pow(this.vertexSet.length);
		base = base.add(BigInteger.ONE.negate());
	}
	
	@Override
	public boolean hasNext() {
		return binaryForm.compareTo(base) <= 0;
	}

	@Override
	public Set<V> next() {
		if (!hasNext()) throw new NoSuchElementException();
		Set<V> result = new HashSet<V>();
		BigInteger tmp = binaryForm;		
		int pos = tmp.getLowestSetBit(); 
		while (pos != -1){
			result.add(vertexSet[pos]);
			tmp = tmp.flipBit(pos);
			pos = tmp.getLowestSetBit();
		}
		//binaryForm.
		binaryForm = binaryForm.add(BigInteger.ONE);
		return result;
	}

	@Override
	public void remove() {
	}

}
