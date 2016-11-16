package com.mondego.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;

import com.mondego.utility.Util;

public class FileParser {

    long startFileIdCounter = 0;
    long endFileIdCounter = 0;
    long project_id = 0l;
    public static final int MIN_TOKEN_IN_FILE = 65;
    Writer parsedFileWriter = null;
    Writer idFileWriter = null;
    String parsedFilePath = "";
    String idFilePath = "";
    String datasetPath;
    String bookkeepingPath;
    String idGenFile;
    String idGenFileStatus;
    Set<String> processedProjects;
    Set<String> processedFiles;
    long maxIdProcessed;
    String processName;

    public FileParser(String processName) throws IOException {
        this.processName=processName;
        this.datasetPath = "input/dataset";
        this.bookkeepingPath = "input/bookkeeping";
        this.parsedFilePath = this.datasetPath + "/" + processName
                + "_clone-input.txt";
        this.idFilePath = this.bookkeepingPath + "/" + processName
                + "_idFile.txt";
        Util.createDirs(this.datasetPath);
        Util.createDirs(this.bookkeepingPath);
        this.parsedFileWriter = Util.openFile(this.parsedFilePath, true);
        this.idFileWriter = Util.openFile(this.idFilePath, true);
        this.idGenFile = "idgen.txt";
        this.idGenFileStatus = "idgenstatus.txt";
        this.processedProjects = new HashSet<String>();
        this.processedFiles = new HashSet<String>();
        this.maxIdProcessed = 0;
        this.populateProcessedInfo();
        
        System.out.println("calling updateIds");
        this.updateIds();
    }

    private void populateProcessedInfo() throws FileNotFoundException {
        this.processedProjects.add("2120");
        File bookkeepingDir = new File(this.bookkeepingPath);
        if(bookkeepingDir.isDirectory()){
            File[] files = bookkeepingDir.listFiles();
            for (File bkfile : files){
                if (FilenameUtils.getExtension(bkfile.getName()).equals(
                        "txt")) {
                    ReversedLinesFileReader rlfr = null;
                    try {
                        rlfr = new ReversedLinesFileReader(bkfile,4*1024,Charsets.UTF_8);
                        String line;
                        boolean lastLineRead = false;
                        String projectIdToIgnore="";
                        String previousLine="";
                        while ((line = rlfr.readLine()) != null
                                && line.trim().length() > 0) {
                            if(!previousLine.equalsIgnoreCase("")){
                                line = line.trim()+"^M"+previousLine.trim();
                                previousLine="";
                            }
                            String[] info = line.split(",");
                            try{
                                if(lastLineRead && !info[0].equals(projectIdToIgnore)){
                                    // last line alread processed, add project ids from here
                                    this.processedProjects.add(info[0]); // project id
                                }else{
                                    projectIdToIgnore=info[0];
                                    this.processedFiles.add(info[2]); // file name. file id is not deterministic hence not using it.
                                    //System.out.println("projectIdToIgnore, "+projectIdToIgnore);
                                    lastLineRead=true;
                                }
                                long fileIdProcessed = Long.parseLong(info[1]);
                                if(this.maxIdProcessed< fileIdProcessed){
                                    this.maxIdProcessed=fileIdProcessed;
                                }
                                if(this.processedProjects.size()%1000==0){
                                    System.out.println("processedprojects size: "+ this.processedProjects.size());
                                }
                            }catch(ArrayIndexOutOfBoundsException e){
                                previousLine=line;
                                System.out.println("EXCEPTION caught for line: "+ line + ", file: "+ bkfile.getAbsolutePath());
                                //System.out.println("exiting, + "+ this.processName);
                                //System.exit(1);
                            }
                            
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
        }else{
            System.out.println("bookkeeping dir not found, exiting");
            System.exit(1);
        }
        System.out.println("max id processed : "+ this.maxIdProcessed);
        if(!this.isIdGenUpdated()){
            try {
                File file = new File(this.idGenFile);
                FileLock lock = null;
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                FileChannel channel = raf.getChannel();
                try {
                    lock = channel.lock();
                    ByteBuffer outBuffer = ByteBuffer.allocate(8);
                    outBuffer.clear();
                    String endidStr = (this.maxIdProcessed+1) + "";
                    outBuffer.put(endidStr.getBytes());
                    outBuffer.flip();
                    // System.out.println(new String(outBuffer.array()));
                    channel.write(outBuffer, 0);
                    channel.force(false);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        lock.release();
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isIdGenUpdated() throws FileNotFoundException {
        File file = new File(this.idGenFileStatus);
        FileLock lock = null;
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel channel = raf.getChannel();
        try {
            lock = channel.lock();
            long fileSize = channel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            channel.read(buffer);
            String line = new String(buffer.array());
            if(line.trim().equalsIgnoreCase("true")){
                // ignore updating the idgenFile
                return true;
            }else{
                ByteBuffer outBuffer = ByteBuffer.allocate(8);
                outBuffer.clear();
                String statusStr = "true";
                outBuffer.put(statusStr.getBytes());
                outBuffer.flip();
                // System.out.println(new String(outBuffer.array()));
                channel.write(outBuffer, 0);
                channel.force(false);
                return false;
            }
        } catch (IOException e) {
            System.out.println("exiting");
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                lock.release();
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void updateIds() throws FileNotFoundException {

        File file = new File(this.idGenFile);
        FileLock lock = null;
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel channel = raf.getChannel();
        try {
            lock = channel.lock();
            long fileSize = channel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            channel.read(buffer);
            String line = new String(buffer.array());
            //System.out.println("line in updateid: "+ line.trim());
            this.startFileIdCounter = Long.parseLong(line.trim());
            this.endFileIdCounter = this.startFileIdCounter + 250000;
            // System.out.println("start_id: "+ this.startId + ", end_id: "+
            // this.endId);
            ByteBuffer outBuffer = ByteBuffer.allocate(8);
            outBuffer.clear();
            String endidStr = this.endFileIdCounter + "";
            outBuffer.put(endidStr.getBytes());
            outBuffer.flip();
            // System.out.println(new String(outBuffer.array()));
            channel.write(outBuffer, 0);
            channel.force(false);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                lock.release();
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void parseAllFilesInDir(File root) throws FileNotFoundException {
        File[] files = root.listFiles();
        for (File file : files) {
            if(!this.processedFiles.contains(file.getAbsolutePath())){
                if (file.canRead()) {
                    if (file.isFile()) {

                        if (FilenameUtils.getExtension(file.getName()).equals(
                                "java")) {
                            try {
                                //System.out
                                  //      .println("reading file " + file.getName());
                                String fileContent = FileUtils.readFileToString(
                                        file, "utf-8");
                                List<String> tokens = Tokenizer
                                        .processMethodBody(fileContent);
                                if (tokens.size() > MIN_TOKEN_IN_FILE) {
                                    //System.out.println("tokens size of file: "+ file.getAbsolutePath() +", "+tokens.size());
                                    Map<String, Integer> tokenToFrequencyMap = this
                                            .makeTokenToFrequencyMap(tokens);
                                    StringBuilder sb = new StringBuilder();
                                    for (String token : tokenToFrequencyMap
                                            .keySet()) {
                                        sb.append(token + "@@::@@"
                                                + tokenToFrequencyMap.get(token)
                                                + ",");
                                    }
                                    if (sb.length() > 0) {
                                        Util.writeToFile(this.parsedFileWriter,
                                                this.project_id + ","
                                                        + this.startFileIdCounter
                                                        + "@#@", false);
                                        Util.writeToFile(this.parsedFileWriter,
                                                sb.substring(0, sb.length() - 1),
                                                true);
                                    } else {
                                        System.out
                                                .println("No tokens found in file: "
                                                        + file.getAbsolutePath());
                                    }
                                    Util.writeToFile(this.idFileWriter,
                                            this.project_id + ","
                                                    + this.startFileIdCounter + ","
                                                    + file.getAbsolutePath(), true);
                                    this.startFileIdCounter += 1;
                                    if (this.startFileIdCounter == this.endFileIdCounter) {
                                        this.updateIds();
                                    }
                                } else {
                                    //System.out
                                      //      .println("ignoring file, num tokens can not match min token threshold: "
                                        //            + file.getAbsolutePath());
                                }
                            } catch (IOException e) {
                                System.out.println("Error in reading file, "
                                        + file.getAbsolutePath());
                                e.printStackTrace();
                            }
                        }
                    } else if (file.isDirectory()) {
                        this.parseAllFilesInDir(file);
                    }
                    try {
                        this.idFileWriter.flush();
                        this.parsedFileWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("No permission to read : "
                            + file.getAbsolutePath());
                }
            }else{
                //System.out.println("ignoring file, reason: already processed, "+ file.getAbsolutePath());
            }
        }
    }

    private Map<String, Integer> makeTokenToFrequencyMap(List<String> tokens) {
        Map<String, Integer> tokenToFrequencyMap = new HashMap<String, Integer>();
        for (String token : tokens) {
            if (token.trim().length() > 0) {
                if (tokenToFrequencyMap.containsKey(token)) {
                    tokenToFrequencyMap.put(token,
                            tokenToFrequencyMap.get(token) + 1);
                } else {
                    tokenToFrequencyMap.put(token, 1);
                }
            }
        }
        return tokenToFrequencyMap;
    }

    private void traverseProjectPath(String filename) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    filename), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                line = line.trim();
                String[] projectInfo = line.split(",");
                try {
                    String project_id_str = projectInfo[0].trim();
                    if(!this.processedProjects.contains(project_id_str)){
                        this.project_id = Long.parseLong(project_id_str);
                        System.out.println("reading project: " + line);
                        String content_folder = projectInfo[1].trim()
                                + File.separator + "content";
                        String latest_folder = projectInfo[1].trim()
                                + File.separator + "latest";
                        File rootDir = new File(content_folder);
                        if (rootDir.isDirectory()) {
                            if (rootDir.canRead()) {
                                this.parseAllFilesInDir(rootDir);
                            } else {
                                System.out.println("No permission to read Dir: "
                                        + rootDir.getAbsolutePath());
                            }
                        } else {
                            rootDir = new File(latest_folder);
                            if (rootDir.isDirectory()) {
                                if (rootDir.canRead()) {
                                    this.parseAllFilesInDir(rootDir);
                                } else {
                                    System.out
                                            .println("No permission to read Dir: "
                                                    + rootDir.getAbsolutePath());
                                }
                            } else {
                                // ignore
                            }
                        }
                    }else{
                        //System.out.println("ignoring project, "+ project_id_str+ ", reason: already processed");
                    }
                    
                } catch (NumberFormatException e) {
                    // ignore this project
                    System.out
                            .println("invalid line in projects file, " + line);
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        String projectsFilePath = args[0];
        try {
            FileParser fp = new FileParser(args[0]);
            fp.traverseProjectPath(projectsFilePath);
            Util.closeOutputFile(fp.idFileWriter);
            Util.closeOutputFile(fp.parsedFileWriter);
            System.out.println("Done for process, "+ args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
