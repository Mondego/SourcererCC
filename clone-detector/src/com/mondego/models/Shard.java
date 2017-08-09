package com.mondego.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
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
            Util.closeOutputFile(this.candidateFileWriter);
            Util.closeOutputFile(this.queryFileWriter);
            logger.info("Shard size: "+ this.size+", Shard Path: "+this.indexPath);
        }
    }
    
    public void sort(){
    	String shardFolderPath = SearchManager.ROOT_DIR
                + SearchManager.NODE_PREFIX + "/index/shards/" + this.indexPath;
    	File candidateFile =  new File(shardFolderPath+"/candidates.file");
    	BufferedReader br = null;
    	List<String> candidates = new ArrayList<String>();
        try {
			br = new BufferedReader(new FileReader(candidateFile));
			String line=null;
			while ((line = br.readLine()) != null) {
				candidates.add(line);
			}
			candidates.sort(new Comparator<String>(){

				@Override
				public int compare(String arg0, String arg1) {
					String[] columns0 = arg0.split("~~");
					String[] columns1 = arg1.split("~~");
					return Integer.valueOf(columns0[7])-Integer.valueOf(columns1[7]); // based on tokens
				}
			});
			this.candidateFileWriter = Util.openFile(candidateFile, false);
			for (String row : candidates){
				Util.writeToFile(this.candidateFileWriter, row, true);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
