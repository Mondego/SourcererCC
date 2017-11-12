/**
 * 
 */
package com.mondego.models;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
    public Set<TokenFrequency> tokenFrequencySet;
    public String thash;
    public String metriHash;
    MessageDigest messageDigest;
    public int numActionTokens;
    public int minCandidateActionTokens;
    public int maxCandidateActionTokens;
    private static final Logger logger = LogManager.getLogger(Block.class);

    /**
     * @param id
     * @param size
     */
    public Block(String rawQuery) {
        this.tokenFrequencySet = new HashSet<TokenFrequency>();
        this.populateFields(rawQuery);
        this.minCandidateActionTokens = BlockInfo.getMinimumSimilarityThreshold(this.numActionTokens, SearchManager.th);
        this.maxCandidateActionTokens = BlockInfo.getMaximumSimilarityThreshold(this.numActionTokens, SearchManager.th);
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
            //this.functionId = Integer.parseInt(columns[8]);
            //this.id = Long.parseLong(columns[9]);
            //this.thash = columns[10];
            // this.uniqueChars = columns[10];
            StringBuilder sb = new StringBuilder();
            this.metrics = new ArrayList<Double>();
            for (int i = 11; i < 38; i++) {
                //this.metrics.add(Double.parseDouble(columns[i]));
                //sb.append(columns[i]);
            }
            try {
                messageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            messageDigest.reset();
            messageDigest.update(sb.toString().getBytes(Charset.forName("UTF8")));
            this.metriHash = new String(messageDigest.digest());
           /* for (int i = 38; i < columns.length; i++) {
                TokenFrequency tokenFrequency = new TokenFrequency();
                String tf = null;
                tf = columns[i];
                String[] tfparts = tf.split(":");
                tokenFrequency.setToken(new Token(tfparts[0]));
                tokenFrequency.setFrequency(Integer.parseInt(tfparts[1]));
                this.tokenFrequencySet.add(tokenFrequency);
                this.numActionTokens += tokenFrequency.getFrequency();
            }*/
            this.computedThreshold = BlockInfo.getMinimumSimilarityThreshold(this.size, SearchManager.th);
            this.setMaxCandidateSize(BlockInfo.getMaximumSimilarityThreshold(this.size, SearchManager.th));
            /*
             * this.maxNOS =
             * BlockInfo.getMaximumSimilarityThreshold(this.metrics.get(2),
             * SearchManager.th); this.minNOS =
             * BlockInfo.getMinimumSimilarityThreshold(this.metrics.get(2),
             * SearchManager.th);
             */

            /*
             * this.maxNEXP =
             * BlockInfo.getMaximumSimilarityThreshold(this.metrics.get(25),
             * SearchManager.th); this.minNEXP =
             * BlockInfo.getMinimumSimilarityThreshold(this.metrics.get(25),
             * SearchManager.th);
             */

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
        return "Block [id=" + id + ", size=" + size + ", functionId=" + functionId + ", computedThreshold="
                + computedThreshold + ", maxCandidateSize=" + maxCandidateSize + ", numUniqueTokens=" + numUniqueTokens
                + ", projectName=" + projectName + ", fileName=" + fileName + ", startLine=" + startLine + ", endLine="
                + endLine + ", metrics=" + metrics + ", fqmn=" + fqmn + ", rowId=" + rowId + ", minNOS=" + minNOS
                + ", maxNOS=" + maxNOS + ", minNEXP=" + minNEXP + ", maxNEXP=" + maxNEXP + ", uniqueChars="
                + uniqueChars + ", tokenFrequencySet=" + tokenFrequencySet + ", thash=" + thash + ", metriHash="
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
