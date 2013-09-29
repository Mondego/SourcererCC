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
        this.threshold = .8F;
    }

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        CloneDetectorWithFilter cd = new CloneDetectorWithFilter(new CloneHelper());
        PrintWriter inputSetsWriter = null;
        try {
            cd.cloneHelper.setClonesWriter(Util.openFileToWrite("clonesWithFilter.txt"));
            cd.cloneHelper.setThreshold(cd.threshold);
            Set<Bag> setA = CloneTestHelper.getTestSet(1, 11);
            Set<Bag> setB = CloneTestHelper.getTestSet(11, 21);
            cd.setGlobalTokenPositionMap(CloneTestHelper.getGlobalTokenPositionMap(setA,setB));
            inputSetsWriter = Util.openFileToWrite("inputForFilter.txt");
            cd.cloneHelper.bookKeepInputs(setA, setB, inputSetsWriter);
            cd.detectClones(setA, setB);
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
                if (this.isCandidate(bagInSetB, bagInSetB)) {
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
        Collections.sort(list, new Comparator<TokenFrequency>() {
            public int compare(TokenFrequency tfFirst, TokenFrequency tfSecond) {
                return globalTokenPositionMap.get(tfFirst)
                        - globalTokenPositionMap.get(tfSecond);
            }
        });
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
    private int getMustMatchNumber(List<TokenFrequency> listA,
            List<TokenFrequency> listB) {
        int maxToeknLength = Math.max(listA.size(), listB.size());
        // 5-i+1 = .8*5
        // i = maxToeknLength+1 -.8*maxToeknLength
        return (maxToeknLength + 1) - (int) (this.threshold * maxToeknLength);
    }

    /**
     * checks if bagA is a possible candidateClone of bagB
     * 
     * @param bagA
     * @param bagB
     * @return boolean
     */
    private boolean isCandidate(Bag bagA, Bag bagB) {
        List<TokenFrequency> listA = this.convertToSortedList(bagA);
        List<TokenFrequency> listB = this.convertToSortedList(bagB);
        int threshold = this.getMustMatchNumber(listA, listB);
        Iterator<TokenFrequency> listBItr = listB.iterator();
        int matched = 0;
        for (TokenFrequency tokenFrequencyA : listA) {
            TokenFrequency tokenFrequencyB = listBItr.next();
            if (tokenFrequencyA.equals(tokenFrequencyB)) {
                // check the frequency
                if (tokenFrequencyA.getFrequency() == tokenFrequencyB
                        .getFrequency()) {
                    matched += tokenFrequencyA.getFrequency();
                } else {
                    matched += Math.min(tokenFrequencyA.getFrequency(),
                            tokenFrequencyB.getFrequency());
                    return matched <= threshold;
                }
                if (matched <= threshold) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the globalTokenPositionMap
     */
    public Map<TokenFrequency, Integer> getGlobalTokenPositionMap() {
        return globalTokenPositionMap;
    }

    /**
     * @param globalTokenPositionMap the globalTokenPositionMap to set
     */
    public void setGlobalTokenPositionMap(
            Map<TokenFrequency, Integer> globalTokenPositionMap) {
        this.globalTokenPositionMap = globalTokenPositionMap;
    }
}
