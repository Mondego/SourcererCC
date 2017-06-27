package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.utility.Util;

public class CloneValidator implements IListener, Runnable {
    private CandidatePair candidatePair;
    private static final Logger logger = LogManager.getLogger(CloneValidator.class);

    public CloneValidator(CandidatePair candidatePair) {
        // TODO Auto-generated constructor stub
        this.candidatePair = candidatePair;
    }

    @Override
    public void run() {
        try {
            this.validate(this.candidatePair);
        } catch (NoSuchElementException e) {
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

    private void validate(CandidatePair candidatePair)
            throws InterruptedException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        /*
         * System.out.println(SearchManager.NODE_PREFIX + "validating, " +
         * candidatePair.candidateId + "query: " +
         * candidatePair.queryBlock.getFunctionId() + "," +
         * candidatePair.queryBlock.getId());
         */

        // long start_time = System.currentTimeMillis();
        long startTime = System.nanoTime();
        if (candidatePair.simInfo.doc.tokenFrequencies!= null && candidatePair.simInfo.doc.tokenFrequencies.size() > 0) {
            /*
             * if(candidatePair.queryBlock.getFunctionId()==1042 &&
             * candidatePair.queryBlock.getId()==494 &&
             * candidatePair.functionIdCandidate==1042 &&
             * candidatePair.candidateId==492){ logger.debug("qid: "+
             * candidatePair.queryBlock.getFunctionId()+","+candidatePair.
             * queryBlock.getId()); logger.debug("cid: "+
             * candidatePair.functionIdCandidate+","+candidatePair.candidateId);
             */
            int similarity = this.updateSimilarity(candidatePair.queryBlock,
                    candidatePair.computedThreshold, candidatePair.candidateSize, candidatePair.simInfo);
            if (similarity > 0) {
                ClonePair cp = new ClonePair(candidatePair.queryBlock.getFunctionId(), candidatePair.queryBlock.getId(),
                        candidatePair.functionIdCandidate, candidatePair.candidateId);
                long estimatedTime = System.nanoTime() - startTime;
                logger.debug(SearchManager.NODE_PREFIX + " CloneValidator, QueryBlock " + candidatePair + " in "
                        + estimatedTime / 1000 + " micros");
                SearchManager.reportCloneQueue.send(cp);
            }

            // }

            /*
             * candidatePair.queryBlock = null; candidatePair.simInfo = null;
             * candidatePair = null;
             */

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
        /*
         * logger.debug("qsize: "+ queryBlock.getSize());
         * logger.debug("csize: "+ candidateSize); logger.debug("qtseen: "+
         * simInfo.queryMatchPosition); logger.debug("ctseen: "+
         * simInfo.candidateMatchPosition); logger.debug("th: "+
         * computedThreshold); logger.debug("sim: "+ similarity);
         */
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
        // logger.debug("sim: "+ similarity + ", token:"+ token + ", qf:"+
        // tokenInfo.getFrequency()+", cf:"+candidatesTokenFreq);
        // System.out.println("similarity: "+ similarity);

        return similarity;
    }
}
