package models;

import java.util.NoSuchElementException;

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
            candidatePair.queryBlock = null;
            candidatePair.simInfo = null;
            candidatePair = null;
        } else {
            System.out.println("tokens not found for document");
        }
    }

    private int updateSimilarity(QueryBlock queryBlock, String tokens,
            int computedThreshold, int candidateSize, CandidateSimInfo simInfo) {
        int tokensSeenInCandidate = 0;
        int similarity = simInfo.similarity;
        try {
            for (String tokenfreqFrame : tokens.split("::")) {
                String[] tokenFreqInfo = tokenfreqFrame.split(":");
                if (Util.isSatisfyPosFilter(similarity, queryBlock.getSize(),
                        simInfo.queryMatchPosition, candidateSize,
                        simInfo.candidateMatchPosition, computedThreshold)) {
                    // System.out.println("sim: "+ similarity);
                    int candidatesTokenFreq = Integer
                            .parseInt(tokenFreqInfo[1]);
                    tokensSeenInCandidate += candidatesTokenFreq;
                    if (tokensSeenInCandidate > simInfo.candidateMatchPosition) {
                        TokenInfo tokenInfo = null;
                        boolean matchFound = false;
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
