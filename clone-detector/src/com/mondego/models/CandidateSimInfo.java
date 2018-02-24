package com.mondego.models;


public class CandidateSimInfo {
    public int queryMatchPosition;
    public int candidateMatchPosition;
    public int actionTokenSimilarity;
    public int stopwordActionTokenSimilarity;
    public int methodNameActionTokenSimilarity;
    public int totalActionTokenSimilarity;
    public int candidateSize;
    public Block doc;
	
    public CandidateSimInfo(){
	this.queryMatchPosition=0;
	this.candidateMatchPosition=0;
	this.actionTokenSimilarity=0;
	this.candidateSize=0;
	
    }

    @Override
    public String toString() {
        return "CandidateSimInfo [queryMatchPosition=" + queryMatchPosition + ", candidateMatchPosition="
                + candidateMatchPosition + ", actionTokenSimilarity=" + actionTokenSimilarity
                + ", stopwordActionTokenSimilarity=" + stopwordActionTokenSimilarity
                + ", methodNameActionTokenSimilarity=" + methodNameActionTokenSimilarity
                + ", totalActionTokenSimilarity=" + totalActionTokenSimilarity + ", candidateSize=" + candidateSize
                + ", doc=" + doc + "]";
    }
}
