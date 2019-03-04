package com.mondego.models;

import java.util.LinkedHashSet;

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
        return this.getFunctionId()+":"+this.getId()+":"+ this.getSize();
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
}
