package codecs;

public class NonEncodingCode extends LinearCode {

	public NonEncodingCode() {
		super(1,1);
	}

	@Override
	public String encode(String message) {
		return message;
	}

	@Override
	public String decode(String codeword) {
		return codeword;
	}

	@Override
	public String correctedCodeWord(String codeword) {
		return codeword;
	}

}
