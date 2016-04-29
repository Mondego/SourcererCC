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
     * @param minMedian
     * @return
     */
    private int getIndexCandidateJustBiggerThan(float minMedian){
        int low=0;
        int high=this.candidates.size()-1;
        int mid;
        int bestHigh=-1;
        while(low<=high){
            mid = (low+high)/2;
            if(this.candidates.get(mid).median>minMedian){
                bestHigh = mid;
                high = mid-1;
            }else if (this.candidates.get(mid).median<minMedian){
                low = mid+1;
            }else{
                // medians are equal
                bestHigh = mid;
                return bestHigh;
            }
        }
        return bestHigh;
    }
    
    private int getIndexCandidateJustSmallerThan(float maxMedian){
        int low=0;
        int high=this.candidates.size()-1;
        int mid;
        int bestLow=-1;
        while(low<=high){
            mid = (low+high)/2;
            if(this.candidates.get(mid).median>maxMedian){
                high = mid-1;
            }else if (this.candidates.get(mid).median<maxMedian){
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
    
    public int[] getIndexRangeCandidates(float minMedian, float maxMedian){
        int min = 0;
        int max = 0;
        int minIndex = this.getIndexCandidateJustBiggerThan(minMedian);
        
        if(minIndex==-1){
            // no min found;
            System.out.println("CEHCK THIS. no minIndex found for range: "+minMedian+","+maxMedian);
        }else{
           min  = this.getFirstOccurence(minIndex);
        }
        int maxIndex = this.getIndexCandidateJustSmallerThan(maxMedian);
        if(maxIndex==-1){
            // no max found
            System.out.println("CEHCK THIS. no maxIndex found for range: "+minMedian+","+maxMedian);
        }else{
            max = this.getLastOccurence(maxIndex);
        }
        int[] minmax = new int[2];
        minmax[0]=min;
        minmax[1]=max;
        return minmax;
    }
    
    private int getFirstOccurence(int index){
        float val = this.candidates.get(index).median;
        while(index>0 && this.candidates.get(index).median==val){
            index=index-1;
        }
        return index+1;
    }
    
    private int getLastOccurence(int index){
        float val = this.candidates.get(index).median;
        while(index<this.candidates.size() && this.candidates.get(index).median==val){
            index=index+1;
        }
        return index+1;
    }
}
