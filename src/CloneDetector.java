import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private float threshold;
    int run;
    private PrintWriter analysisWriter;

    /**
     * @param cloneHelper
     */
    public CloneDetector() {
        super();
        this.threshold = .8F;
        this.run = 1000;
    }

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        String folder ="t3";
        CloneDetector cd = new CloneDetector();
        try {
            cd.analysisWriter = Util.openFile("/Users/vaibhavsaini/Dropbox/clonedetection/testinputfiles/clonesAnalysis.csv");
            String header = "detect_clones_time, total_comparision, num_clones_detected";
            Util.writeToFile(cd.analysisWriter, header, true);
            for(int i=0;i<cd.run;i++){
                CloneHelper cloneHelper = new CloneHelper();
                cd.cloneHelper = cloneHelper;
                cd.runExperiment();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            Util.closeOutputFile(cd.analysisWriter);
        }
    }
    
    private void runExperiment(){
        PrintWriter inputSetsWriter = null;
        try {
            this.cloneHelper.setClonesWriter(Util.openFile("clones.txt"));
            this.cloneHelper.setThreshold(this.threshold);
            Set<Bag> setA = new HashSet<Bag>();
             
            this.cloneHelper.parseInputFileAndPopulateSet("/Users/vaibhavsaini/Documents/codetime/repo/ast/output/clone-INPUT.txt", setA);
            Set<Bag> setB = new HashSet<Bag>();
            this.cloneHelper.parseInputFileAndPopulateSet("/Users/vaibhavsaini/Documents/codetime/repo/ast/output/clone-INPUT.txt", setB);
            //inputSetsWriter = Util.openFile("/Users/vaibhavsaini/Dropbox/clonedetection/testinputfiles/"+folder+"/intInput.txt");
            //this.cloneHelper.bookKeepInputs(setA, setB, inputSetsWriter);
            long start_time = System.currentTimeMillis();
            this.cloneHelper.detectClones(setA, setB); // input
            long end_time = System.currentTimeMillis();
            System.out.println("time in milliseconds :"+(end_time-start_time));
            StringBuilder sb = new StringBuilder();
            sb.append(end_time-start_time+",");
            System.out.println("comparisions :"+this.cloneHelper.getComparisions());
            sb.append(this.cloneHelper.getComparisions()+",");
            sb.append(this.cloneHelper.getNumClonesFound());
            Util.writeToFile(this.analysisWriter, sb.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Util.closeOutputFile(this.cloneHelper.getClonesWriter());
                Util.closeOutputFile(inputSetsWriter);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * @return the cloneHelper
     */
    public CloneHelper getCloneHelper() {
        return cloneHelper;
    }

    /**
     * @param cloneHelper the cloneHelper to set
     */
    public void setCloneHelper(CloneHelper cloneHelper) {
        this.cloneHelper = cloneHelper;
    }
}
