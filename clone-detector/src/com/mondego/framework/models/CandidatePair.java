package com.mondego.framework.models;

public class CandidatePair {
    public QueryBlock queryBlock;
    public CandidateSimInfo simInfo;
    public int computedThreshold;
    public int candidateSize;
    public long candidateId;
    public long functionIdCandidate;
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
