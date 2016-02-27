package models;

public class CandidatePair {
	QueryBlock queryBlock;
	String candidateTokens;
	CandidateSimInfo simInfo;
	int computedThreshold;
	int candidateSize;
	long candidateId;
	
	public CandidatePair(QueryBlock queryBlock, String candidateTokens,
			CandidateSimInfo simInfo, int computedThreshold, int candidateSize, long candidateId) {
		super();
		this.queryBlock = queryBlock;
		this.candidateTokens = candidateTokens;
		this.simInfo = simInfo;
		this.computedThreshold = computedThreshold;
		this.candidateSize = candidateSize;
		this.candidateId = candidateId;
	}
}
