package com.mondego.postprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.mondego.utility.Util;

public class SummaryProcessor {
    int INDEX_PROJECT_NAME = 0;
    int INDEX_TIME_WITH_FILTER = 1;
    int INDEX_COMPARISION_WITH_FILTER = 3;
    int INDEX_CLONES_COUNT = 5;
    int INDEX_THRESHOLD = 6;
    String TIME_WITH_FILTER = "TIME_WITH_FILTER";
    String COMPARISION_WITH_FILTER = "COMPARISION_WITH_FILTER";
    String CLONES_COUNT = "CLONES_COUNT";
    String THRESHOLD = "THRESHOLD";
    String inputDir;
    private Writer processedSummryWriter_time;
    private Writer processedSummryWriter_comparisions;
    private Writer processedSummryWriter_clones;
    Map<String, SummaryProcessor.Project> projects;

    public SummaryProcessor() throws IOException {
        this.projects = new HashMap<String, SummaryProcessor.Project>();
        this.inputDir = "output";
        Util.createDirs("summaryFolder");
        this.processedSummryWriter_time = Util.openFile(
                "summaryFolder/processedSummary_time.csv", true);
        this.processedSummryWriter_comparisions = Util.openFile(
                "summaryFolder/processedSummary_comparisions.csv", true);
        this.processedSummryWriter_clones = Util.openFile(
                "summaryFolder/processedSummary_clones.csv", true);
    }

    private void process() {
        float i = 7.5f;
        int j = 8;
        boolean isThisInt = false;
        String threshold = "";
        while (true) {
            String name = null;
            if (isThisInt) {
                name = this.inputDir + j;
                threshold = j + "";
                j = j + 1;
                isThisInt = false;
            } else {
                name = this.inputDir + i;
                threshold = i + "";
                i = i + 1;
                isThisInt = true;
            }
            String filename = name + "/summary.csv";
            this.processFile(filename, threshold);
            if (j > 10) {
                break;
            }
        }
        this.writeOutput();
        System.out.println("Done");
    }

    private void writeOutput() {
        String header = "project_name,7.5, 8," + "8.5,9,9.5,10";
        
        Util.writeToFile(this.processedSummryWriter_time, header, true);
        for (Project project : this.projects.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(project.name + ",");
            sb.append(project.thresholdToAttribtueMap.get("7.5").get(
                    this.TIME_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("8").get(
                    this.TIME_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("8.5").get(
                    this.TIME_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("9").get(
                    this.TIME_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("9.5").get(
                    this.TIME_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("10").get(
                    this.TIME_WITH_FILTER)
                    + ",");
            System.out.println("hello " + sb);
            Util.writeToFile(this.processedSummryWriter_time, sb.toString(),
                    true);
        }
        Util.closeOutputFile(this.processedSummryWriter_time);

        // comparisions
        Util.writeToFile(this.processedSummryWriter_comparisions, header, true);
        for (Project project : this.projects.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(project.name + ",");
            sb.append(project.thresholdToAttribtueMap.get("7.5").get(
                    this.COMPARISION_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("8").get(
                    this.COMPARISION_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("8.5").get(
                    this.COMPARISION_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("9").get(
                    this.COMPARISION_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("9.5").get(
                    this.COMPARISION_WITH_FILTER)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("10").get(
                    this.COMPARISION_WITH_FILTER)
                    + ",");
            Util.writeToFile(this.processedSummryWriter_comparisions,
                    sb.toString(), true);

        }
        Util.closeOutputFile(this.processedSummryWriter_comparisions);
        // clones

        
        Util.writeToFile(this.processedSummryWriter_clones, header, true);
        for (Project project : this.projects.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(project.name + ",");
            sb.append(project.thresholdToAttribtueMap.get("7.5").get(
                    this.CLONES_COUNT)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("8").get(
                    this.CLONES_COUNT)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("8.5").get(
                    this.CLONES_COUNT)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("9").get(
                    this.CLONES_COUNT)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("9.5").get(
                    this.CLONES_COUNT)
                    + ",");
            sb.append(project.thresholdToAttribtueMap.get("10").get(
                    this.CLONES_COUNT)
                    + ",");

            Util.writeToFile(this.processedSummryWriter_clones, sb.toString(),
                    true);

        }
        Util.closeOutputFile(this.processedSummryWriter_clones);
    }

    private void processFile(String filename, String threshold) {
        System.out.println("processing file: " + filename + " threshold is "
                + threshold);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            br.readLine(); // ignore first line, it's header
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                this.processRow(line, threshold);
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

    private void processRow(String line, String threshold) {
        String[] cells = line.split(",");
        String projectName = cells[this.INDEX_PROJECT_NAME];
        Project project = null;
        if (this.projects.containsKey(projectName)) {
            project = this.projects.get(projectName);
        } else {
            project = new Project();
            project.name = projectName;
            this.projects.put(projectName, project);
        }
        Map<String, String> properties = null;
        if (project.thresholdToAttribtueMap.containsKey(threshold)) {
            properties = project.thresholdToAttribtueMap.get(threshold);
        } else {
            properties = new HashMap<String, String>();
            project.thresholdToAttribtueMap.put(threshold, properties);

        }
        properties.put(this.TIME_WITH_FILTER,
                cells[this.INDEX_TIME_WITH_FILTER]);
        properties.put(this.COMPARISION_WITH_FILTER,
                cells[this.INDEX_COMPARISION_WITH_FILTER]);
        properties.put(this.CLONES_COUNT, cells[this.INDEX_CLONES_COUNT]);
    }

    class Project {
        String name;
        Map<String, Map<String, String>> thresholdToAttribtueMap;

        public Project() {
            this.thresholdToAttribtueMap = new HashMap<String, Map<String, String>>();
        }
    }

    public static void main(String[] args) throws IOException {
        SummaryProcessor processor = new SummaryProcessor();
        processor.process();
    }
}
