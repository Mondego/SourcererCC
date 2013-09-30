import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utility.Util;

import com.rits.cloning.Cloner;

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

    /**
     * @param cloneHelper
     */
    public CloneDetectorWithFilter(CloneHelper cloneHelper) {
        super();
        this.cloneHelper = cloneHelper;
        this.threshold = .5F;
    }

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        CloneDetectorWithFilter cd = new CloneDetectorWithFilter(
                new CloneHelper());
        PrintWriter inputSetsWriter = null;
        try {
            cd.cloneHelper.setClonesWriter(Util
                    .openFileToWrite("clonesWithFilter.txt"));
            cd.cloneHelper.setThreshold(cd.threshold);
            Set<Bag> setA = CloneTestHelper.getTestSet(1, 11);
             Set<Bag> setB = CloneTestHelper.getTestSet(11, 21);
            cd.setGlobalTokenPositionMap(CloneTestHelper
                    .getGlobalTokenPositionMap(setA, setA)); // input
            inputSetsWriter = Util.openFileToWrite("inputForFilter.txt");
            cd.cloneHelper.bookKeepInputs(setA, setA, inputSetsWriter); // input
            cd.detectClones(setA, setA);// input
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Util.closeOutputFile(cd.cloneHelper.getClonesWriter());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            try {
                Util.closeOutputFile(inputSetsWriter);
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
        List<TokenFrequency> list = new ArrayList<TokenFrequency>(bag);
        if (bag.getId() == 7) {
            System.out.println("before: " + list);
        }

        Collections.sort(list, new Comparator<TokenFrequency>() {
            public int compare(TokenFrequency tfFirst, TokenFrequency tfSecond) {
                return globalTokenPositionMap.get(tfFirst)
                        - globalTokenPositionMap.get(tfSecond);
            }
        });
        if (bag.getId() == 7) {
            System.out.println("after: " + list);
        }
        return list;
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
        return (maxTokenLength + 1)
                - (int) Math.ceil(this.threshold * maxTokenLength);
    }

    /**
     * checks if bagA is a possible candidateClone of bagB
     * 
     * @param bagA
     * @param bagB
     * @return boolean
     */
    private boolean isCandidate(Bag bagA, Bag bagB) {
        int computedThreshold = (int) Math.ceil(this.threshold
                * (Math.max(bagA.getSize(), bagB.getSize())));
        int prefixSize = this.computePrefixSize(computedThreshold);
        System.out.println("prefixSize for " + bagA.getId() + ", "
                + bagB.getId() + " is: " + prefixSize);
        if (prefixSize <= Math.min(bagA.getSize(), bagB.getSize())) {
            List<TokenFrequency> listA = this.convertToSortedList(bagA);
            List<TokenFrequency> listB = this.convertToSortedList(bagB);
            int matched = 0;
            Iterator<TokenFrequency> listBItr = listB.iterator();
            for (TokenFrequency tokenFrequencyA : listA) {
                System.out.println("tokenFrequencyA is " + tokenFrequencyA);
                if (listBItr.hasNext()) {
                    TokenFrequency tokenFrequencyB = listBItr.next();
                    System.out.println("tokenFrequencyB" + tokenFrequencyB);
                    if (tokenFrequencyA.equals(tokenFrequencyB)) {
                        // check the frequency
                        if (tokenFrequencyA.getFrequency() == tokenFrequencyB
                                .getFrequency()) {
                            matched += tokenFrequencyA.getFrequency();
                        } else {

                            matched += Math.min(tokenFrequencyA.getFrequency(),
                                    tokenFrequencyB.getFrequency());

                            if (matched >= prefixSize) {
                                return true;
                            } else {
                                System.out
                                        .println("Rejected: tokens matched but frequencies did not match, matched count is "
                                                + matched
                                                + " , prefixSize is "
                                                + prefixSize);
                                return false;
                            }
                        }
                        if (matched >= prefixSize) {
                            return true;
                        }
                    }
                } else {
                    System.out.println("Rejected: listB.hasNext is false");
                    return false;
                }
            }
        }
        System.out.println("min size is less than prefix size");
        return false;
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
