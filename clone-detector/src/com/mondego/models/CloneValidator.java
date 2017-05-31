package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;

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
        if (candidatePair.candidateTokenFrequencies != null && candidatePair.candidateTokenFrequencies.size() > 0) {
            int similarity = this.updateSimilarity(candidatePair.queryBlock, candidatePair.candidateTokenFrequencies,
                    candidatePair.computedThreshold, candidatePair.candidateSize, candidatePair.simInfo);
            if (similarity > 0) {
                com.mondego.models.ClonePair cp = new ClonePair(candidatePair.queryBlock.getFunctionId(),
                        candidatePair.queryBlock.getId(), candidatePair.functionIdCandidate, candidatePair.candidateId);

                /*
                 * long end_time = System.currentTimeMillis(); Duration
                 * duration; try { duration =
                 * DatatypeFactory.newInstance().newDuration( end_time -
                 * start_time); System.out.printf(SearchManager.NODE_PREFIX +
                 * ", validated: " + candidatePair.candidateId + "query: " +
                 * candidatePair.queryBlock.getFunctionId() + "," +
                 * candidatePair.queryBlock.getId() +
                 * " time taken: %02dh:%02dm:%02ds", duration.getDays() 24 +
                 * duration.getHours(), duration.getMinutes(),
                 * duration.getSeconds()); start_time = end_time;
                 * logger.debug(); } catch (DatatypeConfigurationException e) {
                 * e.printStackTrace(); }
                 */
                long estimatedTime = System.nanoTime() - startTime;
                logger.debug(SearchManager.NODE_PREFIX + " CloneValidator, QueryBlock " + candidatePair + " in "
                        + estimatedTime / 1000 + " micros");
                SearchManager.reportCloneQueue.send(cp);
            }
            /*
             * candidatePair.queryBlock = null; candidatePair.simInfo = null;
             * candidatePair = null;
             */

        } else {
            logger.debug("tokens not found for document");
        }
    }

    private int updateSimilarity(QueryBlock queryBlock, LinkedHashSet<TokenFrequency> candidateTokenFrequencies,
            int computedThreshold, int candidateSize, CandidateSimInfo simInfo) {
        int tokensSeenInCandidate = 0;
        int similarity = simInfo.similarity;
        TokenInfo tokenInfo = null;
        boolean matchFound = false;
        int candidatesTokenFreq = -1;
        for (TokenFrequency tf : candidateTokenFrequencies) {
            if (Util.isSatisfyPosFilter(similarity, queryBlock.getSize(), simInfo.queryMatchPosition, candidateSize,
                    simInfo.candidateMatchPosition, computedThreshold)) {
                // System.out.println("sim: "+ similarity);
                candidatesTokenFreq = tf.getFrequency();
                tokensSeenInCandidate += candidatesTokenFreq;
                if (tokensSeenInCandidate > simInfo.candidateMatchPosition) {
                    matchFound = false;
                    if (simInfo.queryMatchPosition < queryBlock.getPrefixMapSize()) {
                        // check in prefix
                        if (queryBlock.getPrefixMap().containsKey(tf.getToken().getValue())) {
                            matchFound = true;
                            tokenInfo = queryBlock.getPrefixMap().get(tf.getToken().getValue());
                            similarity = updateSimilarityHelper(simInfo, tokenInfo, similarity, candidatesTokenFreq);
                        }
                    }
                    // check in suffix
                    if (!matchFound && queryBlock.getSuffixMap().containsKey(tf.getToken().getValue())) {
                        tokenInfo = queryBlock.getSuffixMap().get(tf.getToken().getValue());
                        similarity = updateSimilarityHelper(simInfo, tokenInfo, similarity, candidatesTokenFreq);
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
            int candidatesTokenFreq) {
        simInfo.queryMatchPosition = tokenInfo.getPosition();
        similarity += Math.min(tokenInfo.getFrequency(), candidatesTokenFreq);
        // System.out.println("similarity: "+ similarity);
        return similarity;
    }
}
