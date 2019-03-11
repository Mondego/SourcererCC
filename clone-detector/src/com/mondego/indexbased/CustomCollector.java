package com.mondego.indexbased;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;

public class CustomCollector extends Collector {
    private Map<Integer, Long> codeBlockIds;
    private int docBase;
    private String searchTerm;

    public CustomCollector(IndexSearcher searcher) {
        this.codeBlockIds = new HashMap<Integer, Long>();
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    @Override
    public void collect(int doc) throws IOException {}

    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        this.docBase = context.docBase;
    }
    
    @Override
    public void setScorer(Scorer arg0) throws IOException {}

    public Map<Integer, Long> getCodeBlockIds() {
        return codeBlockIds;
    }

    public void setCodeBlockIds(Map<Integer, Long> codeBlockIds) {
        this.codeBlockIds = codeBlockIds;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public void setFreqOfSearchTerm(int frequency) {}
}
