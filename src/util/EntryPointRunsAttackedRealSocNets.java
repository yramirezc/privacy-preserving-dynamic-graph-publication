package util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import attacks.AttackThreeMethod;

public class EntryPointRunsAttackedRealSocNets {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		AttackThreeMethod.oneRunAttackedFacebookPanzURV(args);
	}

}
