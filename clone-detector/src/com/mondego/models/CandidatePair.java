package com.mondego.models;


public class CandidatePair {
    QueryBlock queryBlock;
    String candidateTokens;
    CandidateSimInfo simInfo;
    int computedThreshold;
    int candidateSize;
    long candidateId;
    long functionIdCandidate;
    public CandidatePair(QueryBlock queryBlock, String candidateTokens,
            CandidateSimInfo simInfo, int computedThreshold, int candidateSize,
            long functionIdCandidate, long candidateId) {
        super();
        this.queryBlock = queryBlock;
        this.candidateTokens = candidateTokens;
        this.simInfo = simInfo;
        this.computedThreshold = computedThreshold;
        this.candidateSize = candidateSize;
        this.functionIdCandidate = functionIdCandidate;
        this.candidateId = candidateId;
    }
    @Override
    public String toString() {
        return "QueryBlock["+this.queryBlock.toString()+"], Candidate["+ this.functionIdCandidate+":"+this.candidateId+":"+ this.candidateSize+"]"; 
    }
}
