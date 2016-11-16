package com.mondego.validation;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import com.mondego.postprocessing.ClonesBugsAssembler;
import com.mondego.utility.Util;

public class CloneBugPattern {
    private Writer outputWriter;
    /**
     * @param args
     */
    private ClonesBugsAssembler assembler;

    /**
     * @param outputWriter
     * @param assembler
     */
    public CloneBugPattern() {
        super();
        this.assembler = new ClonesBugsAssembler();
        ;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        CloneBugPattern bugPattern = new CloneBugPattern();
        if (args.length > 0) {
            bugPattern.assembler.setProjectName(args[0]);
            String filename = "output/" + bugPattern.assembler.getProjectName()
                    + "-clones_validation.csv";
            System.out.println(filename);
            try {
                bugPattern.outputWriter = Util.openFile(filename, false);
                String bugInfoFile = "input/findbug/findbugs-" + bugPattern.assembler.getProjectName()
                        + ".csv"; //findbugs-cglib
                bugPattern.assembler.setBugInfoFile(bugInfoFile);
                bugPattern.assembler.process();
                bugPattern.createOutput();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out
                .println("error in file creation, exiting");
                System.exit(1);
            }finally{
                Util.closeOutputFile(bugPattern.outputWriter);
            }
            
        } else {
            System.out
                    .println("Please provide inputfile prefix, e.g. ANT,cocoon,hadoop.");
            System.exit(1);
        }
    }

    // 1)read clonesname.csv
    // 2) read methodbugdensity.csv
    // 3) for a method and it's clones, get row from methodbugdensity and put
    // that in outputfile
    // 4) enter a blank line

    private void createOutput() {
        Map<String, String> clonesNameMap = this.assembler.getClonesNameMap();
        Set<String> methods = clonesNameMap.keySet();
        boolean found=false;
        boolean ret = false;
        Util.writeToFile(this.outputWriter, "sep="+Util.CSV_DELIMITER, true);
        for (String method : methods) {
            StringBuilder sb = new StringBuilder();
            String valueString = clonesNameMap.get(method);
            String[] values = valueString.split(Util.CSV_DELIMITER);
            String clones = values[0];
            String[] cv = clones.split("::");
            ret =  this.appendBugInfo(method, sb);
            found = found || ret;
            for (int i = 0; i < cv.length; i++) {
                ret = this.appendBugInfo(cv[i], sb);
                found = found || ret;
            }
            if(found){
                Util.writeToFile(this.outputWriter, sb.toString(), true);
                found = false;
            }
            
        }
    }

    private boolean appendBugInfo(String method, StringBuilder sb) {
        Map<String, String> bugsInfoMap = this.assembler.getMethodListing();
        if (bugsInfoMap.containsKey(method)) {
            sb.append(method + Util.CSV_DELIMITER + bugsInfoMap.get(method)+"\n");
            //Util.writeToFile(this.outputWriter, sb.toString(), true);
            return true;
        }else{
            sb.append(method +"\n");
        }
        return false;
    }

}
