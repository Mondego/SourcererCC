package com.mondego.models;

public class QueryCandidates {
    public Block queryBlock;
    int candidateLowerIndex;
    int candidateUpperIndex;
    
    public QueryCandidates(Block queryBlock, int low, int high){
    	this.queryBlock=queryBlock;
    	this.candidateLowerIndex=low;
    	this.candidateUpperIndex=high;
    }

}
