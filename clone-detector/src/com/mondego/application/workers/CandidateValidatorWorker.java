package com.mondego.application.workers;

import java.lang.reflect.InvocationTargetException;

import com.mondego.application.models.CandidatePair;
import com.mondego.application.models.CandidateSimInfo;
import com.mondego.application.models.ClonePair;
import com.mondego.application.models.QueryBlock;
import com.mondego.application.models.TokenFrequency;
import com.mondego.application.models.TokenInfo;
import com.mondego.framework.controllers.MainController;
import com.mondego.framework.workers.Worker;
import com.mondego.utility.Util;

public class CandidateValidatorWorker extends Worker<CandidatePair> {

    public CandidateValidatorWorker(CandidatePair t) {
        super(t);
    }

    @Override
    public void process() {
        long startTime = System.nanoTime();
        if (this.dataObject.simInfo.doc.tokenFrequencies!= null && this.dataObject.simInfo.doc.tokenFrequencies.size() > 0) {
            int similarity = this.updateSimilarity(this.dataObject.queryBlock,
                    this.dataObject.computedThreshold, this.dataObject.candidateSize, this.dataObject.simInfo);
            if (similarity > 0) {
                ClonePair cp = new ClonePair(this.dataObject.queryBlock.getFunctionId(), this.dataObject.queryBlock.getId(),
                        this.dataObject.functionIdCandidate, this.dataObject.candidateId);
                long estimatedTime = System.nanoTime() - startTime;
                logger.debug(MainController.NODE_PREFIX + " CloneValidator, QueryBlock " + this.dataObject + " in "
                        + estimatedTime / 1000 + " micros");
                try {
                    this.pipe.getChannel("REPORT_CLONE").send(cp);
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
        } else {
            logger.debug("tokens not found for document");
        }
    }

    private int updateSimilarity(QueryBlock queryBlock,
            int computedThreshold, int candidateSize, CandidateSimInfo simInfo) {
        int tokensSeenInCandidate = 0;
        int similarity = simInfo.similarity;
        TokenInfo tokenInfo = null;
        boolean matchFound = false;
        for (TokenFrequency tf : simInfo.doc.tokenFrequencies) {
            if (Util.isSatisfyPosFilter(similarity, queryBlock.getSize(), simInfo.queryMatchPosition, candidateSize,
                    simInfo.candidateMatchPosition, computedThreshold)) {
                // System.out.println("sim: "+ similarity);
                tokensSeenInCandidate += tf.getFrequency();
                // logger.debug("cttseen: "+ tokensSeenInCandidate);
                if (tokensSeenInCandidate > simInfo.candidateMatchPosition) {
                    simInfo.candidateMatchPosition = tokensSeenInCandidate;
                    matchFound = false;
                    if (simInfo.queryMatchPosition < queryBlock.getPrefixMapSize()) {
                        // check in prefix
                        if (queryBlock.getPrefixMap().containsKey(tf.getToken().getValue())) {
                            matchFound = true;
                            tokenInfo = queryBlock.getPrefixMap().get(tf.getToken().getValue());
                            similarity = updateSimilarityHelper(simInfo, tokenInfo, similarity, tf.getFrequency(),
                                    tf.getToken().getValue());
                        }
                    }
                    // check in suffix
                    if (!matchFound && queryBlock.getSuffixMap().containsKey(tf.getToken().getValue())) {
                        tokenInfo = queryBlock.getSuffixMap().get(tf.getToken().getValue());
                        similarity = updateSimilarityHelper(simInfo, tokenInfo, similarity, tf.getFrequency(),
                                tf.getToken().getValue());
                    }
                    if (similarity >= computedThreshold) {
                        return similarity;
                    }
                }
            } else {
                break;
            }
        }
        return -1;
    }

    private int updateSimilarityHelper(CandidateSimInfo simInfo, TokenInfo tokenInfo, int similarity,
            int candidatesTokenFreq, String token) {
        simInfo.queryMatchPosition = tokenInfo.getPosition();
        similarity += Math.min(tokenInfo.getFrequency(), candidatesTokenFreq);
        return similarity;
    }

}
