package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;

public class CandidateProcessor implements IListener, Runnable {
    private QueryCandidates qc;
    private static final Logger logger = LogManager.getLogger(CandidateProcessor.class);

    public CandidateProcessor(QueryCandidates qc) {
        // TODO Auto-generated constructor stub
        this.qc = qc;
    }

    @Override
    public void run() {
        try {
            // System.out.println( "QCQ size: "+
            // SearchManager.queryCandidatesQueue.size() + Util.debug_thread());
            this.processResultWithFilter();
        } catch (NoSuchElementException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
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
            e.printStackTrace();
        }
    }

    private void processResultWithFilter() throws InterruptedException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // System.out.println("HERE, thread_id: " + Util.debug_thread() +
        // ", query_id "+ queryBlock.getId());
        Map<Long, CandidateSimInfo> simMap = this.qc.simMap;
        QueryBlock queryBlock = this.qc.queryBlock;
        long sstart_time = System.currentTimeMillis();
        logger.debug(
                SearchManager.NODE_PREFIX + ", num candidates: " + simMap.entrySet().size() + ", query: " + queryBlock);
        for (Entry<Long, CandidateSimInfo> entry : simMap.entrySet()) {
            long startTime = System.nanoTime();
            CandidateSimInfo simInfo = entry.getValue();
            long candidateId = simInfo.doc.fId;
            long functionIdCandidate = simInfo.doc.pId;
            int newCt = -1;
            int candidateSize = simInfo.doc.size;
            if (candidateSize < queryBlock.getComputedThreshold() || candidateSize > queryBlock.getMaxCandidateSize()) {
                continue; // ignore this candidate
            }
            if (candidateSize > queryBlock.getSize()) {
                newCt = simInfo.doc.ct;
            }
            CandidatePair candidatePair = null;
            if (newCt != -1) {
                candidatePair = new CandidatePair(queryBlock, simInfo, newCt, candidateSize,
                        functionIdCandidate, candidateId);
            } else {
                candidatePair = new CandidatePair(queryBlock, simInfo,
                        queryBlock.getComputedThreshold(), candidateSize, functionIdCandidate, candidateId);
            }
            long estimatedTime = System.nanoTime() - startTime;
            // System.out.println(SearchManager.NODE_PREFIX + "
            // CandidateProcessor, " + candidatePair + " in " +
            // estimatedTime/1000 + " micros");
            SearchManager.verifyCandidateQueue.send(candidatePair);
            entry = null;
        }
    }

}
