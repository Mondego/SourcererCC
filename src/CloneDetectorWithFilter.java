import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utility.Util;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class CloneDetectorWithFilter {
    private Map<String, Integer> globalTokenPositionMap; // we will sort
                                                         // bags using
                                                         // it.
    private CloneHelper cloneHelper;
    private float threshold; // threshold for matching the clones.e.g. .8 or
                             // .9
    private Map<Integer, List<TokenFrequency>> bagToListMap;
    private long filterComparision;
    private boolean doSort;
    private int run;
    private PrintWriter analysisWriter;
    private long candidateCumulativeTime;
    private long comparisions;
    private Bag previousBag;
    private String filePrefix;

    /**
     * @param cloneHelper
     */
    public CloneDetectorWithFilter() {
        super();
        this.threshold = .8F;
        this.bagToListMap = new HashMap<Integer, List<TokenFrequency>>();
        this.filterComparision = 0;
        this.doSort = true;
        this.run = 1;
        this.candidateCumulativeTime = 0;
        this.comparisions = 0;
    }

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        CloneDetectorWithFilter cd = new CloneDetectorWithFilter();
        if(args.length>0){
            cd.filePrefix=args[0];
            if(args.length==2){
                try{
                    cd.run=Integer.parseInt(args[1]);
                }catch(NumberFormatException e){
                    System.out.println("Not a valid number. 2nd argument should be an Int");
                    System.exit(1);
                }
            }
        }else{
            System.out.println("Please provide inputfile prefix, e.g. ANT,cocoon,hadoop.");
            System.exit(1);
        }
        try {
            Util.createDirs("output");
            String filename = "output/"+cd.filePrefix+"clonesAnalysis_WITH_FILTER.csv";
            System.out.println("writing in file : "+ filename);
            cd.analysisWriter = Util
                    .openFile(filename,true);
            String header = "sort_time, detect_clones_time, token_comparision_filter, token_comparision ,total_comparision,num_clones_detected,candidateCumulativeTime";
            Util.writeToFile(cd.analysisWriter, header, true);
            for (int i = 0; i < cd.run; i++) {
                CloneHelper cloneHelper = new CloneHelper();
                cd.cloneHelper = cloneHelper;
                cd.init();
                cd.runExperiment();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            Util.closeOutputFile(cd.analysisWriter);
        }
    }

    private void runExperiment() {
        try {
            System.out.println("running, please wait...");
            this.cloneHelper.setClonesWriter(Util
                    .openFile("output/"+this.filePrefix+"clones_WITH_FILTER.txt",false));
            this.cloneHelper.setThreshold(this.threshold);
            Set<Bag> setA = new HashSet<Bag>();
            String projectAfile = "input/dataset/"+this.filePrefix+"-clone-INPUT.txt";
            String projectBfile = "input/dataset/"+this.filePrefix+"-clone-INPUT.txt";
            this.cloneHelper.parseInputFileAndPopulateSet(projectAfile, setA);
            Set<Bag> setB = new HashSet<Bag>();
            this.cloneHelper.parseInputFileAndPopulateSet(projectBfile, setB);
            this.setGlobalTokenPositionMap(CloneTestHelper
                    .getGlobalTokenPositionMap(setA, setB)); // input
            // sort
            long start_time = System.currentTimeMillis();
            for (Bag bag : setA) {
                this.convertToList(bag, this.doSort);
            }
            for (Bag bag : setB) {
                this.convertToList(bag, this.doSort);
            }
            long end_time = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            System.out.println("sort time in milliseconds:"
                    + (end_time - start_time));
            sb.append(end_time - start_time + ",");
            // sorting done
            start_time = System.currentTimeMillis();
            
            
            this.detectClones(setA, setB);// input
            
            
            end_time = System.currentTimeMillis();
            System.out.println("time in milliseconds :"
                    + (end_time - start_time));
            sb.append(end_time - start_time + ",");
            System.out.println("filterComparision :" + this.filterComparision);
            sb.append(this.filterComparision + ",");
            System.out.println("comparisions :" + this.comparisions);
            sb.append(this.comparisions + ",");
            System.out.println("total comparision :"
                    + (this.filterComparision + this.comparisions));
            sb.append(this.filterComparision + this.comparisions + ",");
            sb.append(this.cloneHelper.getNumClonesFound() + ",");
            sb.append(this.candidateCumulativeTime);
            Util.writeToFile(this.analysisWriter, sb.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Util.closeOutputFile(this.cloneHelper.getClonesWriter());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void init() {
        this.bagToListMap = new HashMap<Integer, List<TokenFrequency>>();
        this.filterComparision = 0;
    }

    /**
     * setA and setB represents two projects. Each element of the set is a Bag.
     * Each Bag is a Map that has Token as a key and token's frequency in the
     * method as value. the method compares two sets and outputs the clones.
     * 
     * @param setA
     *            set of Bags
     * @param setB
     *            set of Bags
     */
    private void detectClones(Set<Bag> setA, Set<Bag> setB) {
        // iterate on setA
        for (Bag bagInSetA : setA) {
            // compare this map with every map in setB and report clones
            // iterate on setB
            for (Bag bagInSetB : setB) {
                if(bagInSetA.getId()!=bagInSetB.getId()){
                    String pairId = "";
                    if(bagInSetA.getId()<bagInSetB.getId()){
                        this.isCandidate(bagInSetA, bagInSetB);
                    }
                }
            }
        }
    }

    public void detectClones(Iterator<TokenFrequency> bagAItr, Bag bagB,
            int computedThreshold, int matched, Bag bagA) {
        while (bagAItr.hasNext()) {
            // search this token in bagB
            TokenFrequency tokenFrequencyA = bagAItr.next();
            
            TokenFrequency tokenFrequencyB = bagB.get(tokenFrequencyA);
            this.comparisions += bagB.comparisions;
            if (null != tokenFrequencyB) {
                // token found.
                matched += Math.min(tokenFrequencyA.getFrequency(),
                        tokenFrequencyB.getFrequency());
                if (matched >= computedThreshold) {
                    // report clone.
                    this.cloneHelper.reportClone(bagA, bagB, this.previousBag);
                    this.previousBag = bagA;
                    break; // no need to iterate on other keys clone has been
                           // found
                }
            }
        }
    }

    /**
     * Creates a List of tokenFrequency objects in the bag and returns this list
     * after sorting it
     * 
     * @param bag
     *            the bag to be sorted
     * @return List<TokenFrequency>
     */
    private List<TokenFrequency> convertToSortedList(Bag bag) {
        if (this.bagToListMap.containsKey(bag)) {
            return bagToListMap.get(bag);
        } else {
            List<TokenFrequency> list = new ArrayList<TokenFrequency>(bag);
            Collections.sort(list, new Comparator<TokenFrequency>() {
                public int compare(TokenFrequency tfFirst,
                        TokenFrequency tfSecond) {
                    return globalTokenPositionMap.get(tfFirst.getToken()
                            .getValue())
                            - globalTokenPositionMap.get(tfSecond.getToken()
                                    .getValue());
                }
            });
            this.bagToListMap.put(bag.getId(), list);
            return list;
        }

    }

    private List<TokenFrequency> convertToList(Bag bag, boolean sort) {
        if (sort) {
            return this.convertToSortedList(bag);
        } else {
            return this.convertToList(bag);
        }
    }

    private List<TokenFrequency> convertToList(Bag bag) {
        if (this.bagToListMap.containsKey(bag)) {
            return bagToListMap.get(bag);
        } else {
            List<TokenFrequency> list = new ArrayList<TokenFrequency>(bag);
            this.bagToListMap.put(bag.getId(), list);
            return list;
        }
    }

    /**
     * returns the minmum number of tokens that must match in listA and listB
     * for listB to be a possible clone candidate.
     * 
     * @param listA
     * @param listB
     * @return
     */
    private int computePrefixSize(int maxTokenLength) {
        // System.out.println("t is " + maxTokenLength);
        // System.out.println("threshld is " + this.threshold);
        double x = this.threshold * maxTokenLength;
        // System.out.println("x is " + x);
        int thetaT = (int) Math.ceil(x);
        // System.out.println("thetaT is " + thetaT);
        return (maxTokenLength + 1) - thetaT;
    }

    /**
     * checks if bagA is a possible candidateClone of bagB
     * 
     * @param bagA
     * @param bagB
     * @return boolean
     */
    private boolean isCandidate(Bag bagA, Bag bagB) {
        long startTime = System.currentTimeMillis();
        int maxLength = Math.max((bagA.getSize()), bagB.getSize());
        int computedThreshold = (int) Math.ceil(this.threshold * maxLength);
        int prefixSize = this.computePrefixSize(maxLength);
        boolean candidate = false;
        /*
         * System.out.println("prefixSize for " + bagA.getId() + ", " +
         * bagB.getId() + " is: " + prefixSize);
         */
        if (prefixSize <= Math.min(bagB.getSize(), bagA.getSize())) {
            List<TokenFrequency> listA = this.bagToListMap.get(bagA.getId());
            List<TokenFrequency> listB = this.bagToListMap.get(bagB.getId());
            Iterator<TokenFrequency> listAItr = listA.iterator();
            Iterator<TokenFrequency> listBItr = listB.iterator();
            int count = 0;
            TokenFrequency tokenFrequencyA = listAItr.next();
            TokenFrequency tokenFrequencyB = listBItr.next();
            int tokenSeenInB = tokenFrequencyB.getFrequency();
            int tokenSeenInA = tokenFrequencyA.getFrequency();
            while (true) {
                this.filterComparision += 1;
                if (tokenFrequencyB.equals(tokenFrequencyA)) {
                    // candidate = true;
                    int matched = Math.min(tokenFrequencyA.getFrequency(),
                            tokenFrequencyB.getFrequency());
                    long stopTime = System.currentTimeMillis();
                    this.candidateCumulativeTime += stopTime - startTime;
                    this.detectClones(listAItr, bagB, computedThreshold,
                            matched, bagA);
                    return true;
                } else {
                    count = Math.min(tokenSeenInA, tokenSeenInB);
                    if (count >= prefixSize) {
                        break;
                    }
                    int globalPositionA = this.globalTokenPositionMap.get(tokenFrequencyA
                            .getToken().getValue());
                    int globalPositionB = this.globalTokenPositionMap.get(tokenFrequencyB
                            .getToken().getValue());
                    if (globalPositionB <= globalPositionA) {
                        if (listBItr.hasNext()) {
                            tokenFrequencyB = listBItr.next();
                            tokenSeenInB += tokenFrequencyB.getFrequency();
                        } else {
                            break;
                        }
                    } else {
                        if (listAItr.hasNext()) {
                            tokenFrequencyA = listAItr.next();
                            tokenSeenInA += tokenFrequencyA.getFrequency();
                        } else {
                            break;
                        }
                    }
                }
            }

        }
        long stopTime = System.currentTimeMillis();
        this.candidateCumulativeTime += stopTime - startTime;
        return candidate;
    }

    /*private boolean incrementItr(int count, int tokenSeenInA, int tokenSeenInB,
            int prefixSize, TokenFrequency tokenFrequencyA,
            TokenFrequency tokenFrequencyB, Iterator<TokenFrequency> listBItr,
            Iterator<TokenFrequency> listAItr) {

        count = Math.min(tokenSeenInA, tokenSeenInB);
        if (count >= prefixSize) {
            return true;
        }
        int globalPositionA = this.globalTokenPositionMap.get(tokenFrequencyA
                .getToken().getValue());
        int globalPositionB = this.globalTokenPositionMap.get(tokenFrequencyB
                .getToken().getValue());
        return this
                .incItr(globalPositionB, globalPositionA, tokenFrequencyB,
                        tokenFrequencyA, listBItr, tokenSeenInA, tokenSeenInB,
                        listAItr);

    }

    private boolean incItr(int globalPositionB, int globalPositionA,
            TokenFrequency tokenFrequencyB, TokenFrequency tokenFrequencyA,
            Iterator<TokenFrequency> listBItr, int tokenSeenInA,
            int tokenSeenInB, Iterator<TokenFrequency> listAItr) {
        if (globalPositionB <= globalPositionA) {
            if (listBItr.hasNext()) {
                tokenFrequencyB = listBItr.next();
                tokenSeenInB += tokenFrequencyB.getFrequency();
            } else {
                return true;
            }
        } else {
            if (listAItr.hasNext()) {
                tokenFrequencyA = listAItr.next();
                tokenSeenInA += tokenFrequencyA.getFrequency();
            } else {
                return true;
            }
        }
        return false;
    }*/

    /**
     * @return the globalTokenPositionMap
     */
    public Map<String, Integer> getGlobalTokenPositionMap() {
        return globalTokenPositionMap;
    }

    /**
     * @param globalTokenPositionMap
     *            the globalTokenPositionMap to set
     */
    public void setGlobalTokenPositionMap(
            Map<String, Integer> globalTokenPositionMap) {
        this.globalTokenPositionMap = globalTokenPositionMap;
    }
}
