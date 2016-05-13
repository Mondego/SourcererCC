package models;

import java.util.ArrayList;
import java.util.List;

import medianbased.CloneDetector;
import utility.BlockInfo;

public class TokenShard {
    public int id;
    public long minTokens;
    public long maxTokens;
    public List<Block> candidates;
    
    public TokenShard(int id, int minBagSizeToSearch, int maxBagSizeToSearch){
        this.id = id;
        this.minTokens = minBagSizeToSearch;
        this.maxTokens = BlockInfo.getMaximumSimilarityThreshold(maxBagSizeToSearch, CloneDetector.th);
        this.candidates = new ArrayList<Block>();
    }
    
    /**
     * find the index of the candidate whose median value is just greater or equal to the minMedian
     * @param min
     * @return
     */
    private int getIndexCandidateJustBiggerThan(float min){
        int low=0;
        int high=this.candidates.size()-1;
        int mid;
        int bestHigh=-1;
        while(low<=high){
            mid = (low+high)/2;
            if(this.candidates.get(mid).stdDev>=min){
                bestHigh = mid;
                high = mid-1;
            }else if (this.candidates.get(mid).stdDev<min){
                low = mid+1;
            }else{
                // medians are equal
                bestHigh = mid;
                return bestHigh;
            }
        }
        return bestHigh;
    }
    
    private int getIndexCandidateJustSmallerThan(float max){
        int low=0;
        int high=this.candidates.size()-1;
        int mid;
        int bestLow=-1;
        while(low<=high){
            mid = (low+high)/2;
            if(this.candidates.get(mid).stdDev>max){
                high = mid-1;
            }else if (this.candidates.get(mid).stdDev<=max){
                bestLow = mid;
                low = mid+1;
            }else{
                // medians are equal
                bestLow = mid;
                return bestLow;
            }
        }
        return bestLow;
    }
    
    public int[] getIndexRangeCandidates(float minVal, float maxVal){
        int min = 0;
        int max = 0;
        int minIndex = this.getIndexCandidateJustBiggerThan(minVal);
        
        if(minIndex==-1){
            // no min found;
            System.out.println("CEHCK THIS. no minIndex found for range: "+minVal+","+maxVal);
        }else{
           min  = this.getFirstOccurence(minIndex);
        }
        int maxIndex = this.getIndexCandidateJustSmallerThan(maxVal);
        if(maxIndex==-1){
            // no max found
            System.out.println("CEHCK THIS. no maxIndex found for range: "+minVal+","+maxVal);
        }else{
            max = this.getLastOccurence(maxIndex);
        }
        int[] minmax = new int[2];
        minmax[0]=min;
        minmax[1]=max;
        return minmax;
    }
    
    private int getFirstOccurence(int index){
        float val = this.candidates.get(index).stdDev;
        while(index>0 && this.candidates.get(index).stdDev==val){
            index=index-1;
        }
        return index+1;
    }
    
    private int getLastOccurence(int index){
        float val = this.candidates.get(index).stdDev;
        while(index<this.candidates.size() && this.candidates.get(index).stdDev==val){
            index=index+1;
        }
        return index-1;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TokenShard [id=" + id + ", minTokens=" + minTokens
                + ", maxTokens=" + maxTokens + "]";
    }
}
