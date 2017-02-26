package utility;

import models.Bag;
import models.QueryBlock;

public class BlockInfo {
	private int prefixSize;
	private int computedThreshold;
	
	public BlockInfo(float threshold, QueryBlock queryBlock){
		this.computedThreshold =  this.getMinimumSimilarityThreshold(queryBlock, threshold);
        this.prefixSize =  (queryBlock.getSize() + 1) - this.computedThreshold;
	}
	
	public BlockInfo(float threshold, Bag bag){
		this.computedThreshold =  this.getMinimumSimilarityThreshold(bag, threshold);
        this.prefixSize =  (bag.getSize() + 1) - this.computedThreshold;
	}
	
	public int getPrefixSize() {
		return prefixSize;
	}

	//test done
	public int getComputedThreshold() {
		return computedThreshold;
	}

	private int getMinimumSimilarityThreshold(QueryBlock queryBlock,float threshold) {
        return (int) Math.ceil((threshold * queryBlock.getSize())/ (Util.MUL_FACTOR*10));
    }
	private int getMinimumSimilarityThreshold(Bag bag,float threshold) {
        return (int) Math.ceil((threshold * bag.getSize())/ (Util.MUL_FACTOR*10));
    }

	//test done
	public static int getMinimumSimilarityThreshold(int metric,float threshold) {
        return (int) Math.ceil((threshold * metric)/ (Util.MUL_FACTOR*10));
    }
	//test done
	public static int getMaximumSimilarityThreshold(int metric,float threshold) {
        return (int) Math.floor((metric*Util.MUL_FACTOR*10)/threshold);
    }
	//test done
	public static float getMinimumSimilarityThreshold(float metric,float threshold) {
        return (threshold * metric)/ (Util.MUL_FACTOR*10);
    }
	//test done
    public static float getMaximumSimilarityThreshold(float metric,float threshold) {
        return (metric*Util.MUL_FACTOR*10)/threshold;
    }
	//test done
    public static long getMinimumSimilarityThreshold(long metric,float threshold) {
        return (long) Math.ceil((threshold * metric)/ (Util.MUL_FACTOR*10));
    }
	//test done
    public static long getMaximumSimilarityThreshold(long metric,float threshold) {
        return (long) Math.floor((metric*Util.MUL_FACTOR*10)/threshold);
    }
	
	public static int getPrefixSize(int size, int computedThreshold){
		return (size + 1) - computedThreshold;
	}
	
}
