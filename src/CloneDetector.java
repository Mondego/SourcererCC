import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import utility.Util;

import com.google.gson.Gson;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class CloneDetector {
    private int threshold; // threshold for matching the clones.
    private Bag currentBag; // the bag whose clones we are finding in other set
    private PrintWriter clonesWriter;

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        CloneDetector cd = new CloneDetector();
        PrintWriter outputWriter = null;
        PrintWriter inputSetsWriter = null;
        try {
            outputWriter = Util.openFileToWrite("clones.txt");
            Set<Bag> setA = cd.getTestSet();
            Set<Bag> setB = cd.getTestSet();
            Gson gson = new Gson();
            String setAJsonString = gson.toJson(setA);
            String setBJsonString = gson.toJson(setB);
            inputSetsWriter = Util.openFileToWrite("input.txt");
            Util.writeToFile(inputSetsWriter, setAJsonString, true);
            Util.writeToFile(inputSetsWriter, setBJsonString, true);
            cd.detectClones(setA, setB);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Util.closeOutputFile(outputWriter);
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
     * returns a set of 20 bags
     * 
     * @return Set<Bag>
     */
    private Set<Bag> getTestSet() {
        Set<Bag> set = new HashSet<Bag>();
        for (int i = 0; i < 20; i++) {
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
    private Token getTestToken(int i) {
        return new Token("t" + i);
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
            Token t = this.getTestToken(j);
            bag.put(t, Util.getRandomNumber());
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
            this.currentBag = bagInSetA;
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
                if (count >= this.threshold) {
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
        if (bagA.equals(this.currentBag)) {
            Util.writeToFile(this.clonesWriter, " ," + bagB.toString(), false);
        } else {
            // start a new line
            Util.writeToFile(this.clonesWriter, bagB.toString(), true);
        }
    }

}
