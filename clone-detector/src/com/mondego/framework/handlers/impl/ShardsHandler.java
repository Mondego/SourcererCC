package com.mondego.framework.handlers.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.framework.controllers.MainController;
import com.mondego.framework.handlers.interfaces.IActionHandler;
import com.mondego.framework.models.Bag;
import com.mondego.framework.models.ITokensFileProcessor;
import com.mondego.framework.models.Shard;
import com.mondego.framework.services.ShardService;
import com.mondego.indexbased.CodeSearcher;
import com.mondego.utility.TokensFileReader;
import com.mondego.utility.Util;

public class ShardsHandler implements IActionHandler {
    private static final Logger logger = LogManager
            .getLogger(ShardsHandler.class);
    private ShardService shardService;
    private int threadsToProcessBagsToSortQueue;
    public static List<String> METRICS_ORDER_IN_SHARDS;

    public ShardsHandler() {
        this.shardService = ShardService.getInstance();
    }

    @Override
    public void handle(String action) {
        long begin_time = System.currentTimeMillis();
        this.setProperties();
        this.shardService.createShards(true);
        this.doPartitions();
        for (Shard shard : MainController.shards) {
            shard.closeWriters();
        }
        logger.info(action + " over!");
        long totalTime = System.currentTimeMillis() - begin_time;
        logger.debug("time taken to complete " + action + " : "
                + (totalTime / 1000) + " micors");
    }

    private void setProperties() {
        // TODO Auto-generated method stub
        this.threadsToProcessBagsToSortQueue = Integer
                .parseInt(MainController.properties.getProperty("BTSQ_THREADS", "1"));
     // read and set metrics order in shards
        String shardsOrder = MainController.properties.getProperty("METRICS_ORDER_IN_SHARDS");
        ShardsHandler.METRICS_ORDER_IN_SHARDS = new ArrayList<String>();
        for (String metric : shardsOrder.split(",")) {
            ShardsHandler.METRICS_ORDER_IN_SHARDS.add(metric.trim());
        }
        if (!(ShardsHandler.METRICS_ORDER_IN_SHARDS.size() > 0)) {
            logger.fatal(
                    "ERROR WHILE CREATING METRICS ORDER IN SHARDS, EXTING");
            System.exit(1);
        } else {
            logger.info("METRICS_ORDER_IN_SHARDS created: "
                    + ShardsHandler.METRICS_ORDER_IN_SHARDS.size());
        }
    }

    private void doPartitions() {
        MainController.gtpmSearcher = new CodeSearcher(Util.GTPM_INDEX_DIR,
                "key");
        File datasetDir = new File(MainController.DATASET_DIR);
        if (datasetDir.isDirectory()) {
            logger.info("Directory: " + datasetDir.getAbsolutePath());
            for (File inputFile : Util.getAllFilesRecur(datasetDir)) {
                logger.info("indexing file: " + inputFile.getAbsolutePath());
                try {
                    TokensFileReader tfr = new TokensFileReader(
                            MainController.NODE_PREFIX, inputFile,
                            MainController.max_tokens,
                            new ITokensFileProcessor() {
                                public void processLine(String line)
                                        throws ParseException {
                                    if (!MainController.FATAL_ERROR) {
                                        Bag bag = TokensFileReader.deserialise(line);
                                        if (null == bag || bag
                                                .getSize() < MainController.min_tokens) {
                                            if (null == bag) {
                                                logger.debug(
                                                        MainController.NODE_PREFIX
                                                                + " empty bag, ignoring. statusCounter= "
                                                                + MainController.statusCounter);
                                            } else {
                                                logger.debug(
                                                        MainController.NODE_PREFIX
                                                                + " ignoring bag "
                                                                + ", " + bag
                                                                + ", statusCounter="
                                                                + MainController.statusCounter);
                                            }
                                            return; // ignore this bag.
                                        }
                                        Util.sortBag(bag);
                                        List<Shard> shards = shardService.getShards(bag);
                                        String bagString = bag.serialize();
                                        for (Shard shard : shards) {
                                            Util.writeToFile(
                                                    shard.candidateFileWriter,
                                                    bagString, true);
                                            shard.size++;
                                        }
                                        Shard shard = shardService.getShardToSearch(bag);
                                        if (null != shard) {
                                            Util.writeToFile(
                                                    shard.queryFileWriter,
                                                    bagString, true);
                                        }
                                    } else {
                                        logger.fatal(
                                                "FATAL error detected. exiting now");
                                        System.exit(1);
                                    }
                                }
                            });
                    tfr.read();
                } catch (FileNotFoundException e) {
                    logger.fatal("fatal error detected", e);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.error(MainController.NODE_PREFIX
                            + ", something nasty, exiting. counter:"
                            + MainController.statusCounter);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        } else {
            logger.error("File: " + datasetDir.getName()
                    + " is not a directory. Exiting now");
            System.exit(1);
        }
    }
}
