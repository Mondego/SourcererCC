package com.mondego.models;


public class CandidateSimInfo {
    public int queryMatchPosition;
    public int candidateMatchPosition;
    public int actionTokenSimilarity;
    public int stopwordActionTokenSimilarity;
    public int totalActionTokenSimilarity;
    public int candidateSize;
    public Block doc;
	
    public CandidateSimInfo(){
	this.queryMatchPosition=0;
	this.candidateMatchPosition=0;
	this.actionTokenSimilarity=0;
	this.candidateSize=0;
	
    }
}
