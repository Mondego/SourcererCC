package com.mondego.models;

import java.util.HashMap;
import java.util.Map;

public class QueryCandidates {
	Map<Long, CandidateSimInfo> simMap;
	public Block queryBlock;
	public QueryCandidates(Block block) {
	    this.queryBlock = block;
        this.simMap = new HashMap<Long, CandidateSimInfo>(); 
    }
}
