package noindex;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import models.Bag;
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
    private String th; // args[1]
    private PrintWriter analysisWriter;
    private String filePrefix;
    private boolean useJaccardSimilarity;

    /**
     * @param cloneHelper
     */
    public CloneDetector() {
        super();
        this.threshold = .8F;
        this.useJaccardSimilarity=false;
        
    }

    /**
     * main method
     * 
     * @param args
     */
    public static void main(String args[]) {
        CloneDetector cd = new CloneDetector();
        if (args.length > 0) {
            cd.filePrefix = args[0];
            cd.threshold = Float.parseFloat(args[1]) / 10;
            cd.th = args[1];
            cd.useJaccardSimilarity=Boolean.parseBoolean(args[2]);
        } else {
            System.out
                    .println("Please provide inputfile prefix, e.g. ANT,cocoon,hadoop.");
            System.exit(1);
        }
        try {
            System.out.println("***********" + System.getProperty("user.dir"));
            Util.createDirs("output"+cd.th);
            String filename = "output"+cd.th+"/" + cd.filePrefix
                    + "clonesAnalysis_NO_FILTER.csv";
            System.out.println("writing in file : " + filename);
            File file = new File(filename);
            boolean skipHeader = false;
            if (file.exists()) {
                skipHeader = true;
            }
            cd.analysisWriter = Util.openFile(filename, true);
            if (!skipHeader) {
                String header = "detect_clones_time,"
                        + " total_comparision, "
                        + "num_clones_detected,threshold,"
                        + "isJaccardEnabled";
                Util.writeToFile(cd.analysisWriter, header, true);
            }
            CloneHelper cloneHelper = new CloneHelper();
            cd.cloneHelper = cloneHelper;
            cd.runExperiment();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            Util.closeOutputFile(cd.analysisWriter);
        }
    }

    private void runExperiment() {
        try {
            System.out.println("running, please wait...");
            this.cloneHelper.setClonesWriter(Util.openFile("output"+this.th+"/"
                    + this.filePrefix + "clones_NO_FILTER.txt", false));
            this.cloneHelper.setThreshold(this.threshold);
            Set<Bag> setA = new HashSet<Bag>();
            String projectAfile = "input/dataset/" + this.filePrefix
                    + "-clone-INPUT.txt";
            String projectBfile = "input/dataset/" + this.filePrefix
                    + "-clone-INPUT.txt";// change diskwrites
            this.cloneHelper.parseInputFileAndPopulateSet(new File(projectAfile), setA);
            Set<Bag> setB = new HashSet<Bag>();
            this.cloneHelper.parseInputFileAndPopulateSet(new File(projectBfile), setB);
            long start_time = System.currentTimeMillis();

            this.cloneHelper.detectClones(setA, setB,this.useJaccardSimilarity); // input

            long end_time = System.currentTimeMillis();
            System.out.println("time in milliseconds :"
                    + (end_time - start_time));
            StringBuilder sb = new StringBuilder();
            sb.append(end_time - start_time + ",");
            System.out.println("comparisions :"
                    + this.cloneHelper.getComparisions());
            sb.append(this.cloneHelper.getComparisions() + ",");
            sb.append(this.cloneHelper.getNumClonesFound() + ",");
            System.out.println("threshold set to : " + this.threshold);
            sb.append(this.threshold + ",");
            sb.append(this.useJaccardSimilarity);
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
     * @param cloneHelper
     *            the cloneHelper to set
     */
    public void setCloneHelper(CloneHelper cloneHelper) {
        this.cloneHelper = cloneHelper;
    }
}
