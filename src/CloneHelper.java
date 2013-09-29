import java.io.PrintWriter;
import java.util.Set;

import utility.Util;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class CloneHelper {
    private PrintWriter clonesWriter; // writer to write the output
    private Bag previousBag; // the previous bag whose clones we were finding in
                             // other set
    private float threshold; // threshold for matching the clones.e.g. 80% or
                             // 90%

    /**
     * outputs the bagB as a clone of bagA
     * 
     * @param bagA
     * @param bagB
     */
    public void reportClone(Bag bagA, Bag bagB, Bag previousBag) {
        if (bagA.equals(previousBag)) {
            System.out.println("equal");
            Util.writeToFile(this.clonesWriter, " ," + bagB.getId(), false);
        } else {
            // start a new line
            System.out.println("different");
            Util.writeToFile(this.clonesWriter, "", true);
            Util.writeToFile(this.clonesWriter,
                    "Clones of Bag " + bagA.getId(), true);
            Util.writeToFile(this.clonesWriter, bagB.getId() + "", false);
        }
    }

    /**
     * @return the threshold
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     * @param threshold
     *            the threshold to set
     */
    public void setThreshold(float threshold) {
        this.threshold = threshold;
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
    public void detectClones(Set<Bag> setA, Set<Bag> setB) {
        // iterate on setA
        for (Bag bagInSetA : setA) {
            // compare this map with every map in setB and report clones
            // iterate on setB
            for (Bag bagInSetB : setB) {
                this.detectClones(bagInSetA, bagInSetB);
            }
        }
    }

    /**
     * the method compares two maps and reports if they are clones.
     * 
     * @param bagA
     *            map of token as key and it's frequency in a method as value
     * @param bagB
     *            map of token as key and it's frequency in a method as value
     */
    public void detectClones(Bag bagA, Bag bagB) {
        int computedThreshold = (int) Math.ceil(this.threshold
                * (Math.max(bagA.size(), bagB.size()))); // integer value of
                                                         // threshold.
        // iterate on bagA
        int count = 0;
        for (TokenFrequency tokenFrequencyA : bagA) {
            // search this token in bagB
            if (bagB.contains(tokenFrequencyA)) {
                // token found.
                TokenFrequency tokenFrequencyB = bagB.get(tokenFrequencyA);
                count += Math.min(tokenFrequencyA.getFrequency(),
                        tokenFrequencyB.getFrequency());
                if (count >= computedThreshold) {
                    // report clone.
                    this.reportClone(bagA, bagB, this.previousBag);
                    this.previousBag = bagA;
                    break; // no need to iterate on other keys clone has been
                           // found
                }
            }
        }
    }

    /**
     * returns the string for the insuptSet.
     * 
     * @param inputSet
     * @return String
     */
    public String stringify(Set<Bag> inputSet) {
        String returnString = "";
        for (Bag bag : inputSet) {
            returnString += bag.toString();
        }
        return returnString;
    }

    /**
     * @return the clonesWriter
     */
    public PrintWriter getClonesWriter() {
        return clonesWriter;
    }

    /**
     * @param clonesWriter the clonesWriter to set
     */
    public void setClonesWriter(PrintWriter clonesWriter) {
        this.clonesWriter = clonesWriter;
    }

    public void bookKeepInputs(Set<Bag> setA, Set<Bag> setB,
            PrintWriter inputSetsWriter) {
        String setAString = this.stringify(setA);
        String setBString = this.stringify(setB);
        Util.writeToFile(inputSetsWriter, setAString, true);
        Util.writeToFile(inputSetsWriter, "********************************",
                true);
        Util.writeToFile(inputSetsWriter, "********************************",
                true);
        Util.writeToFile(inputSetsWriter, "********************************",
                true);
        Util.writeToFile(inputSetsWriter, setBString, true);
    }

}
