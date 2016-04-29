package models;

import medianbased.CloneDetector;
import utility.BlockInfo;

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
    
    
    public Block(float median, int project_id, int file_id, int numTokens,float stdDev, float variance) {
        super();
        this.median = median;
        this.project_id = project_id;
        this.file_id = file_id;
        this.numTokens = numTokens;
        this.min_median = BlockInfo.getMinimumSimilarityThreshold(this.median, CloneDetector.th);
        this.max_median = BlockInfo.getMaximumSimilarityThreshold(this.median, CloneDetector.th);
        this.minNumTokens = BlockInfo.getMinimumSimilarityThreshold(this.numTokens, CloneDetector.th);
        this.maxNumTokens = BlockInfo.getMaximumSimilarityThreshold(this.numTokens, CloneDetector.th);
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
}
