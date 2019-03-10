package com.mondego.models;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

import com.mondego.indexbased.CustomCollectorFwdIndex;
import com.mondego.indexbased.SearchManager;
import com.mondego.indexbased.TermSearcher;

public class CandidateProcessor implements IListener, Runnable {
    private QueryCandidates qc;
    private static final Logger logger = LogManager.getLogger(CandidateProcessor.class);
    public CandidateProcessor(QueryCandidates qc) {
        this.qc = qc;
    }

    @Override
    public void run() {
        try {
            this.processResultWithFilter();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void processResultWithFilter() throws InterruptedException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        TermSearcher result = this.qc.termSearcher;
        QueryBlock queryBlock = this.qc.queryBlock;
        int shard = queryBlock.getShardId();
        Map<Long, CandidateSimInfo> codeBlockIds = result.getSimMap();
        if (SearchManager.isGenCandidateStats) {
            SearchManager.updateNumCandidates(codeBlockIds.size());
        }
        logger.debug(SearchManager.NODE_PREFIX + ", num candidates: " + codeBlockIds.entrySet().size() + ", query: " + queryBlock.getFunctionId() + "," + queryBlock.getId());
        for (Entry<Long, CandidateSimInfo> entry : codeBlockIds.entrySet()) {
            Document doc = null;
            try {
                doc = SearchManager.searcher.get(shard).getDocument(entry.getKey());
                CandidateSimInfo simInfo = entry.getValue();
                long candidateId = Long.parseLong(doc.get("id"));
                long functionIdCandidate = Long.parseLong(doc.get("functionId"));
                int newCt = -1;
                int candidateSize = Integer.parseInt(doc.get("size"));
                if (candidateSize < queryBlock.getComputedThreshold() || candidateSize > queryBlock.getMaxCandidateSize()) {
                    continue; // ignore this candidate
                }
                if (candidateSize > queryBlock.getSize()) {
                    newCt = Integer.parseInt(doc.get("ct"));
                }
                CustomCollectorFwdIndex collector = SearchManager.fwdSearcher.get(shard).search(doc);
                List<Integer> blocks = collector.getBlocks();
                if (!blocks.isEmpty()) {
                    if (blocks.size() == 1) {
                        Document document = SearchManager.fwdSearcher.get(shard).getDocument(blocks.get(0));
                        String tokens = document.get("tokens");
                        int computedThreshold = newCt;
                        if (newCt == -1) {
                            computedThreshold = queryBlock.getComputedThreshold();
                        }
                        CandidatePair candidatePair = new CandidatePair(queryBlock, tokens, simInfo, computedThreshold, candidateSize, functionIdCandidate, candidateId);
                        SearchManager.verifyCandidateQueue.send(candidatePair);
                        entry = null;
                    } else {
                        logger.error(SearchManager.NODE_PREFIX + "ERROR: more than one doc found. some error here," + doc.get("functionId") + ", " + doc.get("id"));
                    }
                } else {
                    logger.error(SearchManager.NODE_PREFIX + ", document not found in fwd index," + doc.get("functionId") + ", " + doc.get("id"));
                }
            } catch (NumberFormatException e) {
                logger.error(SearchManager.NODE_PREFIX + e.getMessage() + ", cant parse id for " + doc.get("functionId") + ", " + doc.get("id"));
            } catch (IOException e) {
                logger.error(e.getMessage() + ", can't find document from searcher" + entry.getKey());
            }
        }
    }
}
