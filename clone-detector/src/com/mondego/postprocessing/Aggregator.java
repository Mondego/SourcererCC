package com.mondego.postprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import com.mondego.utility.Util;

public class Aggregator {
    private String project;
    private Writer summryWriter;
    private String inputFile;
    private boolean isFilter = false;
    private int readings = 0;
    private double time_with_filter=0;
    private long comparisions_with_filter= 0;
    private float threshold=0;
    private int clones_count=0;
    private double time_without_filter=0;
    private long comparisions_without_filter=0;
    private double avg_comparisions_with_filter=0;
    private double avg_comparisions_without_filter=0;
    private double avg_time_with_filter=0;
    private double avg_time_without_filter=0;
    private String th; // args[1]
    private String output;
    /**
     * @param args
     */
    public static void main(String args[]) {
        Aggregator aggregator = new Aggregator();
        if (args.length > 0) {
            aggregator.project = args[0];
            aggregator.th = args[1];
            aggregator.output = "output"+aggregator.th;
        } else {
            System.out
                    .println("Please provide inputfile prefix, e.g. ANT,cocoon,hadoop.");
            System.exit(1);
        }
        try {
            Util.createDirs(aggregator.output);
            aggregator.inputFile = aggregator.output+"/" + aggregator.project
                    + "clonesAnalysis_WITH_FILTER.csv";
            String filename = aggregator.output+"/summary.csv";
            File file = new File(filename);
            boolean skipHeader = false;
            if (file.exists()) {
                skipHeader = true;
            }
            aggregator.summryWriter = Util.openFile(filename, true);
            if (!skipHeader) {
                String header = "project,time_with_filter, time_without_filter,comparision_with_filter,comparision_without_filter,clones_count,threshold,numCandidates,numPairs";
                Util.writeToFile(aggregator.summryWriter, header, true);
            }
            aggregator.inputFile = aggregator.output+"/" + aggregator.project
                    + "clonesAnalysis_WITH_FILTER.csv";
            System.out.println("reading file : " + aggregator.inputFile);
            File input = new File(aggregator.inputFile);
            if (!input.exists()) {
                System.out.println("Exiting. File not found : "
                        + aggregator.inputFile);
                System.exit(1);
            }
            aggregator.isFilter = true;
            aggregator.processInputFile();
            aggregator.avg_comparisions_with_filter=aggregator.comparisions_with_filter/aggregator.readings;
            aggregator.avg_time_with_filter=aggregator.time_with_filter/aggregator.readings;
            aggregator.readings=0;
            aggregator.inputFile = aggregator.output+"/" + aggregator.project
                    + "clonesAnalysis_NO_FILTER.csv";
            
            System.out.println("reading file : " + aggregator.inputFile);
            input = new File(aggregator.inputFile);
            if (!input.exists()) {
                System.out.println("File not found : "
                        + aggregator.inputFile);
                //System.exit(1);
            }else{
                aggregator.isFilter = false;
                aggregator.processInputFile();
                aggregator.avg_comparisions_without_filter=aggregator.comparisions_without_filter/aggregator.readings;
                aggregator.avg_time_without_filter=aggregator.time_without_filter/aggregator.readings;
            }
            aggregator.writeOutput();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            Util.closeOutputFile(aggregator.summryWriter);
        }
    }

    private void processInputFile() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.inputFile));
            String line;
            br.readLine(); // ignore first line, it's header
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                this.processRow(line);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void processRow(String line) {
        this.readings++;
        String[] cells = line.split(",");
        if(this.isFilter){
            this.time_with_filter += Double.parseDouble(cells[1]);
            this.comparisions_with_filter += Long.parseLong(cells[4]);
            this.threshold = Float.parseFloat(cells[7]);
            this.clones_count= Integer.parseInt(cells[5]);
        }else{
            this.time_without_filter +=Double.parseDouble(cells[0]);
            this.comparisions_without_filter += Long.parseLong(cells[1]);
        }
    }
    
    private void writeOutput(){
        //String header = "project,time_with_filter, time_without_filter," + "comparision_with_filter,comparision_without_filter,clones_count,threshold";
        StringBuilder sb = new StringBuilder();
        sb.append(this.project+",");
        sb.append(this.avg_time_with_filter+",");
        sb.append(this.avg_time_without_filter+",");
        sb.append(this.avg_comparisions_with_filter+",");
        sb.append(this.avg_comparisions_without_filter+",");
        sb.append(this.clones_count+",");
        sb.append(this.threshold);
        Util.writeToFile(this.summryWriter, sb.toString(), true);
    }

}
