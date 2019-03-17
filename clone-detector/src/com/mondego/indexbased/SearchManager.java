package com.mondego.indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jmatrix.eproperties.EProperties;
import net.jmatrix.eproperties.Key;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import com.mondego.models.Bag;
import com.mondego.models.BagSorter;
import com.mondego.models.CandidatePair;
import com.mondego.models.CandidateProcessor;
import com.mondego.models.CandidateSearcher;
import com.mondego.models.ClonePair;
import com.mondego.models.CloneReporter;
import com.mondego.models.CloneValidator;
import com.mondego.models.ForwardIndexCreator;
import com.mondego.models.ITokensFileProcessor;
import com.mondego.models.InvertedIndexCreator;
import com.mondego.models.QueryBlock;
import com.mondego.models.QueryCandidates;
import com.mondego.models.QueryFileProcessor;
import com.mondego.models.QueryLineProcessor;
import com.mondego.models.Shard;
import com.mondego.models.ThreadedChannel;
import com.mondego.noindex.CloneHelper;
import com.mondego.utility.TokensFileReader;
import com.mondego.utility.Util;
import com.mondego.validation.TestGson;

public class SearchManager {
    private static long clonePairsCount = 0;
    public static ArrayList<CodeSearcher> searcher;
    public static ArrayList<CodeSearcher> fwdSearcher;
    public static CodeSearcher gtpmSearcher;
    public CloneHelper cloneHelper = new CloneHelper();
    public static String QUERY_DIR_PATH;
    public static String DATASET_DIR;
    public static String WFM_DIR_PATH;
    // writer to write the output
    public static Writer clonesWriter;
    // writes the lines processed during search. for recovery purpose.
    public static Writer recoveryWriter;
    public static float th; // args[2]
    private static final String ACTION_INIT = "init";
    public final static String ACTION_INDEX = "index";
    public final static String ACTION_SEARCH = "search";

    private long timeSpentInProcessResult = 0;
    public static long timeSpentInSearchingCandidates = 0;
    private long timeIndexing = 0;
    private long timeGlobalTokenPositionCreation = 0;
    private long timeSearch = 0;
    private static long numCandidates = 0;
    private Writer reportWriter;
    private long timeTotal = 0;
    public static String ACTION;
    public boolean appendToExistingFile = true;
    public TestGson testGson;
    public static final Integer MUL_FACTOR = 100;
    public int deletemeCounter = 0;
    public static double ramBufferSizeMB = 100 * 1;
    private long bagsSortTime = 0;
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
    public static List<IndexWriter> indexerWriters;
    private static EProperties properties = new EProperties();

    public static Object lock = new Object();
    public static int min_tokens;
    public static int max_tokens;
    public static boolean isGenCandidateStats;
    public static int statusCounter = 0;
    public static boolean isStatusCounterOn;
    public static String NODE_PREFIX;
    public static String OUTPUT_DIR;
    public static int LOG_PROCESSED_LINENUMBER_AFTER_X_LINES;
    public static Map<String, Long> globalWordFreqMap = new HashMap<String, Long>();
    public static List<Shard> shards;
    public Set<Long> completedQueries;
    private boolean isSharding;
    public static String completedNodes;
    public static int totalNodes = -1;
    private static long RUN_COUNT;
    public static long QUERY_LINES_TO_IGNORE = 0;
    public static String ROOT_DIR;
    public static boolean FATAL_ERROR;

    public SearchManager(String[] args) throws IOException {
        SearchManager.ACTION = args[0];
        int qlq_thread_count = -1;
        int qbq_thread_count = -1;
        int qcq_thread_count = -1;
        int vcq_thread_count = -1;
        int rcq_thread_count = -1;
        int threadsToProcessBagsToSortQueue = -1;
        int threadToProcessIIQueue = -1;
        int threadsToProcessFIQueue = -1;
        try {
            SearchManager.th = (Float.parseFloat(args[1]) * SearchManager.MUL_FACTOR);

            qlq_thread_count = Integer.parseInt(properties.getProperty("QLQ_THREADS", "1"));
            qbq_thread_count = Integer.parseInt(properties.getProperty("QBQ_THREADS", "1"));
            qcq_thread_count = Integer.parseInt(properties.getProperty("QCQ_THREADS", "1"));
            vcq_thread_count = Integer.parseInt(properties.getProperty("VCQ_THREADS", "1"));
            rcq_thread_count = Integer.parseInt(properties.getProperty("RCQ_THREADS", "1"));
            SearchManager.min_tokens = Integer.parseInt(properties.getProperty("MIN_TOKENS", "65"));
            SearchManager.max_tokens = Integer.parseInt(properties.getProperty("MAX_TOKENS", "500000"));
            threadsToProcessBagsToSortQueue = Integer.parseInt(properties.getProperty("BTSQ_THREADS", "1"));
            threadToProcessIIQueue = Integer.parseInt(properties.getProperty("BTIIQ_THREADS", "1"));
            threadsToProcessFIQueue = Integer.parseInt(properties.getProperty("BTFIQ_THREADS", "1"));
            this.isSharding = Boolean.parseBoolean(properties.getProperty("IS_SHARDING"));
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] " + e.getMessage() + ", exiting now");
            e.printStackTrace();
            System.exit(1);
        }
        if (SearchManager.ACTION.equals(ACTION_SEARCH)) {
            SearchManager.completedNodes = SearchManager.ROOT_DIR + "nodes_completed.txt";
            this.completedQueries = new HashSet<Long>();

            this.createShards(false);

            System.out.println("action: " + SearchManager.ACTION
                    + System.lineSeparator() + "threshold: " + args[1]
                    + System.lineSeparator() + " QLQ_THREADS: "
                    + qlq_thread_count + " QBQ_THREADS: "
                    + qbq_thread_count + " QCQ_THREADS: "
                    + qcq_thread_count + " VCQ_THREADS: "
                    + vcq_thread_count + " RCQ_THREADS: "
                    + rcq_thread_count + System.lineSeparator());
            SearchManager.queryLineQueue = new ThreadedChannel<String>(qlq_thread_count, QueryLineProcessor.class);
            SearchManager.queryBlockQueue = new ThreadedChannel<QueryBlock>(qbq_thread_count, CandidateSearcher.class);
            SearchManager.queryCandidatesQueue = new ThreadedChannel<QueryCandidates>(qcq_thread_count, CandidateProcessor.class);
            SearchManager.verifyCandidateQueue = new ThreadedChannel<CandidatePair>(vcq_thread_count, CloneValidator.class);
            SearchManager.reportCloneQueue = new ThreadedChannel<ClonePair>(rcq_thread_count, CloneReporter.class);
        } else if (SearchManager.ACTION.equals(ACTION_INDEX)) {
            indexerWriters = new ArrayList<IndexWriter>();
            this.createShards(true);
            System.out.println("[ERROR] " + "action: " + SearchManager.ACTION
                    + System.lineSeparator() + "threshold: " + args[1]
                    + System.lineSeparator() + " BQ_THREADS: "
                    + threadsToProcessBagsToSortQueue
                    + System.lineSeparator() + " SBQ_THREADS: "
                    + threadToProcessIIQueue + System.lineSeparator()
                    + " IIQ_THREADS: " + threadsToProcessFIQueue
                    + System.lineSeparator());
            SearchManager.bagsToSortQueue = new ThreadedChannel<Bag>(threadsToProcessBagsToSortQueue, BagSorter.class);
            SearchManager.bagsToInvertedIndexQueue = new ThreadedChannel<Bag>(threadToProcessIIQueue, InvertedIndexCreator.class);
            SearchManager.bagsToForwardIndexQueue = new ThreadedChannel<Bag>(threadsToProcessFIQueue, ForwardIndexCreator.class);
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
        System.out.println("[DEBUG] " + "Number of shards created: " + SearchManager.shards.size());
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
            if (qb.getSize() >= shard.getMinSize() && qb.getSize() <= shard.getMaxSize())
                return shard;
        return null;
    }

    private static String getProperty(String name) {
        return getProperty(name, null);
    }

    private static String getProperty(String name, String defaultValue) {
        Key key = new Key(name);
        Object result = properties.get(key);
        if (result == null)
            return defaultValue;
        return String.valueOf(result);
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        long start_time = System.nanoTime();
        System.out.println("user.dir is: " + System.getProperty("user.dir"));
        System.out.println("root dir is:" + System.getProperty("properties.rootDir"));
        SearchManager.ROOT_DIR = System.getProperty("properties.rootDir");
        System.out.println("reading Q values from properties file");
        String propertiesPath = System.getProperty("properties.location");
        System.out.println("[DEBUG] " + "propertiesPath: " + propertiesPath);

        try {
            properties.load(propertiesPath);
        } catch (IOException e) {
            System.out.println("[ERROR] " + "ERROR READING PROPERTIES FILE, " + e.getMessage());
            System.exit(1);
        }

        String[] params = new String[2];
        params[0] = args[0];
        params[1] = args[1];
        SearchManager.DATASET_DIR = SearchManager.ROOT_DIR + getProperty("DATASET_DIR_PATH");
        SearchManager.isGenCandidateStats = Boolean.parseBoolean(getProperty("IS_GEN_CANDIDATE_STATISTICS"));
        SearchManager.isStatusCounterOn = Boolean.parseBoolean(getProperty("IS_STATUS_REPORTER_ON"));
        SearchManager.NODE_PREFIX = getProperty("NODE_PREFIX").toUpperCase();
        SearchManager.OUTPUT_DIR = SearchManager.ROOT_DIR + getProperty("OUTPUT_DIR");
        SearchManager.QUERY_DIR_PATH = SearchManager.ROOT_DIR + getProperty("QUERY_DIR_PATH");
        System.out.println("[DEBUG] " + "Query path:" + SearchManager.QUERY_DIR_PATH);
        SearchManager.LOG_PROCESSED_LINENUMBER_AFTER_X_LINES = Integer.parseInt(getProperty("LOG_PROCESSED_LINENUMBER_AFTER_X_LINES", "1000"));
        theInstance = new SearchManager(params);

        System.out.println("[DEBUG] " + SearchManager.NODE_PREFIX + " MAX_TOKENS=" + max_tokens + " MIN_TOKENS=" + min_tokens);

        Util.createDirs(SearchManager.OUTPUT_DIR + SearchManager.th / SearchManager.MUL_FACTOR);
        String reportFileName = SearchManager.OUTPUT_DIR + SearchManager.th / SearchManager.MUL_FACTOR + "/report.csv";
        File reportFile = new File(reportFileName);
        theInstance.appendToExistingFile = reportFile.exists();
        theInstance.reportWriter = Util.openFile(reportFileName, theInstance.appendToExistingFile);
        if (SearchManager.ACTION.equalsIgnoreCase(ACTION_INDEX)) {
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
            System.out.println("indexing over!");
            theInstance.timeIndexing = System.currentTimeMillis() - begin_time;
        } else if (SearchManager.ACTION.equalsIgnoreCase(ACTION_SEARCH)) {
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
            theInstance.timeSearch = System.currentTimeMillis()
                    - timeStartSearch;
            signOffNode();
            if ("NODE_1".equals(SearchManager.NODE_PREFIX)) {
                System.out.println("[DEBUG] " + "NODES COMPLETED SO FAR: " + getCompletedNodes());
                while (true) {
                    if (allNodesCompleted()) {
                        theInstance.backupInput();
                        break;
                    } else {
                        System.out.println("waiting for all nodes to complete, check "
                                + SearchManager.completedNodes
                                + " file to see the list of completed nodes");
                        Thread.sleep(4000);
                    }
                }
            }
        } else if (SearchManager.ACTION.equalsIgnoreCase(ACTION_INIT)) {
            WordFrequencyStore wfs = new WordFrequencyStore();
            wfs.populateLocalWordFreqMap();
        }
        long estimatedTime = System.nanoTime() - start_time;
        System.out.println("Total run Time: " + (estimatedTime / 1000) + " micors");
        System.out.println("number of clone pairs detected: " + SearchManager.clonePairsCount);
        theInstance.timeTotal = estimatedTime;
        theInstance.genReport();
        Util.closeOutputFile(theInstance.reportWriter);
        try {
            Util.closeOutputFile(SearchManager.clonesWriter);
            Util.closeOutputFile(SearchManager.recoveryWriter);
            if (SearchManager.ACTION.equals(ACTION_SEARCH)) {
                theInstance.backupOutput();
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + "exception caught in main " + e.getMessage());
        }
        System.out.println("completed on " + SearchManager.NODE_PREFIX);
    }

    private void readAndUpdateRunMetadata() {
        this.readRunMetadata();
        // update the runMetadata
        SearchManager.RUN_COUNT += 1;
        this.updateRunMetadata(SearchManager.RUN_COUNT + "");
    }

    private void readRunMetadata() {
        File f = new File(Util.RUN_METADATA);
        BufferedReader br = null;
        if (f.exists()) {
            System.out.println("[DEBUG] " + Util.RUN_METADATA + " file exists, reading it to get the run metadata");
            try {
                br = Util.getReader(f);
                String line = br.readLine().trim();
                if (!line.isEmpty()) {
                    SearchManager.RUN_COUNT = Long.parseLong(line);
                    System.out.println("[DEBUG] " +  "last run count was: " + SearchManager.RUN_COUNT);
                } else {
                    SearchManager.RUN_COUNT = 1;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
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
        } else {
            SearchManager.RUN_COUNT = 1;
        }
    }

    private void updateRunMetadata(String text) {
        File f = new File(Util.RUN_METADATA);
        try {
            Writer writer = Util.openFile(f, false);
            Util.writeToFile(writer, text, true);
            Util.closeOutputFile(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void backupOutput() throws IOException {
        theInstance.readRunMetadata();
        String destDir = Util.OUTPUT_BACKUP_DIR + "/" + SearchManager.RUN_COUNT
                + "/" + SearchManager.NODE_PREFIX;
        Util.createDirs(destDir); // creates if it doesn't exist
        String sourceDir = SearchManager.OUTPUT_DIR
                + SearchManager.th / SearchManager.MUL_FACTOR;
        System.out.println("[DEBUG] " + "moving " + sourceDir + " to " + destDir);
        // copy the output folder instead of moving it
        FileUtils.copyDirectory(new File(sourceDir), new File(destDir), true);
    }

    private void backupInput() {
        String previousDataFolder = SearchManager.DATASET_DIR + "/oldData/";
        Util.createDirs(previousDataFolder);
        File sourceDataFile = new File(SearchManager.DATASET_DIR + "/" + Util.QUERY_FILE_NAME);
        String targetFileName = previousDataFolder + System.currentTimeMillis() + "_" + Util.QUERY_FILE_NAME;
        sourceDataFile.renameTo(new File(targetFileName));
        File completedNodesFile = new File(SearchManager.completedNodes);
        // delete the completedNodes file
        completedNodesFile.delete();
    }

    private static boolean allNodesCompleted() {
        return getNodes() == getCompletedNodes();
    }

    private static int getCompletedNodes() {
        File completedNodeFile = new File(SearchManager.completedNodes);
        FileLock lock = null;
        int count = 0;
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(completedNodeFile, "rw");
            FileChannel channel = raf.getChannel();
            try {
                lock = channel.lock();
                while (raf.readLine() != null) {
                    count++;
                }
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
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        return count;
    }

    private static int getNodes() {
        if (SearchManager.totalNodes == -1) {
            File searchMertadaFile = new File(Util.SEARCH_METADATA);
            try {
                BufferedReader br = Util.getReader(searchMertadaFile);
                String line = br.readLine();
                if (null != line) {
                    SearchManager.totalNodes = Integer.parseInt(line.trim());
                    return SearchManager.totalNodes;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SearchManager.totalNodes;
    }

    private static void signOffNode() {
        System.out.println("[DEBUG] " + "signing off " + SearchManager.NODE_PREFIX);
        File file = new File(SearchManager.completedNodes);
        FileLock lock = null;
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "rwd");
            FileChannel channel = raf.getChannel();
            try {
                lock = channel.lock();
                System.out.println("[DEBUG] " + "lock obtained? " + lock);
                ByteBuffer outBuffer = ByteBuffer.allocate(100);
                outBuffer.clear();
                String endidStr = SearchManager.NODE_PREFIX + "\n";
                outBuffer.put(endidStr.getBytes());
                outBuffer.flip();
                channel.write(outBuffer, raf.length());
                channel.force(false);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[ERROR] " + e.getMessage());
            } finally {
                try {
                    lock.release();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("[ERROR] " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            System.out.println("[ERROR] " + e1.getMessage());
        }

    }

    private void populateCompletedQueries() {
        BufferedReader br = null;
        String filename = SearchManager.OUTPUT_DIR + SearchManager.th / SearchManager.MUL_FACTOR + "/recovery.txt";
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    if (line.trim().length() > 0) {
                        SearchManager.QUERY_LINES_TO_IGNORE = Long.parseLong(line.trim());
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[ERROR] " +  SearchManager.NODE_PREFIX + ", error in parsing:" + e.getMessage() + ", line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("[ERROR] " + 
                    SearchManager.NODE_PREFIX + ", " + filename + " not found");
        } catch (UnsupportedEncodingException e) {
            System.out.println("[ERROR] " + SearchManager.NODE_PREFIX + ", error in populateCompleteQueries" + e.getMessage());
            System.out.println("[ERROR] " + "stacktrace: ");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[ERROR] " + SearchManager.NODE_PREFIX + ", error in populateCompleteQueries IO" + e.getMessage());
            System.out.println("[ERROR] " + "stacktrace: ");
            e.printStackTrace();
        }
        System.out.println("lines to ignore in query file: " + SearchManager.QUERY_LINES_TO_IGNORE);
    }

    private void initIndexEnv() throws IOException, ParseException {
        long timeGlobalPositionStart = System.currentTimeMillis();
        SearchManager.gtpmSearcher = new CodeSearcher(Util.GTPM_INDEX_DIR, "key");
        this.timeGlobalTokenPositionCreation = System.currentTimeMillis() - timeGlobalPositionStart;
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
        if (SearchManager.ACTION.equalsIgnoreCase("index")) {
            header += SearchManager.ACTION + ",";
            header += this.bagsSortTime;
        } else {
            header += SearchManager.ACTION;
        }
        Util.writeToFile(this.reportWriter, header, true);
    }

    private void doIndex() throws InterruptedException, FileNotFoundException {
        File datasetDir = new File(SearchManager.DATASET_DIR);
        if (datasetDir.isDirectory()) {
            System.out.println("Directory: " + datasetDir.getAbsolutePath());
            for (File inputFile : Util.getAllFilesRecur(datasetDir)) {
                System.out.println("indexing file: " + inputFile.getAbsolutePath());
                try {
                    TokensFileReader tfr = new TokensFileReader(
                            SearchManager.NODE_PREFIX, inputFile,
                            SearchManager.max_tokens,
                            new ITokensFileProcessor() {
                                public void processLine(String line) throws ParseException {
                                    if (!SearchManager.FATAL_ERROR) {
                                        Bag bag = cloneHelper.deserialise(line);
                                        if (bag == null || bag.getSize() < SearchManager.min_tokens) {
                                            if (null == bag) {
                                                System.out.println("[DEBUG] " + 
                                                        SearchManager.NODE_PREFIX
                                                                + " empty bag, ignoring. statusCounter= "
                                                                + SearchManager.statusCounter);
                                            } else {
                                                System.out.println("[DEBUG] " + 
                                                        SearchManager.NODE_PREFIX
                                                                + " ignoring bag "
                                                                + ", " + bag
                                                                + ", statusCounter="
                                                                + SearchManager.statusCounter);
                                            }
                                            return; // ignore this bag.
                                        }
                                        try {
                                            SearchManager.bagsToSortQueue.send(bag);
                                        } catch (Exception e) {
                                            System.out.println("[ERROR] " + SearchManager.NODE_PREFIX
                                                            + "Unable to send bag "
                                                            + bag.getId()
                                                            + " to queue" + e);
                                        }
                                    }else{
                                        System.out.println("[FATAL] " + "FATAL error detected. exiting now");
                                        System.exit(1);
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
                    System.out.println("[ERROR] " + SearchManager.NODE_PREFIX
                            + ", something nasty, exiting. counter:"
                            + SearchManager.statusCounter);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        } else {
            System.out.println("[ERROR] " + "File: " + datasetDir.getName() + " is not a directory. Exiting now");
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
                    String cloneReportFileName = SearchManager.OUTPUT_DIR
                            + SearchManager.th / SearchManager.MUL_FACTOR + "/"
                            + filename + "clones_index_WITH_FILTER.txt";
                    File cloneReportFile = new File(cloneReportFileName);
                    this.appendToExistingFile = cloneReportFile.exists();
                    SearchManager.clonesWriter = Util.openFile(
                            SearchManager.OUTPUT_DIR
                                    + SearchManager.th / SearchManager.MUL_FACTOR
                                    + "/" + filename
                                    + "clones_index_WITH_FILTER.txt",
                            this.appendToExistingFile);
                    // recoveryWriter
                    SearchManager.recoveryWriter = Util.openFile(SearchManager.OUTPUT_DIR
                                    + SearchManager.th / SearchManager.MUL_FACTOR
                                    + "/recovery.txt", false);
                } catch (IOException e) {
                    System.out.println("[ERROR] " + e.getMessage() + " exiting");
                    System.exit(1);
                }
                try {
                    TokensFileReader tfr = new TokensFileReader(
                            SearchManager.NODE_PREFIX, queryFile,
                            SearchManager.max_tokens, queryFileProcessor);
                    tfr.read();
                } catch (IOException e) {
                    System.out.println("[ERROR] " + e.getMessage() + " skiping to next file");
                } catch (ParseException e) {
                    System.out.println("[ERROR] " + SearchManager.NODE_PREFIX
                            + "parseException caught. message: "
                            + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("[ERROR] " + e.getMessage() + "exiting");
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
        if ("NODE_1".equals(SearchManager.NODE_PREFIX)) {
            theInstance.readAndUpdateRunMetadata();
        }
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
