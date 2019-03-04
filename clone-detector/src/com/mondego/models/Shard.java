package com.mondego.models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.mondego.indexbased.SearchManager;
import com.mondego.utility.BlockInfo;

public class Shard {
    int id;
    int minSize, maxSize;
    int minBagSizeToIndex;
    int maxBagSizeToIndex;
    IndexWriter invertedIndexWriter;
    IndexWriter forwardIndexWriter;
    private static final Logger logger = LogManager.getLogger(Shard.class);

    public Shard(int id, int minBagSizeToSearch, int maxBagSizeToSearch,
            boolean forWriting) {
        this.id = id;
        this.minSize = minBagSizeToSearch;
        this.maxSize = maxBagSizeToSearch;
        this.minBagSizeToIndex = BlockInfo.getMinimumSimilarityThreshold(
                minBagSizeToSearch, SearchManager.th);
        ; // minBagSizeToSearch;
        this.maxBagSizeToIndex = BlockInfo.getMaximumSimilarityThreshold(
                maxBagSizeToSearch, SearchManager.th);
        if (forWriting) {
            logger.debug("setinverted index");
            this.setInvertedIndexWriter();
            logger.debug("set forward index");
            this.setForwardIndexWriter();
        }
        logger.info("shard " + this + " created");
    }

    public int getId() {
        return id;
    }

    public int getMinSize() {
        return minSize;
    }

    public int getMaxSize() {
        return maxSize;
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
            FSDirectory dir = FSDirectory.open(new File(SearchManager.ROOT_DIR
                    + SearchManager.NODE_PREFIX + "/index/shards/" + this.id));
            if (SearchManager.invertedIndexDirectoriesOfShard.containsKey(id)) {
                List<FSDirectory> dirs = SearchManager.invertedIndexDirectoriesOfShard
                        .get(id);
                dirs.add(dir);
            } else {
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
            FSDirectory dir = FSDirectory.open(new File(SearchManager.ROOT_DIR
                    + SearchManager.NODE_PREFIX + "/fwdindex/shards/" + id));
            if (SearchManager.forwardIndexDirectoriesOfShard.containsKey(id)) {
                List<FSDirectory> dirs = SearchManager.forwardIndexDirectoriesOfShard
                        .get(id);
                dirs.add(dir);
            } else {
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
            logger.error(e.getMessage());
        }
    }

    public void closeForwardIndexWriter() {
        try {
            this.forwardIndexWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Shard [id=" + id + ", minBagSizeToIndex=" + minBagSizeToIndex
                + ", maxBagSizeToIndex=" + maxBagSizeToIndex + "]";
    }
}
