package com.mondego.application.workers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mondego.application.handlers.SearchActionHandler;
import com.mondego.framework.controllers.MainController;
import com.mondego.framework.models.CandidateSimInfo;
import com.mondego.framework.models.DocumentForInvertedIndex;
import com.mondego.framework.models.QueryBlock;
import com.mondego.framework.models.QueryCandidates;
import com.mondego.framework.models.TokenInfo;
import com.mondego.framework.workers.Worker;
import com.mondego.utility.Util;

public class CandidateSearchWorker extends Worker<QueryBlock> {

    public CandidateSearchWorker(QueryBlock queryBlock) {
        super(queryBlock);
    }

    @Override
    public void process() {
        long startTime = System.nanoTime();
        QueryCandidates qc = new QueryCandidates();
        qc.simMap = this.search();
        if (qc.simMap.size() > 0) {
            qc.queryBlock = this.dataObject;
            long estimatedTime = System.nanoTime() - startTime;
            logger.debug(MainController.NODE_PREFIX + " CandidateSearcher, QueryBlock " + this.dataObject + " in "
                    + estimatedTime / 1000 + " micros");
            try {
                this.pipe.getChannel("PROCESS_CANDIDATES").send(qc);
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private Map<Long, CandidateSimInfo> search() {
        Map<Long, CandidateSimInfo> simMap = new HashMap<Long, CandidateSimInfo>();
        Set<Long> earlierDocs = new HashSet<Long>();
        int termsSeenInQuery = 0;
        for (Entry<String, TokenInfo> entry : this.dataObject.getPrefixMap().entrySet()) {
            String searchTerm = entry.getKey();
            int searchTermFreq = entry.getValue().getFrequency();
            termsSeenInQuery += searchTermFreq;
            Set<Long> docIds = SearchActionHandler.invertedIndex.get(searchTerm);
            if (null != docIds) {
                for (Long docId : docIds) {
                    CandidateSimInfo simInfo = null;
                    DocumentForInvertedIndex doc = SearchActionHandler.documentsForII.get(docId);
                    if (simMap.containsKey(docId)) {
                        simInfo = simMap.get(docId);
                        simInfo.similarity = simInfo.similarity
                                + Math.min(searchTermFreq, doc.termInfoMap.get(searchTerm).frequency);
                    } else {
                        if (earlierDocs.contains(docId)) {
                            continue;
                        }
                        if (doc.fId >= this.dataObject.getId()) {
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
                    if (!Util.isSatisfyPosFilter(simMap.get(doc.id).similarity, this.dataObject.getSize(), termsSeenInQuery,
                            simInfo.candidateSize, simInfo.candidateMatchPosition, this.dataObject.getComputedThreshold())) {
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
