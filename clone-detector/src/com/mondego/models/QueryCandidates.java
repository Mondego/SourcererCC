package com.mondego.models;

public class QueryCandidates {
    public QueryBlock queryBlock;
    int candidateLowerIndex;
    int candidateUpperIndex;
    
    public QueryCandidates(QueryBlock queryBlock, int low, int high){
    	this.queryBlock=queryBlock;
    	this.candidateLowerIndex=low;
    	this.candidateUpperIndex=high;
    }

}
