package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import models.Bag;
import models.ITokensFileProcessor;
import noindex.CloneHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utility.TokensFileReader;
import utility.Util;

/**
 * for every project's input file (one file is one project) read all lines for
 * each line create a Bag. for each project create one output file, this file
 * will have all the tokens, in the bag.
 * 
 * @author vaibhavsaini
 * 
 */
public class TermSorter implements ITokensFileProcessor {
    private CloneHelper cloneHelper;
    public static Map<String, Long> wordFreq;
    public static String SORTED_FILES_DIR = "output/sortedFiles";
    public static Map<String, Long> globalTokenPositionMap = new HashMap<String, Long>();;
    public static final int mapSize = 50000;
    public static int wfm_file_count = 0;
    private int lineNumber = 0;

    public TermSorter() {
        TermSorter.wordFreq = new TreeMap<String, Long>();
        Util.createDirs(SORTED_FILES_DIR);
        this.cloneHelper = new CloneHelper();

    }

    public static void main(String[] args) throws IOException, ParseException {
        TermSorter externalSort = new TermSorter();
        // externalSort.populateGlobalPositionMap();
    }

    public void populateLocalWordFreqMap() throws IOException, ParseException {

        File wfmFile = new File(SearchManager.WFM_DIR_PATH + "/wordFreqMap.wfm");
        if (wfmFile.exists()) {
            System.out.println("wfm file exists, not creating a new one");
            /*
             * TermSorter.globalTokenPositionMap = Util
             * .readMapFromFile(SearchManager.GTPM_DIR_PATH+"/gtpm.json");
             * System.out.println("search size of GTPM: " +
             * TermSorter.globalTokenPositionMap.size());
             */
        } else {
            System.out.println("wfm file doesn't exist. Creating WFM from query File");
            Util.createDirs(SearchManager.WFM_DIR_PATH);
            File queryDir = new File(SearchManager.QUERY_DIR_PATH);
            if (queryDir.isDirectory()) {
                System.out.println("Directory: " + queryDir.getAbsolutePath());
                for (File inputFile : queryDir.listFiles()) {
                    this.populateWordFreqMap(inputFile);
                }
                // write the last map to file
                TermSorter.wfm_file_count += 1;
                Util.writeMapToFile(SearchManager.WFM_DIR_PATH + "/wordFreqMap_" + TermSorter.wfm_file_count + ".wfm",
                        TermSorter.wordFreq);
                TermSorter.wordFreq = null; // we don't need it, let GC get it.
            } else {
                System.out.println("File: " + queryDir.getName() + " is not a direcory. exiting now");
                System.exit(1);
            }
        }
    }

    public void populateGlobalPositionMap() throws IOException {
        File gtpmFile = new File(Util.GTPM_DIR + "/tokens.gtpm");
        if (gtpmFile.exists()) {
            System.out.println("GTPM file exists, reading from file");
            this.indexGPTM(gtpmFile);
            /*
             * TermSorter.globalTokenPositionMap = Util
             * .readMapFromFile(Util.GTPM_DIR + "/gtpm.json");
             * System.out.println("search size of GTPM: " +
             * TermSorter.globalTokenPositionMap.size());
             */
            return;
        } else {
            System.out.println("GTPM files doesn't exist. reading from wfm files");
            // File currentDir = new File(System.getProperty("user.dir"));
            // external sort and merge the sorted wfms from all NODES.
            // the final sorted WFM file will be present in UTIL.GLOBAL_WFM_DIR
            File globalWFMDIr = new File(Util.GLOBAL_WFM_DIR);
            if(globalWFMDIr.exists()){
                globalWFMDIr.delete();
            }
            Util.createDirs(Util.GLOBAL_WFM_DIR);
            this.mergeWfms(System.getProperty("user.dir"), Util.GLOBAL_WFM_DIR, false);
            // this.populateGlobalWordFreqMapIttrative(currentDir);
            // create the tokens.gtpm file
            File globalWFMFile = new File(Util.GLOBAL_WFM_DIR).listFiles()[0]; // there
                                                                               // should
                                                                               // be
                                                                               // only
                                                                               // one
                                                                               // file
                                                                               // in
                                                                               // this
                                                                               // directory
            // System.out.println("Indexing GTPM, size of GTPM: " + (count - 1)
            // + " entries");
            this.indexGPTM(globalWFMFile);
            TermSorter.globalTokenPositionMap = null;
            SearchManager.globalWordFreqMap = null;
        }
    }

    private void indexGPTM(File inputFile) {
        KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
        IndexWriterConfig gtpmIndexWriterConfig = new IndexWriterConfig(Version.LUCENE_46, keywordAnalyzer);
        TieredMergePolicy mergePolicy = (TieredMergePolicy) gtpmIndexWriterConfig.getMergePolicy();
        mergePolicy.setNoCFSRatio(0);// what was this for?
        mergePolicy.setMaxCFSSegmentSizeMB(0); // what was this for?
        gtpmIndexWriterConfig.setOpenMode(OpenMode.CREATE);
        gtpmIndexWriterConfig.setRAMBufferSizeMB(1024);
        IndexWriter gtpmIndexWriter = null;
        DocumentMaker gtpmIndexer = null;
        try {
            gtpmIndexWriter = new IndexWriter(FSDirectory.open(new File(Util.GTPM_INDEX_DIR)), gtpmIndexWriterConfig);
            gtpmIndexer = new DocumentMaker(gtpmIndexWriter);

            BufferedReader br = null;
            int count = 0;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null && line.trim().length() > 0) {
                    gtpmIndexer.indexGtpmEntry(line);
                    count++;
                    if ((count % 100000) == 0)
                        System.out.println("gtpm entries indexed: " + count);
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
        } catch (IOException e) {
            System.out.println(e.getMessage() + ", exiting now.");
            System.exit(1);
        } finally {
            gtpmIndexer.closeIndexWriter();
        }
    }

    private void populateGlobalWordFreqMapIttrative(File root) {
        /*
         * Writer processedWFMfilesWriter = null; String processedWFMFilename =
         * "processedWFMFiles.txt"; try { processedWFMfilesWriter =
         * Util.openFile(processedWFMFilename, true); } catch (IOException e) {
         * System.out.println("cant open processedWFMFiles.txt");
         * e.printStackTrace(); System.exit(1); }
         */
        /*
         * File tempgtpm = new File("temp_gwfm.txt"); if (tempgtpm.exists()) {
         * SearchManager.globalWordFreqMap = Util
         * .readMapFromFile("temp_gwfm.txt"); }
         * System.out.println("current Dir: " + root.getName()); Set<String>
         * processedWFMset = new HashSet<String>();
         * Util.populateProcessedWFMSet(processedWFMFilename, processedWFMset);
         * System.out.println("size of populateProcessedWFMSet " +
         * processedWFMset.size());
         */
        Stack<File> fileStack = new Stack<File>();
        fileStack.push(root);
        while (!fileStack.isEmpty()) {
            File[] files = fileStack.pop().listFiles();
            for (File currFile : files) {
                if (currFile.isFile()) {
                    if (FilenameUtils.getExtension(currFile.getName()).equals("wfm")) {
                        /*
                         * if (processedWFMset
                         * .contains(currFile.getAbsolutePath())) {
                         * System.out.println("ignore wfm file, " +
                         * currFile.getAbsolutePath()); continue; }
                         */
                        System.out.println("populating globalWordFreqMap, reading file: " + currFile.getAbsolutePath());
                        Map<String, Long> wordFreqMap = Util.readMapFromFile(currFile.getAbsolutePath());
                        for (Entry<String, Long> entry : wordFreqMap.entrySet()) {

                            long value = 0;
                            if (SearchManager.globalWordFreqMap.containsKey(entry.getKey())) {
                                value = SearchManager.globalWordFreqMap.get(entry.getKey()) + entry.getValue();
                            } else {
                                value = entry.getValue();
                            }
                            SearchManager.globalWordFreqMap.put(entry.getKey(), value);
                        }
                        /*
                         * Util.writeMapToFile("temp_gwfm.txt",
                         * SearchManager.globalWordFreqMap);
                         */
                        /*
                         * System.out
                         * .println("writing to processedWFMfilesWriter");
                         * Util.writeToFile(processedWFMfilesWriter,
                         * currFile.getAbsolutePath(), true);
                         */
                        /*
                         * try { processedWFMfilesWriter.flush(); } catch
                         * (IOException e) { e.printStackTrace(); }
                         */
                    }
                } else if (currFile.isDirectory()) {
                    if (currFile.getName().contains("NODE_") || currFile.getName().contains("gtpm")) {
                        fileStack.push(currFile);
                    }
                }

            }
        }
        /* Util.closeOutputFile(processedWFMfilesWriter); */
    }

    public void processLine(String line) throws ParseException {

        Bag bag = cloneHelper.deserialise(line);

        if (null != bag && bag.getSize() > SearchManager.min_tokens && bag.getSize() < SearchManager.max_tokens) {
            cloneHelper.populateWordFreqMap(bag);
        } else {
            if (null == bag) {
                System.out.println("empty block, ignoring");
            } else {
                System.out.println(
                        "not adding tokens of line to GPTM, REASON: " + bag.getFunctionId() + ", " + bag.getId()
                                + ", size: " + bag.getSize() + " (max tokens is " + SearchManager.max_tokens + ")");
            }
        }
        this.lineNumber++;
        System.out.println(SearchManager.NODE_PREFIX + " , GTPM line_number: " + this.lineNumber);
    }

    /**
     * Reads the input file and writes the partial word frequency maps to .wfm
     * files.
     * 
     * @param file
     * @throws IOException
     * @throws ParseException
     */
    private void populateWordFreqMap(File file) throws IOException, ParseException {
        TokensFileReader tfr = new TokensFileReader(SearchManager.NODE_PREFIX, file, SearchManager.max_tokens, this);
        tfr.read();
    }

    public void mergeWfms(String inputWfmDirectoryPath, String outputWfmDirectoryPath,
            boolean deleteInputfilesAfterProcessing) throws IOException {
        // Iterate on wfm fies in the input directory
        List<File> wfmFiles = (List<File>) FileUtils.listFiles(new File(inputWfmDirectoryPath), new String[] { "wfm" },
                true);
        //File inputFolder = new File(inputWfmDirectoryPath);
        /*
         * File[] wfmFiles = inputFolder.listFiles(new FilenameFilter() {
         * 
         * @Override public boolean accept(File dir, String name) {
         * System.out.println("dir to consider: "+ dir.getAbsolutePath());
         * return !dir.getAbsolutePath().contains(".git") &&
         * name.endsWith(".wfm"); } });
         */
        System.out.println("wfm files to merge: " + wfmFiles.size());
        for (File f : wfmFiles) {
            System.out.println("wfm files: " + f.getAbsolutePath());
        }
        File resultFile = null;
        File previousResultFile = new File(outputWfmDirectoryPath + "/sorted_0.wfm");
        boolean created = previousResultFile.createNewFile();
        System.out.println("temp wfm file created, status: " + created);
        int i = 1;
        for (File wfmFile : wfmFiles) {
            resultFile = new File(outputWfmDirectoryPath + "/sorted_" + i + ".wfm");
            this.externalMerge(wfmFile, previousResultFile, resultFile);
            previousResultFile.delete();
            if (deleteInputfilesAfterProcessing) {
                wfmFile.delete();
            }
            previousResultFile = resultFile;
            i++;
        }

    }

    private void externalMerge(File a, File b, File output) throws IOException {
        BufferedReader aBr = Util.getReader(a);
        BufferedReader bBr = Util.getReader(b);
        Writer sortedFileWriter = Util.openFile(output, false);
        System.out.println();
        try {
            String aLine = aBr.readLine();
            String bLine = bBr.readLine();
            while (null != aLine && null != bLine) {
                // System.out.println("aLine is: " + aLine);
                // System.out.println("bLine is: " + bLine);
                String[] aKeyValuePair = aLine.split(":");
                String[] bKeyValuePair = bLine.split(":");
                int result = aKeyValuePair[0].compareTo(bKeyValuePair[0]);
                if (result == 0) {
                    // add frequency
                    // System.out.println("adding frequency");
                    long freq = Long.parseLong(aKeyValuePair[1]) + Long.parseLong(bKeyValuePair[1]);
                    Util.writeToFile(sortedFileWriter, aKeyValuePair[0] + ":" + freq, true);
                    // increment readers for both files.
                    // System.out.println("incrementing both file pointers");
                    aLine = aBr.readLine();
                    bLine = bBr.readLine();
                } else if (result < 0) {
                    // a has smaller key than b, write it down and increment a's
                    // reader
                    // System.out.println("a's key is smaller");
                    Util.writeToFile(sortedFileWriter, aLine, true);
                    // System.out.println("incrementing a's file pointers");
                    aLine = aBr.readLine();
                } else {
                    // b has smaller key than a, write it down and increment b's
                    // reader
                    // System.out.println("b's key is smaller");
                    Util.writeToFile(sortedFileWriter, bLine, true);
                    // System.out.println("incrementing b' file pointers");
                    bLine = bBr.readLine();
                }
            }
            // write what is left to the output file
            // note: one of the two lines must be null.
            while (null != aLine) {
                // System.out.println("Writing remaining contents of file a");
                Util.writeToFile(sortedFileWriter, aLine, true);
                aLine = aBr.readLine();
            }
            while (null != bLine) {
                // System.out.println("Writing remaining contents of file b");
                Util.writeToFile(sortedFileWriter, bLine, true);
                bLine = bBr.readLine();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // close files.
            try {
                Util.closeOutputFile(sortedFileWriter);
                aBr.close();
                bBr.close();
            } catch (IOException e) {
                System.out.println("Caught Exception");
                e.printStackTrace();
            }
        }
    }
}
