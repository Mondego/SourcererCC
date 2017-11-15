/**
 * 
 */
package com.mondego.models;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.utility.BlockInfo;
import com.mondego.utility.Util;

/**
 * @author vaibhavsaini
 *
 */
public class Block {
    public long id; // file id
    public int size; // num tokens
    public long parentId; // project id
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
    public Set<TokenFrequency> actionTokenFrequencySet;
    public Set<TokenFrequency> stopwordActionTokenFrequencySet;
    public String thash;
    public String metriHash;
    MessageDigest messageDigest;
    public int numActionTokens;
    public int numStopActionToken;
    public int numTotalActionToken;
    public int minCandidateActionTokens;
    public int maxCandidateActionTokens;
    public int minCandidateTotalActionTokens;
    public int maxCandidateTotalActionTokens;
    private static final Logger logger = LogManager.getLogger(Block.class);

    /**
     * @param id
     * @param size
     */
    public Block(String rawQuery) {
        this.actionTokenFrequencySet = new HashSet<TokenFrequency>();
        this.stopwordActionTokenFrequencySet = new HashSet<TokenFrequency>();
        this.populateFields(rawQuery);
        this.minCandidateActionTokens = BlockInfo.getMinimumSimilarityThreshold(this.numActionTokens, SearchManager.th);
        this.maxCandidateActionTokens = BlockInfo.getMaximumSimilarityThreshold(this.numActionTokens, SearchManager.th);
        this.numTotalActionToken = this.numActionTokens + this.numStopActionToken;
        this.minCandidateTotalActionTokens = BlockInfo.getMinimumSimilarityThreshold(this.numTotalActionToken, SearchManager.th);
        this.maxCandidateTotalActionTokens = BlockInfo.getMaximumSimilarityThreshold(this.numTotalActionToken, SearchManager.th);
        // this.uniqueChars =
        // SearchManager.ijaMapping.get(this.fqmn).split(",")[8];

    }

    public void populateFields(String rawQuery) {
        // 465632~~selected_2351875.org.lnicholls.galleon.togo.ToGo.clean(String)~~selected~~2351875.java~~750~~763~~40~~6~~13~~10009103025115~~1~~0~~13~~109~~24~~6173.52~~0.17~~1~~1~~0~~14~~0~~1~~1~~12.35~~0~~0~~0~0~~0~~499.76~~60~~23~~49~~0~~22~~0

        try {
            String[] lineParts = rawQuery.split("@#@");
            String metadataPart = lineParts[0];
            String[] metadata = metadataPart.split(",");

            this.rowId = Long.parseLong(metadata[Util.ROW_ID]);
            this.projectName = metadata[Util.DIRECTORY];
            this.fileName = metadata[Util.FILE];
            this.startLine = Integer.parseInt(metadata[Util.START_LINE]);
            this.endLine = Integer.parseInt(metadata[Util.END_LINE]);
            this.size = Integer.parseInt(metadata[Util.NUM_TOKENS]);
            this.numUniqueTokens = Integer.parseInt(metadata[Util.NUM_UNIQUE_TOKENS]);
            this.metriHash = metadata[Util.METRIC_HASH];
            this.parentId = Integer.parseInt(metadata[Util.PARENT_ID]);
            this.id = Long.parseLong(metadata[Util.BLOCK_ID]);

            String metricPart = lineParts[1];
            String[] metrics = metricPart.split(",");
            this.metrics = new ArrayList<Double>();
            for (String metricVal : metrics) {
                this.metrics.add(Double.parseDouble(metricVal));
            }
            String actionTokensPart = lineParts[2];
            if (actionTokensPart.trim().length() > 0) {
                String[] actionTokens = actionTokensPart.split(",");
                for (String actionTokenFreqPairString : actionTokens) {
                    String[] actionTokenFreqPair = actionTokenFreqPairString.split(":");
                    TokenFrequency tokenFrequency = new TokenFrequency();
                    tokenFrequency.setToken(new Token(actionTokenFreqPair[0]));
                    tokenFrequency.setFrequency(Integer.parseInt(actionTokenFreqPair[1]));
                    this.actionTokenFrequencySet.add(tokenFrequency);
                    this.numActionTokens += tokenFrequency.getFrequency();
                }
            }

            String stopwordActionTokensPart = lineParts[2];
            if (stopwordActionTokensPart.trim().length() > 0) {
                String[] stopwordActionTokens = actionTokensPart.split(",");
                for (String stopwordactionTokenFreqPairString : stopwordActionTokens) {
                    String[] stopwwordActionTokenFreqPair = stopwordactionTokenFreqPairString.split(":");
                    TokenFrequency tokenFrequency = new TokenFrequency();
                    tokenFrequency.setToken(new Token(stopwwordActionTokenFreqPair[0]));
                    tokenFrequency.setFrequency(Integer.parseInt(stopwwordActionTokenFreqPair[1]));
                    this.stopwordActionTokenFrequencySet.add(tokenFrequency);
                    this.numStopActionToken += tokenFrequency.getFrequency();
                }
            }
            this.computedThreshold = BlockInfo.getMinimumSimilarityThreshold(this.size, SearchManager.th);
            this.setMaxCandidateSize(BlockInfo.getMaximumSimilarityThreshold(this.size, SearchManager.th));

        } catch (ArrayIndexOutOfBoundsException e) {

            logger.error(e.getMessage() + ", " + rawQuery);
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
        return parentId;
    }

    public void setFunctionId(long functionId) {
        this.parentId = functionId;
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
        return "Block [id=" + id + ", size=" + size + ", functionId=" + parentId + ", computedThreshold="
                + computedThreshold + ", maxCandidateSize=" + maxCandidateSize + ", numUniqueTokens=" + numUniqueTokens
                + ", projectName=" + projectName + ", fileName=" + fileName + ", startLine=" + startLine + ", endLine="
                + endLine + ", metrics=" + metrics + ", fqmn=" + fqmn + ", rowId=" + rowId + ", minNOS=" + minNOS
                + ", maxNOS=" + maxNOS + ", minNEXP=" + minNEXP + ", maxNEXP=" + maxNEXP + ", uniqueChars="
                + uniqueChars + ", tokenFrequencySet=" + actionTokenFrequencySet + ", thash=" + thash + ", metriHash="
                + metriHash + ", numActionTokens=" + numActionTokens + ", minCandidateActionTokens="
                + minCandidateActionTokens + ", maxCandidateActionTokens=" + maxCandidateActionTokens + "]";
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
