
package test;

import models.Bag;
import models.Block;
import models.QueryBlock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Assert.*;
import utility.BlockInfo;

/**
 * Created by Farima on 2/21/2017.
 */
public class TestBlockInfo {

    //getMinimumSimilarityThreshold for float
    @Test
    public void testgetMinimumSimilarityThresholdZeroFloat(){
        Assert.assertEquals(0.0, BlockInfo.getMinimumSimilarityThreshold((float)0.0,800),0.001);
    }

    @Test
    public void testgetMinimumSimilarityThresholdBetweenZeroOneFloat(){
        Assert.assertEquals(0.4, BlockInfo.getMinimumSimilarityThreshold((float)0.5,800),0.001);
    }

    @Test
    public void testgetMinimumSimilarityThresholdMoreThanOneFloat(){
        Assert.assertEquals(35.6, BlockInfo.getMinimumSimilarityThreshold((float)44.5,800),0.001);
    }

    //getMaximumSimilarityThreshold for float
    @Test
    public void testgetMaximumSimilarityThresholdZeroFloat(){
        Assert.assertEquals(0, BlockInfo.getMaximumSimilarityThreshold((float) 0.0,(float) 800),0.001);
    }

    @Test
    public void testgetMaximumSimilarityThresholdBetweenZeroOneFloat(){
        Assert.assertEquals(0.625, BlockInfo.getMaximumSimilarityThreshold((float)0.5,(float)800),0.001);
    }

    @Test
    public void testgetMaximumSimilarityThresholdMoreThanOneFloat(){
        Assert.assertEquals(55.625, BlockInfo.getMaximumSimilarityThreshold((float)44.5,(float)800),0.001);
    }

    //getMinimumSimilarityThreshold for Int
    @Test
    public void testgetMinimumSimilarityThresholdZeroInt(){
        Assert.assertEquals(0, BlockInfo.getMinimumSimilarityThreshold(0,800));
    }

    @Test
    public void testgetMinimumSimilarityThresholdBetweenZeroOneInt(){
        Assert.assertEquals(1, BlockInfo.getMinimumSimilarityThreshold(1,800));
    }

    @Test
    public void testgetMinimumSimilarityThresholdMoreThanOneInt(){
        Assert.assertEquals(36, BlockInfo.getMinimumSimilarityThreshold(44,800));
    }

    //getMaximumSimilarityThreshold for Int
    @Test
    public void testgetMaximumSimilarityThresholdZeroInt(){
        Assert.assertEquals(0, BlockInfo.getMaximumSimilarityThreshold( 0, 800));
    }

    @Test
    public void testgetMaximumSimilarityThresholdBetweenZeroOneInt(){
        Assert.assertEquals(1, BlockInfo.getMaximumSimilarityThreshold(1,800));
    }

    @Test
    public void testgetMaximumSimilarityThresholdMoreThanOneInt(){
        Assert.assertEquals(52, BlockInfo.getMaximumSimilarityThreshold(42,800));
    }

    //getMinimumSimilarityThreshold for long
    @Test
    public void testgetMinimumSimilarityThresholdZeroLong(){
        Assert.assertEquals(0, BlockInfo.getMinimumSimilarityThreshold((long) 0,800));
    }

    @Test
    public void testgetMinimumSimilarityThresholdOneLong(){
        Assert.assertEquals((long) 1, BlockInfo.getMinimumSimilarityThreshold((long) 1,800));
    }

    @Test
    public void testgetMinimumSimilarityThresholdMoreThanOneLong(){
        Assert.assertEquals((long) 35200000000L, BlockInfo.getMinimumSimilarityThreshold( 44000000000L,800));
    }

    //getMaximumSimilarityThreshold for long
    @Test
    public void testgetMaximumSimilarityThresholdZeroLong(){
        Assert.assertEquals(0, BlockInfo.getMaximumSimilarityThreshold((long) 0,(float) 800));
    }

    @Test
    public void testgetMaximumSimilarityThresholdOneLong(){
        Assert.assertEquals(1, BlockInfo.getMaximumSimilarityThreshold((long)1,(float)800));
    }

    @Test
    public void testgetMaximumSimilarityThresholdMoreThanOneLong(){
        Assert.assertEquals(1250000000L, BlockInfo.getMaximumSimilarityThreshold( 1000000000L,(float)800));
    }

    //getComputedThreshold based on QueryBlock
    @Test
    public void testgetComputedThresholdZero(){
        Assert.assertEquals(0, new BlockInfo(800,new QueryBlock(1,0)).getComputedThreshold());
    }

    @Test
    public void testgetComputedThresholdOne(){
        Assert.assertEquals(1, new BlockInfo(800,new QueryBlock(1,1)).getComputedThreshold());
    }

    @Test
    public void testgetComputedThresholdMoreThanOne(){
        Assert.assertEquals(412, new BlockInfo(800,new QueryBlock(1,514)).getComputedThreshold());
    }


}
