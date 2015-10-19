package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import utility.Util;

public class FileParser {

    long file_id_counter = 0l;
    long project_id = 0l;
    public static final int MIN_TOKEN_IN_FILE = 0;
    Writer parsedFileWriter = null;
    Writer idFileWriter = null;
    String parsedFilePath = "";
    String idFilePath = "";
    String datasetPath;
    String bookkeepingPath;

    public FileParser() throws IOException {
        this.datasetPath = "input/dataset";
        this.bookkeepingPath = "input/bookkeeping";
        this.parsedFilePath = this.datasetPath + "/clone-input.txt";
        this.idFilePath = this.bookkeepingPath + "/idFile.txt";
        Util.createDirs(this.datasetPath);
        Util.createDirs(this.bookkeepingPath);
        this.parsedFileWriter = Util.openFile(this.parsedFilePath, true);
        this.idFileWriter = Util.openFile(this.idFilePath, true);
        
    }

    public void parseAllFilesInDir(File root) {
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
                                       + "," + this.file_id_counter + "@#@", false);
                               Util.writeToFile(this.parsedFileWriter, sb.substring(0,
                                        sb.length() - 1), true);
                            }
                            Util.writeToFile(this.idFileWriter, this.project_id+","+this.file_id_counter
                                    + "," + file.getAbsolutePath(), true);
                            this.file_id_counter+=1;
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
                System.out.println("reading project: "+ line);
                String content_folder = line+File.separator+ "content";
                String latest_folder = line+ File.separator+ "latest";
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
                this.project_id+=1;
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
    
    public void createParsedFileSet(){
        
    }

    public static void main(String[] args) {
        String projectsFilePath="projects.txt";
        try {
            FileParser fp = new FileParser();
            fp.traverseProjectPath(projectsFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
