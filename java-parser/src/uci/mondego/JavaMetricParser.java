package uci.mondego;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class JavaMetricParser {
    public String outputDirPath;
    public String metricsFileName;
    public String tokensFileName;
    public String bookkeepingFileName;
    public String errorFileName;
    public static String prefix;
    public static String fileIdPrefix;
    public static long FILE_COUNTER;
    public static long METHOD_COUNTER;
    private IInputProcessor inputProcessor;

    public JavaMetricParser(String inputFilePath) {
        this.metricsFileName = "mlcc_input.file";
        this.bookkeepingFileName = "bookkeeping.file";
        this.errorFileName = "error_metrics.file";
        this.tokensFileName = "scc_input.file";
        // JavaMetricParser.fileIdPrefix = "1";
        JavaMetricParser.prefix = this.getBaseName(inputFilePath);
        this.outputDirPath = JavaMetricParser.prefix + "_metric_output";
        File outDir = new File(this.outputDirPath);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
    }

    private String getBaseName(String path) {
        File inputFile = new File(path);
        String fileName = inputFile.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }
    private void initializeWriters() throws IOException{
        FileWriters.metricsFileWriter = Util.openFile(
                this.outputDirPath + File.separator + this.metricsFileName, false);

        FileWriters.errorPw = Util.openFile(
                this.outputDirPath + File.separator + this.errorFileName, false);
        FileWriters.bookkeepingWriter = Util.openFile(
                this.outputDirPath + File.separator + this.bookkeepingFileName,
                false);
        FileWriters.tokensFileWriter = Util.openFile(
                this.outputDirPath + File.separator + this.tokensFileName, false);
    }
    
    private void closeWriters(){
        Util.closeOutputFile(FileWriters.metricsFileWriter);
        Util.closeOutputFile(FileWriters.errorPw);
        Util.closeOutputFile(FileWriters.tokensFileWriter);
        Util.closeOutputFile(FileWriters.bookkeepingWriter);
    }
    
    
    private void handleInput(String inputMode, String filename){
        BufferedReader br;
        try {
                this.initializeWriters();

            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            String line;
            if ("dir".equals(inputMode)) {
                this.inputProcessor = new FolderInputProcessor();
            }else if ("tgz".equals(inputMode)){
                
            }else if ("zip".equals(inputMode)){
                
            }
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                this.inputProcessor.processInput(line.trim());
            }
            this.closeWriters();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length > 0) {
            String filename = args[0];
            String inputMode = args[1];
            JavaMetricParser javaMetricParser = new JavaMetricParser(filename);
            javaMetricParser.handleInput(inputMode, filename);
        } else {
            System.out.println("FATAL: please specify the file with list of directories!");
        }
        System.out.println("done!");
    }
}