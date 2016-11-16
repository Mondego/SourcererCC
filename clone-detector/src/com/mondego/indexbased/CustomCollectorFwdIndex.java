/**
 * 
 */
package com.mondego.indexbased;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

/**
 * @author vaibhavsaini
 * 
 */
public class CustomCollectorFwdIndex extends Collector {
    private List<Integer> blocks;
    private int docBase;

    public CustomCollectorFwdIndex() {
        this.blocks = new ArrayList<Integer>();
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    @Override
    public void collect(int doc) throws IOException {
        Integer docId = doc + docBase;
        this.blocks.add(docId);
    }

    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        this.docBase = context.docBase;
    }

    @Override
    public void setScorer(Scorer arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * @return the blocks
     */
    public List<Integer> getBlocks() {
        return blocks;
    }

    /**
     * @param blocks the blocks to set
     */
    public void setBlocks(List<Integer> blocks) {
        this.blocks = blocks;
    }

}
