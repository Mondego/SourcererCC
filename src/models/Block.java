package models;

import medianbased.CloneDetector;
import utility.BlockInfo;
import utility.Util;

public class Block implements Comparable<Block> {
    public float median;
    public int project_id;
    public int file_id;
    public int numTokens;
    public float min_median;
    public float max_median;
    public float stdDev;
    public float variance;
    public int maxNumTokens;
    public int minNumTokens;
    public float minVariance;
    public float maxVariance;
    public long numChars;
    public long minNumChars;
    public long maxNumChars;
    
    
    public Block(float median, int project_id, int file_id, int numTokens,float stdDev, float variance, long numChars) {
        super();
        this.median = median;
        this.project_id = project_id;
        this.file_id = file_id;
        this.numTokens = numTokens;
        this.min_median = BlockInfo.getMinimumSimilarityThreshold(this.median, CloneDetector.th+Util.MUL_FACTOR);
        this.max_median = BlockInfo.getMaximumSimilarityThreshold(this.median, CloneDetector.th+Util.MUL_FACTOR);
        this.minNumTokens = BlockInfo.getMinimumSimilarityThreshold(this.numTokens, CloneDetector.th);
        this.maxNumTokens = BlockInfo.getMaximumSimilarityThreshold(this.numTokens, CloneDetector.th);
        this.minVariance = BlockInfo.getMaximumSimilarityThreshold(this.variance, CloneDetector.th+Util.MUL_FACTOR);
        this.maxVariance = BlockInfo.getMaximumSimilarityThreshold(this.variance, CloneDetector.th+Util.MUL_FACTOR);
        this.minNumChars = BlockInfo.getMinimumSimilarityThreshold(this.numChars, CloneDetector.th-Util.MUL_FACTOR);
        this.maxNumChars = BlockInfo.getMaximumSimilarityThreshold(this.numChars, CloneDetector.th-Util.MUL_FACTOR);
    }
    
    
    
    @Override
    public int compareTo(Block o) {
        if (this.median<o.median){
            return -1;
        }else if (this.median==o.median){
            return 0;
        }else{
            return 1;
        }
    }



    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Block [median=" + median + ", project_id=" + project_id
                + ", file_id=" + file_id + ", numTokens=" + numTokens
                + ", min_median=" + min_median + ", max_median=" + max_median
                + ", stdDev=" + stdDev + ", variance=" + variance
                + ", maxNumTokens=" + maxNumTokens + ", minNumTokens="
                + minNumTokens + ", minVariance=" + minVariance
                + ", maxVariance=" + maxVariance + ", numChars=" + numChars
                + ", minNumChars=" + minNumChars + ", maxNumChars="
                + maxNumChars + "]";
    }
}
