/**
 * 
 */
package postprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import utility.Util;

/**
 * @author vaibhavsaini
 * 
 */
public class ClonesBugsAssembler {
    String projectName;
    Map<String,String> clonesNameMap = new HashMap<String,String>();
    Map<String,String> methodListing = new HashMap<String,String>();
    private PrintWriter outputWriter;
    int linesWritten;
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        ClonesBugsAssembler assembler = new ClonesBugsAssembler();
        assembler.projectName = "codeclonedetection";
        assembler.linesWritten=0;
        assembler.process();
        try{
            assembler.createOutput();
            System.out.println("done!");
        }catch(Exception e){
            System.out.println("ERROR::"+ e.getMessage());
        }

    }

    private void process() {
        String sCurrentLine;
        BufferedReader br;
        String bugsInfoFile = "input/bugsReport/" + this.projectName
                + "-bugsInfo.txt";
        String clonesNameFile = "output/" + this.projectName
                + "-clones_names.csv";
        try {
            br = new BufferedReader(new FileReader(bugsInfoFile));
            while ((sCurrentLine = br.readLine()) != null) {
                this.populateMainMethodListing(sCurrentLine);
            }
            br = new BufferedReader(new FileReader(clonesNameFile));
            while ((sCurrentLine = br.readLine()) != null) {
                this.populateClonesNameMap(sCurrentLine);
            }
        } catch (FileNotFoundException e) {
            System.out.println("filenote not found " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void createOutput() throws IOException {
        String filename = "output/" + this.projectName
                + "-clones_bugs.csv";
        System.out.println(filename);
        this.outputWriter = Util.openFile(filename, false);
        StringBuilder sb = new StringBuilder();
        Set<String> methods = this.methodListing.keySet();
        try{
            for (String method : methods) {
                sb.append(method);
                String otherColumns = this.methodListing.get(method);
                if(otherColumns.trim()!=""){
                    sb.append(","+otherColumns);
                }
                String clonesColumns = this.clonesNameMap.get(method);
                if(null!=clonesColumns && clonesColumns.trim()!=""){
                    sb.append(","+clonesColumns);
                }
                Util.writeToFile(this.outputWriter, sb.toString(), true);
                sb.setLength(0);
                this.linesWritten++;
                if((this.linesWritten % 1000)==0){
                    System.out.println("lines written so far "+ this.linesWritten);
                }
            }
        }finally{
            Util.closeOutputFile(this.outputWriter);
        }
    }
    private void populateMainMethodListing(String line){
        this.populateMap(line, this.methodListing);
    }
    
    private void populateClonesNameMap(String line){
        this.populateMap(line, this.clonesNameMap);
    }
    private void populateMap(String line,Map<String,String> map){
        int index = line.indexOf(",");
        if(index!=-1){
            String key = line.substring(0, index);
            String value = line.substring(index+1);
            System.out.println(value);
            map.put(key, value);
        }
    }
}

