package com.mondego.models;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.mondego.utility.Util;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class Bag extends LinkedHashSet<TokenFrequency>  { // TODO: why is this
                                                         // not a
                                                         // linkedhashmap?
    /**
     * 
     */
    private static final long serialVersionUID = 1721183896451527542L;
    private long id;
    private int size;
    private int comparisions;
    private long functionId;
    private int numUniqueTokens;
    public Map<String,Long> metrics;
    
    public int getComparisions() {
        return comparisions;
    }

    public void setComparisions(int comparisions) {
        this.comparisions = comparisions;
    }

    /**
     * @param bagId
     */
    public Bag(long bagId) {
        super();
        this.id = bagId;
        this.size = 0;
        this.comparisions = 0;
        this.functionId = -1;
        this.numUniqueTokens=0;
        this.metrics = new HashMap<String,Long>();
        
    }

    public Bag() {
        super();
    }

    public long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(long functionId) {
        this.functionId = functionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bag other = (Bag) obj;
        if (id != other.id)
            return false;
        return true;
    }

    /**
     * @return the id
     */
    public long getId() {
        return this.id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getFunctionId()+":"+this.getId()+":"+ this.getSize()+":"+ this.getNumUniqueTokens();
    }

    public TokenFrequency get(TokenFrequency tokenFrequency) {
        this.comparisions = 0;
        for (TokenFrequency tf : this) {
            this.comparisions += 1;
            if (tf.equals(tokenFrequency)) {
                return tf;
            }
        }
        return null;
    }

    public int getSize() {
        if (this.size == 0) {
            for (TokenFrequency tf : this) {
                this.size += tf.getFrequency();
            }
        }
        return this.size;
    }
    
    public void setSize(int size){
        this.size = size;
    }

    /**
     * @return the numUniqueTokens
     */
    public int getNumUniqueTokens() {
        if (this.numUniqueTokens==0){
            this.numUniqueTokens=this.size();
        }
        return numUniqueTokens;
    }

    /**
     * @param numUniqueTokens the numUniqueTokens to set
     */
    public void setNumUniqueTokens(int numUniqueTokens) {
        this.numUniqueTokens = numUniqueTokens;
    }
    
    public String serialize(){
        StringBuilder bagString = new StringBuilder();
        StringBuilder metaData = new StringBuilder();
        StringBuilder data = new StringBuilder();
        String sep = "";
        for (TokenFrequency tf : this){
            data.append(sep).append(tf.getToken().getValue()).append("@@::@@").append(tf.getFrequency());
            sep=",";
        }
        sep =",";
        metaData.append(this.functionId).append(sep).append(this.id);
        for (int index = 0; index < Util.METRICS_ORDER_IN_INPUT_FILE.size(); index++) {
            metaData.append(sep).append(this.metrics.get(Util.METRICS_ORDER_IN_INPUT_FILE.get(index)));
        }
        bagString.append(metaData.toString()).append("@#@").append(data.toString());
        return bagString.toString();
    }
    
}
