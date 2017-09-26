/**
 * 
 */
package com.mondego.models;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mondego.framework.controllers.MainController;
import com.mondego.utility.BlockInfo;

/**
 * @author vaibhavsaini
 *
 */
public class QueryBlock {
    private long id;
    private int size;
    private long functionId;
    private Map<String, TokenInfo> prefixMap;
    private Map<String, TokenInfo> suffixMap;
    private int prefixSize;
    private int computedThreshold;
    private int prefixMapSize;
    private int maxCandidateSize;
    private String shardPath;
    private int numUniqueTokens;
    public Map<String,Long> metrics;

    /**
     * @param id
     * @param size
     */
    public QueryBlock(long id, int size) {
        super();
        this.id = id;
        this.size = size;
        this.functionId = -1;
        this.prefixMap = new LinkedHashMap<String, TokenInfo>();
        this.suffixMap = new HashMap<String, TokenInfo>();
        this.computedThreshold = BlockInfo
                .getMinimumSimilarityThreshold(this.size, MainController.th);
        this.setMaxCandidateSize(BlockInfo
                .getMaximumSimilarityThreshold(this.size, MainController.th));
        this.prefixSize = BlockInfo.getPrefixSize(this.size,
                this.computedThreshold);
        this.numUniqueTokens = 0;
        this.metrics = new HashMap<String,Long>();

    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    public int getSize() {
        if (this.size == 0) {
            for (TokenInfo tokenInfo : this.prefixMap.values()) {
                this.size += tokenInfo.getFrequency();
            }
            for (TokenInfo tokenInfo : this.suffixMap.values()) {
                this.size += tokenInfo.getFrequency();
            }
        }
        return this.size;
    }

    public long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(long functionId) {
        this.functionId = functionId;
    }

    public Map<String, TokenInfo> getPrefixMap() {
        return prefixMap;
    }

    public void setPrefixMap(Map<String, TokenInfo> prefixMap) {
        this.prefixMap = prefixMap;
    }

    public Map<String, TokenInfo> getSuffixMap() {
        return suffixMap;
    }

    public void setSuffixMap(Map<String, TokenInfo> suffixMap) {
        this.suffixMap = suffixMap;
    }

    public int getPrefixSize() {
        return prefixSize;
    }

    public void setPrefixSize(int prefixSize) {
        this.prefixSize = prefixSize;
    }

    public int getComputedThreshold() {
        return computedThreshold;
    }

    public void setComputedThreshold(int computedThreshold) {
        this.computedThreshold = computedThreshold;
    }

    public int getPrefixMapSize() {
        return prefixMapSize;
    }

    public void setPrefixMapSize(int prefixMapSize) {
        this.prefixMapSize = prefixMapSize;
    }


    public int getMaxCandidateSize() {
        return maxCandidateSize;
    }

    public void setMaxCandidateSize(int maxCandidateSize) {
        this.maxCandidateSize = maxCandidateSize;
    }

    public void setShardPath(String path) {
        this.shardPath = path;
    }

    public String getShardPath() {
        return this.shardPath;
    }

    @Override
    public String toString() {
        return this.getFunctionId() + "," + this.getId() + "," + this.getSize() + ","+ this.getNumUniqueTokens();
    }

    /**
     * @return the numUniqueTokens
     */
    public int getNumUniqueTokens() {
        return numUniqueTokens;
    }

    /**
     * @param numUniqueTokens
     *            the numUniqueTokens to set
     */
    public void setNumUniqueTokens(int numUniqueTokens) {
        this.numUniqueTokens = numUniqueTokens;
    }
}
