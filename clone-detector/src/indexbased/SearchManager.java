/**
 * 
 */
package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import models.Bag;
import models.BagSorter;
import models.CandidatePair;
import models.CandidateProcessor;
import models.CandidateSearcher;
import models.ClonePair;
import models.CloneReporter;
import models.CloneValidator;
import models.ForwardIndexCreator;
import models.ITokensFileProcessor;
import models.InvertedIndexCreator;
import models.QueryBlock;
import models.QueryCandidates;
import models.QueryFileProcessor;
import models.QueryLineProcessor;
import models.Shard;
import models.ThreadedChannel;
import net.jmatrix.eproperties.EProperties;
import noindex.CloneHelper;
import utility.TokensFileReader;
import utility.Util;
import validation.TestGson;

/**
 * @author vaibhavsaini
 * 
 */
public class SearchManager {
    private static long clonePairsCount;
    public static ArrayList<CodeSearcher> searcher;
    public static ArrayList<CodeSearcher> fwdSearcher;
    public static CodeSearcher gtpmSearcher;
    public CloneHelper cloneHelper;
    public static String QUERY_DIR_PATH;
    public static String DATASET_DIR;
    public static String WFM_DIR_PATH;
    public static Writer clonesWriter; // writer to write the output
    public static float th; // args[2]
                            // search
    private final static String ACTION_INDEX = "index";
    private final static String ACTION_SEARCH = "search";

    private long timeSpentInProcessResult;
    public static long timeSpentInSearchingCandidates;
    private long timeIndexing;
    private long timeGlobalTokenPositionCreation;
    private long timeSearch;
    private static long numCandidates;
    private Writer reportWriter;
    private long timeTotal;
    private String action;
    public boolean appendToExistingFile;
    TestGson testGson;
    public static final Integer MUL_FACTOR = 100;
    private static final String ACTION_INIT = "init";
    int deletemeCounter = 0;
    public static double ramBufferSizeMB;
    private long bagsSortTime;
    public static ThreadedChannel<String> queryLineQueue;
    public static ThreadedChannel<QueryBlock> queryBlockQueue;
    public static ThreadedChannel<QueryCandidates> queryCandidatesQueue;
    public static ThreadedChannel<CandidatePair> verifyCandidateQueue;
    public static ThreadedChannel<ClonePair> reportCloneQueue;

    public static ThreadedChannel<Bag> bagsToSortQueue;
    public static ThreadedChannel<Bag> bagsToInvertedIndexQueue;
    public static ThreadedChannel<Bag> bagsToForwardIndexQueue;
    public static SearchManager theInstance;
    public static Map<Integer, List<FSDirectory>> invertedIndexDirectoriesOfShard;
    public static Map<Integer, List<FSDirectory>> forwardIndexDirectoriesOfShard;
    // private List<FSDirectory> invertedIndexDirectories;
    // private List<FSDirectory> forwardIndexDirectories;
    public static List<IndexWriter> indexerWriters;
    private static EProperties properties = new EProperties();

    public static Object lock = new Object();
    private int qlq_thread_count;
    private int qbq_thread_count;
    private int qcq_thread_count;
    private int vcq_thread_count;
    private int rcq_thread_count;
    private int threadsToProcessBagsToSortQueue;
    private int threadToProcessIIQueue;
    private int threadsToProcessFIQueue;
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
    public static List<Shard> shards;
    public Set<Long> completedQueries;
    private boolean isSharding;

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
        SearchManager.ramBufferSizeMB = 100 * 1;
        this.bagsSortTime = 0;
        this.action = args[0];
        SearchManager.statusCounter = 0;
        SearchManager.globalWordFreqMap = new HashMap<String, Long>();
        try {

            SearchManager.th = (Float.parseFloat(args[1]) * SearchManager.MUL_FACTOR);

            this.qlq_thread_count = Integer.parseInt(properties.getProperty("QLQ_THREADS", "1"));
            this.qbq_thread_count = Integer.parseInt(properties.getProperty("QBQ_THREADS", "1"));
            this.qcq_thread_count = Integer.parseInt(properties.getProperty("QCQ_THREADS", "1"));
            this.vcq_thread_count = Integer.parseInt(properties.getProperty("VCQ_THREADS", "1"));
            this.rcq_thread_count = Integer.parseInt(properties.getProperty("RCQ_THREADS", "1"));
            SearchManager.min_tokens = Integer.parseInt(properties.getProperty("MIN_TOKENS", "65"));
            SearchManager.max_tokens = Integer.parseInt(properties.getProperty("MAX_TOKENS", "500000"));
            this.threadsToProcessBagsToSortQueue = Integer.parseInt(properties.getProperty("BTSQ_THREADS", "1"));
            this.threadToProcessIIQueue = Integer.parseInt(properties.getProperty("BTIIQ_THREADS", "1"));
            this.threadsToProcessFIQueue = Integer.parseInt(properties.getProperty("BTFIQ_THREADS", "1"));
            this.isSharding = Boolean.parseBoolean(properties.getProperty("IS_SHARDING"));

        } catch (NumberFormatException e) {
            System.out.println(e.getMessage() + ", exiting now");
            System.exit(1);
        }
        if (this.action.equals(ACTION_SEARCH)) {
            this.completedQueries = new HashSet<Long>();

            this.createShards(false);

            System.out.println("action: " + this.action + System.lineSeparator() + "threshold: " + args[1]
                    + System.lineSeparator() + " QLQ_THREADS: " + this.qlq_thread_count + " QBQ_THREADS: "
                    + this.qbq_thread_count + " QCQ_THREADS: " + this.qcq_thread_count + " VCQ_THREADS: "
                    + this.vcq_thread_count + " RCQ_THREADS: " + this.rcq_thread_count + System.lineSeparator());
            SearchManager.queryLineQueue = new ThreadedChannel<String>(this.qlq_thread_count, QueryLineProcessor.class);
            SearchManager.queryBlockQueue = new ThreadedChannel<QueryBlock>(this.qbq_thread_count,
                    CandidateSearcher.class);
            SearchManager.queryCandidatesQueue = new ThreadedChannel<QueryCandidates>(this.qcq_thread_count,
                    CandidateProcessor.class);
            SearchManager.verifyCandidateQueue = new ThreadedChannel<CandidatePair>(this.vcq_thread_count,
                    CloneValidator.class);
            SearchManager.reportCloneQueue = new ThreadedChannel<ClonePair>(this.rcq_thread_count, CloneReporter.class);
        } else if (this.action.equals(ACTION_INDEX)) {
            indexerWriters = new ArrayList<IndexWriter>();
            this.createShards(true);

            System.out.println("action: " + this.action + System.lineSeparator() + "threshold: " + args[1]
                    + System.lineSeparator() + " BQ_THREADS: " + this.threadsToProcessBagsToSortQueue
                    + System.lineSeparator() + " SBQ_THREADS: " + this.threadToProcessIIQueue + System.lineSeparator()
                    + " IIQ_THREADS: " + this.threadsToProcessFIQueue + System.lineSeparator());

            SearchManager.bagsToSortQueue = new ThreadedChannel<Bag>(this.threadsToProcessBagsToSortQueue,
                    BagSorter.class);
            SearchManager.bagsToInvertedIndexQueue = new ThreadedChannel<Bag>(this.threadToProcessIIQueue,
                    InvertedIndexCreator.class);
            SearchManager.bagsToForwardIndexQueue = new ThreadedChannel<Bag>(this.threadsToProcessFIQueue,
                    ForwardIndexCreator.class);
        }

    }

    private void createShards(boolean forWriting) {
        int minTokens = SearchManager.min_tokens;
        int maxTokens = SearchManager.max_tokens;
        int shardId = 1;
        SearchManager.invertedIndexDirectoriesOfShard = new HashMap<Integer, List<FSDirectory>>();
        SearchManager.forwardIndexDirectoriesOfShard = new HashMap<Integer, List<FSDirectory>>();
        SearchManager.shards = new ArrayList<Shard>();
        if (this.isSharding) {
            String shardSegment = properties.getProperty("SHARD_MAX_NUM_TOKENS");
            System.out.println("shardSegments String is : " + shardSegment);
            String[] shardSegments = shardSegment.split(",");
            for (String segment : shardSegments) {
                // create shards
                maxTokens = Integer.parseInt(segment);
                Shard shard = new Shard(shardId, minTokens, maxTokens, forWriting);
                SearchManager.shards.add(shard);
                minTokens = maxTokens + 1;
                shardId++;
            }
            // create the last shard
            Shard shard = new Shard(shardId, minTokens, SearchManager.max_tokens, forWriting);
            SearchManager.shards.add(shard);
        } else {
            Shard shard = new Shard(shardId, SearchManager.min_tokens, SearchManager.max_tokens, forWriting);
            SearchManager.shards.add(shard);
        }
        System.out.println("Number of shards created: " + SearchManager.shards.size());
    }

    // this bag needs to be indexed in following shards
    public static List<Shard> getShards(Bag bag) {
        List<Shard> shardsToReturn = new ArrayList<Shard>();
        for (Shard shard : SearchManager.shards)
            if (bag.getSize() >= shard.getMinBagSizeToIndex() && bag.getSize() <= shard.getMaxBagSizeToIndex()) {
                shardsToReturn.add(shard);
            }

        return shardsToReturn;
    }

    // This query needs to be directed to the following shard
    public static Shard getShard(QueryBlock qb) {
        for (Shard shard : SearchManager.shards)
            if (qb.getSize() >= shard.getMinSize() && qb.getSize() <= shard.getMaxSize()) {
                return shard;
            }

        return null;
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        long start_time = System.currentTimeMillis();
        FileInputStream fis = null;
        System.out.println("reading Q values from properties file");
        String propertiesPath = System.getProperty("properties.location");
        System.out.println("propertiesPath: " + propertiesPath);
        fis = new FileInputStream(propertiesPath);
        try {
            properties.load(fis);
            String[] params = new String[2];
            params[0] = args[0];
            params[1] = args[1];

            SearchManager.DATASET_DIR = properties.getProperty("DATASET_DIR_PATH");
            SearchManager.isGenCandidateStats = Boolean
                    .parseBoolean(properties.getProperty("IS_GEN_CANDIDATE_STATISTICS"));
            SearchManager.isStatusCounterOn = Boolean.parseBoolean(properties.getProperty("IS_STATUS_REPORTER_ON"));
            SearchManager.printAfterEveryXQueries = Integer
                    .parseInt(properties.getProperty("PRINT_STATUS_AFTER_EVERY_X_QUERIES_ARE_PROCESSED"));
            SearchManager.loggingMode = properties.getProperty("LOGGING_MODE").toUpperCase();
            SearchManager.NODE_PREFIX = properties.getProperty("NODE_PREFIX").toUpperCase();
            SearchManager.OUTPUT_DIR = properties.getProperty("OUTPUT_DIR");
            SearchManager.QUERY_DIR_PATH = properties.getProperty("QUERY_DIR_PATH");
            System.out.println("Query path:" + SearchManager.QUERY_DIR_PATH);

            SearchManager.WFM_DIR_PATH = properties.getProperty("WFM_DIR_PATH");
            theInstance = new SearchManager(params);
        } catch (IOException e) {
            System.out.println("ERROR READING PROPERTIES FILE, " + e.getMessage());
            System.exit(1);
        } finally {

            if (null != fis) {
                fis.close();
            }
        }
        System.out.println(SearchManager.NODE_PREFIX + " MAX_TOKENS=" + max_tokens + " MIN_TOKENS=" + min_tokens);

        Util.createDirs(SearchManager.OUTPUT_DIR + SearchManager.th / SearchManager.MUL_FACTOR);
        String reportFileName = SearchManager.OUTPUT_DIR + SearchManager.th / SearchManager.MUL_FACTOR + "/report.csv";
        File reportFile = new File(reportFileName);
        if (reportFile.exists()) {
            theInstance.appendToExistingFile = true;
        } else {
            theInstance.appendToExistingFile = false;
        }
        theInstance.reportWriter = Util.openFile(reportFileName, theInstance.appendToExistingFile);
        if (theInstance.action.equalsIgnoreCase(ACTION_INDEX)) {
            theInstance.initIndexEnv();
            long begin_time = System.currentTimeMillis();
            theInstance.doIndex();

            System.out.println("attempting to shutdown Qs");
            System.out.println(SearchManager.NODE_PREFIX + ", shutting down BTSQ, " + (System.currentTimeMillis()));
            SearchManager.bagsToSortQueue.shutdown();
            System.out.println(SearchManager.NODE_PREFIX + "shutting down BTIIQ, " + System.currentTimeMillis());
            SearchManager.bagsToInvertedIndexQueue.shutdown();
            System.out.println(SearchManager.NODE_PREFIX + "shutting down BTFIQ, " + System.currentTimeMillis());
            SearchManager.bagsToForwardIndexQueue.shutdown();

            for (Shard shard : SearchManager.shards) {
                shard.closeInvertedIndexWriter();
                shard.closeForwardIndexWriter();
            }
            /*
             * System.out.println("merging indexes");
             * theInstance.mergeindexes(); System.out.println("merge done");
             */
            System.out.println("indexing over!");
            theInstance.timeIndexing = System.currentTimeMillis() - begin_time;
        } else if (theInstance.action.equalsIgnoreCase(ACTION_SEARCH)) {
            theInstance.initSearchEnv();
            long timeStartSearch = System.currentTimeMillis();
            System.out.println(NODE_PREFIX + " Starting to search");
            theInstance.populateCompletedQueries();
            theInstance.findCandidates();

            SearchManager.queryLineQueue.shutdown();
            System.out.println("shutting down QLQ, " + System.currentTimeMillis());
            System.out.println("shutting down QBQ, " + (System.currentTimeMillis()));
            SearchManager.queryBlockQueue.shutdown();
            System.out.println("shutting down QCQ, " + System.currentTimeMillis());
            SearchManager.queryCandidatesQueue.shutdown();
            System.out.println("shutting down VCQ, " + System.currentTimeMillis());
            SearchManager.verifyCandidateQueue.shutdown();
            System.out.println("shutting down RCQ, " + System.currentTimeMillis());
            SearchManager.reportCloneQueue.shutdown();
            theInstance.timeSearch = System.currentTimeMillis() - timeStartSearch;
        } else if (theInstance.action.equalsIgnoreCase(ACTION_INIT)) {
            WordFrequencyStore wfs = new WordFrequencyStore();
            wfs.populateLocalWordFreqMap();
        }
        long end_time = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis((end_time - start_time));
        try {
            Duration duration = DatatypeFactory.newInstance().newDuration(end_time - start_time);
            System.out.printf("Total run Time:  %02dh:%02dm:%02ds", duration.getDays() * 24 + duration.getHours(),
                    duration.getMinutes(), duration.getSeconds());
            System.out.println();
        } catch (DatatypeConfigurationException e1) {
            e1.printStackTrace();
        }
        System.out.println("number of clone pairs detected: " + SearchManager.clonePairsCount);
        theInstance.timeTotal = end_time - start_time;
        theInstance.genReport();
        Util.closeOutputFile(theInstance.reportWriter);
        try {
            Util.closeOutputFile(SearchManager.clonesWriter);
        } catch (Exception e) {
            System.out.println("exception caught in main " + e.getMessage());
        }
    }

    private void populateCompletedQueries() {
        // TODO Auto-generated method stub
        BufferedReader br = null;
        String filename = "completed_queries.txt";
        int count = 1;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    if (line.trim().length() > 0) {
                        this.completedQueries.add(Long.parseLong(line.trim()));
                        count++;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(
                            SearchManager.NODE_PREFIX + ", error in parsing:" + e.getMessage() + ", line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(SearchManager.NODE_PREFIX + ", " + filename + " not found");
        } catch (UnsupportedEncodingException e) {
            System.out.println(SearchManager.NODE_PREFIX + ", error in populateCompleteQueries" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(SearchManager.NODE_PREFIX + ", error in populateCompleteQueries IO" + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("queries completed already: " + this.completedQueries.size() + ", count: " + count);
    }

    private void initIndexEnv() throws IOException, ParseException {
        // TermSorter termSorter = new TermSorter();

        long timeGlobalPositionStart = System.currentTimeMillis();
        SearchManager.gtpmSearcher = new CodeSearcher(Util.GTPM_INDEX_DIR, "key");
        this.timeGlobalTokenPositionCreation = System.currentTimeMillis() - timeGlobalPositionStart;
    }

    private void genReport() {
        String header = "";
        if (!this.appendToExistingFile) {
            header = "index_time, " + "globalTokenPositionCreationTime,num_candidates, "
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
        Util.writeToFile(this.reportWriter, header, true);
    }

    private void doIndex() throws InterruptedException, FileNotFoundException {
        File datasetDir = this.getQueryDirectory();
        if (datasetDir.isDirectory()) {
            System.out.println("Directory: " + this.getQueryDirectory().getAbsolutePath());
            for (File inputFile : datasetDir.listFiles()) {
                try {
                    TokensFileReader tfr = new TokensFileReader(SearchManager.NODE_PREFIX, inputFile,
                            SearchManager.max_tokens, new ITokensFileProcessor() {
                                public void processLine(String line) throws ParseException {
                                    Bag bag = cloneHelper.deserialise(line);
                                    if (null == bag || bag.getSize() < SearchManager.min_tokens) {
                                        if (null == bag) {
                                            System.out.println(
                                                    SearchManager.NODE_PREFIX + " empty bag, ignoring. statusCounter= "
                                                            + SearchManager.statusCounter);
                                        } else {
                                            System.out.println(SearchManager.NODE_PREFIX + " ignoring bag " + ", " + bag
                                                    + ", statusCounter=" + SearchManager.statusCounter);
                                        }
                                        return; // ignore this bag.
                                    }
                                    try {
                                        SearchManager.bagsToSortQueue.send(bag);
                                    } catch (Exception e) {
                                        System.out.println(SearchManager.NODE_PREFIX + "Unable to send bag "
                                                + bag.getId() + " to queue");
                                        e.printStackTrace();
                                    }
                                }
                            });
                    tfr.read();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println(SearchManager.NODE_PREFIX + ", something nasty, exiting. counter:"
                            + SearchManager.statusCounter);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        } else {
            System.out.println("File: " + datasetDir.getName() + " is not a directory. Exiting now");
            System.exit(1);
        }
    }

    private void findCandidates() throws InterruptedException {
        try {
            File queryDirectory = this.getQueryDirectory();
            File[] queryFiles = this.getQueryFiles(queryDirectory);
            QueryFileProcessor queryFileProcessor = new QueryFileProcessor();
            for (File queryFile : queryFiles) {
                // System.out.println("Query File: " + queryFile);
                String filename = queryFile.getName().replaceFirst("[.][^.]+$", "");
                try {
                    String cloneReportFileName = SearchManager.OUTPUT_DIR + SearchManager.th / SearchManager.MUL_FACTOR
                            + "/" + filename + "clones_index_WITH_FILTER.txt";
                    File cloneReportFile = new File(cloneReportFileName);
                    if (cloneReportFile.exists()) {
                        this.appendToExistingFile = true;
                    } else {
                        this.appendToExistingFile = false;
                    }
                    SearchManager.clonesWriter = Util
                            .openFile(SearchManager.OUTPUT_DIR + SearchManager.th / SearchManager.MUL_FACTOR + "/"
                                    + filename + "clones_index_WITH_FILTER.txt", this.appendToExistingFile);
                } catch (IOException e) {
                    System.out.println(e.getMessage() + " exiting");
                    System.exit(1);
                }
                try {
                    TokensFileReader tfr = new TokensFileReader(SearchManager.NODE_PREFIX, queryFile,
                            SearchManager.max_tokens, queryFileProcessor);
                    tfr.read();
                } catch (IOException e) {
                    System.out.println(e.getMessage() + " skiping to next file");
                } catch (ParseException e) {
                    System.out.println(SearchManager.NODE_PREFIX + "parseException caught. message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage() + "exiting");
            System.exit(1);
        }
    }

    private void initSearchEnv() {
        SearchManager.fwdSearcher = new ArrayList<CodeSearcher>();
        SearchManager.searcher = new ArrayList<CodeSearcher>();
        for (Shard shard : SearchManager.shards) {
            SearchManager.fwdSearcher.add(new CodeSearcher(Util.FWD_INDEX_DIR + "/" + shard.getId(), "id"));
            SearchManager.searcher.add(new CodeSearcher(Util.INDEX_DIR + "/" + shard.getId(), "tokens"));
        }
        SearchManager.gtpmSearcher = new CodeSearcher(Util.GTPM_INDEX_DIR, "key");
    }

    public static synchronized void updateNumCandidates(int num) {
        SearchManager.numCandidates += num;
    }

    public static synchronized void updateClonePairsCount(int num) {
        SearchManager.clonePairsCount += num;
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

}
