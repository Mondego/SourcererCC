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
    private String filePrefix;

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
        CloneDetector cd = new CloneDetector();
        if(args.length>0){
            cd.filePrefix=args[0];
            if(args.length==2){
                try{
                    cd.run=Integer.parseInt(args[1]);
                }catch(NumberFormatException e){
                    System.out.println("Not a valid number. 2nd argument should be an Int");
                    System.exit(1);
                }
            }
        }else{
            System.out.println("Please provide inputfile prefix, e.g. ANT,cocoon,hadoop.");
            System.exit(1);
        }
        try {
            String filename = "output/"+cd.filePrefix+"clonesAnalysis_NO_FILTER.csv";
            System.out.println("writing in file : "+ filename);
            cd.analysisWriter = Util.openFile(filename,true);
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
            this.cloneHelper.setClonesWriter(Util.openFile("output/"+this.filePrefix+"clones2_NO_FILTER.txt",false));
            this.cloneHelper.setThreshold(this.threshold);
            Set<Bag> setA = new HashSet<Bag>();
            String projectAfile = "input/dataset/"+this.filePrefix+"-clone-INPUT.txt";
            String projectBfile = "input/dataset/"+this.filePrefix+"-clone-INPUT.txt";// change diskwrites
            this.cloneHelper.parseInputFileAndPopulateSet(projectAfile, setA);
            Set<Bag> setB = new HashSet<Bag>();
            this.cloneHelper.parseInputFileAndPopulateSet(projectBfile, setB);
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
