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
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import models.Bag;
import models.CandidatePair;
import models.CandidateProcessor;
import models.CandidateSearcher;
import models.ClonePair;
import models.CloneReporter;
import models.CloneValidator;
import models.IListener;
import models.QueryBlock;
import models.QueryCandidates;
import models.Queue;
import models.TokenInfo;
import noindex.CloneHelper;
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
    private CloneHelper cloneHelper;
    private static String QUERY_DIR_PATH;
    public static String DATASET_DIR;
    public static Writer clonesWriter; // writer to write the output
    public static float th; // args[2]
                            // search
    private final static String ACTION_INDEX = "index";
    private final static String ACTION_SEARCH = "search";
    private final static int CANDIDATE_SEARCHER = 1;
    private final static int CANDIDATE_PROCESSOR = 2;
    private final static int CLONE_VALIDATOR = 3;
    private final static int CLONE_REPORTER = 4;

    private CodeIndexer indexer;
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
    private Writer cloneSiblingCountWriter;
    public static final Integer MUL_FACTOR = 100;
    int deletemeCounter = 0;
    private double ramBufferSizeMB;
    private long bagsSortTime;
    public static Queue<QueryCandidates> queryCandidatesQueue;
    public static Queue<QueryBlock> queryBlockQueue;
    public static Queue<ClonePair> reportCloneQueue;
    public static Queue<CandidatePair> verifyCandidateQueue;
    public static Object lock = new Object();
    private int qbq_thread_count;
    private int qcq_thread_count;
    private int vcq_thread_count;
    private int rcq_thread_count;
    private int qbq_size;
    private int qcq_size;
    private int vcq_size;
    private int rcq_size;
    public static boolean isGenCandidateStats;
    public static int statusCounter;
    public static boolean isStatusCounterOn;
    public static int printAfterEveryXQueries;
    public static String loggingMode;

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
        SearchManager.th = (Float.parseFloat(args[1]) * SearchManager.MUL_FACTOR);
        this.qbq_thread_count = Integer.parseInt(args[2]);
        this.qcq_thread_count = Integer.parseInt(args[3]);
        this.vcq_thread_count = Integer.parseInt(args[4]);
        this.rcq_thread_count = Integer.parseInt(args[5]);
        this.qbq_size = Integer.parseInt(args[6]);
        this.qcq_size = Integer.parseInt(args[7]);
        this.vcq_size = Integer.parseInt(args[8]);
        this.rcq_size = Integer.parseInt(args[9]);
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

    public static void main(String[] args) throws IOException, ParseException,
            InterruptedException {
        long start_time = System.currentTimeMillis();
        SearchManager searchManager = null;
        Properties properties = new Properties();
        FileInputStream fis = null;
        System.out.println("reading Q values from properties file");
        fis = new FileInputStream("sourcerer-cc.properties");
        try {
            properties.load(fis);
            String[] params = new String[10];
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
            searchManager = new SearchManager(params);
        } catch (IOException e) {
            System.out.println("ERROR READING PROPERTIES FILE, "
                    + e.getMessage());
            System.exit(1);
        } finally {
            SearchManager.QUERY_DIR_PATH = properties
                    .getProperty("QUERY_DIR_PATH");
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
            if (null != fis) {
                fis.close();
            }
        }
        Util.createDirs("output" + SearchManager.th / SearchManager.MUL_FACTOR);
        String reportFileName = "output" + SearchManager.th
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
        /*
         * System.out.println( "total run time: " + cal.get(Calendar.HOUR)+
         * "::"+ cal.get(Calendar.MINUTE)+ "::"+ cal.get(Calendar.SECOND));
         */
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

    private void initIndexEnv() throws IOException, ParseException {
        TermSorter termSorter = new TermSorter();
        long timeGlobalPositionStart = System.currentTimeMillis();
        try {
            FileUtils.deleteDirectory(new File(Util.GTPM_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Util.createDirs(Util.GTPM_DIR);
        termSorter.populateGlobalPositionMap();
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

    private void doIndex() throws IOException, ParseException {

        KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
        WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(
                Version.LUCENE_46);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_46, whitespaceAnalyzer);
        IndexWriterConfig fwdIndexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_46, keywordAnalyzer);
        indexWriterConfig.setOpenMode(OpenMode.CREATE);// add new
                                                       // docs to
                                                       // exisiting
                                                       // index
        TieredMergePolicy mergePolicy = (TieredMergePolicy) indexWriterConfig
                .getMergePolicy();

        mergePolicy.setNoCFSRatio(0);// what was this for?
        mergePolicy.setMaxCFSSegmentSizeMB(0); // what was this for?
        // indexWriterConfig.setMergePolicy(mergePolicy);
        indexWriterConfig.setRAMBufferSizeMB(this.ramBufferSizeMB);
        fwdIndexWriterConfig.setOpenMode(OpenMode.CREATE);
        fwdIndexWriterConfig.setRAMBufferSizeMB(this.ramBufferSizeMB);
        // fwdIndexWriterConfig.setMergePolicy(mergePolicy);
        IndexWriter indexWriter;
        IndexWriter fwdIndexWriter = null;
        CodeIndexer fwdIndexer = null;
        try {
            indexWriter = new IndexWriter(FSDirectory.open(new File(
                    Util.INDEX_DIR)), indexWriterConfig);
            this.indexer = new CodeIndexer(Util.INDEX_DIR, indexWriter,
                    cloneHelper, SearchManager.th);
            fwdIndexWriter = new IndexWriter(FSDirectory.open(new File(
                    Util.FWD_INDEX_DIR)), fwdIndexWriterConfig);
            fwdIndexer = new CodeIndexer(Util.FWD_INDEX_DIR, fwdIndexWriter,
                    cloneHelper, SearchManager.th);
            File datasetDir = new File(SearchManager.DATASET_DIR);

            if (datasetDir.isDirectory()) {
                System.out.println("Directory: " + datasetDir.getName());
                BufferedReader br = null;
                for (File inputFile : datasetDir.listFiles()) {
                    try {
                        System.out.println("indexing file : " + inputFile.getName());
                        br = new BufferedReader(new InputStreamReader(
                                new FileInputStream(inputFile), "UTF-8"));
                        String line;
                        while ((line = br.readLine()) != null
                                && line.trim().length() > 0) {
                            Bag bag = cloneHelper.deserialise(line);
                            long startTime = System.currentTimeMillis();
                            Util.sortBag(bag);
                            this.bagsSortTime += System.currentTimeMillis()
                                    - startTime;
                            fwdIndexer.fwdIndexCodeBlock(bag);
                            this.indexer.indexCodeBlock(bag);
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
                    // fwdIndexer.createFwdIndex(inputFile);
                }
            } else {
                System.out.println("File: " + datasetDir.getName()
                        + " is not a direcory. exiting now");
                System.exit(1);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage() + ", exiting now.");
            System.exit(1);
        } finally {
            fwdIndexer.closeIndexWriter();
            this.indexer.closeIndexWriter();
        }
    }

    private void findCandidates() throws InterruptedException {
        long start_time = System.currentTimeMillis();
        try {
            File queryDirectory = this.getQueryDirectory();
            File[] queryFiles = this.getQueryFiles(queryDirectory);
            for (File queryFile : queryFiles) {
                System.out.println("Query File: " + queryFile);
                String filename = queryFile.getName().replaceFirst("[.][^.]+$",
                        "");
                try {

                    SearchManager.clonesWriter = Util.openFile("output"
                            + SearchManager.th / SearchManager.MUL_FACTOR + "/"
                            + filename + "clones_index_WITH_FILTER.txt", false);
                } catch (IOException e) {
                    System.out.println(e.getMessage() + " exiting");
                    System.exit(1);
                }
                BufferedReader br = this.getReader(queryFile);
                String line = null;
                try {
                    QueryBlock queryBlock = null;
                    while ((line = br.readLine()) != null
                            && line.trim().length() > 0) {
                        try {
                            queryBlock = this.getNextQueryBlock(line);
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
                                                .printf("queries processed: "
                                                        + SearchManager.statusCounter
                                                        + " time taken: %02dh:%02dm:%02ds",
                                                        duration.getDays()
                                                                * 24
                                                                + duration
                                                                        .getHours(),
                                                        duration.getMinutes(),
                                                        duration.getSeconds());
                                        start_time = end_time;
                                        System.out.println();
                                    } catch (DatatypeConfigurationException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            }
                            SearchManager.queryBlockQueue.put(queryBlock);
                        } catch (ParseException e) {
                            System.out.println(e.getMessage()
                                    + " skiping to next bag");
                            e.printStackTrace();
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
        // testGson = new TestGson(); // remove this line later. for
        // validation only.
        // testGson.populateMap(); // this is for validation only, remove this
        // line.
        Util.createDirs("output" + SearchManager.th / SearchManager.MUL_FACTOR
                + "/cloneGroups/");
        try {
            this.cloneSiblingCountWriter = Util.openFile("output"
                    + SearchManager.th / SearchManager.MUL_FACTOR
                    + "/cloneGroups/siblings_count.csv", false);
            Util.writeToFile(this.cloneSiblingCountWriter,
                    "query_block_id,siblings", true);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        TermSorter termSorter = new TermSorter();
        try {

            long timeGlobalPositionStart = System.currentTimeMillis();
            termSorter.populateGlobalPositionMap();
            this.timeGlobalTokenPositionCreation = System.currentTimeMillis()
                    - timeGlobalPositionStart;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Error in Parsing: " + e.getMessage());
            e.printStackTrace();
        }
        SearchManager.fwdSearcher = new CodeSearcher(Util.FWD_INDEX_DIR, "id"); // searches
                                                                                // on
                                                                                // fwd
                                                                                // index
        SearchManager.searcher = new CodeSearcher(Util.INDEX_DIR, "tokens");

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

    private QueryBlock getNextQueryBlock(String line) throws ParseException {
        List<Entry<String, TokenInfo>> listOfTokens = new ArrayList<Entry<String, TokenInfo>>();
        QueryBlock queryBlock = this.cloneHelper.deserialiseToQueryBlock(line,
                listOfTokens);

        Collections.sort(listOfTokens,
                new Comparator<Entry<String, TokenInfo>>() {
                    public int compare(Entry<String, TokenInfo> tfFirst,
                            Entry<String, TokenInfo> tfSecond) {
                        long position1 = 0;
                        try {
                            position1 = TermSorter.globalTokenPositionMap
                                    .get(tfFirst.getKey());
                        } catch (Exception e) {
                            position1 = -1;
                        }
                        long position2 = 0;
                        try {
                            position2 = TermSorter.globalTokenPositionMap
                                    .get(tfSecond.getKey());
                        } catch (Exception e) {
                            position2 = -1;
                        }
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
        return queryBlock;
    }
}
