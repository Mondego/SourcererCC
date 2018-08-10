package com.mondego.models;

public class ProgressMonitor {
    private  static ProgressMonitor instance;
    public  Shard currentShard;
    public  int numCandidatesIndexed;
    public  int iterationCount;
    public  int queriesProcessed;
    
    private ProgressMonitor(){
        
    }
    public static synchronized ProgressMonitor getInstance(){
        if (null==instance){
            instance = new ProgressMonitor();
        }
        return instance;
    }
    @Override
    public String toString() {
        return "ProgressMonitor [currentShard=" + currentShard + ", numCandidatesIndexed=" + numCandidatesIndexed
                + ", iterationCount=" + iterationCount + ", queriesProcessed=" + queriesProcessed + "]";
    }
}
