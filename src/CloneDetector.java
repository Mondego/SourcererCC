import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import utility.Util;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class CloneDetector {
    private float threshold; // threshold for matching the clones.e.g. 80% or
                             // 90%
    private Bag previousBag; // the previous bag whose clones we were finding in
                             // other set
    private PrintWriter clonesWriter; // writer to write the output

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        CloneDetector cd = new CloneDetector();
        cd.threshold = .8F;
        PrintWriter inputSetsWriter = null;
        try {
            cd.clonesWriter = Util.openFileToWrite("clones.txt");
            Set<Bag> setA = cd.getTestSet(1, 11);
            Set<Bag> setB = cd.getTestSet(11, 21);
            inputSetsWriter = Util.openFileToWrite("input.txt");
            String setAString = cd.Stringify(setA);
            String setBString = cd.Stringify(setB);
            Util.writeToFile(inputSetsWriter, setAString, true);
            Util.writeToFile(inputSetsWriter,
                    "********************************", true);
            Util.writeToFile(inputSetsWriter,
                    "********************************", true);
            Util.writeToFile(inputSetsWriter,
                    "********************************", true);
            Util.writeToFile(inputSetsWriter, setBString, true);
            cd.detectClones(setA, setB);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Util.closeOutputFile(cd.clonesWriter);
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

    private String Stringify(Set<Bag> inputSet) {
        String returnString = "";
        for (Bag bag : inputSet) {
            returnString += bag.toString();
        }
        return returnString;
    }

    /**
     * returns a set of 10 bags
     * 
     * @return Set<Bag>
     */
    private Set<Bag> getTestSet(int start, int stop) {
        Set<Bag> set = new HashSet<Bag>();
        for (int i = start; i < stop; i++) {
            set.add(this.getTestBag(i));
        }
        return set;
    }

    /**
     * 
     * @param i
     *            integer to create value of a token
     * @return Token
     */
    private Token getTestToken() {
        return new Token("t" + Util.getRandomNumber(21, 1));
    }

    /**
     * creates and return a bag of 10 tokens
     * 
     * @param i
     *            id of the bag
     * @return Bag
     */
    private Bag getTestBag(int i) {
        Bag bag = new Bag(i);
        for (int j = 0; j < 10; j++) {
            Token t = this.getTestToken();
            bag.put(t, Util.getRandomNumber(10, 1));
        }
        return bag;
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
    private void detectClones(Bag bagA, Bag bagB) {
        int computedThreshold = (int) Math.ceil(this.threshold
                * (Math.max(bagA.size(), bagB.size()))); // integer value of
                                                         // threshold.
        // iterate on keys of bagA
        int count = 0;
        Set<Token> keysInBagA = bagA.keySet();
        for (Token tokenA : keysInBagA) {
            // search this token in bagB
            if (null != bagB.get(tokenA)) {
                // token found.
                int frequencyTokenA = bagA.get(tokenA).intValue();
                int frequencyTokenB = bagB.get(tokenA).intValue();
                count += Math.min(frequencyTokenA, frequencyTokenB);
                if (count >= computedThreshold) {
                    // report clone.
                    this.reportClone(bagA, bagB);
                    break; // no need to iterate on other keys clone has been
                           // found
                }
            }
        }
    }

    /**
     * outputs the bagB as a clone of bagA
     * 
     * @param bagA
     * @param bagB
     */
    private void reportClone(Bag bagA, Bag bagB) {
        if (bagA.equals(this.previousBag)) {
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
        this.previousBag = bagA;
    }

}
