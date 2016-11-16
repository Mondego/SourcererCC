/**
 * 
 */
package com.mondego.postprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mondego.utility.Util;

/**
 * @author vaibhavsaini
 * 
 */
public class ClonesBugsAssembler {
    String projectName;
    private Map<String,String> clonesNameMap;
    private Map<String,String> methodListing;
    private Writer outputWriter;
    private Set<String> clones;
    private int linesWritten;
    private String headerString;
    private String bugInfoFile;
    /**
     * @param args
     */
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        ClonesBugsAssembler assembler = new ClonesBugsAssembler();
        if (args.length > 0) {
            assembler.projectName = args[0];
        } else {
            System.out
                    .println("Please provide inputfile prefix, e.g. ANT,cocoon,hadoop.");
            System.exit(1);
        }
        assembler.bugInfoFile = "input/bugsReport/methodbugdensity-" + assembler.projectName
                + ".csv"; //methodbugdensity-cglib
        assembler.process();
        try{
            assembler.createOutput();
            System.out.println("done!");
        }catch(Exception e){
            System.out.println("ERROR::"+ e.getMessage());
        }

    }

    /**
     * 
     */
    public ClonesBugsAssembler() {
        super();
        this.clonesNameMap = new HashMap<String,String>();
        this.methodListing = new HashMap<String,String>();
        this.clones = new HashSet<String>();
        this.linesWritten = 0;
    }

    public void process() {
        String sCurrentLine;
        BufferedReader br;
        String clonesNameFile = "output/" + this.projectName
                + "-clones_names.csv";
        boolean isHeadder = true;
        try {
            br = new BufferedReader(new FileReader(this.bugInfoFile));
            
            while ((sCurrentLine = br.readLine()) != null) {
                if(isHeadder){
                    this.headerString=sCurrentLine;
                    isHeadder=false;
                }else{
                    this.populateMainMethodListing(sCurrentLine);
                }
            }
            br = new BufferedReader(new FileReader(clonesNameFile));
            while ((sCurrentLine = br.readLine()) != null) {
                this.populateClonesNameMap(sCurrentLine);
            }
            this.populateClonesList();
        } catch (FileNotFoundException e) {
            System.out.println("file not found " + e.getMessage());
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
        sb.append("sep="+Util.CSV_DELIMITER+"\n");
        sb.append(this.headerString);
        sb.append(Util.CSV_DELIMITER+"Clones"+Util.CSV_DELIMITER+"CloneCount"+Util.CSV_DELIMITER+"hasClone");
        Util.writeToFile(this.outputWriter, sb.toString(), true);
        sb.setLength(0);
        Set<String> methods = this.methodListing.keySet();
        int rows = 1;
        try{
            for (String method : methods) {
                sb.append(method);
                String otherColumns = this.methodListing.get(method);
                if(!otherColumns.trim().equalsIgnoreCase("")){
                    sb.append(Util.CSV_DELIMITER+otherColumns);
                }
                String clonesColumns = this.clonesNameMap.get(method);
                if(null!=clonesColumns && !clonesColumns.trim().equalsIgnoreCase("")){
                    sb.append(Util.CSV_DELIMITER+clonesColumns+Util.CSV_DELIMITER+"1");
                }
                else if(this.clones.contains(method)){
                    sb.append(Util.CSV_DELIMITER+Util.CSV_DELIMITER+"0"+Util.CSV_DELIMITER+"1");
                }else{
                    sb.append(Util.CSV_DELIMITER+Util.CSV_DELIMITER+"0"+Util.CSV_DELIMITER+"0");
                }
                Util.writeToFile(this.outputWriter, sb.toString(), true);
                sb.setLength(0);
                this.linesWritten++;
                if((this.linesWritten % 1000)==0){
                    System.out.println("lines written so far "+ this.linesWritten);
                }
                rows++;
            }
            this.appendSummary(rows);
        }finally{
            Util.closeOutputFile(this.outputWriter);
        }
    }
    private void appendSummary(int rows){
        StringBuilder sb = new StringBuilder();
        sb.append("&SUMMARY-START&");
        sb.append(Util.CSV_DELIMITER+Util.CSV_DELIMITER+"non clones"+Util.CSV_DELIMITER+"clones"+Util.CSV_DELIMITER+"total"+"\n");
        int columnOffset = rows+2;
        sb.append(Util.CSV_DELIMITER+"LOC"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",B:B)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",B:B)"+Util.CSV_DELIMITER+"=SUM(C"+columnOffset+":D"+columnOffset+")\n");
        sb.append(Util.CSV_DELIMITER+"BUGS"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",L:L)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",L:L)"+Util.CSV_DELIMITER+"=SUM(C"+(columnOffset+1)+":D"+(columnOffset+1)+")\n");
        sb.append(Util.CSV_DELIMITER+"DD"+Util.CSV_DELIMITER+"=(C"+(columnOffset+1)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+1)+"*1000)/"+"D"+columnOffset+Util.CSV_DELIMITER+"=SUM(C"+(columnOffset+1)+":D"+(columnOffset+1)+")\n");
        sb.append(Util.CSV_DELIMITER+"Category Wise"+Util.CSV_DELIMITER+"NonClones Bugcount"+Util.CSV_DELIMITER+"Clones Bugcount"+Util.CSV_DELIMITER+"NonClonesDD"+Util.CSV_DELIMITER+"ClonesDD"+"\n");
        int offset = 4;
        sb.append("C"+Util.CSV_DELIMITER+"Style"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",C:C)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",C:C)"+Util.CSV_DELIMITER+"=(C"+(columnOffset+offset)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+offset)+"*1000)/"+"D"+columnOffset+"\n");
        offset++;
        sb.append("D"+Util.CSV_DELIMITER+"Bad_Practice"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",D:D)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",D:D)"+Util.CSV_DELIMITER+"=(C"+(columnOffset+offset)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+offset)+"*1000)/"+"D"+columnOffset+"\n");
        offset++;
        sb.append("E"+Util.CSV_DELIMITER+"Correctness"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",E:E)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",E:E)"+Util.CSV_DELIMITER+"=(C"+(columnOffset+offset)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+offset)+"*1000)/"+"D"+columnOffset+"\n");
        offset++;
        sb.append("F"+Util.CSV_DELIMITER+"I18N"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",F:F)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",F:F)"+Util.CSV_DELIMITER+"=(C"+(columnOffset+offset)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+offset)+"*1000)/"+"D"+columnOffset+"\n");
        offset++;
        sb.append("G"+Util.CSV_DELIMITER+"Security"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",G:G)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",G:G)"+Util.CSV_DELIMITER+"=(C"+(columnOffset+offset)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+offset)+"*1000)/"+"D"+columnOffset+"\n");
        offset++;
        sb.append("H"+Util.CSV_DELIMITER+"Performance"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",H:H)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",H:H)"+Util.CSV_DELIMITER+"=(C"+(columnOffset+offset)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+offset)+"*1000)/"+"D"+columnOffset+"\n");
        offset++;
        sb.append("I"+Util.CSV_DELIMITER+"MT_Correctness"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",I:I)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",I:I)"+Util.CSV_DELIMITER+"=(C"+(columnOffset+offset)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+offset)+"*1000)/"+"D"+columnOffset+"\n");
        offset++;
        sb.append("J"+Util.CSV_DELIMITER+"Experimental"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",J:J)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",J:J)"+Util.CSV_DELIMITER+"=(C"+(columnOffset+offset)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+offset)+"*1000)/"+"D"+columnOffset+"\n");
        offset++;
        sb.append("K"+Util.CSV_DELIMITER+"Malicious_Code"+Util.CSV_DELIMITER+"=SUMIF(P:P,\"=0\",K:K)"+Util.CSV_DELIMITER+"=SUMIF(P:P,\">0\",K:K)"+Util.CSV_DELIMITER+"=(C"+(columnOffset+offset)+"*1000)/"+"C"+columnOffset+Util.CSV_DELIMITER+"=(D"+(columnOffset+offset)+"*1000)/"+"D"+columnOffset+"\n");
        sb.append("Total"+Util.CSV_DELIMITER+""+Util.CSV_DELIMITER+"=SUM(C"+(columnOffset+4)+":C"+(columnOffset+offset)+")"+Util.CSV_DELIMITER+"=SUM(D"+(columnOffset+4)+":D"+(columnOffset+offset)+")"+"\n");
        System.out.println(sb);
        Util.writeToFile(this.outputWriter, sb.toString(), true);
    }
    private void populateClonesList(){
        Set<String> keys = this.clonesNameMap.keySet();
        for(String key : keys){
            this.clones.add(key);
            String value = this.clonesNameMap.get(key);
            String [] values = value.split(Util.CSV_DELIMITER);
            String cloneValues = values[0];
            String [] cv = cloneValues.split("::");
            for(int i=0;i<cv.length;i++){
                this.clones.add(cv[i]);
            }
        }
    }
    private void populateMainMethodListing(String line){
        this.populateMap(line, this.methodListing);
    }
    
    private void populateClonesNameMap(String line){
        this.populateMap(line, this.clonesNameMap);
    }
    private void populateMap(String line,Map<String,String> map){
        int index = line.indexOf(Util.CSV_DELIMITER);
        if(index!=-1){
            String key = line.substring(0, index);
            String value = line.substring(index+1);
            map.put(key, value);
        }
    }

    /**
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return the clonesNameMap
     */
    public Map<String, String> getClonesNameMap() {
        return clonesNameMap;
    }

    /**
     * @param clonesNameMap the clonesNameMap to set
     */
    public void setClonesNameMap(Map<String, String> clonesNameMap) {
        this.clonesNameMap = clonesNameMap;
    }

    /**
     * @return the methodListing
     */
    public Map<String, String> getMethodListing() {
        return methodListing;
    }

    /**
     * @param methodListing the methodListing to set
     */
    public void setMethodListing(Map<String, String> methodListing) {
        this.methodListing = methodListing;
    }

    /**
     * @return the outputWriter
     */
    public Writer getOutputWriter() {
        return outputWriter;
    }

    /**
     * @param outputWriter the outputWriter to set
     */
    public void setOutputWriter(PrintWriter outputWriter) {
        this.outputWriter = outputWriter;
    }

    /**
     * @return the clones
     */
    public Set<String> getClones() {
        return clones;
    }

    /**
     * @param clones the clones to set
     */
    public void setClones(Set<String> clones) {
        this.clones = clones;
    }

    /**
     * @return the linesWritten
     */
    public int getLinesWritten() {
        return linesWritten;
    }

    /**
     * @param linesWritten the linesWritten to set
     */
    public void setLinesWritten(int linesWritten) {
        this.linesWritten = linesWritten;
    }

    /**
     * @return the headerString
     */
    public String getHeaderString() {
        return headerString;
    }

    /**
     * @param headerString the headerString to set
     */
    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }

    /**
     * @return the bugInfoFile
     */
    public String getBugInfoFile() {
        return bugInfoFile;
    }

    /**
     * @param bugInfoFile the bugInfoFile to set
     */
    public void setBugInfoFile(String bugInfoFile) {
        this.bugInfoFile = bugInfoFile;
    }
}

