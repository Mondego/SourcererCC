package models;

import indexbased.SearchManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utility.BlockInfo;

public class Shard {
    int id;
    int minBagSizeToIndex;
    int maxBagSizeToIndex;
    IndexWriter invertedIndexWriter;
    IndexWriter forwardIndexWriter;
    
    public Shard(int id, int minBagSizeToSearch, int maxBagSizeToSearch){
        this.id = id;
        this.minBagSizeToIndex = minBagSizeToSearch;
        this.maxBagSizeToIndex = BlockInfo.getMaximumSimilarityThreshold(maxBagSizeToSearch, SearchManager.th);
        System.out.println("setinverted index");
        this.setInvertedIndexWriter();
        System.out.println("set forward index");
        this.setForwardIndexWriter();
        System.out.println("ok");
    }

    public int getMinBagSizeToIndex() {
        return minBagSizeToIndex;
    }

    public void setMinBagSizeToIndex(int minBagSizeToIndex) {
        this.minBagSizeToIndex = minBagSizeToIndex;
    }

    public int getMaxBagSizeToIndex() {
        return maxBagSizeToIndex;
    }

    public void setMaxBagSizeToIndex(int maxBagSizeToIndex) {
        this.maxBagSizeToIndex = maxBagSizeToIndex;
    }

    public IndexWriter getInvertedIndexWriter() {
        return invertedIndexWriter;
    }

    public void setInvertedIndexWriter() {
        WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(
                Version.LUCENE_46);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_46, whitespaceAnalyzer);
        indexWriterConfig.setRAMBufferSizeMB(SearchManager.ramBufferSizeMB);
        indexWriterConfig.setOpenMode(OpenMode.CREATE);
        // index
        TieredMergePolicy mergePolicy = (TieredMergePolicy) indexWriterConfig
                .getMergePolicy();

        mergePolicy.setNoCFSRatio(0);// what was this for?
        mergePolicy.setMaxCFSSegmentSizeMB(0); // what was this for?
        
        try {
            FSDirectory dir = FSDirectory.open(new File(
                    SearchManager.NODE_PREFIX + "/index/shards/" + this.id));
            if(SearchManager.invertedIndexDirectoriesOfShard.containsKey(id)){
                List<FSDirectory> dirs =  SearchManager.invertedIndexDirectoriesOfShard.get(id);
                dirs.add(dir);
            }else{
                List<FSDirectory> dirs = new ArrayList<FSDirectory>();
                dirs.add(dir);
                SearchManager.invertedIndexDirectoriesOfShard.put(id, dirs);
            }
            this.invertedIndexWriter = new IndexWriter(dir, indexWriterConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IndexWriter getForwardIndexWriter() {
        return forwardIndexWriter;
    }

    public void setForwardIndexWriter() {
        KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
        IndexWriterConfig fwdIndexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_46, keywordAnalyzer);
        fwdIndexWriterConfig.setRAMBufferSizeMB(SearchManager.ramBufferSizeMB);
        fwdIndexWriterConfig.setOpenMode(OpenMode.CREATE);
        TieredMergePolicy mergePolicy = (TieredMergePolicy) fwdIndexWriterConfig
                .getMergePolicy();

        mergePolicy.setNoCFSRatio(0);// what was this for?
        mergePolicy.setMaxCFSSegmentSizeMB(0); // what was this for?
        try {
            FSDirectory dir = FSDirectory
                    .open(new File(SearchManager.NODE_PREFIX
                            + "/fwdindex/shards/" + id));
            if(SearchManager.forwardIndexDirectoriesOfShard.containsKey(id)){
                List<FSDirectory> dirs =  SearchManager.forwardIndexDirectoriesOfShard.get(id);
                dirs.add(dir);
            }else{
                List<FSDirectory> dirs = new ArrayList<FSDirectory>();
                dirs.add(dir);
                SearchManager.forwardIndexDirectoriesOfShard.put(id, dirs);
            }
            this.forwardIndexWriter = new IndexWriter(dir, fwdIndexWriterConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void closeInvertedIndexWriter() {
        try {
            this.invertedIndexWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void closeForwardIndexWriter() {
        try {
            this.forwardIndexWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Shard [id=" + id + ", minBagSizeToIndex=" + minBagSizeToIndex
                + ", maxBagSizeToIndex=" + maxBagSizeToIndex + "]";
    }
}
