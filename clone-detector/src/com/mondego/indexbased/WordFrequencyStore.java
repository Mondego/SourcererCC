package com.mondego.indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.mondego.models.Bag;
import com.mondego.models.ITokensFileProcessor;
import com.mondego.models.TokenFrequency;
import com.mondego.noindex.CloneHelper;
import com.mondego.utility.TokensFileReader;
import com.mondego.utility.Util;

/**
 * for every project's input file (one file is one project) read all lines for
 * each line create a Bag. for each project create one output file, this file
 * will have all the tokens, in the bag.
 * 
 * @author vaibhavsaini
 * 
 */
public class WordFrequencyStore implements ITokensFileProcessor {
    private CloneHelper cloneHelper;
    private Map<String, Long> wordFreq;
    private int wfm_file_count = 0;
    private int lineNumber = 0;
    private CodeSearcher wfmSearcher = null;
    private IndexWriter wfmIndexWriter = null;
    private DocumentMaker wfmIndexer = null;
    private static final Logger logger = LogManager.getLogger(WordFrequencyStore.class);
    public WordFrequencyStore() {
        this.wordFreq = new TreeMap<String, Long>();
        this.cloneHelper = new CloneHelper();
    }

    public static void main(String[] args) throws IOException, ParseException {
        WordFrequencyStore wfs = new WordFrequencyStore();
        wfs.populateLocalWordFreqMap();
    }

    /**
     * Reads the input file and writes the partial word frequency maps to .wfm
     * files.
     * 
     * @param file
     * @throws IOException
     * @throws ParseException
     */
    private void readTokensFile(File file) throws IOException, ParseException {
        TokensFileReader tfr = new TokensFileReader(SearchManager.NODE_PREFIX, file, SearchManager.max_tokens, this);
        tfr.read();
    }

    public void processLine(String line) throws ParseException {

        Bag bag = cloneHelper.deserialise(line);

        if (null != bag && bag.getSize() > SearchManager.min_tokens && bag.getSize() < SearchManager.max_tokens) {
            populateWordFreqMap(bag);
        } else {
            if (null == bag) {
                logger.debug("empty block, ignoring");
            } else {
                logger.debug("not adding tokens of line to WFM, REASON: " + bag.getFunctionId() + ", " + bag.getId()
                                + ", size: " + bag.getSize() + " (max tokens is " + SearchManager.max_tokens + ")");
            }
        }
        this.lineNumber++;
    }

    private void populateWordFreqMap(Bag bag) {
        for (TokenFrequency tf : bag) {
            String tokenStr = tf.getToken().getValue();
            if (this.wordFreq.containsKey(tokenStr)) {
                long value = this.wordFreq.get(tokenStr) + tf.getFrequency();
                this.wordFreq.put(tokenStr, value);
            } else {
                this.wordFreq.put(tokenStr, (long) tf.getFrequency());
            }
        }
        // if map size if more than 8 Million flush it.
        if (this.wordFreq.size() > 8000000) {
            // write it in a file. it is a treeMap, so it is already sorted by
            // keys (alphbatically)
            wfm_file_count += 1;
            flushToIndex();

            // Util.writeMapToFile(SearchManager.WFM_DIR_PATH + "/wordFreqMap_"
            // + TermSorter.wfm_file_count + ".wfm",
            // TermSorter.wordFreq);

            // reinit the map
            this.wordFreq = new TreeMap<String, Long>();
        }
    }

    public void populateLocalWordFreqMap() throws IOException, ParseException {

        File queryDir = new File(SearchManager.QUERY_DIR_PATH);
        if (queryDir.isDirectory()) {
            logger.info("Directory: " + queryDir.getAbsolutePath());

            this.prepareIndex();

            for (File inputFile : queryDir.listFiles()) {

                this.readTokensFile(inputFile);

            }

            // write the last map to the index
            wfm_file_count += 1;
            flushToIndex();
            // Util.writeMapToFile(SearchManager.WFM_DIR_PATH + "/wordFreqMap_"
            // + TermSorter.wfm_file_count + ".wfm",
            // TermSorter.wordFreq);
            wordFreq = null; // we don't need it, let GC get it.
            // shutdown
            shutdown();
        } else {
            logger.error("File: " + queryDir.getName() + " is not a directory. Exiting now");
            System.exit(1);
        }
    }

    public void prepareIndex() throws IOException {
        File globalWFMDIr = new File(Util.GTPM_INDEX_DIR);
        if (!globalWFMDIr.exists()) {
            Util.createDirs(Util.GTPM_INDEX_DIR);
        }
        KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
        IndexWriterConfig wfmIndexWriterConfig = new IndexWriterConfig(Version.LUCENE_46, keywordAnalyzer);
        wfmIndexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
        wfmIndexWriterConfig.setRAMBufferSizeMB(1024);

        logger.info("PREPARE INDEX");
        try {
            wfmIndexWriter = new IndexWriter(FSDirectory.open(new File(Util.GTPM_INDEX_DIR)), wfmIndexWriterConfig);
            wfmIndexWriter.commit();
            wfmIndexer = new DocumentMaker(wfmIndexWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flushToIndex() {
        logger.info("*** FLUSHING WFM TO INDEX *** " + this.wordFreq.size());
        long start = System.currentTimeMillis();
        this.wfmSearcher = new CodeSearcher(Util.GTPM_INDEX_DIR, "key");
        int count = 0;
        for (Entry<String, Long> entry : this.wordFreq.entrySet()) {

            long oldfreq = wfmSearcher.getFrequency(entry.getKey());
            if (oldfreq < 0)
                oldfreq = 0;

            wfmIndexer.indexWFMEntry(entry.getKey(), oldfreq + entry.getValue());
            if (++count % 1000000 == 0)
                logger.info("...flushed " + count);
        }
        this.wfmSearcher.close();
        try {
            this.wfmIndexWriter.forceMerge(1);
            this.wfmIndexWriter.commit();
        } catch (Exception e) {
            logger.error(SearchManager.NODE_PREFIX + ", exception on commit",e);
            e.printStackTrace();
        }
        long elapsed = System.currentTimeMillis() - start;
        logger.info("*** FLUSHING END *** " + count + " in " + elapsed / 1000 + "s");
    }

    private void shutdown() {
        logger.debug("Shutdown");
        try {
            wfmIndexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mergeWfms(String inputWfmDirectoryPath, String outputWfmDirectoryPath,
            boolean deleteInputfilesAfterProcessing) throws IOException {
        // Iterate on wfm fies in the input directory
        List<File> wfmFiles = (List<File>) FileUtils.listFiles(new File(inputWfmDirectoryPath), new String[] { "wfm" },
                true);
        // File inputFolder = new File(inputWfmDirectoryPath);
        /*
         * File[] wfmFiles = inputFolder.listFiles(new FilenameFilter() {
         * 
         * @Override public boolean accept(File dir, String name) {
         * System.out.println("dir to consider: "+ dir.getAbsolutePath());
         * return !dir.getAbsolutePath().contains(".git") &&
         * name.endsWith(".wfm"); } });
         */
        logger.debug("wfm files to merge: " + wfmFiles.size());
        for (File f : wfmFiles) {
            logger.debug("wfm files: " + f.getAbsolutePath());
        }
        File resultFile = null;
        File previousResultFile = new File(outputWfmDirectoryPath + "/sorted_0.wfm");
        boolean created = previousResultFile.createNewFile();
        logger.debug("temp wfm file created, status: " + created);
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
                logger.error("Caught Exception");
                e.printStackTrace();
            }
        }
    }
}
