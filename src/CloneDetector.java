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
            cd.cloneHelper.setClonesWriter(Util.openFile("clones.txt"));
            cd.cloneHelper.setThreshold(.8F);
            Set<Bag> setA = new HashSet<Bag>();
            String folder ="t3"; 
            cd.cloneHelper.parseInputFileAndPopulateSet("/Users/vaibhavsaini/Documents/codetime/repo/ast/output/clone-INPUT.txt", setA);
            Set<Bag> setB = new HashSet<Bag>();
            cd.cloneHelper.parseInputFileAndPopulateSet("/Users/vaibhavsaini/Documents/codetime/repo/ast/output/clone-INPUT.txt", setB);
            inputSetsWriter = Util.openFile("/Users/vaibhavsaini/Dropbox/clonedetection/testinputfiles/"+folder+"/intInput.txt");
            cd.cloneHelper.bookKeepInputs(setA, setB, inputSetsWriter);
            cd.cloneHelper.detectClones(setA, setB); // input
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Util.closeOutputFile(cd.cloneHelper.getClonesWriter());
                Util.closeOutputFile(inputSetsWriter);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
