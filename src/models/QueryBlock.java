/**
 * 
 */
package models;

import java.util.LinkedHashMap;


/**
 * @author vaibhavsaini
 *
 */
public class QueryBlock extends LinkedHashMap<String, Integer> {
    private long id;
    private int size;

    /**
     * @param id
     */
    public QueryBlock(long id) {
        super();
        this.id = id;
        this.size=0;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }
    
    public int getSize() {
        if(this.size == 0){
            for (Integer freq : this.values()) {
                this.size += freq;
            }
        }
        return this.size;
    }
}
