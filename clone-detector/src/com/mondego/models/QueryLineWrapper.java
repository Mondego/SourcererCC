package com.mondego.models;

public class QueryLineWrapper {
    String line;
    Shard shard;
    
    /**
     * @param line
     * @param shard
     */
    public QueryLineWrapper(String line, Shard shard) {
        super();
        this.line = line;
        this.shard = shard;
    }
    
}
