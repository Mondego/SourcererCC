package com.mondego.framework.models;

import java.util.Set;

public class CandidatePair {
    QueryBlock queryBlock;
    CandidateSimInfo simInfo;
    int computedThreshold;
    int candidateSize;
    long candidateId;
    long functionIdCandidate;
    public CandidatePair(QueryBlock queryBlock,
            CandidateSimInfo simInfo, int computedThreshold, int candidateSize,
            long functionIdCandidate, long candidateId) {
        super();
        this.queryBlock = queryBlock;
        //this.candidateTokenFrequencies = candidateTokenFrequencies;
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
