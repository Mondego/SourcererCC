package uci.mondego;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

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
    public static Set<String> filesToprocess;
    
    public JavaMetricParser(String inputFilePath) {
        this.metricsFileName = "mlcc_input.file";
        this.bookkeepingFileName = "bookkeeping.file";
        this.errorFileName = "error_metrics.file";
        this.tokensFileName = "scc_input.file";
        JavaMetricParser.filesToprocess = new HashSet<String>();
        JavaMetricParser.prefix = this.getBaseName(inputFilePath);
        JavaMetricParser.fileIdPrefix = JavaMetricParser.prefix;//"1";
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
            String line = null;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                JavaMetricParser.filesToprocess.add(line);
            }
            try{
                br.close();
            }catch(IOException e){
                System.out.println("WARN, couldn't close inputfile: "+ filename);
            }
            if ("dir".equals(inputMode)) {
                this.inputProcessor = new FolderInputProcessor();
            }else if ("tgz".equals(inputMode)){
                this.inputProcessor = new TGZInputProcessor();
            }else if ("zip".equals(inputMode)){
                
            }
            this.inputProcessor.processInput("dummy");
            this.closeWriters();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length > 0) {
            System.out.println(args);
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