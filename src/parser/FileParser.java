package parser;

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
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import utility.Util;

public class FileParser {

    long startFileIdCounter = 0;
    long endFileIdCounter = 0;
    long project_id = 0l;
    public static final int MIN_TOKEN_IN_FILE = 0;
    Writer parsedFileWriter = null;
    Writer idFileWriter = null;
    String parsedFilePath = "";
    String idFilePath = "";
    String datasetPath;
    String bookkeepingPath;
    String idGenFile;

    public FileParser(String processName) throws IOException {
        this.datasetPath = "input/dataset";
        this.bookkeepingPath = "input/bookkeeping";
        this.parsedFilePath = this.datasetPath +"/"+ processName+ "_clone-input.txt";
        this.idFilePath = this.bookkeepingPath + "/"+ processName+ "_idFile.txt";
        Util.createDirs(this.datasetPath);
        Util.createDirs(this.bookkeepingPath);
        this.parsedFileWriter = Util.openFile(this.parsedFilePath, true);
        this.idFileWriter = Util.openFile(this.idFilePath, true);
        this.idGenFile="idgen.txt";
        this.updateIds();
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
            this.startFileIdCounter = Long.parseLong(line.trim());
            this.endFileIdCounter = this.startFileIdCounter+ 100000;
            //System.out.println("start_id: "+ this.startId + ", end_id: "+ this.endId);
            ByteBuffer outBuffer = ByteBuffer.allocate(8);
            outBuffer.clear();
            String endidStr= this.endFileIdCounter+"";
            outBuffer.put(endidStr.getBytes());
            outBuffer.flip();
            //System.out.println(new String(outBuffer.array()));
            channel.write(outBuffer,0);
            channel.force(false);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                lock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void parseAllFilesInDir(File root) throws FileNotFoundException {
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (FilenameUtils.getExtension(file.getName()).equals("java")) {
                    try {
                        System.out.println("reading file "+ file.getName());
                        String fileContent = FileUtils.readFileToString(file,
                                "utf-8");
                        List<String> tokens = Tokenizer
                                .processMethodBody(fileContent);
                        if (tokens.size() > MIN_TOKEN_IN_FILE) {
                            Map<String, Integer> tokenToFrequencyMap = this
                                    .makeTokenToFrequencyMap(tokens);
                            StringBuilder sb = new StringBuilder();
                            for (String token : tokenToFrequencyMap.keySet()) {
                                sb.append(token + "@@::@@"
                                        + tokenToFrequencyMap.get(token) + ",");
                            }
                            if (sb.length() > 0) {
                               Util.writeToFile(this.parsedFileWriter, this.project_id
                                       + "," + this.startFileIdCounter + "@#@", false);
                               Util.writeToFile(this.parsedFileWriter, sb.substring(0,
                                        sb.length() - 1), true);
                            }else{
                                System.out.println("No tokens found in file: "+ file.getAbsolutePath());
                            }
                            Util.writeToFile(this.idFileWriter, this.project_id+","+this.startFileIdCounter
                                    + "," + file.getAbsolutePath(), true);
                            this.startFileIdCounter+=1;
                            if(this.startFileIdCounter==this.endFileIdCounter){
                                this.updateIds();
                            }
                        }else{
                            System.out.println("ignoring file, num tokens can not match min token threshold: "+ file.getAbsolutePath());
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
                try{
                    this.project_id= Long.parseLong(projectInfo[0].trim());
                    System.out.println("reading project: "+ line);
                    String content_folder = projectInfo[1].trim()+File.separator+ "content";
                    String latest_folder = projectInfo[1].trim()+ File.separator+ "latest";
                    File rootDir = new File(content_folder);
                    if(rootDir.isDirectory()){
                        this.parseAllFilesInDir(rootDir);
                    }else{
                        rootDir = new File(latest_folder);
                        if(rootDir.isDirectory()){
                            this.parseAllFilesInDir(rootDir);
                        }else{
                            // ignore
                        }
                    }
                }catch(NumberFormatException e){
                    //ignore this project
                    System.out.println("invalid line in projects file, "+ line);
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
        String projectsFilePath=args[0];
        try {
            FileParser fp = new FileParser(args[0]);
            fp.traverseProjectPath(projectsFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
