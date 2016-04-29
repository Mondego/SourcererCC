package models;

import indexbased.SearchManager;

import java.util.ArrayList;
import java.util.List;

import utility.BlockInfo;

public class CharacterShard {
    int id;
    public long minChar;
    public long maxChar;
    List<TokenShard> tokenShards;
   
    public CharacterShard(int id, int minBagSizeToSearch, int maxBagSizeToSearch){
        this.id = id;
        this.minChar = BlockInfo.getMinimumSimilarityThreshold(minBagSizeToSearch, SearchManager.th-150);
        this.maxChar = BlockInfo.getMaximumSimilarityThreshold(maxBagSizeToSearch, SearchManager.th-150);
        this.tokenShards = new ArrayList<TokenShard>();
    }
    public CharacterShard(){
        this.tokenShards = new ArrayList<TokenShard>();
    }
    
    public List<TokenShard> getTokenShardsForTokenLength(long size){
        List<TokenShard> shards = new ArrayList<TokenShard>();
        for (TokenShard shard : this.tokenShards){
            if(shard.minTokens<=size && shard.maxTokens>=size){
                shards.add(shard);
            }
        }
        return shards;
    }
}
