package com.mondego.models;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.utility.Util;

public class CandidateSearcher implements IListener, Runnable {
    private QueryBlock queryBlock;
    private static final Logger logger = LogManager.getLogger(CandidateSearcher.class);

    public CandidateSearcher(QueryBlock queryBlock) {
        // TODO Auto-generated constructor stub
        this.queryBlock = queryBlock;
    }

    @Override
    public void run() {
        try {
            this.searchCandidates(queryBlock);

        } catch (NoSuchElementException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("EXCEPTION CAUGHT::", e);
        }
    }

    private void searchCandidates(QueryBlock queryBlock)
            throws IOException, InterruptedException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        long startTime = System.nanoTime();
        QueryCandidates qc = new QueryCandidates();
        qc.simMap = this.search(queryBlock);
        if (qc.simMap.size() > 0) {
            qc.queryBlock = queryBlock;
            long estimatedTime = System.nanoTime() - startTime;
            logger.debug(SearchManager.NODE_PREFIX + " CandidateSearcher, QueryBlock " + queryBlock + " in "
                    + estimatedTime / 1000 + " micros");
            SearchManager.queryCandidatesQueue.send(qc);
        }
    }

    private Map<Long, CandidateSimInfo> search(QueryBlock queryBlock) {
        Map<Long, CandidateSimInfo> simMap = new HashMap<Long, CandidateSimInfo>();
        Set<Long> earlierDocs = new HashSet<Long>();
        int termsSeenInQuery = 0;
        for (Entry<String, TokenInfo> entry : queryBlock.getPrefixMap().entrySet()) {
            String searchTerm = entry.getKey();
            int searchTermFreq = entry.getValue().getFrequency();
            termsSeenInQuery += searchTermFreq;
            Set<Long> docIds = SearchManager.invertedIndex.get(searchTerm);
            if (null != docIds) {
                for (Long docId : docIds) {
                    CandidateSimInfo simInfo = null;
                    DocumentForInvertedIndex doc = SearchManager.documentsForII.get(docId);
                    if (simMap.containsKey(docId)) {
                        simInfo = simMap.get(docId);
                        simInfo.similarity = simInfo.similarity
                                + Math.min(searchTermFreq, doc.termInfoMap.get(searchTerm).frequency);
                    } else {
                        if (earlierDocs.contains(docId)) {
                            continue;
                        }
                        if (doc.fId >= queryBlock.getId()) {
                            earlierDocs.add(doc.id);
                            continue; // we reject the candidate
                        }
                        simInfo = new CandidateSimInfo();
                        simInfo.doc = doc;
                        simInfo.candidateSize = doc.size;
                        simInfo.similarity = Math.min(searchTermFreq, doc.termInfoMap.get(searchTerm).frequency);
                        // System.out.println("before putting in simmap "+
                        // Util.debug_thread());
                        simMap.put(doc.id, simInfo);
                    }
                    simInfo.queryMatchPosition = termsSeenInQuery;
                    int candidatePos = doc.termInfoMap.get(searchTerm).position;
                    simInfo.candidateMatchPosition = candidatePos + doc.termInfoMap.get(searchTerm).frequency;
                    if (!Util.isSatisfyPosFilter(simMap.get(doc.id).similarity, queryBlock.getSize(), termsSeenInQuery,
                            simInfo.candidateSize, simInfo.candidateMatchPosition, queryBlock.getComputedThreshold())) {
                        simMap.remove(doc.id);
                    }
                }
            } else {
                //logger.debug("no docs found for searchTerm: " + searchTerm);
            }
        }
        return simMap;
    }

}
