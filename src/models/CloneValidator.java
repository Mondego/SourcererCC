package models;

import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import utility.Util;
import indexbased.SearchManager;

public class CloneValidator implements IListener, Runnable {

    @Override
    public void run() {
        try {
            CandidatePair candidatePair = SearchManager.verifyCandidateQueue
                    .remove();
            this.validate(candidatePair);
        } catch (NoSuchElementException e) {
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void validate(CandidatePair candidatePair)
            throws InterruptedException {
        /*
         * System.out.println(SearchManager.NODE_PREFIX + "validating, " +
         * candidatePair.candidateId + "query: " +
         * candidatePair.queryBlock.getFunctionId() + "," +
         * candidatePair.queryBlock.getId());
         */

        long start_time = System.currentTimeMillis();
        if (candidatePair.candidateTokens != null
                && candidatePair.candidateTokens.trim().length() > 0) {
            int similarity = this.updateSimilarity(candidatePair.queryBlock,
                    candidatePair.candidateTokens,
                    candidatePair.computedThreshold,
                    candidatePair.candidateSize, candidatePair.simInfo);
            if (similarity > 0) {
                ClonePair cp = new ClonePair(candidatePair.queryBlock.getId(),
                        candidatePair.candidateId);
                SearchManager.reportCloneQueue.put(cp);
            }
            
            long end_time = System.currentTimeMillis();
            Duration duration;
            try {
                duration = DatatypeFactory.newInstance().newDuration(
                        end_time - start_time);
                System.out.printf(SearchManager.NODE_PREFIX + ", validated: "
                        + candidatePair.candidateId + "query: "
                        + candidatePair.queryBlock.getFunctionId() + ","
                        + candidatePair.queryBlock.getId()
                        + " time taken: %02dh:%02dm:%02ds", duration.getDays()
                        * 24 + duration.getHours(), duration.getMinutes(),
                        duration.getSeconds());
                start_time = end_time;
                System.out.println();
            } catch (DatatypeConfigurationException e) {
                e.printStackTrace();
            }
            
    /*        candidatePair.queryBlock = null;
            candidatePair.simInfo = null;
            candidatePair = null;*/

            

        } else {
            System.out.println("tokens not found for document");
        }
    }

    private int updateSimilarity(QueryBlock queryBlock, String tokens,
            int computedThreshold, int candidateSize, CandidateSimInfo simInfo) {
        int tokensSeenInCandidate = 0;
        int similarity = simInfo.similarity;
        Scanner scanner = new Scanner(tokens);
        try {
            scanner.useDelimiter("::");
            String tokenfreqFrame = null;
            String[] tokenFreqInfo;
            TokenInfo tokenInfo = null;
            boolean matchFound = false;
            int candidatesTokenFreq = -1;
            while (scanner.hasNext()) {
                tokenfreqFrame = scanner.next();
                tokenFreqInfo = tokenfreqFrame.split(":");
                if (Util.isSatisfyPosFilter(similarity, queryBlock.getSize(),
                        simInfo.queryMatchPosition, candidateSize,
                        simInfo.candidateMatchPosition, computedThreshold)) {
                    // System.out.println("sim: "+ similarity);
                    candidatesTokenFreq = Integer.parseInt(tokenFreqInfo[1]);
                    tokensSeenInCandidate += candidatesTokenFreq;
                    if (tokensSeenInCandidate > simInfo.candidateMatchPosition) {
                        matchFound = false;
                        if (simInfo.queryMatchPosition < queryBlock
                                .getPrefixMapSize()) {
                            // check in prefix
                            if (queryBlock.getPrefixMap().containsKey(
                                    tokenFreqInfo[0])) {
                                matchFound = true;
                                tokenInfo = queryBlock.getPrefixMap().get(
                                        tokenFreqInfo[0]);
                                similarity = updateSimilarityHelper(simInfo,
                                        tokenInfo, similarity,
                                        candidatesTokenFreq);
                            }
                        }
                        // check in suffix
                        if (!matchFound
                                && queryBlock.getSuffixMap().containsKey(
                                        tokenFreqInfo[0])) {
                            tokenInfo = queryBlock.getSuffixMap().get(
                                    tokenFreqInfo[0]);
                            similarity = updateSimilarityHelper(simInfo,
                                    tokenInfo, similarity, candidatesTokenFreq);
                        }
                        if (similarity >= computedThreshold) {
                            return similarity;
                        }
                    }
                } else {
                    break;
                }
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("possible error in the format. tokens: "
                    + tokens);
        } catch (NumberFormatException e) {
            System.out.println("possible error in the format. tokens: "
                    + tokens);
        } finally {
            scanner.close();
        }
        return -1;
    }

    private int updateSimilarityHelper(CandidateSimInfo simInfo,
            TokenInfo tokenInfo, int similarity, int candidatesTokenFreq) {
        simInfo.queryMatchPosition = tokenInfo.getPosition();
        similarity += Math.min(tokenInfo.getFrequency(), candidatesTokenFreq);
        // System.out.println("similarity: "+ similarity);
        return similarity;
    }
}
