/**
 * 
 */
package com.mondego.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.utility.BlockInfo;

/**
 * @author vaibhavsaini
 *
 */
public class Block {
    public long id; // file id
    public int size;  // num tokens
    public long functionId; // project id
    public int prefixSize;
    public int computedThreshold;
    public int maxCandidateSize;
    public int numUniqueTokens;
    public String projectName;
    public String fileName;
    public int startLine;
    public int endLine;
    public List<Double> metrics;
    public String fqmn;
    public long rowId;
    public int minNOS;
    public int maxNOS;
    public int minNEXP;
    public int maxNEXP;
    public String uniqueChars;
    private static final Logger logger = LogManager.getLogger(Block.class);
    /**
     * @param id
     * @param size
     */
    public Block(String rawQuery) {
        this.populateMetrics(rawQuery);
        this.uniqueChars = SearchManager.ijaMapping.get(this.fqmn).split(",")[8];
        
    }

    public void populateMetrics(String rawQuery){
    	// 465632~~selected_2351875.org.lnicholls.galleon.togo.ToGo.clean(String)~~selected~~2351875.java~~750~~763~~40~~6~~13~~10009103025115~~1~~0~~13~~109~~24~~6173.52~~0.17~~1~~1~~0~~14~~0~~1~~1~~12.35~~0~~0~~0~0~~0~~499.76~~60~~23~~49~~0~~22~~0
    	
    	try{
    		String[] columns = rawQuery.split("~~");
        	this.rowId = Long.parseLong(columns[0]);
        	this.fqmn = columns[1];
        	this.projectName= columns[2];
        	this.fileName = columns[3];
        	this.startLine = Integer.parseInt(columns[4]);
        	this.endLine = Integer.parseInt(columns[5]);
        	this.size = Integer.parseInt(columns[6]);
        	this.numUniqueTokens = Integer.parseInt(columns[7]);
        	this.functionId = Integer.parseInt(columns[8]);
        	this.id = Long.parseLong(columns[9]);
        	this.metrics = new ArrayList<Double>();
        	for (int i=11;i<columns.length;i++){
        		this.metrics.add(Double.parseDouble(columns[i]));
        	}
        	this.computedThreshold = BlockInfo
                    .getMinimumSimilarityThreshold(this.numUniqueTokens, 600);
            this.setMaxCandidateSize(BlockInfo
                    .getMaximumSimilarityThreshold(this.numUniqueTokens, 600));
            this.maxNOS = BlockInfo.getMaximumSimilarityThreshold(this.metrics.get(2), SearchManager.th);
            this.minNOS = BlockInfo.getMinimumSimilarityThreshold(this.metrics.get(2), SearchManager.th);
            
            this.maxNEXP = BlockInfo.getMaximumSimilarityThreshold(this.metrics.get(25), SearchManager.th);
            this.minNEXP = BlockInfo.getMinimumSimilarityThreshold(this.metrics.get(25), SearchManager.th);
            
    	}catch (ArrayIndexOutOfBoundsException e){
    		
    		logger.error(e.getMessage()+", "+rawQuery);
    		System.exit(1);
    	}
    	
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

    public long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(long functionId) {
        this.functionId = functionId;
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

    public int getMaxCandidateSize() {
        return maxCandidateSize;
    }

    public void setMaxCandidateSize(int maxCandidateSize) {
        this.maxCandidateSize = maxCandidateSize;
    }

    @Override
	public String toString() {
		return "Block [id=" + id + ", size=" + size + ", functionId=" + functionId + ", prefixSize=" + prefixSize
				+ ", computedThreshold=" + computedThreshold + ", maxCandidateSize=" + maxCandidateSize
				+ ", numUniqueTokens=" + numUniqueTokens + ", projectName=" + projectName + ", fileName=" + fileName
				+ ", startLine=" + startLine + ", endLine=" + endLine + ", metrics=" + metrics + ", fqmn=" + fqmn
				+ ", rowId=" + rowId + "]";
	}

    public long getSize() {
		return this.size;
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
