package codecs;

public class Hamming74Code extends LinearCode {

	public Hamming74Code() {
		super(4, 7);
	}

	@Override
	public String encode(String message) {
		String encodedWord = "";
		int iterCount = (message.length() % 4 == 0)? message.length() / 4 : 1 + message.length() / 4; 
		for (int i = 0; i < iterCount; i++) {
			String chunk = "";
			if (message.length() <= 4*(i+1)) {
				chunk = message.substring(4*i, message.length());
				for (int j = message.length(); j < 4*(i+1); j++)
					chunk += "0"; 
			}
			else
				chunk = message.substring(4*i, 4*(i+1));
			String encodedChunk = chunk;
			int bit = ((int)(chunk.charAt(0)-'0') + (int)(chunk.charAt(2)-'0') + (int)(chunk.charAt(3)-'0')) % 2;
			encodedChunk += bit;
			bit = ((int)(chunk.charAt(0)-'0') + (int)(chunk.charAt(1)-'0') + (int)(chunk.charAt(3)-'0')) % 2;
			encodedChunk += bit;
			bit = ((int)(chunk.charAt(0)-'0') + (int)(chunk.charAt(1)-'0') + (int)(chunk.charAt(2)-'0')) % 2;
			encodedChunk += bit;
			encodedWord += encodedChunk;
		}
		return encodedWord;
	}

	@Override
	public String decode(String codeword) {
		if (codeword.length() % 7 != 0)
			return null;
		else {
			String decodedWord = "";
			
			for (int i = 0; i < codeword.length() / 7; i++) {
				String codedChunk = codeword.substring(7*i, 7*(i+1));
				String decodedChunk = "";
				
				boolean check1 = (((int)(codedChunk.charAt(0)-'0') + (int)(codedChunk.charAt(2)-'0') + (int)(codedChunk.charAt(3)-'0') + (int)(codedChunk.charAt(4)-'0')) % 2 == 0);
				boolean check2 = (((int)(codedChunk.charAt(0)-'0') + (int)(codedChunk.charAt(1)-'0') + (int)(codedChunk.charAt(3)-'0') + (int)(codedChunk.charAt(5)-'0')) % 2 == 0);
				boolean check3 = (((int)(codedChunk.charAt(0)-'0') + (int)(codedChunk.charAt(1)-'0') + (int)(codedChunk.charAt(2)-'0') + (int)(codedChunk.charAt(6)-'0')) % 2 == 0);
				
				if ((check1 && check2 && check3) || (!check1 && check2 && check3) || (check1 && !check2 && check3) || (check1 && check2 && !check3)) 
					decodedChunk = codedChunk.substring(0, 4);
				else if (!check1 && !check2 && check3) {
					decodedChunk += codedChunk.substring(0, 3); 
					decodedChunk += (codedChunk.charAt(3) == '0')? "1" : "0";
				}
				else if (!check1 && check2 && !check3) {
					decodedChunk += codedChunk.charAt(0);
					decodedChunk += codedChunk.charAt(1);
					decodedChunk += (codedChunk.charAt(2) == '0')? "1" : "0";
					decodedChunk += codedChunk.charAt(3);
				}
				else if (check1 && !check2 && !check3) {
					decodedChunk += codedChunk.charAt(0);
					decodedChunk += (codedChunk.charAt(1) == '0')? "1" : "0";
					decodedChunk += codedChunk.charAt(2);
					decodedChunk += codedChunk.charAt(3);
				}
				else {   // !check1 && !check2 && !check3
					decodedChunk += (codedChunk.charAt(0) == '0')? "1" : "0";
					decodedChunk += codedChunk.substring(1, 4);
				}
				decodedWord += decodedChunk;
			}
			
			return decodedWord;
		}
	}
	
	@Override
	public String correctedCodeWord(String codeword) {
		if (codeword.length() % 7 != 0)
			return null;
		else {
			String correctedCodeword = "";
			
			for (int i = 0; i < codeword.length() / 7; i++) {
				String codedChunk = codeword.substring(7*i, 7*(i+1));
				String correctedChunk = "";
				
				boolean check1 = (((int)(codedChunk.charAt(0)-'0') + (int)(codedChunk.charAt(2)-'0') + (int)(codedChunk.charAt(3)-'0') + (int)(codedChunk.charAt(4)-'0')) % 2 == 0);
				boolean check2 = (((int)(codedChunk.charAt(0)-'0') + (int)(codedChunk.charAt(1)-'0') + (int)(codedChunk.charAt(3)-'0') + (int)(codedChunk.charAt(5)-'0')) % 2 == 0);
				boolean check3 = (((int)(codedChunk.charAt(0)-'0') + (int)(codedChunk.charAt(1)-'0') + (int)(codedChunk.charAt(2)-'0') + (int)(codedChunk.charAt(6)-'0')) % 2 == 0);
				
				if (check1 && check2 && check3)  
					correctedChunk = codedChunk;
				else if (!check1 && check2 && check3) {
					correctedChunk += codedChunk.substring(0, 4);
					correctedChunk += (codedChunk.charAt(4) == '0')? "1" : "0";
					correctedChunk += codedChunk.substring(5, 7);
				}
				else if (check1 && !check2 && check3) {
					correctedChunk += codedChunk.substring(0, 5);
					correctedChunk += (codedChunk.charAt(5) == '0')? "1" : "0";
					correctedChunk += codedChunk.charAt(6);
				}
				else if (check1 && check2 && !check3) {
					correctedChunk += codedChunk.substring(0, 6);
					correctedChunk += (codedChunk.charAt(6) == '0')? "1" : "0";
				}
				else if (!check1 && !check2 && check3) {
					correctedChunk += codedChunk.substring(0, 3); 
					correctedChunk += (codedChunk.charAt(3) == '0')? "1" : "0";
					correctedChunk += codedChunk.substring(4, 7);
				}
				else if (!check1 && check2 && !check3) {
					correctedChunk += codedChunk.charAt(0);
					correctedChunk += codedChunk.charAt(1);
					correctedChunk += (codedChunk.charAt(2) == '0')? "1" : "0";
					correctedChunk += codedChunk.substring(3, 7);
				}
				else if (check1 && !check2 && !check3) {
					correctedChunk += codedChunk.charAt(0);
					correctedChunk += (codedChunk.charAt(1) == '0')? "1" : "0";
					correctedChunk += codedChunk.substring(2, 7);
				}
				else {   // !check1 && !check2 && !check3
					correctedChunk += (codedChunk.charAt(0) == '0')? "1" : "0";
					correctedChunk += codedChunk.substring(1, 7);
				}
				correctedCodeword += correctedChunk;
			}
			
			return correctedCodeword;
		}
	}
	
	public static void main(String [] args) {
		Hamming74Code codec = new Hamming74Code();
		System.out.println("Encoding 0100: " + codec.encode("0100"));
		System.out.println("Decoding 0100011: " + codec.decode("0100011"));
		System.out.println("Decoding 1100011: " + codec.decode("1100011"));
		System.out.println("Encoding 0011: " + codec.encode("0011"));
		System.out.println("Decoding 0011011: " + codec.decode("0011011"));
		System.out.println("Encoding 01000011: " + codec.encode("01000011"));
		System.out.println("Decoding 01000110011011: " + codec.decode("01000110011011"));
		System.out.println("Decoding 11000110010011: " + codec.decode("11000110010011"));
		System.out.println("Encoding 0100001101: " + codec.encode("0100001101"));
	}
}
