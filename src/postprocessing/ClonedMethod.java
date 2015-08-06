package postprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utility.Util;

public class ClonedMethod {
	private static Map<Long,String> idMethodMap;
	private static Set<String> clonedMethods;
	
	public ClonedMethod(){
		ClonedMethod.idMethodMap = new HashMap<Long, String>();
		ClonedMethod.clonedMethods = new HashSet<String>();
	}
	
	public static void populateIdMethodNameMap(String idMethodFileName) throws FileNotFoundException{
		BufferedReader br = new BufferedReader(new FileReader(idMethodFileName)); 
		String sCurrentLine;
		try {
			while ((sCurrentLine = br.readLine()) != null) {
				ClonedMethod.populateIdNameMap(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void populateClonedMethodsSet(String cloneResultsFile) throws FileNotFoundException{
		BufferedReader br = new BufferedReader(new FileReader(cloneResultsFile)); 
		String sCurrentLine;
		try {
			while ((sCurrentLine = br.readLine()) != null) {
				ClonedMethod.populateSet(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void writeClonedMethodsToFile(String filename) throws IOException{
		Writer writer = Util.openFile(filename, false);
		for (String methodName : this.clonedMethods){
			Util.writeToFile(writer, methodName, true);
		}
		Util.closeOutputFile(writer);
	}
	
	private static void populateSet(String line) {
		String[] ids = line.split(",");
		Long id1 = Long.parseLong(ids[0]);
		Long id2 = Long.parseLong(ids[1]);
		ClonedMethod.clonedMethods.add(ClonedMethod.idMethodMap.get(id1));
		ClonedMethod.clonedMethods.add(ClonedMethod.idMethodMap.get(id2));
	}

	private static void populateIdNameMap(String line) {
        String[] tokens = line.split(" ");
        ClonedMethod.idMethodMap.put(Long.parseLong(tokens[0]), tokens[1]);
    }
}
