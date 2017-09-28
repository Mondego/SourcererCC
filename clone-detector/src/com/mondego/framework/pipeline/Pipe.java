package com.mondego.framework.pipeline;

import java.util.HashMap;
import java.util.Map;

import com.mondego.framework.workers.Worker;

public class Pipe {
    int size;
    private static Pipe instance;
    public Map<String,IChannle> channles;
    private Pipe(){
        this.channles = new HashMap<>();
    }
    
    public static synchronized Pipe getInstance(){
        if (null==instance){
            instance = new Pipe();
        }
        return instance;
    }
    public <U,T extends Worker<U>> void registerChannel(String key, ThreadedChannel<U,T> channel){
        this.channles.put(key,channel);
        this.size+=1;
    }
    
    public <U,T extends Worker<U>> void deregisterChannel(String key){
        if(null!=this.channles.remove(key)){
            this.size-=1;
        }
    }
    public IChannle getChannel(String key){
        return this.channles.get(key);
    }
    
}
