package com.mondego.application.workers;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import com.mondego.application.models.CandidatePair;
import com.mondego.application.models.CandidateSimInfo;
import com.mondego.application.models.QueryBlock;
import com.mondego.application.models.QueryCandidates;
import com.mondego.framework.controllers.MainController;
import com.mondego.framework.workers.Worker;

public class CandidateProcessorWorker extends Worker<QueryCandidates> {

    public CandidateProcessorWorker(QueryCandidates t) {
        super(t);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void process() {
        Map<Long, CandidateSimInfo> simMap = this.dataObject.simMap;
        QueryBlock queryBlock = this.dataObject.queryBlock;
        logger.debug(
                MainController.NODE_PREFIX + ", num candidates: " + simMap.entrySet().size() + ", query: " + queryBlock);
        for (Entry<Long, CandidateSimInfo> entry : simMap.entrySet()) {
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
            try {
                this.pipe.getChannel("VALIDATE_CANDIDATE").send(candidatePair);
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
            entry = null;
        }        
    }

}
