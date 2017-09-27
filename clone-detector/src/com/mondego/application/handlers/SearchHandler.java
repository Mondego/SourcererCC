package com.mondego.application.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.framework.controllers.MainController;
import com.mondego.framework.handlers.interfaces.IActionHandler;
import com.mondego.framework.models.Bag;
import com.mondego.framework.models.CandidatePair;
import com.mondego.framework.models.CandidateProcessor;
import com.mondego.framework.models.CandidateSearcher;
import com.mondego.framework.models.ClonePair;
import com.mondego.framework.models.CloneReporter;
import com.mondego.framework.models.CloneValidator;
import com.mondego.framework.models.DocumentForInvertedIndex;
import com.mondego.framework.models.InvertedIndexCreator;
import com.mondego.framework.models.QueryBlock;
import com.mondego.framework.models.QueryCandidates;
import com.mondego.framework.models.QueryFileProcessor;
import com.mondego.framework.models.QueryLineProcessor;
import com.mondego.framework.models.Shard;
import com.mondego.framework.models.ThreadedChannel;
import com.mondego.framework.services.RuntimeStateService;
import com.mondego.framework.services.ShardService;
import com.mondego.utility.TokensFileReader;
import com.mondego.utility.Util;

public class SearchHandler implements IActionHandler {
    private static final Logger logger = LogManager
            .getLogger(SearchHandler.class);
    private RuntimeStateService runtimeStateService;
    private ShardService shardService;
    
    
    private int max_index_size;
    private boolean appendToExistingFile;
    private int qlq_thread_count;
    private int qbq_thread_count;
    private int qcq_thread_count;
    private int vcq_thread_count;
    private int rcq_thread_count;
    
    public static ThreadedChannel<String> queryLineQueue;
    public static ThreadedChannel<QueryBlock> queryBlockQueue;
    public static ThreadedChannel<QueryCandidates> queryCandidatesQueue;
    public static ThreadedChannel<CandidatePair> verifyCandidateQueue;
    public static ThreadedChannel<ClonePair> reportCloneQueue;
    public static ThreadedChannel<Bag> bagsToInvertedIndexQueue;
    private int threadToProcessIIQueue;
    
    public static Map<String, Set<Long>> invertedIndex;
    public static Map<Long, DocumentForInvertedIndex> documentsForII;

    
    public SearchHandler() {
        this.runtimeStateService = RuntimeStateService.getInstance();
        this.shardService = ShardService.getInstance();
    }
    @Override
    
    public void handle(String action) {
        long timeStartSearch = System.currentTimeMillis();
        this.initSearchEnv();
        this.startSearch();
        // logger.info(NODE_PREFIX + " Starting to search");
        // theInstance.populateCompletedQueries();
        // theInstance.findCandidates();

        this.queryLineQueue.shutdown();
        logger.info("shutting down QLQ, " + System.currentTimeMillis());
        logger.info("shutting down QBQ, " + (System.currentTimeMillis()));
        this.queryBlockQueue.shutdown();
        logger.info("shutting down QCQ, " + System.currentTimeMillis());
        this.queryCandidatesQueue.shutdown();
        logger.info("shutting down VCQ, " + System.currentTimeMillis());
        this.verifyCandidateQueue.shutdown();
        logger.info("shutting down RCQ, " + System.currentTimeMillis());
        this.reportCloneQueue.shutdown();
        this.runtimeStateService.timeSearch = System.currentTimeMillis()
                - timeStartSearch;
        this.runtimeStateService.signOffNode();
        if (MainController.NODE_PREFIX.equals("NODE_1")) {
            logger.debug("NODES COMPLETED SO FAR: " + this.runtimeStateService.getCompletedNodes());
            boolean firstTimeWait = true;
            while (true) {
                if (this.runtimeStateService.allNodesCompleted()) {
                    this.runtimeStateService.backupInput();
                    break;
                } else {
                    if(firstTimeWait){
                        logger.info("waiting for all nodes to complete, check "
                                + MainController.completedNodes
                                + " file to see the list of completed nodes");
                        firstTimeWait=false;
                    }
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        long estimatedTime = System.nanoTime() - timeStartSearch;
        logger.info("Total search Time: " + (estimatedTime / 1000) + " micors");
        logger.info("number of clone pairs detected: "
                + this.runtimeStateService.clonePairsCount);
        // theInstance.genReport();
        try {
            Util.closeFile(MainController.clonesWriter);
            Util.closeFile(MainController.recoveryWriter);
            this.runtimeStateService.backupOutput();
        } catch (Exception e) {
            logger.error("exception caught in main " + e.getMessage());
        }
        logger.info("completed on " + MainController.NODE_PREFIX);

    }
    private void initSearchEnv() {
        this.setProperties();
        MainController.completedNodes = MainController.ROOT_DIR
                + "nodes_completed.txt";
        this.shardService.createShards(false);

        logger.info("action: " + MainController.ACTION
                + System.lineSeparator() + "threshold: " + (MainController.th / MainController.MUL_FACTOR)
                + System.lineSeparator() + " QLQ_THREADS: "
                + this.qlq_thread_count + " QBQ_THREADS: "
                + this.qbq_thread_count + " QCQ_THREADS: "
                + this.qcq_thread_count + " VCQ_THREADS: "
                + this.vcq_thread_count + " RCQ_THREADS: "
                + this.rcq_thread_count + System.lineSeparator());
        
        this.queryLineQueue = new ThreadedChannel<String>(
                this.qlq_thread_count, QueryLineProcessor.class);
        this.queryBlockQueue = new ThreadedChannel<QueryBlock>(
                this.qbq_thread_count, CandidateSearcher.class);
        this.queryCandidatesQueue = new ThreadedChannel<QueryCandidates>(
                this.qcq_thread_count, CandidateProcessor.class);
        this.verifyCandidateQueue = new ThreadedChannel<CandidatePair>(
                this.vcq_thread_count, CloneValidator.class);
        this.reportCloneQueue = new ThreadedChannel<ClonePair>(
                this.rcq_thread_count, CloneReporter.class);
        
        if (MainController.NODE_PREFIX.equals("NODE_1")) {
            this.runtimeStateService.readAndUpdateRunMetadata();
            File completedNodeFile = new File(MainController.completedNodes);
            if (completedNodeFile.exists()) {
                logger.debug(completedNodeFile.getAbsolutePath()
                        + "exists, deleting it.");
                completedNodeFile.delete();
            }
        }

        // MainController.gtpmSearcher = new CodeSearcher(Util.GTPM_INDEX_DIR,
        // "key");
    }
    private void setProperties() {
        this.qlq_thread_count = Integer
                .parseInt(MainController.properties.getProperty("QLQ_THREADS", "1"));
        this.qbq_thread_count = Integer
                .parseInt(MainController.properties.getProperty("QBQ_THREADS", "1"));
        this.qcq_thread_count = Integer
                .parseInt(MainController.properties.getProperty("QCQ_THREADS", "1"));
        this.vcq_thread_count = Integer
                .parseInt(MainController.properties.getProperty("VCQ_THREADS", "1"));
        this.rcq_thread_count = Integer
                .parseInt(MainController.properties.getProperty("RCQ_THREADS", "1"));
        this.threadToProcessIIQueue = Integer
        .parseInt(MainController.properties.getProperty("BTIIQ_THREADS", "1"));
    }
    private void startSearch(){
        Set<Integer> searchShards = new HashSet<Integer>();
        String searchShardsString = MainController.properties.getProperty("SEARCH_SHARDS",
                "ALL");
        if (searchShardsString.equalsIgnoreCase("ALL")) {
            searchShardsString = null;
        }
        if (null != searchShardsString) {
            String[] searchShardsArray = searchShardsString.split(",");
            for (String shardId : searchShardsArray) {
                searchShards.add(Integer.parseInt(shardId));
            }
        }
        for (Shard shard : MainController.shards) {
            if (searchShards.size() > 0) {
                if (searchShards.contains(shard.getId())) {
                    this.launchSearchers(shard);
                }
            } else {
                // search on all shards.
                this.launchSearchers(shard);
            }
        }
    }
    
    private void launchSearchers(Shard shard) {
        this.max_index_size = Integer
                .parseInt(MainController.properties.getProperty("MAX_INDEX_SIZE", "12"));
        if (shard.subShards.size() > 0) {
            for (Shard subShard : shard.subShards) {
                this.launchSearchers(subShard);
            }
        } else {
            try {
                this.findCandidates(shard);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void findCandidates(Shard shard) throws InterruptedException {
        try {
            String shardFolderPath = MainController.ROOT_DIR + "/index/"
                    + shard.indexPath;
            File queryFile = new File(shardFolderPath + "/query.file");
            File candidateFile = new File(shardFolderPath + "/candidates.file");
            QueryFileProcessor queryFileProcessor = new QueryFileProcessor();
            logger.info("Query File: " + queryFile.getAbsolutePath());
            String filename = queryFile.getName().replaceFirst("[.][^.]+$", "");
            try {
                String cloneReportFileName = MainController.OUTPUT_DIR
                        + MainController.th / MainController.MUL_FACTOR + "/"
                        + filename + "clones_index_WITH_FILTER.txt";
                File cloneReportFile = new File(cloneReportFileName);
                if (cloneReportFile.exists()) {
                    this.appendToExistingFile = true;
                } else {
                    this.appendToExistingFile = false;
                }
                MainController.clonesWriter = Util
                        .openFile(
                                MainController.OUTPUT_DIR
                                        + MainController.th
                                                / MainController.MUL_FACTOR
                                        + "/" + filename
                                        + "clones_index_WITH_FILTER.txt",
                                this.appendToExistingFile);
                // recoveryWriter
                MainController.recoveryWriter = Util
                        .openFile(MainController.OUTPUT_DIR
                                + MainController.th / MainController.MUL_FACTOR
                                + "/recovery.txt", false);
            } catch (IOException e) {
                logger.error(e.getMessage() + " exiting");
                System.exit(1);
            }
            int completedLines = 0;
            while (true) {
                logger.info("creating indexes for "
                        + candidateFile.getAbsolutePath());
                completedLines = this.createIndexes(candidateFile,
                        completedLines);
                logger.info("indexes created");
                try {
                    TokensFileReader tfr = new TokensFileReader(
                            MainController.NODE_PREFIX, queryFile,
                            MainController.max_tokens, queryFileProcessor);
                    tfr.read();
                } catch (IOException e) {
                    logger.error(e.getMessage() + " skiping to next file");
                } catch (ParseException e) {
                    logger.error(MainController.NODE_PREFIX
                            + "parseException caught. message: "
                            + e.getMessage());
                    e.printStackTrace();
                }
                logger.debug("COMPLETED LINES: " + completedLines);
                if (completedLines == -1) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage() + "exiting");
            System.exit(1);
        }
    }
    
    private int createIndexes(File candidateFile, int avoidLines)
            throws FileNotFoundException {
        SearchHandler.invertedIndex = new ConcurrentHashMap<String, Set<Long>>();
        SearchHandler.documentsForII = new ConcurrentHashMap<Long, DocumentForInvertedIndex>();
        BufferedReader br = new BufferedReader(new FileReader(candidateFile));
        String line = "";
        long size = 0;
        long gig = 1000000000l;
        long maxMemory = this.max_index_size * gig;
        int completedLines = 0;
        try {
            // MainController.bagsToSortQueue = new ThreadedChannel<Bag>(
            // this.threadsToProcessBagsToSortQueue, BagSorter.class);
            this.bagsToInvertedIndexQueue = new ThreadedChannel<Bag>(
                    this.threadToProcessIIQueue, InvertedIndexCreator.class);
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                completedLines++;
                if (completedLines <= avoidLines) {
                    continue;
                }
                Bag bag = TokensFileReader.deserialise(line);
                if (null != bag) {
                    size = size + (bag.getNumUniqueTokens() * 300); // approximate
                                                                    // mem
                                                                    // utilization.
                                                                    // 1 key
                                                                    // value
                                                                    // pair =
                                                                    // 300 bytes
                    logger.debug("indexing " + completedLines + " bag: " + bag
                            + ", mem: " + size + " bytes");
                    this.bagsToInvertedIndexQueue.send(bag);
                    if (size >= maxMemory) {
                        return completedLines;
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // MainController.bagsToSortQueue.shutdown();
            this.bagsToInvertedIndexQueue.shutdown();
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }


}
