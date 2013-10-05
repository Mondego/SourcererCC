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
        this.run = 1;
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
            cd.analysisWriter = Util.openFile("/Users/vaibhavsaini/Dropbox/clonedetection/testinputfiles/ANTItrclonesAnalysis.csv");
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
        try {
            System.out.println("running, please wait...");
            this.cloneHelper.setClonesWriter(Util.openFile("Antclones2.txt"));
            this.cloneHelper.setThreshold(this.threshold);
            Set<Bag> setA = new HashSet<Bag>();
            String projectAfile = "/Users/vaibhavsaini/Dropbox/clonedetection/dataset/ANT-clone-INPUT.txt";
            String projectBfile = "/Users/vaibhavsaini/Dropbox/clonedetection/dataset/ANT-clone-INPUT.txt";// change diskwrites
            this.cloneHelper.parseInputFileAndPopulateSet(projectAfile, setA);
            Set<Bag> setB = new HashSet<Bag>();
            this.cloneHelper.parseInputFileAndPopulateSet(projectBfile, setB);
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
