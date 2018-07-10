package codecs;

public abstract class LinearCode {
	
	protected int messageLength;
	protected int codewordLength;

	public LinearCode(int ml, int cwl) { 
		messageLength = ml;
		codewordLength = cwl;
	}
	
	public abstract String encode(String message);
	
	public abstract String decode(String codeword);
	
	public abstract String correctedCodeWord(String codeword);

	public int getCodewordLength() {
		return codewordLength;
	}

	public int getMessageLength() {
		return messageLength;
	}

}
