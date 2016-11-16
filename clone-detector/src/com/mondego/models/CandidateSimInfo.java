package com.mondego.models;

import org.apache.lucene.document.Document;

public class CandidateSimInfo {
    public int queryMatchPosition;
    public int candidateMatchPosition;
    public int similarity;
    public int candidateSize;
    public Document doc;
	
    public CandidateSimInfo(){
	this.queryMatchPosition=0;
	this.candidateMatchPosition=0;
	this.similarity=0;
	this.candidateSize=0;
	
    }
}
