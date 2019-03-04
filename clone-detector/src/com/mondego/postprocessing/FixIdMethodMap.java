package com.mondego.postprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import com.mondego.utility.Util;

public class FixIdMethodMap {
	
	public static void fixIdMethodMap(String inputFile, String newIdMethodFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(inputFile)); 
		String sCurrentLine;
		Writer writer = Util.openFile(newIdMethodFile, false);
		try {
			while ((sCurrentLine = br.readLine()) != null) {
				try{
					String functionAndBlockId = sCurrentLine.split("@#@")[0];
					long blockId = Long.parseLong(functionAndBlockId.split(",")[1]);
					if(ClonedMethod.idMethodMap.containsKey(blockId)){
						Util.writeToFile(writer, blockId+" "+ ClonedMethod.idMethodMap.get(blockId) , true);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			Util.closeOutputFile(writer);
		}
	}

	public static void main(String[] args){
		String filename = args[0]; 
		String projectInfo = filename.split("-clone-INPUT.txt")[0];
		//"/home/saini/code/repos/codeclonedetection/";
		final String baseDir = "/home/sourcerer/hitesh-vaibhav/metrics/clone-detection/";
		final String idMethodDir = baseDir+"input/output_ast/idMethod/";
		final String fixedIdMethodDir = baseDir+"input/output_ast/idMethod_fixed/";
		final String parsedAstFileDir = baseDir+"input/output_ast/dataset/";
		Util.createDirs(fixedIdMethodDir);
		try {
			System.out.println("processing project: "+ projectInfo);
			ClonedMethod.populateIdMethodNameMap(idMethodDir+projectInfo+"-idMethodMap.txt"); // activity-state-machine@1.0-alpha-1-idMethodMap.txt
			String inputFile = parsedAstFileDir+filename;
			String newIdMethodFile = fixedIdMethodDir+projectInfo+"-idMethodMap.txt";
			FixIdMethodMap.fixIdMethodMap(inputFile, newIdMethodFile);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
