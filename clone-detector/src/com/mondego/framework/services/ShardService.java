package com.mondego.framework.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.application.handlers.ShardsHandler;
import com.mondego.framework.controllers.MainController;
import com.mondego.framework.models.Bag;
import com.mondego.framework.models.Shard;

public class ShardService {
    private static ShardService instance;
    private static final Logger logger = LogManager
            .getLogger(ShardService.class);
    
    private ShardService(){
        //this.runtimeStateService = RuntimeStateService.getInstance();
    }
    
    public static synchronized ShardService getInstance(){
        if (null==instance){
            instance = new ShardService();
        }
        return instance;
    }
    public void createShards(boolean forWriting) {
        int l1MinTokens = MainController.min_tokens;
        int l1MaxTokens = MainController.max_tokens;
        int l1ShardId = 1;
        MainController.shards = new ArrayList<Shard>();
        if (MainController.isSharding) {
            String level1ShardSegmentString = MainController.properties
                    .getProperty("LEVEL_1_SHARD_MAX_NUM_TOKENS");
            logger.info("level1ShardSegmentString String is : "
                    + level1ShardSegmentString);
            List<String> level1ShardSegments = new ArrayList<String>(
                    Arrays.asList(level1ShardSegmentString.split(",")));
            level1ShardSegments.add(MainController.max_tokens + ""); // add the
                                                                     // last
                                                                     // shard
            for (String segment : level1ShardSegments) {
                // create shards
                l1MaxTokens = Integer.parseInt(segment);
                String l1Path = l1ShardId + "";
                Shard level1Shard = null;

                String level2ShardSegmentString = MainController.properties
                        .getProperty("LEVEL_2_SHARD_MAX_NUM_TOKENS");
                if (null != level2ShardSegmentString) {
                    level1Shard = new Shard(l1ShardId, l1MinTokens, l1MaxTokens,
                            l1Path, false);
                    this.createSubShards(l1Path, level1Shard, 2, forWriting);
                } else {
                    level1Shard = new Shard(l1ShardId, l1MinTokens, l1MaxTokens,
                            l1Path, forWriting);
                }
                MainController.shards.add(level1Shard);
                l1MinTokens = l1MaxTokens + 1;
                l1ShardId++;
            }
        } else {
            Shard shard = new Shard(l1ShardId, MainController.min_tokens,
                    MainController.max_tokens, l1ShardId + "", forWriting);
            MainController.shards.add(shard);
        }
        logger.debug("Number of Top level shards created: "
                + MainController.shards.size());
    }

    private void createSubShards(String parentShardPath, Shard parentShard,
            int level, boolean forWriting) {
        String shardSegmentString = MainController.properties
                .getProperty("LEVEL_" + level + "_SHARD_MAX_NUM_TOKENS");
        logger.info(level + " Segment String is : " + shardSegmentString);

        int metricMin = Integer.parseInt(
                MainController.properties.getProperty("LEVEL_" + level + "_MIN_TOKENS"));
        int metricMax = 0;
        int shardId = 1;
        List<String> shardSegments = new ArrayList<String>(
                Arrays.asList(shardSegmentString.split(",")));
        shardSegments
                .add(MainController.properties.getProperty("LEVEL_" + level + "_MAX_TOKENS")); // add
        // the
        // last
        // shard
        for (String segment : shardSegments) {
            // create shards
            metricMax = Integer.parseInt(segment);
            String shardPath = parentShardPath + "/" + shardId;
            int nextLevel = level + 1;
            String nextShardSegmentString = MainController.properties.getProperty(
                    "LEVEL_" + nextLevel + "_SHARD_MAX_NUM_TOKENS");
            Shard shard = null;
            if (null != nextShardSegmentString) {
                shard = new Shard(shardId, metricMin, metricMax, shardPath,
                        false);
                this.createSubShards(shardPath, shard, nextLevel, forWriting);
            } else {
                shard = new Shard(shardId, metricMin, metricMax, shardPath,
                        forWriting);
            }

            parentShard.subShards.add(shard);
            metricMin = metricMax + 1;
            shardId++;
        }
    }

    // this bag needs to be indexed in following shards
    public List<Shard> getShards(Bag bag) {
        List<Shard> shardsToReturn = new ArrayList<Shard>();
        int level = 0;
        for (Shard shard : MainController.shards)
            if (bag.metrics.get(
                    ShardsHandler.METRICS_ORDER_IN_SHARDS.get(level)) >= shard
                            .getMinMetricValueToIndex()
                    && bag.metrics.get(ShardsHandler.METRICS_ORDER_IN_SHARDS
                            .get(level)) <= shard.getMaxMetricValueToIndex()) {
                this.getSubShards(bag, shard, level + 1,
                        shardsToReturn);
            }
        return shardsToReturn;
    }

    public void getSubShards(Bag bag, Shard parentShard, int level,
            List<Shard> shardsToReturn) {
        if (parentShard.subShards.size() > 0) {
            for (Shard shard : parentShard.subShards) {
                if (bag.metrics.get(ShardsHandler.METRICS_ORDER_IN_SHARDS
                        .get(level)) >= shard.getMinMetricValueToIndex()
                        && bag.metrics
                                .get(ShardsHandler.METRICS_ORDER_IN_SHARDS
                                        .get(level)) <= shard
                                                .getMaxMetricValueToIndex()) {
                    this.getSubShards(bag, shard, level + 1,
                            shardsToReturn);
                }
            }
        } else {
            shardsToReturn.add(parentShard);
        }
    }

    // This query needs to be directed to the following shard
    public Shard getShardToSearch(Bag bag) {
        Shard shard = this.getRootShard(bag);
        int level = 1;
        if (null != shard) {
            return this.getShardRecursive(bag, shard, level);
        } else {
            return shard;
        }
    }

    public Shard getRootShard(Bag bag) {

        int low = 0;
        int high = MainController.shards.size() - 1;
        int mid = (low + high) / 2;
        Shard shard = null;
        while (low <= high) {
            shard = MainController.shards.get(mid);
            if (bag.getSize() >= shard.getMinMetricValue()
                    && bag.getSize() <= shard.getMaxMetricValue()) {
                break;
            } else {
                if (bag.getSize() < shard.getMinMetricValue()) {
                    high = mid - 1;
                } else if (bag.getSize() > shard.getMaxMetricValue()) {
                    low = mid + 1;
                }
                mid = (low + high) / 2;
            }
        }
        return shard;
    }

    public Shard getShardRecursive(Bag bag, Shard parentShard,
            int level) {
        if (parentShard.subShards.size() > 0) {
            int low = 0;
            int high = parentShard.subShards.size() - 1;
            int mid = (low + high) / 2;
            Shard shard = null;
            while (low <= high) {
                shard = parentShard.subShards.get(mid);
                if (bag.metrics.get(ShardsHandler.METRICS_ORDER_IN_SHARDS
                        .get(level)) >= shard.getMinMetricValue()
                        && bag.metrics
                                .get(ShardsHandler.METRICS_ORDER_IN_SHARDS.get(
                                        level)) <= shard.getMaxMetricValue()) {
                    return this.getShardRecursive(bag, shard,
                            level + 1);
                } else {
                    if (bag.metrics.get(ShardsHandler.METRICS_ORDER_IN_SHARDS
                            .get(level)) < shard.getMinMetricValue()) {
                        high = mid - 1;
                    } else if (bag.metrics
                            .get(ShardsHandler.METRICS_ORDER_IN_SHARDS
                                    .get(level)) > shard.getMaxMetricValue()) {
                        low = mid + 1;
                    }
                    mid = (low + high) / 2;

                }
            }
            return shard;
        } else {
            return parentShard;
        }
    }
}
