package com.mondego.models;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.utility.BlockInfo;
import com.mondego.utility.Util;

public class Shard {
    int id;
    int minMetricValue, maxMetricValue;
    int minMetricValueToIndex;
    int maxMetricValueToIndex;
    public String indexPath;
    public List<Shard> subShards;
    public int size;
    public Writer candidateFileWriter;
    public Writer queryFileWriter;
    private static final Logger logger = LogManager.getLogger(Shard.class);

    public Shard(int id, int minBagSizeToSearch, int maxBagSizeToSearch,
            String indexPath, boolean forWriting) {
        this.id = id;
        this.indexPath = indexPath;
        this.minMetricValue = minBagSizeToSearch;
        this.maxMetricValue = maxBagSizeToSearch;
        this.minMetricValueToIndex = BlockInfo.getMinimumSimilarityThreshold(
                minBagSizeToSearch, SearchManager.th);
        ; // minBagSizeToSearch;
        this.maxMetricValueToIndex = BlockInfo.getMaximumSimilarityThreshold(
                maxBagSizeToSearch, SearchManager.th);
        this.subShards = new ArrayList<Shard>();
        if (forWriting) {
            logger.debug("setinverted index");
            this.setWriters();
        }
        this.size =0;
        logger.info("shard " + this + " created");
    }

    public int getId() {
        return id;
    }

    public int getMinMetricValue() {
        return minMetricValue;
    }

    public int getMaxMetricValue() {
        return maxMetricValue;
    }

    public int getMinMetricValueToIndex() {
        return minMetricValueToIndex;
    }

    public void setMinMetricValueToIndex(int minMetricValueToIndex) {
        this.minMetricValueToIndex = minMetricValueToIndex;
    }

    public int getMaxMetricValueToIndex() {
        return maxMetricValueToIndex;
    }

    public void setMaxMetricValueToIndex(int maxMetricValueToIndex) {
        this.maxMetricValueToIndex = maxMetricValueToIndex;
    }

    public void setWriters() {
        String shardFolderPath = SearchManager.ROOT_DIR
                + SearchManager.NODE_PREFIX + "/index/shards/" + this.indexPath;
        Util.createDirs(shardFolderPath);
        File candidateFile =  new File(shardFolderPath+"/candidates.file");
        File queryFile = new File(shardFolderPath+"/query.file");
        
        try {
            this.candidateFileWriter = Util.openFile(candidateFile, true);
            this.queryFileWriter = Util.openFile(queryFile, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeWriters() {
        if (this.subShards.size()>0){
            for (Shard shard : this.subShards){
                shard.closeWriters();
            }
        }else{
            Util.closeFile(this.candidateFileWriter);
            Util.closeFile(this.queryFileWriter);
            logger.info("Shard size: "+ this.size+", Shard Path: "+this.indexPath);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Shard [id=" + id + ", minSizeToSearch=" + minMetricValue + ", maxSizeToSearch="
                + maxMetricValue + ", minBagSizeToIndex=" + minMetricValueToIndex
                + ", maxBagSizeToIndex=" + maxMetricValueToIndex + ", indexPath="
                + indexPath +", subshardsSize="+subShards.size() +"]";
    }

}
