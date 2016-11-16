/**
 * 
 */
package com.mondego.indexbased;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;

import com.mondego.models.CandidateSimInfo;
import com.mondego.utility.Util;

/**
 * @author vaibhavsaini
 * 
 */
public class TermSearcher {
    private long queryId;
    private String searchTerm;
    private int freqTerm;
    private IndexReader reader;
    List<Long> earlierDocs;
    private Map<Long, CandidateSimInfo> simMap;
    private int querySize;
    private int computedThreshold;
    private int shard;
    private static final Logger logger = LogManager.getLogger(TermSearcher.class);
    public TermSearcher(int shard, long qid) {
        this.earlierDocs = new ArrayList<Long>();
        this.simMap = new HashMap<Long, CandidateSimInfo>();
        this.shard = shard;
        this.queryId = qid;
    }

    public synchronized void searchWithPosition(int queryTermsSeen) {
        if (null != this.reader) {
            if (null != this.reader.getContext()) {
                if (null != this.reader.getContext().leaves()) {
                    Term term = new Term("tokens", this.searchTerm);
                    for (AtomicReaderContext ctx : this.reader.getContext()
                            .leaves()) {
                        int base = ctx.docBase;
                        // SpanTermQuery spanQ = new SpanTermQuery(term);
                        try {
                            DocsAndPositionsEnum docEnum = MultiFields
                                    .getTermPositionsEnum(ctx.reader(),
                                            MultiFields.getLiveDocs(ctx
                                                    .reader()), "tokens", term
                                                    .bytes());
                            if (null != docEnum) {
                                int doc = DocsEnum.NO_MORE_DOCS;
                                while ((doc = docEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
                                    long docId = doc + base;
                                    CandidateSimInfo simInfo = null;
                                    if (this.simMap.containsKey(docId)) {
                                        simInfo = this.simMap.get(docId);
                                        simInfo.similarity = simInfo.similarity
                                                + Math.min(freqTerm,
                                                        docEnum.freq());

                                    } else {
                                        if (earlierDocs.contains(docId))
                                            continue;

                                        Document d = SearchManager.searcher
                                                .get(shard).getDocument(docId);
                                        long candidateId = Long.parseLong(d
                                                .get("id"));
                                        // Get rid of these early -- we're only
                                        // looking for candidates
                                        // whose ids are smaller than the query
                                        if (candidateId >= this.queryId) {
                                            // System.out.println("Query " +
                                            // this.queryId +
                                            // ", getting rid of " +
                                            // candidateId);
                                            earlierDocs.add(docId);
                                            continue; // we reject the candidate
                                        }

                                        simInfo = new CandidateSimInfo();
                                        simInfo.doc = d;
                                        simInfo.candidateSize = Integer
                                                .parseInt(d.get("size"));
                                        simInfo.similarity = Math.min(freqTerm,
                                                docEnum.freq());
                                        // System.out.println("before putting in simmap "+
                                        // Util.debug_thread());
                                        this.simMap.put(docId, simInfo);
                                        // System.out.println("after putting in simmap "+
                                        // Util.debug_thread());
                                    }
                                    simInfo.queryMatchPosition = queryTermsSeen;
                                    int candidatePos = docEnum.nextPosition();
                                    simInfo.candidateMatchPosition = candidatePos
                                            + docEnum.freq();
                                    if (!Util.isSatisfyPosFilter(
                                            this.simMap.get(docId).similarity,
                                            this.querySize, queryTermsSeen,
                                            simInfo.candidateSize,
                                            simInfo.candidateMatchPosition,
                                            this.computedThreshold)) {
                                        // System.out.println("before removing in simmap "+
                                        // Util.debug_thread());
                                        this.simMap.remove(docId);
                                        // System.out.println("after removing in simmap "+
                                        // Util.debug_thread());
                                    }
                                }
                            } else {
                                logger.trace("docEnum is null, " + base
                                        + ", term: " + this.searchTerm
                                        + Util.debug_thread());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("exception caught " + e.getMessage()
                                    + Util.debug_thread() + " search term:"
                                    + this.searchTerm);
                        }
                    }
                } else {
                    logger.debug("leaves are null, " + this.searchTerm
                            + Util.debug_thread());
                }
            } else {
                logger.debug("getContext is null, " + this.searchTerm
                        + Util.debug_thread());
            }
        } else {
            logger.debug("this.reader is null, " + this.searchTerm
                    + Util.debug_thread());
        }
    }

    /**
     * @return the searchTerm
     */
    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * @param searchTerm
     *            the searchTerm to set
     */
    public void setSearchTerm(String searchTerm) {
        // System.out.println(Util.debug_thread() + "setting searchTerm: "+
        // searchTerm);
        this.searchTerm = searchTerm;
    }

    /**
     * @return the freqTerm
     */
    public int getFreqTerm() {
        return freqTerm;
    }

    /**
     * @param freqTerm
     *            the freqTerm to set
     */
    public void setFreqTerm(int freqTerm) {
        this.freqTerm = freqTerm;
    }

    /**
     * @return the reader
     */
    public IndexReader getReader() {
        return reader;
    }

    /**
     * @param reader
     *            the reader to set
     */
    public void setReader(IndexReader reader) {
        this.reader = reader;
    }

    /**
     * @return the simMap
     */
    public Map<Long, CandidateSimInfo> getSimMap() {
        return simMap;
    }

    /**
     * @param simMap
     *            the simMap to set
     */
    public void setSimMap(ConcurrentMap<Long, CandidateSimInfo> simMap) {
        this.simMap = simMap;
    }

    public void setQuerySize(int size) {
        this.querySize = size;

    }

    public void setComputedThreshold(int computedThreshold) {
        this.computedThreshold = computedThreshold;

    }

}
