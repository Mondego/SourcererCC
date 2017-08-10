package com.mondego.utility;

import com.mondego.indexbased.SearchManager;
import com.mondego.models.Bag;
import com.mondego.models.Block;

public class BlockInfo {
	private int prefixSize;
	private int computedThreshold;
	
	public BlockInfo(float threshold, Block queryBlock){
		this.computedThreshold =  this.getMinimumSimilarityThreshold(queryBlock, threshold);
	}
	
	public BlockInfo(float threshold, Bag bag){
		this.computedThreshold =  this.getMinimumSimilarityThreshold(bag, threshold);
        this.prefixSize =  (bag.getSize() + 1) - this.computedThreshold;
	}
	
	public int getPrefixSize() {
		return prefixSize;
	}

	public int getComputedThreshold() {
		return computedThreshold;
	}

	private int getMinimumSimilarityThreshold(Block queryBlock,float threshold) {
        return (int) Math.ceil((threshold * queryBlock.getSize())/ (SearchManager.MUL_FACTOR*10));
    }
	private int getMinimumSimilarityThreshold(Bag bag,float threshold) {
        return (int) Math.ceil((threshold * bag.getSize())/ (SearchManager.MUL_FACTOR*10));
    }
	
	public static int getMinimumSimilarityThreshold(int size,float threshold) {
        return (int) Math.ceil((threshold * size)/ (SearchManager.MUL_FACTOR*10));
    }
	public static int getMaximumSimilarityThreshold(int size,float threshold) {
        return (int) Math.floor((size*SearchManager.MUL_FACTOR*10)/threshold);
    }
	public static int getMaximumSimilarityThreshold(double size,float threshold) {
        return (int) Math.floor((size*SearchManager.MUL_FACTOR*10)/threshold);
    }
	public static int getMinimumSimilarityThreshold(double size,float threshold) {
        return (int) Math.ceil((threshold * size)/ (SearchManager.MUL_FACTOR*10));
    }
	
	public static int getPrefixSize(int size, int computedThreshold){
		return (size + 1) - computedThreshold;
	}
	
}
