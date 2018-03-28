package test;

import java.util.Set;

import test.AntiResolvingPersistence.STATE;


public class Output{
	
	public STATE output;
	public long size;
	public Set<String> antiResolvingBasis;
	
	public Output(STATE output, long size, Set<String> antiResolvingBasis){
		this.output = output;
		this.size = size;
		this.antiResolvingBasis = antiResolvingBasis;
	}
	
}
