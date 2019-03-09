package com.mondego.indexbased;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.BytesRef;

public class CustomCollector extends Collector {
    private Map<Integer, Long> codeBlockIds;
    private int docBase;
    private IndexSearcher searcher;
    private String searchTerm;

    public CustomCollector(IndexSearcher searcher) {
        this.codeBlockIds = new HashMap<Integer, Long>();
        this.searcher = searcher;
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        this.docBase = context.docBase;
    }

    public Map<Integer, Long> getCodeBlockIds() {
        return codeBlockIds;
    }

    /**
     * @param codeBlockIds the codeBlockIds to set
     */
    public void setCodeBlockIds(Map<Integer, Long> codeBlockIds) {
        this.codeBlockIds = codeBlockIds;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * @param searchTerm the searchTerm to set
     */
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}
