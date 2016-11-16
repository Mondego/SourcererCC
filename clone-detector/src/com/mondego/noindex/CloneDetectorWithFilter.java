package com.mondego.noindex;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mondego.models.Bag;
import com.mondego.models.TokenFrequency;
import com.mondego.utility.Util;

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
    private Map<Long, List<TokenFrequency>> bagToListMap;
    private long filterComparision;
    private boolean doSort;
    private Writer analysisWriter;
    // private PrintWriter prefixWriter;
    private long candidateCumulativeTime;
    private long comparisions;
    private Bag previousBag;
    private String filePrefix;
    private Float th; // args[1]
    private long numCandidates;
    private long numPairs;
    private int k_filter;
    private String locFilter;
    private boolean useJaccardSimilarity;
    private final String LOC_FILTER_CANDIDATES = "pos_filter_c";
    private final String LOC_FILTER_VALIDATION = "pos_filter_v";
    private final String LOC_FILTER_BOTH = "pos_filter_cv";
    private final String LOC_FILTER_NONE = "pos_filter_none";
    private final String SIMILARITY_OVERLAP = "overlap";
    private final String SIMILARITY_JACCARD = "jaccard";
    private final String OUTPUT_DIR_PREFIX = "outputnew";
    private final Integer MUL_FACTOR = 100;
    private Set<String> cloneSet;

    /**
     * @param cloneHelper
     */
    public CloneDetectorWithFilter() {
        super();
        this.threshold = 1F;
        this.bagToListMap = new HashMap<Long, List<TokenFrequency>>();
        this.filterComparision = 0;
        this.doSort = true;
        this.candidateCumulativeTime = 0;
        this.comparisions = 0;
        this.numCandidates = 0;
        this.numPairs = 0;
        this.k_filter = 0;
        this.locFilter = this.LOC_FILTER_NONE;
        this.useJaccardSimilarity = false;
        this.cloneSet = new HashSet<String>();

    }

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        CloneDetectorWithFilter cd = new CloneDetectorWithFilter();
        if (args.length > 0) {
            cd.filePrefix = args[0];
            cd.threshold = Float.parseFloat(args[1]) / 10;
            cd.th = (Float.parseFloat(args[1]) * cd.MUL_FACTOR);
            System.out.println(cd.th / cd.MUL_FACTOR);
            cd.k_filter = Integer.parseInt(args[2]);
            cd.locFilter = args[3];
            if (args[4].equals(cd.SIMILARITY_JACCARD)) {
                cd.useJaccardSimilarity = true;
            } else {
                cd.useJaccardSimilarity = false;
            }

        } else {
            System.out
                    .println("Please provide inputfile prefix, e.g. ANT,cocoon,hadoop.");
            System.exit(1);
        }
        try {
            Util.createDirs(cd.OUTPUT_DIR_PREFIX + cd.th / cd.MUL_FACTOR);
            String filename = cd.OUTPUT_DIR_PREFIX + cd.th / cd.MUL_FACTOR
                    + "/" + cd.filePrefix + "clonesAnalysis_WITH_FILTER.csv";
            System.out.println("writing in file : " + filename);
            File file = new File(filename);
            boolean skipHeader = false;
            if (file.exists()) {
                skipHeader = true;
            }
            cd.analysisWriter = Util.openFile(filename, true);
            // cd.prefixWriter = Util.openFile(prefixFilename, false);
            if (!skipHeader) {
                String header = "sort_time, detect_clones_time, "
                        + "token_comparision_filter, " + "token_comparision ,"
                        + "total_comparision," + "num_clones_detected,"
                        + "candidateCumulativeTime,"
                        + "threshold,numCandidates," + "numPairs, "
                        + "k_filter," + "locFilter," + "similarity_function";
                Util.writeToFile(cd.analysisWriter, header, true);
            }
            CloneHelper cloneHelper = new CloneHelper();
            cd.cloneHelper = cloneHelper;
            cd.init();
            cd.runExperiment();
            /*System.out.println("unique clone pairs : " + cd.cloneSet.size());
            List<String> cloneList = new ArrayList<String>(cd.cloneSet);
            Collections.sort(cloneList);
            for (String clonePair : cloneList){
                System.out.println(clonePair);
            }*/
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            Util.closeOutputFile(cd.analysisWriter);
            // Util.closeOutputFile(cd.prefixWriter);
        }
    }

    private void runExperiment() {
        try {
            System.out.println("running, please wait...");
            this.cloneHelper.setClonesWriter(Util
                    .openFile(this.OUTPUT_DIR_PREFIX + this.th
                            / this.MUL_FACTOR + "/" + this.filePrefix
                            + "clones_WITH_FILTER.txt", false));
            this.cloneHelper.setThreshold(this.threshold);
            Set<Bag> setA = new HashSet<Bag>();
            String repositoryDirectory = "input/dataset/";
            String projectBfile = "input/dataset/" + this.filePrefix
                    + "-clone-INPUT.txt";
            this.cloneHelper.parseInputDirAndPopulateSet(
                    new File(repositoryDirectory), setA);
            Set<Bag> setB = new HashSet<Bag>();
            this.cloneHelper.parseInputFileAndPopulateSet(
                    new File(projectBfile), setB);
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
            System.out.println("size, " + this.filePrefix + ", " + setA.size());
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
            sb.append(this.candidateCumulativeTime + ",");
            System.out.println("threshold set to : " + this.threshold);
            sb.append(this.threshold + ",");
            sb.append(this.numCandidates + ",");
            sb.append(this.numPairs + ",");
            sb.append(this.k_filter + ",");
            sb.append(this.locFilter + ",");
            if (this.useJaccardSimilarity) {
                sb.append(this.SIMILARITY_JACCARD);
            } else {
                sb.append(this.SIMILARITY_OVERLAP);
            }
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
        this.bagToListMap = new HashMap<Long, List<TokenFrequency>>();
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
                this.isCandidate(bagInSetA, bagInSetB);
            }
        }
    }

    public void detectClones(Iterator<TokenFrequency> bagAItr,
            Iterator<TokenFrequency> bagBItr, Bag bagB, int computedThreshold,
            int matchCount, Bag bagA, TokenFrequency tokenFrequencyA,
            TokenFrequency tokenFrequencyB, int tokenSeenInA, int tokenSeenInB) {
        /*
         * if (bagA.getId() == 11892 && bagB.getId() == 11893) {
         * System.out.println("DEBUG: it is a candidate"); }
         */
        while (true) {
            this.comparisions += 1;
            if (tokenFrequencyB.equals(tokenFrequencyA)) {
                matchCount += Math.min(tokenFrequencyA.getFrequency(),
                        tokenFrequencyB.getFrequency());
                /*
                 * if (bagA.getId() == 11892 && bagB.getId() == 11893) {
                 * System.out.println("DEBUG: match count " + matchCount +
                 * "computedThreshold " + computedThreshold); }
                 */
                if (matchCount >= computedThreshold) {
                    /*
                     * if (bagA.getId() == 11892 && bagB.getId() == 11893) {
                     * System.out.println("DEBUG: reporting it"); }
                     */
                    // report clone.
                    String clonePair = "";
                    if (bagA.getId() < bagB.getId()) {
                        clonePair = bagA.getId() + "::" + bagB.getId();
                    } else {
                        clonePair = bagB.getId() + "::" + bagA.getId();
                    }
                    this.cloneSet.add(clonePair);
                    this.cloneHelper.reportClone(bagA, bagB, this.previousBag);
                    this.previousBag = bagA;
                    break; // no need to iterate on other keys clone has been
                           // found
                } else {
                    if (this.hasPermission(LOC_FILTER_VALIDATION)) {
                        if (!isSatisfylocFilter(bagA.getSize(), bagB.getSize(),
                                matchCount, computedThreshold, tokenSeenInA,
                                tokenSeenInB)) {
                            break;
                        }
                    }
                    if (bagBItr.hasNext() && bagAItr.hasNext()) {
                        tokenFrequencyB = bagBItr.next();
                        tokenSeenInB += tokenFrequencyB.getFrequency();
                        tokenFrequencyA = bagAItr.next();
                        tokenSeenInA += tokenFrequencyA.getFrequency();
                    } else {
                        break;
                    }
                }
            } else {
                int globalPositionA = this.globalTokenPositionMap
                        .get(tokenFrequencyA.getToken().getValue());
                int globalPositionB = this.globalTokenPositionMap
                        .get(tokenFrequencyB.getToken().getValue());
                if (globalPositionB <= globalPositionA) {
                    if (this.hasPermission(LOC_FILTER_VALIDATION)) {
                        if (!isSatisfylocFilter(bagA.getSize()
                                + tokenFrequencyA.getFrequency(),
                                bagB.getSize(), matchCount, computedThreshold,
                                tokenSeenInA, tokenSeenInB)) {
                            break;
                        }
                    }
                    if (bagBItr.hasNext()) {
                        tokenFrequencyB = bagBItr.next();
                        tokenSeenInB += tokenFrequencyB.getFrequency();
                    } else {
                        break;
                    }
                } else {
                    if (this.hasPermission(LOC_FILTER_VALIDATION)) {
                        if (!isSatisfylocFilter(bagA.getSize(), bagB.getSize()
                                + tokenFrequencyB.getFrequency(), matchCount,
                                computedThreshold, tokenSeenInA, tokenSeenInB)) {
                            break;
                        }
                    }
                    if (bagAItr.hasNext()) {
                        tokenFrequencyA = bagAItr.next();
                        tokenSeenInA += tokenFrequencyA.getFrequency();
                    } else {
                        break;
                    }
                }
            }

        }
        // this.comparisions += bagB.comparisions;
    }

    private boolean isSatisfylocFilter(int sizeA, int sizeB, int matchCount,
            int computedThreshold, int tokenSeenInA, int tokenSeenInB) {
        return (Math.min(sizeA - tokenSeenInA, sizeB - tokenSeenInB) + matchCount) >= computedThreshold;
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
     * checks if bagA is a possible candidateClone of bagB
     * 
     * @param bagA
     * @param bagB
     * @return boolean
     */
    private boolean isCandidate(Bag bagA, Bag bagB) {
        this.numPairs += 1;
        long startTime = System.currentTimeMillis();
        int maxLength = Math.max((bagA.getSize()), bagB.getSize());
        int minLength = Math.min((bagA.getSize()), bagB.getSize());
        int computedThreshold = 0;
        if (this.useJaccardSimilarity) {
            int computedThreshold_jaccard = (int) Math.ceil((this.th * (bagA
                    .getSize() + bagB.getSize()))
                    / (10 * this.MUL_FACTOR + this.th));
            computedThreshold = computedThreshold_jaccard;
        } else {
            int computedThreshold_overlap = (int) Math
                    .ceil((this.th * maxLength) / (10 * this.MUL_FACTOR));
            computedThreshold = computedThreshold_overlap;
        }
        int prefixSize = (maxLength + 1) - computedThreshold;// this.computePrefixSize(maxLength);
        /*
         * if (bagA.getId() == 11892 && bagB.getId() == 11893) {
         * System.out.println("DEBUG: prefix " + prefixSize + ", " +
         * "computed threshold " + computedThreshold + ", bagA.size " +
         * bagA.getSize() + "bagB.size " + bagB.getSize() + ", th " +
         * (this.th/this.MUL_FACTOR) + ", threshold " + this.threshold +
         * ", isJaccard " + this.useJaccardSimilarity + ", maxlength " +
         * maxLength + ", maxlength*th " + (this.th * maxLength) /
         * (10*this.MUL_FACTOR) + ", celi" + Math.ceil((this.th * maxLength) /
         * (10*this.MUL_FACTOR))); }
         */
        boolean candidate = false;
        int matchCount = 0;
        if (hasPermission(LOC_FILTER_CANDIDATES)) {
            if (Math.ceil((this.th * maxLength) / (10 * this.MUL_FACTOR)) <= minLength) {
                return this.candidateHelper(prefixSize, minLength, bagA, bagB,
                        matchCount, candidate, startTime, computedThreshold);
            } else {
                long stopTime = System.currentTimeMillis();
                this.candidateCumulativeTime += (stopTime - startTime);
                return candidate;
            }
        } else {
            return this.candidateHelper(prefixSize, minLength, bagA, bagB,
                    matchCount, candidate, startTime, computedThreshold);
        }
    }

    private boolean candidateHelper(int prefixSize, int minLength, Bag bagA,
            Bag bagB, int matchCount, boolean candidate, long startTime,
            int computedThreshold) {
        if (prefixSize <= minLength) { // optimization
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
                count = Math.min(tokenSeenInA, tokenSeenInB);
                if (tokenFrequencyB.equals(tokenFrequencyA)) {

                    matchCount += Math.min(tokenFrequencyA.getFrequency(),
                            tokenFrequencyB.getFrequency());
                    if (matchCount >= this.k_filter + 1) { // filter condition,
                                                           // 1 is for the
                                                           // original prefix
                                                           // match, k
                                                           // additional
                                                           // matches required
                        this.numCandidates += 1;
                        candidate = true;
                        long stopTime = System.currentTimeMillis();

                        if (listBItr.hasNext() && listAItr.hasNext()) {
                            tokenFrequencyB = listBItr.next();
                            tokenSeenInB += tokenFrequencyB.getFrequency();
                            tokenFrequencyA = listAItr.next();
                            tokenSeenInA += tokenFrequencyA.getFrequency();
                        } else {
                            break;
                        }
                        this.candidateCumulativeTime += (stopTime - startTime);
                        // TODO: optimize for location filter; send bagBItr
                        // maybe?
                        this.detectClones(listAItr, listBItr, bagB,
                                computedThreshold, matchCount, bagA,
                                tokenFrequencyA, tokenFrequencyB, tokenSeenInA,
                                tokenSeenInB);
                        return true;
                    } else {
                        if (this.hasPermission(LOC_FILTER_CANDIDATES)) {
                            if (!isSatisfylocFilter(bagA.getSize(),
                                    bagB.getSize(), matchCount,
                                    computedThreshold, tokenSeenInA,
                                    tokenSeenInB)) {
                                break;
                            }
                        }
                        if (listBItr.hasNext() && listAItr.hasNext()) {
                            tokenFrequencyB = listBItr.next();
                            tokenSeenInB += tokenFrequencyB.getFrequency();
                            tokenFrequencyA = listAItr.next();
                            tokenSeenInA += tokenFrequencyA.getFrequency();
                        } else {
                            break;
                        }
                    }
                } else {
                    if (count >= prefixSize && matchCount == 0) {
                        break;
                    } else if (count >= prefixSize + this.k_filter) {
                        break;
                    }
                    int globalPositionA = this.globalTokenPositionMap
                            .get(tokenFrequencyA.getToken().getValue());
                    int globalPositionB = this.globalTokenPositionMap
                            .get(tokenFrequencyB.getToken().getValue());
                    if (globalPositionB <= globalPositionA) {
                        if (this.hasPermission(LOC_FILTER_CANDIDATES)) {
                            if (!isSatisfylocFilter(bagA.getSize()
                                    + tokenFrequencyA.getFrequency(),
                                    bagB.getSize(), matchCount,
                                    computedThreshold, tokenSeenInA,
                                    tokenSeenInB)) {
                                break;
                            }
                        }
                        if (listBItr.hasNext()) {
                            tokenFrequencyB = listBItr.next();
                            tokenSeenInB += tokenFrequencyB.getFrequency();
                        } else {
                            break;
                        }
                    } else {
                        if (this.hasPermission(LOC_FILTER_CANDIDATES)) {
                            if (!isSatisfylocFilter(
                                    bagA.getSize(),
                                    bagB.getSize()
                                            + tokenFrequencyB.getFrequency(),
                                    matchCount, computedThreshold,
                                    tokenSeenInA, tokenSeenInB)) {
                                break;
                            }
                        }
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
        this.candidateCumulativeTime += (stopTime - startTime);
        return candidate;
    }

    private boolean hasPermission(String caller) {
        return this.locFilter.equals(this.LOC_FILTER_BOTH)
                || this.locFilter.equals(caller);
    }

    /*
     * private boolean incrementItr(int count, int tokenSeenInA, int
     * tokenSeenInB, int prefixSize, TokenFrequency tokenFrequencyA,
     * TokenFrequency tokenFrequencyB, Iterator<TokenFrequency> listBItr,
     * Iterator<TokenFrequency> listAItr) {
     * 
     * count = Math.min(tokenSeenInA, tokenSeenInB); if (count >= prefixSize) {
     * return true; } int globalPositionA =
     * this.globalTokenPositionMap.get(tokenFrequencyA .getToken().getValue());
     * int globalPositionB = this.globalTokenPositionMap.get(tokenFrequencyB
     * .getToken().getValue()); return this .incItr(globalPositionB,
     * globalPositionA, tokenFrequencyB, tokenFrequencyA, listBItr,
     * tokenSeenInA, tokenSeenInB, listAItr);
     * 
     * }
     * 
     * private boolean incItr(int globalPositionB, int globalPositionA,
     * TokenFrequency tokenFrequencyB, TokenFrequency tokenFrequencyA,
     * Iterator<TokenFrequency> listBItr, int tokenSeenInA, int tokenSeenInB,
     * Iterator<TokenFrequency> listAItr) { if (globalPositionB <=
     * globalPositionA) { if (listBItr.hasNext()) { tokenFrequencyB =
     * listBItr.next(); tokenSeenInB += tokenFrequencyB.getFrequency(); } else {
     * return true; } } else { if (listAItr.hasNext()) { tokenFrequencyA =
     * listAItr.next(); tokenSeenInA += tokenFrequencyA.getFrequency(); } else {
     * return true; } } return false; }
     */

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
