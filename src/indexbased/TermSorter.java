package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;

import noindex.CloneHelper;
import utility.Util;

/**
 * for every project's input file (one file is one project) read all lines for
 * each line create a Bag. for each project create one output file, this file
 * will have all the tokens, in the bag.
 * 
 * @author vaibhavsaini
 * 
 */
public class TermSorter {
    private CloneHelper cloneHelper;
    public static Map<String, Long> wordFreq;
    public static String SORTED_FILES_DIR = "output/sortedFiles";
    public static Map<String, Integer> globalTokenPositionMap;
    public String GTPMfilename;

    public TermSorter() {
        TermSorter.wordFreq = new HashMap<String, Long>();
        Util.createDirs(SORTED_FILES_DIR);
        TermSorter.globalTokenPositionMap = new HashMap<String, Integer>();
        this.cloneHelper = new CloneHelper();
        this.GTPMfilename = Util.GTPM_DIR + "/gtpm.json";

    }

    public static void main(String[] args) throws IOException, ParseException {
        TermSorter externalSort = new TermSorter();
        externalSort.populateGlobalPositionMap();
    }

    public void populateGlobalPositionMap() throws IOException, ParseException {
        File gptmFile = new File(this.GTPMfilename);
        if (gptmFile.exists()) {
            TermSorter.globalTokenPositionMap = Util
                    .readJsonStream(this.GTPMfilename);
            System.out.println("search size of GTPM: "
                    + TermSorter.globalTokenPositionMap.size());
        } else {
            File datasetDir = new File(SearchManager.DATASET_DIR);
            if (datasetDir.isDirectory()) {
                System.out.println("Directory: " + datasetDir.getName());
                for (File inputFile : datasetDir.listFiles()) {
                    this.populateWordFreqMap(inputFile);
                }
                Map<String, Long> sortedMap = ImmutableSortedMap.copyOf(
                        TermSorter.wordFreq,
                        Ordering.natural()
                                .onResultOf(
                                        Functions.forMap(TermSorter.wordFreq))
                                .compound(Ordering.natural()));
                int count = 1;
                for (Entry<String, Long> entry : sortedMap.entrySet()) {
                    TermSorter.globalTokenPositionMap
                            .put(entry.getKey(), count);
                    count++;
                }
                System.out.println("index size of GTPM: "
                        + TermSorter.globalTokenPositionMap.size());
                Util.writeJsonStream(this.GTPMfilename,
                        TermSorter.globalTokenPositionMap);
                TermSorter.wordFreq = null;
                sortedMap = null;
            } else {
                System.out.println("File: " + datasetDir.getName()
                        + " is not a direcory. exiting now");
                System.exit(1);
            }
        }
    }

    private void populateWordFreqMap(File file) throws IOException,
            ParseException {
        // System.out.println("Sorting file: " + file.getName());
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null && line.trim().length() > 0) {
            cloneHelper.parseAndPopulateWordFreqMap(line);
        }
        br.close();
    }

}
