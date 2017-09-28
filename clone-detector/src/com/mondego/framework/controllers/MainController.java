/**
 * 
 */
package com.mondego.framework.controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexWriter;

import com.mondego.application.handlers.InitActionHandler;
import com.mondego.application.handlers.SearchActionHandler;
import com.mondego.application.handlers.ShardsActionHandler;
import com.mondego.framework.handlers.interfaces.IActionHandler;
import com.mondego.framework.models.Shard;
import com.mondego.indexbased.CodeSearcher;
import com.mondego.utility.Util;
import com.mondego.validation.TestGson;

import net.jmatrix.eproperties.EProperties;

/**
 * @author saini
 *
 */
public class MainController {

    public static CodeSearcher gtpmSearcher;
    public static String QUERY_DIR_PATH;
    public static String DATASET_DIR;
    public static String WFM_DIR_PATH;
    public static Writer clonesWriter; // writer to write the output
    public static Writer recoveryWriter; // writes the lines processed during
                                         // search. for recovery purpose.
    public static float th; // args[2]
                            // search
    public final static String ACTION_CREATE_SHARDS = "cshard";
    public final static String ACTION_SEARCH = "search";

    public static long timeSpentInSearchingCandidates;
    public static Writer reportWriter;
    public static String ACTION;
    public static boolean appendToExistingFile;
    TestGson testGson;
    public static final Integer MUL_FACTOR = 100;
    private static final String ACTION_INIT = "init";
    int deletemeCounter = 0;
    public static double ramBufferSizeMB;
    public static MainController theInstance;
    public static List<IndexWriter> indexerWriters;
    public static EProperties properties = new EProperties();

    public static Object lock = new Object();
    public static int min_tokens;
    public static int max_tokens;
    public static boolean isGenCandidateStats;
    public static int statusCounter;
    public static boolean isStatusCounterOn;
    public static String NODE_PREFIX;
    public static String OUTPUT_DIR;
    public static int LOG_PROCESSED_LINENUMBER_AFTER_X_LINES;
    public static Map<String, Long> globalWordFreqMap;
    public static List<Shard> shards;
    public Set<Long> completedQueries;
    public static boolean isSharding;
    public static String completedNodes;
    public static int totalNodes = -1;
    public static String ROOT_DIR;
    private static final Logger logger = LogManager
            .getLogger(MainController.class);
    public static boolean FATAL_ERROR;
    
    private IActionHandler shardsHandler;
    private IActionHandler searchHandler;
    private IActionHandler initHandler;

    static {
        // load properties
        FileInputStream fis = null;
        String propertiesPath = System.getProperty("properties.location");
        logger.debug("propertiesPath: " + propertiesPath);
        try {
            fis = new FileInputStream(propertiesPath);
        } catch (FileNotFoundException e1) {
            logger.fatal(
                    "ERROR READING PROPERTIES FILE PATH, " + e1.getMessage());
            e1.printStackTrace();
            System.exit(1);
        }
        try {
            MainController.properties.load(fis);
        } catch (IOException e) {
            logger.fatal("ERROR READING PROPERTIES FILE, " + e.getMessage());
            System.exit(1);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error(
                            "ERROR CLOSING PROPERTIES FILE, " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        // set properties
        MainController.ROOT_DIR = System.getProperty("properties.rootDir");
        MainController.DATASET_DIR = MainController.ROOT_DIR
                + properties.getProperty("DATASET_DIR_PATH");
        MainController.isGenCandidateStats = Boolean.parseBoolean(
                properties.getProperty("IS_GEN_CANDIDATE_STATISTICS"));
        MainController.isStatusCounterOn = properties.getBoolean("IS_STATUS_REPORTER_ON");
        MainController.NODE_PREFIX = properties.getProperty("NODE_PREFIX")
                .toUpperCase();
        MainController.OUTPUT_DIR = MainController.ROOT_DIR
                + properties.getProperty("OUTPUT_DIR");
        MainController.QUERY_DIR_PATH = MainController.ROOT_DIR
                + properties.getProperty("QUERY_DIR_PATH");
        logger.debug("Query path:" + MainController.QUERY_DIR_PATH);
        MainController.LOG_PROCESSED_LINENUMBER_AFTER_X_LINES = properties.getInt("LOG_PROCESSED_LINENUMBER_AFTER_X_LINES", 1000);
        MainController.min_tokens = properties.getInt("LEVEL_1_MIN_TOKENS", 65);
        MainController.max_tokens = properties.getInt("LEVEL_1_MAX_TOKENS", 500000);
        
        logger.debug(MainController.NODE_PREFIX + " MAX_TOKENS=" + max_tokens
                + " MIN_TOKENS=" + min_tokens);
    }

    public MainController(String[] args) throws IOException {
        MainController.appendToExistingFile = true;
        MainController.ACTION = args[0];
        MainController.statusCounter = 0;
        MainController.globalWordFreqMap = new HashMap<String, Long>();
        this.initHandler = new InitActionHandler();
        this.shardsHandler = new ShardsActionHandler();
        this.searchHandler = new SearchActionHandler();
        try {
            MainController.th = (Float.parseFloat(args[1])
                    * MainController.MUL_FACTOR);

        } catch (NumberFormatException e) {
            logger.error(e.getMessage() + ", exiting now", e);
            System.exit(1);
        }
    }

    public static void main(String[] args)
            throws IOException, ParseException, InterruptedException {
        long start_time = System.nanoTime();
        logger.info("user.dir is: " + System.getProperty("user.dir"));
        logger.info("root dir is:" + System.getProperty("properties.rootDir"));
        String[] params = new String[2];
        params[0] = args[0];
        params[1] = args[1];
        theInstance = new MainController(params);
        Util.createDirs(MainController.OUTPUT_DIR
                + MainController.th / MainController.MUL_FACTOR);
        theInstance.invokeActionHandler();
        long estimatedTime = System.nanoTime() - start_time;
        logger.info("Total run Time: " + (estimatedTime / 1000) + " micors");
        // theInstance.genReport();
        Util.closeFile(MainController.reportWriter);
        logger.info("completed on " + MainController.NODE_PREFIX);
    }

    public void invokeActionHandler() {

        if (MainController.ACTION.equalsIgnoreCase(ACTION_CREATE_SHARDS)) {
            this.shardsHandler.handle(ACTION_CREATE_SHARDS);
        } else if (MainController.ACTION.equalsIgnoreCase(ACTION_SEARCH)) {
            this.searchHandler.handle(ACTION_SEARCH);
        } else if (MainController.ACTION.equalsIgnoreCase(ACTION_INIT)) {
            this.initHandler.handle(ACTION_INIT);
        }
    }
}
