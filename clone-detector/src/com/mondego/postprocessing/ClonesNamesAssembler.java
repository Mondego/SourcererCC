/**
 * 
 */
package com.mondego.postprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mondego.utility.Util;

/**
 * @author vaibhavsaini
 * 
 */
public class ClonesNamesAssembler {

    /**
     * @param args
     */
    String projectName;
    Map<Integer, String> idNameMap = new HashMap<Integer, String>();
    Map<Integer, List<Integer>> cloneIdsMap = new HashMap<Integer, List<Integer>>();
    Map<String, List<String>> cloneNameMap = new HashMap<String, List<String>>();
    private Writer outputWriter;
    int linesWritten;

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.out.println("start!");
        ClonesNamesAssembler assembler = new ClonesNamesAssembler();
        if (args.length > 0) {
            assembler.projectName = args[0];
        } else {
            System.out
                    .println("Please provide inputfile prefix, e.g. ANT,cocoon,hadoop.");
            System.exit(1);
        }
        assembler.linesWritten = 0;
        assembler.process();
        try {
            assembler.createOutput();
            System.out.println("done!");
        } catch (Exception e) {
            System.out.println("ERROR::" + e.getMessage());
        }
    }

    private void process() {
        String sCurrentLine;
        int offset = "Clones of Bag ".length();
        BufferedReader br;
        String idMethodFileName = "input/idMethod/" + this.projectName
                + "-idMethodMap.txt";
        String clonesInfoFileName = "output/" + this.projectName
                + "clones_WITH_FILTER.txt";
        try {
            br = new BufferedReader(new FileReader(idMethodFileName));
            while ((sCurrentLine = br.readLine()) != null) {
                this.populateIdNameMap(sCurrentLine);
            }
            br = new BufferedReader(new FileReader(clonesInfoFileName));
            int key = 0;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.trim().length() > 0) {

                    if (sCurrentLine.indexOf("Bag") != -1) {
                        key = Integer.parseInt(sCurrentLine.substring(offset));
                        this.cloneIdsMap.put(key, new ArrayList<Integer>());
                    } else {
                        String[] clones = sCurrentLine.split(",");
                        for (String clone : clones) {
                            List<Integer> cloneIds = this.cloneIdsMap.get(key);
                            cloneIds.add(Integer.parseInt(clone.trim()));
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("filenote not found " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void populateIdNameMap(String line) {
        String[] tokens = line.split(" ");
        this.idNameMap.put(Integer.parseInt(tokens[0]), tokens[1]);
    }

    private void createOutput() throws IOException {
        String filename = "output/" + this.projectName + "-clones_names.csv";
        this.outputWriter = Util.openFile(filename, false);
        StringBuilder sb = new StringBuilder();
        Set<Integer> ids = this.cloneIdsMap.keySet();
        try {
            for (Integer id : ids) {
                sb.append(this.idNameMap.get(id) + Util.CSV_DELIMITER);
                List<Integer> clones = this.cloneIdsMap.get(id);
                for (Integer clone : clones) {
                    sb.append(this.idNameMap.get(clone) + "::");
                }
                sb.setLength(sb.length() - 2);
                sb.append(Util.CSV_DELIMITER + clones.size());
                Util.writeToFile(this.outputWriter, sb.toString(), true);
                sb.setLength(0);
                this.linesWritten++;
                if ((this.linesWritten % 1000) == 0) {
                    System.out.println("lines written so far "
                            + this.linesWritten);
                }
            }
        } finally {
            Util.closeOutputFile(this.outputWriter);
        }
    }
}
