package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.TreeMap;

public class DataFile {

	public static void main(String[] args) throws IOException {
		buildingDataFiles1("./data4/m2l2/outputPrivacy-m-2-l-2.0.txt", "m2l2.DAT");
		buildingDataFiles1("./data4/m2l1/outputPrivacy-m-2-l-1.0.txt", "m2l1.DAT");
		buildingDataFiles1("./data4/m2l4/outputPrivacy-m-2-l-4.0.txt", "m2l4.DAT");
		buildingDataFiles1("./data4/m2l8/outputPrivacy-m-2-l-8.0.txt", "m2l8.DAT");
		buildingDataFiles1("./data4/m2l16/outputPrivacy-m-2-l-16.0.txt", "m2l16.DAT");
		buildingDataFiles1("./data4/m2l32/outputPrivacy-m-2-l-32.0.txt", "m2l32.DAT");
	}
	
	public static void buildingDataFiles1(String filePath, String fileNameForOutput) throws IOException{
		File file = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		TreeMap<Integer, Integer> counter = new TreeMap<>();
		double total = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine()){
			Integer key = Integer.parseInt(line);
			if (counter.containsKey(key)){
				counter.put(key, counter.get(key)+1);
			}
			else{
				counter.put(key, 1);
			}
			total++;
		}
		reader.close();
		File result = new File(fileNameForOutput);
		BufferedWriter writer = new BufferedWriter(new FileWriter(result));
		for (Integer k : counter.keySet()){
			writer.write(k+"\t"+counter.get(k)*100/total);
			writer.newLine();
		}
		writer.close();
	}
	
}
