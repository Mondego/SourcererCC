/**
 * 
 */
package com.mondego.indexbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexReader;

import com.mondego.models.CandidateSimInfo;

/**
 * @author vaibhavsaini
 * 
 */
public class TermSearcher {
    private long queryId;
    private String searchTerm;
    private int freqTerm;
    private IndexReader reader;
    List<Long> earlierDocs;
    private Map<Long, CandidateSimInfo> simMap;
    private int querySize;
    private int computedThreshold;
    private String shardPath;
    private static final Logger logger = LogManager.getLogger(TermSearcher.class);
    public TermSearcher(String shardPath, long qid) {
        this.earlierDocs = new ArrayList<Long>();
        this.simMap = new HashMap<Long, CandidateSimInfo>();
        this.shardPath = shardPath;
        this.queryId = qid;
    }

    public synchronized void searchWithPosition(int queryTermsSeen) {
        return ;
    }

    /**
     * @return the searchTerm
     */
    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * @param searchTerm
     *            the searchTerm to set
     */
    public void setSearchTerm(String searchTerm) {
        // System.out.println(Util.debug_thread() + "setting searchTerm: "+
        // searchTerm);
        this.searchTerm = searchTerm;
    }

    /**
     * @return the freqTerm
     */
    public int getFreqTerm() {
        return freqTerm;
    }

    /**
     * @param freqTerm
     *            the freqTerm to set
     */
    public void setFreqTerm(int freqTerm) {
        this.freqTerm = freqTerm;
    }

    /**
     * @return the reader
     */
    public IndexReader getReader() {
        return reader;
    }

    /**
     * @param reader
     *            the reader to set
     */
    public void setReader(IndexReader reader) {
        this.reader = reader;
    }

    /**
     * @return the simMap
     */
    public Map<Long, CandidateSimInfo> getSimMap() {
        return simMap;
    }

    /**
     * @param simMap
     *            the simMap to set
     */
    public void setSimMap(ConcurrentMap<Long, CandidateSimInfo> simMap) {
        this.simMap = simMap;
    }

    public void setQuerySize(int size) {
        this.querySize = size;

    }

    public void setComputedThreshold(int computedThreshold) {
        this.computedThreshold = computedThreshold;

    }

}
