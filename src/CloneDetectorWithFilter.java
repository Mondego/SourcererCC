import java.io.IOException;
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
    private Map<TokenFrequency, Integer> globalTokenPositionMap; // we will sort
                                                                 // bags using
                                                                 // it.
    private CloneHelper cloneHelper;
    private float threshold; // threshold for matching the clones.e.g. .8 or
                             // .9
    private Map<Bag, List<TokenFrequency>> bagToSortedListMap;

    /**
     * @param cloneHelper
     */
    public CloneDetectorWithFilter(CloneHelper cloneHelper) {
        super();
        this.cloneHelper = cloneHelper;
        this.threshold = .8F;
        bagToSortedListMap = new HashMap<Bag, List<TokenFrequency>>();
    }

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        CloneDetectorWithFilter cd = new CloneDetectorWithFilter(
                new CloneHelper());
        try {
            cd.cloneHelper.setClonesWriter(Util
                    .openFile("clonesWithFilter.txt"));
            cd.cloneHelper.setThreshold(cd.threshold);
            Set<Bag> setA = new HashSet<Bag>();
            String folder = "t3";
            cd.cloneHelper.parseInputFileAndPopulateSet("/Users/vaibhavsaini/Dropbox/clonedetection/testinputfiles/"+folder+"/projectA.txt", setA);
            Set<Bag> setB = new HashSet<Bag>();
            cd.cloneHelper.parseInputFileAndPopulateSet("/Users/vaibhavsaini/Dropbox/clonedetection/testinputfiles/"+folder+"/projectB.txt", setB);
            cd.setGlobalTokenPositionMap(CloneTestHelper
                    .getGlobalTokenPositionMap(setA, setB)); // input
            cd.detectClones(setA, setB);// input
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Util.closeOutputFile(cd.cloneHelper.getClonesWriter());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
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
                if (this.isCandidate(bagInSetA, bagInSetB)) {
                    System.out.println("possible candidates "
                            + bagInSetA.getId() + ", " + bagInSetB.getId());
                    this.cloneHelper.detectClones(bagInSetA, bagInSetB);
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
        if (this.bagToSortedListMap.containsKey(bag)) {
            return bagToSortedListMap.get(bag);
        } else {
            List<TokenFrequency> list = new ArrayList<TokenFrequency>(bag);
            if (bag.getId() == 17) {
                System.out.println("before: " + list);
            }

            Collections.sort(list, new Comparator<TokenFrequency>() {
                public int compare(TokenFrequency tfFirst,
                        TokenFrequency tfSecond) {
                    return globalTokenPositionMap.get(tfFirst)
                            - globalTokenPositionMap.get(tfSecond);
                }
            });
            if (bag.getId() == 17) {
                System.out.println("after: " + list);
            }
            this.bagToSortedListMap.put(bag, list);
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
        System.out.println("t is " + maxTokenLength);
        System.out.println("threshld is " + this.threshold);
        double x = this.threshold * maxTokenLength;
        System.out.println("x is " + x);
        int thetaT = (int) Math.ceil(x);
        System.out.println("thetaT is " + thetaT);
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
        int maxLength = Math.max(bagA.getSize(), bagB.getSize());
        int prefixSize = this.computePrefixSize(maxLength);
        System.out.println("prefixSize for " + bagA.getId() + ", "
                + bagB.getId() + " is: " + prefixSize);
        if (prefixSize <= Math.min(bagA.getSize(), bagB.getSize())) {
            List<TokenFrequency> listA = this.convertToSortedList(bagA);
            List<TokenFrequency> listB = this.convertToSortedList(bagB);
            Iterator<TokenFrequency> listAItr = listA.iterator();
            Iterator<TokenFrequency> listBItr = listB.iterator();
            int count = 0;
            TokenFrequency tokenFrequencyA = listAItr.next();
            TokenFrequency tokenFrequencyB = listBItr.next();
            while (true) {
                if (tokenFrequencyB.equals(tokenFrequencyA)) {
                    return true;
                } else {
                    int globalPositionA = this.globalTokenPositionMap
                            .get(tokenFrequencyA);
                    int globalPositionB = this.globalTokenPositionMap
                            .get(tokenFrequencyB);
                    if (globalPositionB <= globalPositionA) {
                        count += tokenFrequencyB.getFrequency();
                        if (count >= prefixSize) {
                            return false;
                        }
                        if (listBItr.hasNext()) {
                            tokenFrequencyB = listBItr.next();
                        } else {
                            return false;
                        }
                    } else {
                        count += tokenFrequencyA.getFrequency();
                        if (count >= prefixSize) {
                            return false;
                        }
                        if (listAItr.hasNext()) {
                            tokenFrequencyA = listAItr.next();
                        } else {
                            return false;
                        }
                    }
                }
            }

        } else {
            System.out.println("min size is less than prefix size");
            return false;
        }
    }

    /**
     * @return the globalTokenPositionMap
     */
    public Map<TokenFrequency, Integer> getGlobalTokenPositionMap() {
        return globalTokenPositionMap;
    }

    /**
     * @param globalTokenPositionMap
     *            the globalTokenPositionMap to set
     */
    public void setGlobalTokenPositionMap(
            Map<TokenFrequency, Integer> globalTokenPositionMap) {
        this.globalTokenPositionMap = globalTokenPositionMap;
    }
}
