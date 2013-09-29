import java.io.IOException;
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
public class CloneDetector {
    private CloneHelper cloneHelper;

    /**
     * @param cloneHelper
     */
    public CloneDetector(CloneHelper cloneHelper) {
        super();
        this.cloneHelper = cloneHelper;
    }

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        CloneDetector cd = new CloneDetector(new CloneHelper());
        PrintWriter inputSetsWriter = null;
        try {
            cd.cloneHelper.setClonesWriter(Util.openFileToWrite("clones.txt"));
            cd.cloneHelper.setThreshold(.8F);
            Set<Bag> setA = CloneTestHelper.getTestSet(1, 11);
            Set<Bag> setB = CloneTestHelper.getTestSet(11, 21);
            inputSetsWriter = Util.openFileToWrite("input.txt");
            cd.cloneHelper.bookKeepInputs(setA, setB, inputSetsWriter);
            cd.cloneHelper.detectClones(setA, setB);
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

}
