/**
 * 
 */
package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import models.Bag;
import models.BagSorter;
import models.CandidatePair;
import models.CandidateProcessor;
import models.CandidateSearcher;
import models.ClonePair;
import models.CloneReporter;
import models.CloneValidator;
import models.ForwardIndexCreator;
import models.IListener;
import models.InvertedIndexCreator;
import models.QueryBlock;
import models.QueryCandidates;
import models.Queue;
import models.TokenInfo;
import net.jmatrix.eproperties.EProperties;
import noindex.CloneHelper;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utility.Util;
import validation.TestGson;

/**
 * @author vaibhavsaini
 * 
 */
public class SearchManager {
    private static long clonePairsCount;
    public static CodeSearcher searcher;
    public static CodeSearcher fwdSearcher;
    public static CodeSearcher gtpmSearcher;
    private CloneHelper cloneHelper;
    public static String QUERY_DIR_PATH;
    public static String DATASET_DIR;
    public static String GTPM_DIR_PATH;
    public static Writer clonesWriter; // writer to write the output
    public static float th; // args[2]
                            // search
    private final static String ACTION_INDEX = "index";
    private final static String ACTION_SEARCH = "search";
    private final static int CANDIDATE_SEARCHER = 1;
    private final static int CANDIDATE_PROCESSOR = 2;
    private final static int CLONE_VALIDATOR = 3;
    private final static int CLONE_REPORTER = 4;

    private long timeSpentInProcessResult;
    public static long timeSpentInSearchingCandidates;
    private long timeIndexing;
    private long timeGlobalTokenPositionCreation;
    private long timeSearch;
    private static long numCandidates;
    private Writer outputWriter;
    private long timeTotal;
    private String action;
    private boolean appendToExistingFile;
    TestGson testGson;
    public static final Integer MUL_FACTOR = 100;
    private static final String ACTION_INIT = "init";
    int deletemeCounter = 0;
    private double ramBufferSizeMB;
    private long bagsSortTime;
    public static Queue<QueryCandidates> queryCandidatesQueue;
    public static Queue<QueryBlock> queryBlockQueue;
    public static Queue<ClonePair> reportCloneQueue;
    public static Queue<CandidatePair> verifyCandidateQueue;

    public static Queue<Bag> bagsToSortQueue;
    public static Queue<Bag> bagsToInvertedIndexQueue;
    public static Queue<Bag> bagsToForwardIndexQueue;

    private final static int BAG_SORTER = 1;
    private final static int INVERTED_INDEX_CREATOR = 2;
    private final static int FORWARD_INDEX_CREATOR = 3;
    private List<FSDirectory> invertedIndexDirectories;
    private List<FSDirectory> forwardIndexDirectories;
    private List<IndexWriter> indexerWriters;

    public static Object lock = new Object();
    private int qbq_thread_count;
    private int qcq_thread_count;
    private int vcq_thread_count;
    private int rcq_thread_count;
    private int qbq_size;
    private int qcq_size;
    private int vcq_size;
    private int rcq_size;
    private int threadsToProcessBagsToSortQueue;
    private int sizeBagsToSortQ;
    private int threadToProcessIIQueue;
    private int sizeBagsToIIQ;
    private int threadsToProcessFIQueue;
    private int sizeBagsToFIQ;
    public static int min_tokens;
    public static int max_tokens;
    public static boolean isGenCandidateStats;
    public static int statusCounter;
    public static boolean isStatusCounterOn;
    public static int printAfterEveryXQueries;
    public static String loggingMode;
    public static String NODE_PREFIX;
    public static String OUTPUT_DIR;
    public static Map<String, Long> globalWordFreqMap;

    public SearchManager(String[] args) throws IOException {
        SearchManager.clonePairsCount = 0;
        this.cloneHelper = new CloneHelper();
        this.timeSpentInProcessResult = 0;
        SearchManager.timeSpentInSearchingCandidates = 0;
        this.timeIndexing = 0;
        this.timeGlobalTokenPositionCreation = 0;
        this.timeSearch = 0;
        SearchManager.numCandidates = 0;
        this.timeTotal = 0;
        this.appendToExistingFile = true;
        this.ramBufferSizeMB = 1024 * 1;
        this.bagsSortTime = 0;
        this.action = args[0];
        SearchManager.statusCounter = 0;
        SearchManager.globalWordFreqMap = new HashMap<String, Long>();
        try {

            SearchManager.th = (Float.parseFloat(args[1]) * SearchManager.MUL_FACTOR);
            this.qbq_thread_count = Integer.parseInt(args[2]);
            this.qcq_thread_count = Integer.parseInt(args[3]);
            this.vcq_thread_count = Integer.parseInt(args[4]);
            this.rcq_thread_count = Integer.parseInt(args[5]);
            this.qbq_size = Integer.parseInt(args[6]);
            this.qcq_size = Integer.parseInt(args[7]);
            this.vcq_size = Integer.parseInt(args[8]);
            this.rcq_size = Integer.parseInt(args[9]);
            SearchManager.min_tokens = Integer.parseInt(args[10]);
            SearchManager.max_tokens = Integer.parseInt(args[11]);
            this.threadsToProcessBagsToSortQueue = Integer.parseInt(args[12]);
            this.threadToProcessIIQueue = Integer.parseInt(args[13]);
            this.threadsToProcessFIQueue = Integer.parseInt(args[14]);
            this.sizeBagsToSortQ = Integer.parseInt(args[15]);
            this.sizeBagsToIIQ = Integer.parseInt(args[16]);
            this.sizeBagsToFIQ = Integer.parseInt(args[17]);

        } catch (NumberFormatException e) {
            System.out.println(e.getMessage() + ", exiting now");
            System.exit(1);
        }
        if (this.action.equals(ACTION_SEARCH)) {
            System.out.println("acton: " + this.action + System.lineSeparator()
                    + "threshold: " + args[1] + System.lineSeparator()
                    + "QBQ_THREADS: " + this.qbq_thread_count + ", QBQ_SIZE: "
                    + this.qbq_size + System.lineSeparator() + "QCQ_THREADS: "
                    + this.qcq_thread_count + ", QCQ_SIZE: " + this.qcq_size
                    + System.lineSeparator() + "VCQ_THREADS: "
                    + this.vcq_thread_count + ", VCQ_SIZE: " + this.vcq_size
                    + System.lineSeparator() + "RCQ_THREADS: "
                    + this.rcq_thread_count + ", RCQ_SIZE: " + this.rcq_size
                    + System.lineSeparator());
            SearchManager.queryBlockQueue = new Queue<QueryBlock>(
                    this.qbq_thread_count, this.qbq_size);
            SearchManager.queryCandidatesQueue = new Queue<QueryCandidates>(
                    this.qcq_thread_count, this.qcq_size);
            SearchManager.verifyCandidateQueue = new Queue<CandidatePair>(
                    this.vcq_thread_count, this.vcq_size);
            SearchManager.reportCloneQueue = new Queue<ClonePair>(
                    this.rcq_thread_count, this.rcq_size);

            this.registerListeners(this.qbq_thread_count,
                    SearchManager.queryBlockQueue, CANDIDATE_SEARCHER);
            this.registerListeners(this.qcq_thread_count,
                    SearchManager.queryCandidatesQueue, CANDIDATE_PROCESSOR);
            this.registerListeners(this.vcq_thread_count,
                    SearchManager.verifyCandidateQueue, CLONE_VALIDATOR);
            this.registerListeners(this.rcq_thread_count,
                    SearchManager.reportCloneQueue, CLONE_REPORTER);
        } else if (this.action.equals(ACTION_INDEX)) {
            invertedIndexDirectories = new ArrayList<FSDirectory>();
            forwardIndexDirectories = new ArrayList<FSDirectory>();
            indexerWriters = new ArrayList<IndexWriter>();
            System.out.println("acton: " + this.action + System.lineSeparator()
                    + "threshold: " + args[1] + System.lineSeparator()
                    + "BQ_THREADS: " + this.threadsToProcessBagsToSortQueue
                    + ", BQ_SIZE: " + this.sizeBagsToSortQ
                    + System.lineSeparator() + "SBQ_THREADS: "
                    + this.threadToProcessIIQueue + ", SBQ_SIZE: "
                    + this.sizeBagsToIIQ + System.lineSeparator()
                    + "IIQ_THREADS: " + this.threadsToProcessFIQueue
                    + ", IIQ_SIZE: " + this.sizeBagsToFIQ
                    + System.lineSeparator());
            SearchManager.bagsToSortQueue = new Queue<Bag>(
                    this.threadsToProcessBagsToSortQueue, this.sizeBagsToSortQ);
            SearchManager.bagsToInvertedIndexQueue = new Queue<Bag>(
                    this.threadToProcessIIQueue, this.sizeBagsToIIQ);
            SearchManager.bagsToForwardIndexQueue = new Queue<Bag>(
                    this.threadsToProcessFIQueue, this.sizeBagsToFIQ);

            this.registerListenersForIndex(
                    this.threadsToProcessBagsToSortQueue,
                    SearchManager.bagsToSortQueue, BAG_SORTER);
            this.registerListenersForIndex(this.threadToProcessIIQueue,
                    SearchManager.bagsToInvertedIndexQueue,
                    INVERTED_INDEX_CREATOR);
            this.registerListenersForIndex(this.threadsToProcessFIQueue,
                    SearchManager.bagsToForwardIndexQueue,
                    FORWARD_INDEX_CREATOR);
        }

    }

    private void registerListeners(int nListeners, Queue<?> queue,
            int ListenerType) {
        List<IListener> listeners = new ArrayList<IListener>();
        for (int i = 0; i < nListeners; i++) {
            IListener listener = null;
            if (ListenerType == CANDIDATE_PROCESSOR) {
                listener = new CandidateProcessor();
            } else if (ListenerType == CANDIDATE_SEARCHER) {
                listener = new CandidateSearcher();
            } else if (ListenerType == CLONE_REPORTER) {
                listener = new CloneReporter();
            } else if (ListenerType == CLONE_VALIDATOR) {
                listener = new CloneValidator();
            }
            listeners.add(listener);
        }
        queue.setListeners(listeners);
    }

    private void registerListenersForIndex(int nListeners, Queue<?> queue,
            int ListenerType) {
        List<IListener> listeners = new ArrayList<IListener>();
        for (int i = 0; i < nListeners; i++) {
            IListener listener = null;
            if (ListenerType == BAG_SORTER) {
                listener = new BagSorter();
            } else if (ListenerType == INVERTED_INDEX_CREATOR) {
                WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(
                        Version.LUCENE_46);
                IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
                        Version.LUCENE_46, whitespaceAnalyzer);
                indexWriterConfig.setRAMBufferSizeMB(this.ramBufferSizeMB);
                indexWriterConfig.setOpenMode(OpenMode.CREATE);
                IndexWriter indexWriter = null;
                try {
                    FSDirectory dir = FSDirectory.open(new File(
                            SearchManager.NODE_PREFIX + "/index/temp/_" + i));
                    invertedIndexDirectories.add(dir);
                    indexWriter = new IndexWriter(dir, indexWriterConfig);
                    listener = new InvertedIndexCreator(new CodeIndexer(
                            indexWriter));
                    indexerWriters.add(indexWriter);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (ListenerType == FORWARD_INDEX_CREATOR) {

                KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
                IndexWriterConfig fwdIndexWriterConfig = new IndexWriterConfig(
                        Version.LUCENE_46, keywordAnalyzer);
                fwdIndexWriterConfig.setRAMBufferSizeMB(this.ramBufferSizeMB);
                fwdIndexWriterConfig.setOpenMode(OpenMode.CREATE);
                IndexWriter fwdIndexWriter = null;
                try {
                    FSDirectory dir = FSDirectory.open(new File(
                            SearchManager.NODE_PREFIX + "/fwdindex/temp/_" + i));
                    forwardIndexDirectories.add(dir);
                    fwdIndexWriter = new IndexWriter(dir, fwdIndexWriterConfig);
                    listener = new ForwardIndexCreator(new CodeIndexer(
                            fwdIndexWriter));
                    indexerWriters.add(fwdIndexWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            listeners.add(listener);
        }
        queue.setListeners(listeners);
    }

    public static void main(String[] args) throws IOException, ParseException,
            InterruptedException {
        long start_time = System.currentTimeMillis();
        SearchManager searchManager = null;
        EProperties properties = new EProperties();
        FileInputStream fis = null;
        System.out.println("reading Q values from properties file");
        String propertiesPath = System.getProperty("properties.location");
        System.out.println("propertiesPath: " + propertiesPath);
        fis = new FileInputStream(propertiesPath);
        try {
            properties.load(fis);
            String[] params = new String[18];
            params[0] = args[0];
            params[1] = args[1];
            params[2] = properties.getProperty("QBQ_THREADS");
            params[3] = properties.getProperty("QCQ_THREADS");
            params[4] = properties.getProperty("VCQ_THREADS");
            params[5] = properties.getProperty("RCQ_THREADS");
            params[6] = properties.getProperty("QBQ_SIZE");
            params[7] = properties.getProperty("QCQ_SIZE");
            params[8] = properties.getProperty("VCQ_SIZE");
            params[9] = properties.getProperty("RCQ_SIZE");
            params[10] = properties.getProperty("MIN_TOKENS");
            params[11] = properties.getProperty("MAX_TOKENS");

            params[12] = properties.getProperty("BTSQ_THREADS");
            params[13] = properties.getProperty("BTIIQ_THREADS");
            params[14] = properties.getProperty("BTFIQ_THREADS");
            params[15] = properties.getProperty("BTSQ_SIZE");
            params[16] = properties.getProperty("BTIIQ_SIZE");
            params[17] = properties.getProperty("BTFIQ_SIZE");
            SearchManager.DATASET_DIR = properties
                    .getProperty("DATASET_DIR_PATH");
            SearchManager.isGenCandidateStats = Boolean.parseBoolean(properties
                    .getProperty("IS_GEN_CANDIDATE_STATISTICS"));
            SearchManager.isStatusCounterOn = Boolean.parseBoolean(properties
                    .getProperty("IS_STATUS_REPORTER_ON"));
            SearchManager.printAfterEveryXQueries = Integer
                    .parseInt(properties
                            .getProperty("PRINT_STATUS_AFTER_EVERY_X_QUERIES_ARE_PROCESSED"));
            SearchManager.loggingMode = properties.getProperty("LOGGING_MODE")
                    .toUpperCase();
            SearchManager.NODE_PREFIX = properties.getProperty("NODE_PREFIX")
                    .toUpperCase();
            SearchManager.OUTPUT_DIR = properties.getProperty("OUTPUT_DIR");
            SearchManager.QUERY_DIR_PATH = properties
                    .getProperty("QUERY_DIR_PATH");
            System.out.println("Query path:" + SearchManager.QUERY_DIR_PATH);

            SearchManager.GTPM_DIR_PATH = properties
                    .getProperty("GTPM_DIR_PATH");
            searchManager = new SearchManager(params);
        } catch (IOException e) {
            System.out.println("ERROR READING PROPERTIES FILE, "
                    + e.getMessage());
            System.exit(1);
        } finally {

            if (null != fis) {
                fis.close();
            }
        }
        Util.createDirs(SearchManager.OUTPUT_DIR + SearchManager.th
                / SearchManager.MUL_FACTOR);
        String reportFileName = SearchManager.OUTPUT_DIR + SearchManager.th
                / SearchManager.MUL_FACTOR + "/report.csv";
        File reportFile = new File(reportFileName);
        if (reportFile.exists()) {
            searchManager.appendToExistingFile = true;
        } else {
            searchManager.appendToExistingFile = false;
        }
        searchManager.outputWriter = Util.openFile(reportFileName,
                searchManager.appendToExistingFile);
        if (searchManager.action.equalsIgnoreCase(ACTION_INDEX)) {
            searchManager.initIndexEnv();
            long begin_time = System.currentTimeMillis();
            searchManager.doIndex();

            System.out.println("attempting to shutdown Qs");
            while (true) {
                if (SearchManager.bagsToSortQueue.size() == 0
                        && SearchManager.bagsToInvertedIndexQueue.size() == 0
                        && SearchManager.bagsToForwardIndexQueue.size() == 0) {
                    System.out.println("shutting down BTSQ, "
                            + (System.currentTimeMillis()));
                    SearchManager.bagsToSortQueue.shutdown();
                    System.out.println("shutting down BTIIQ, "
                            + System.currentTimeMillis());
                    SearchManager.bagsToInvertedIndexQueue.shutdown();
                    System.out.println("shutting down BTFIQ, "
                            + System.currentTimeMillis());
                    SearchManager.bagsToForwardIndexQueue.shutdown();
                    break;
                } else {
                    Thread.sleep(2 * 1000);
                }
            }

            for (IndexWriter writer : searchManager.indexerWriters) {
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("merging indexes");
            searchManager.mergeindexes();
            System.out.println("merge done");
            System.out.println("indexing over!");
            searchManager.timeIndexing = System.currentTimeMillis()
                    - begin_time;
        } else if (searchManager.action.equalsIgnoreCase(ACTION_SEARCH)) {
            searchManager.initSearchEnv();
            long timeStartSearch = System.currentTimeMillis();
            searchManager.findCandidates();
            while (true) {
                if (SearchManager.queryBlockQueue.size() == 0
                        && SearchManager.queryCandidatesQueue.size() == 0
                        && SearchManager.verifyCandidateQueue.size() == 0
                        && SearchManager.reportCloneQueue.size() == 0) {
                    System.out.println("shutting down QBQ, "
                            + (System.currentTimeMillis()));
                    SearchManager.queryBlockQueue.shutdown();
                    System.out.println("shutting down QCQ, "
                            + System.currentTimeMillis());
                    SearchManager.queryCandidatesQueue.shutdown();
                    System.out.println("shutting down VCQ, "
                            + System.currentTimeMillis());
                    SearchManager.verifyCandidateQueue.shutdown();
                    System.out.println("shutting down RCQ, "
                            + System.currentTimeMillis());
                    SearchManager.reportCloneQueue.shutdown();
                    break;
                } else {
                    Thread.sleep(2 * 1000);
                }
            }
            searchManager.timeSearch = System.currentTimeMillis()
                    - timeStartSearch;
        } else if (searchManager.action.equalsIgnoreCase(ACTION_INIT)) {
            TermSorter termSorter = new TermSorter();
            termSorter.populateLocalWordFreqMap();
        }
        long end_time = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis((end_time - start_time));
        try {
            Duration duration = DatatypeFactory.newInstance().newDuration(
                    end_time - start_time);
            System.out.printf("Total run Time:  %02dh:%02dm:%02ds",
                    duration.getDays() * 24 + duration.getHours(),
                    duration.getMinutes(), duration.getSeconds());
            System.out.println();
        } catch (DatatypeConfigurationException e1) {
            e1.printStackTrace();
        }
        System.out.println("number of clone pairs detected: "
                + SearchManager.clonePairsCount);
        searchManager.timeTotal = end_time - start_time;
        searchManager.genReport();
        Util.closeOutputFile(searchManager.outputWriter);
        try {
            Util.closeOutputFile(SearchManager.clonesWriter);
        } catch (Exception e) {
            System.out.println("exception caught in main " + e.getMessage());
        }
    }

    private void mergeindexes() {
        // TODO Auto-generated method stub
        WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(
                Version.LUCENE_46);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_46, whitespaceAnalyzer);
        indexWriterConfig.setOpenMode(OpenMode.CREATE);
        IndexWriter indexWriter = null;
        try {
            FSDirectory dir = FSDirectory.open(new File(SearchManager.NODE_PREFIX+"/master/index"));
            indexWriter = new IndexWriter(dir, indexWriterConfig);
            FSDirectory[] dirs = this.invertedIndexDirectories
                    .toArray(new FSDirectory[this.invertedIndexDirectories
                            .size()]);
            indexWriter.addIndexes(dirs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
        IndexWriterConfig fwdIndexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_46, keywordAnalyzer);
        fwdIndexWriterConfig.setOpenMode(OpenMode.CREATE);
        try {

            FSDirectory dir = FSDirectory.open(new File(SearchManager.NODE_PREFIX+"/master/fwd/index"));
            indexWriter = new IndexWriter(dir, fwdIndexWriterConfig);
            FSDirectory[] dirs = this.forwardIndexDirectories
                    .toArray(new FSDirectory[this.forwardIndexDirectories
                            .size()]);
            indexWriter.addIndexes(dirs);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initIndexEnv() throws IOException, ParseException {
        TermSorter termSorter = new TermSorter();

        long timeGlobalPositionStart = System.currentTimeMillis();
        termSorter.populateGlobalPositionMap();
        SearchManager.gtpmSearcher = new CodeSearcher(Util.GTPM_INDEX_DIR,
                "key");
        this.timeGlobalTokenPositionCreation = System.currentTimeMillis()
                - timeGlobalPositionStart;
    }

    private void genReport() {
        String header = "";
        if (!this.appendToExistingFile) {
            header = "index_time, "
                    + "globalTokenPositionCreationTime,num_candidates, "
                    + "num_clonePairs, total_run_time, searchTime,"
                    + "timeSpentInSearchingCandidates,timeSpentInProcessResult,"
                    + "operation,sortTime_during_indexing\n";
        }
        header += this.timeIndexing + ",";
        header += this.timeGlobalTokenPositionCreation + ",";
        header += SearchManager.numCandidates + ",";
        header += SearchManager.clonePairsCount + ",";
        header += this.timeTotal + ",";
        header += this.timeSearch + ",";
        header += SearchManager.timeSpentInSearchingCandidates + ",";
        header += this.timeSpentInProcessResult + ",";
        if (this.action.equalsIgnoreCase("index")) {
            header += this.action + ",";
            header += this.bagsSortTime;
        } else {
            header += this.action;
        }
        Util.writeToFile(this.outputWriter, header, true);
    }

    private void doIndex() throws IOException, ParseException,
            InterruptedException {
        File datasetDir = this.getQueryDirectory();
        if (datasetDir.isDirectory()) {
            System.out.println("Directory: " + this.getQueryDirectory().getAbsolutePath());
            BufferedReader br = null;
            for (File inputFile : datasetDir.listFiles()) {
                try {
                    br = new BufferedReader(new InputStreamReader(
                            new FileInputStream(inputFile), "UTF-8"));
                    String line;
                    System.out.println(SearchManager.NODE_PREFIX + "status counter = "+ SearchManager.statusCounter);
                    while ((line = br.readLine()) != null
                            && line.trim().length() > 0) {
                        SearchManager.statusCounter += 1;
                        Bag bag = cloneHelper.deserialise(line);
                        if (null == bag
                                || bag.getSize() < SearchManager.min_tokens
                                || bag.size() > SearchManager.max_tokens) {
                            if (null == bag) {
                                System.out
                                        .println(SearchManager.NODE_PREFIX + "empty bag, ignoring. statusCounter= "
                                                + SearchManager.statusCounter);
                            } else {
                                System.out.println(SearchManager.NODE_PREFIX + "ignoring file, "
                                        + bag.getFunctionId() + ", "
                                        + bag.getId() + ", size is: "
                                        + bag.getSize() + ", statusCounter= "
                                        + SearchManager.statusCounter);
                            }
                            continue; // ignore this bag.
                        }
                        SearchManager.bagsToSortQueue.put(bag);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("File: " + datasetDir.getName()
                    + " is not a direcory. exiting now");
            System.exit(1);
        }
    }

    private void findCandidates() throws InterruptedException {
        try {
            File queryDirectory = this.getQueryDirectory();
            File[] queryFiles = this.getQueryFiles(queryDirectory);
            for (File queryFile : queryFiles) {
                System.out.println("Query File: " + queryFile);
                String filename = queryFile.getName().replaceFirst("[.][^.]+$",
                        "");
                try {

                    SearchManager.clonesWriter = Util.openFile(
                            SearchManager.OUTPUT_DIR + SearchManager.th
                                    / SearchManager.MUL_FACTOR + "/" + filename
                                    + "clones_index_WITH_FILTER.txt", false);
                } catch (IOException e) {
                    System.out.println(e.getMessage() + " exiting");
                    System.exit(1);
                }
                BufferedReader br = this.getReader(queryFile);
                String line = null;
                try {
                    QueryBlock queryBlock = null;
                    int count =0;
                    while ((line = br.readLine()) != null
                            && line.trim().length() > 0) {
                        long start_time = System.currentTimeMillis();
                        try {
                            queryBlock = this.getNextQueryBlock(line);
                            count++;
                            if (queryBlock.getSize() < SearchManager.min_tokens
                                    || queryBlock.getSize() > SearchManager.max_tokens) {
                                System.out.println("ignoring query, REASON:  "
                                        + queryBlock.getFunctionId() + ", "
                                        + queryBlock.getId() + ", size: "
                                        + queryBlock.getSize());
                                continue; // ignore this query
                            }
                            SearchManager.queryBlockQueue.put(queryBlock);
                            if (SearchManager.isStatusCounterOn) {
                                SearchManager.statusCounter += 1;
                                if ((SearchManager.statusCounter % SearchManager.printAfterEveryXQueries) == 0) {
                                    long end_time = System.currentTimeMillis();
                                    Duration duration;
                                    try {
                                        duration = DatatypeFactory
                                                .newInstance().newDuration(
                                                        end_time - start_time);
                                        System.out
                                                .printf(SearchManager.NODE_PREFIX
                                                        + ", queriesBlock created: "
                                                        + queryBlock.getFunctionId() + ","
                                                        + queryBlock.getId() +", size "
                                                        + queryBlock.getSize()
                                                        + SearchManager.statusCounter
                                                        + " time taken: %02dh:%02dm:%02ds",
                                                        duration.getDays()
                                                                * 24
                                                                + duration
                                                                        .getHours(),
                                                        duration.getMinutes(),
                                                        duration.getSeconds());
                                        System.out.println();
                                    } catch (DatatypeConfigurationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            System.out.println(SearchManager.NODE_PREFIX + ", line number: "+ count);
                        } catch (ParseException e) {
                            System.out
                                    .println("catching parseException, dont worry");
                            System.out
                                    .println(e.getMessage()
                                            + " skiping this query block, parse exception: "
                                            + line);
                            // e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            System.out
                                    .println(e.getMessage()
                                            + " skiping this query block, illegal args: "
                                            + line);
                            // e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    System.out
                            .println(e.getMessage() + " skiping to next file");
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage() + "exiting");
            System.exit(1);
        }
    }

    private void initSearchEnv() {
        SearchManager.fwdSearcher = new CodeSearcher(Util.FWD_INDEX_DIR, "id");
        SearchManager.searcher = new CodeSearcher(Util.INDEX_DIR, "tokens");
        SearchManager.gtpmSearcher = new CodeSearcher(Util.GTPM_INDEX_DIR,
                "key");
    }

    public static synchronized void updateNumCandidates(int num) {
        SearchManager.numCandidates += num;
    }

    public static synchronized void updateClonePairsCount(int num) {
        SearchManager.clonePairsCount += num;
    }

    private BufferedReader getReader(File queryFile)
            throws FileNotFoundException {
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(queryFile));
        return br;
    }

    private File getQueryDirectory() throws FileNotFoundException {
        File queryDir = new File(QUERY_DIR_PATH);
        if (!queryDir.isDirectory()) {
            throw new FileNotFoundException("directory not found.");
        } else {
            System.out.println("Directory: " + queryDir.getName());
            return queryDir;
        }
    }

    private File[] getQueryFiles(File queryDirectory) {
        return queryDirectory.listFiles();
    }

    private QueryBlock getNextQueryBlock(String line) throws ParseException,
            IllegalArgumentException {
        List<Entry<String, TokenInfo>> listOfTokens = new ArrayList<Entry<String, TokenInfo>>();
        QueryBlock queryBlock = this.cloneHelper.deserialiseToQueryBlock(line,
                listOfTokens);
        if (queryBlock.getSize() > SearchManager.min_tokens
                && queryBlock.getSize() < SearchManager.max_tokens) {
            Collections.sort(listOfTokens,
                    new Comparator<Entry<String, TokenInfo>>() {
                        public int compare(Entry<String, TokenInfo> tfFirst,
                                Entry<String, TokenInfo> tfSecond) {
                            long position1 = 0;
                            position1 = SearchManager.gtpmSearcher
                                    .getPosition(tfFirst.getKey());
                            long position2 = 0;
                            position2 = SearchManager.gtpmSearcher
                                    .getPosition(tfSecond.getKey());
                            if (position1 - position2 != 0) {
                                return (int) (position1 - position2);
                            } else {
                                return 1;
                            }
                        }
                    });
            int position = 0;
            for (Entry<String, TokenInfo> entry : listOfTokens) {
                TokenInfo tokenInfo = entry.getValue();
                if (position < queryBlock.getPrefixSize()) {
                    queryBlock.getPrefixMap().put(entry.getKey(), tokenInfo);
                    position += tokenInfo.getFrequency();
                    queryBlock.setPrefixMapSize(position);
                } else {
                    queryBlock.getSuffixMap().put(entry.getKey(), tokenInfo);
                    position += tokenInfo.getFrequency();
                }
                tokenInfo.setPosition(position);
            }
        }

        return queryBlock;
    }
}
