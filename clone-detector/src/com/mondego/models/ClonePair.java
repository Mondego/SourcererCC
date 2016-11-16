package com.mondego.models;

public class ClonePair {
    long qid;
    long cid;
    long q_pid; // parent id of query
    long c_pid; // parent id of candidate

    public ClonePair(long q_pid, long qid, long candidate_pid, long candidateId) {
        super();
        this.q_pid = q_pid;
        this.qid = qid;
        this.c_pid = candidate_pid;
        this.cid = candidateId;
    }

    @Override
    public String toString() {
        return q_pid+","+qid+","+c_pid+","+cid;
    }
}
