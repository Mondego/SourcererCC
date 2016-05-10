package models;

import medianbased.CloneDetector;
import utility.BlockInfo;
import utility.Util;

public class Block implements Comparable<Block> {
    
    public int project_id;
    public int file_id;
    public int numTokens;
    public int minNumTokens;
    public int maxNumTokens;
    public float median;
    public float min_median;
    public float max_median;
    public float variance;
    public float minVariance;
    public float maxVariance;
    public long numChars;
    public long minNumChars;
    public long maxNumChars;
    public int uniqueTokens;
    public int minUniqueTokens;
    public int maxUniqueTokens;
    public float stdDev;
    public float minStdDev;
    public float maxStdDev;
    public float mad;
    public float minMad;
    public float maxMad;
    
    public Block(float median, int project_id, int file_id, int numTokens,float stdDev, float variance, long numChars, int uniqueTokens, float mad) {
        this.median = median;
        this.project_id = project_id;
        this.file_id = file_id;
        this.numTokens = numTokens;
        this.variance = variance;
        this.numChars = numChars;
        this.uniqueTokens=uniqueTokens;
        this.stdDev=stdDev;
        this.mad = mad;
        
        this.min_median = BlockInfo.getMinimumSimilarityThreshold(this.median, CloneDetector.th);
        this.max_median = BlockInfo.getMaximumSimilarityThreshold(this.median, CloneDetector.th);
        
        this.minNumTokens = BlockInfo.getMinimumSimilarityThreshold(this.numTokens, CloneDetector.th);
        this.maxNumTokens = BlockInfo.getMaximumSimilarityThreshold(this.numTokens, CloneDetector.th);
        
        this.minVariance = BlockInfo.getMinimumSimilarityThreshold(this.variance, CloneDetector.th);
        this.maxVariance = BlockInfo.getMaximumSimilarityThreshold(this.variance, CloneDetector.th);
        
        this.minNumChars = BlockInfo.getMinimumSimilarityThreshold(this.numChars, CloneDetector.th-(1.5f*Util.MUL_FACTOR));
        this.maxNumChars = BlockInfo.getMaximumSimilarityThreshold(this.numChars, CloneDetector.th-(1.5f*Util.MUL_FACTOR));
        
        this.minUniqueTokens = BlockInfo.getMinimumSimilarityThreshold(this.uniqueTokens, CloneDetector.th);
        this.maxUniqueTokens = BlockInfo.getMaximumSimilarityThreshold(this.uniqueTokens, CloneDetector.th);
        
        this.minStdDev = BlockInfo.getMinimumSimilarityThreshold(this.stdDev, CloneDetector.th);
        this.maxStdDev = BlockInfo.getMaximumSimilarityThreshold(this.stdDev, CloneDetector.th);
        
        this.minMad = BlockInfo.getMinimumSimilarityThreshold(this.mad, CloneDetector.th-Util.MUL_FACTOR);
        this.maxMad = BlockInfo.getMaximumSimilarityThreshold(this.mad, CloneDetector.th-Util.MUL_FACTOR);
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



    @Override
    public String toString() {
        return "Block [project_id=" + project_id + ", file_id=" + file_id
                + ", numTokens=" + numTokens + ", minNumTokens=" + minNumTokens
                + ", maxNumTokens=" + maxNumTokens + ", median=" + median
                + ", min_median=" + min_median + ", max_median=" + max_median
                + ", variance=" + variance + ", minVariance=" + minVariance
                + ", maxVariance=" + maxVariance + ", numChars=" + numChars
                + ", minNumChars=" + minNumChars + ", maxNumChars="
                + maxNumChars + ", uniqueTokens=" + uniqueTokens
                + ", minUniqueTokens=" + minUniqueTokens + ", maxUniqueTokens="
                + maxUniqueTokens + ", stdDev=" + stdDev + ", minStdDev="
                + minStdDev + ", maxStdDev=" + maxStdDev + ", mad=" + mad
                + ", minMad=" + minMad + ", maxMad=" + maxMad + "]";
    }

}
