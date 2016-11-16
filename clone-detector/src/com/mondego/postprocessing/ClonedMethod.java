package com.mondego.postprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mondego.utility.Util;

public class ClonedMethod {
	public static Map<Long,String> idMethodMap;
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
		}finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void writeClonedMethodsToFile(String filename) throws IOException{
		Writer writer = Util.openFile(filename, false);
		for (String methodName : ClonedMethod.clonedMethods){
			Util.writeToFile(writer, methodName, true);
		}
		Util.closeOutputFile(writer);
	}
	
	public static void generateOutput(Set<String> methods, String filename) throws IOException{
		Writer writer = Util.openFile(filename, false);
		Util.writeToFile(writer, "FQMN"+",hasClone", true);
		for (String method: methods){
			if(ClonedMethod.clonedMethods.contains(method)){
				Util.writeToFile(writer, method+",1", true);
			}
			else{
				Util.writeToFile(writer, method+",0", true);
			}
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
	
	public static void main(String[] args){
		String filename = args[0]; 
		String projectInfo = filename.split("-clone-INPUT.txt")[0];
		//"/home/saini/code/repos/codeclonedetection/";
		final String baseDir = "/home/sourcerer/hitesh-vaibhav/metrics/clone-detection/";
		final String outputDir = baseDir+"output7.0/";
		final String methodCloneDir = outputDir+"fixed_method-clone/";
		Util.createDirs(methodCloneDir);
		final String idMethodDir = baseDir+"input/output_ast/idMethod_fixed/";
		try {
			System.out.println("processing project: "+ projectInfo);
			ClonedMethod.populateIdMethodNameMap(idMethodDir+projectInfo+"-idMethodMap.txt"); // activity-state-machine@1.0-alpha-1-idMethodMap.txt
			ClonedMethod.populateClonedMethodsSet(outputDir+projectInfo+"-clone-INPUTclones_index_WITH_FILTER.txt");
			Set<String> methods = new HashSet<String>(ClonedMethod.idMethodMap.values());
			ClonedMethod.generateOutput(methods, methodCloneDir+projectInfo+".csv");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
