package util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import attacks.ExperimentsRobustSybils;

public class EntryPointExpRobustSybilsOneRealSocNetOnePert {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		ExperimentsRobustSybils.experimentRobustSybilsRealNetworksOnePerturbation(args);
	}

}
