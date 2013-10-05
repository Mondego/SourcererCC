import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class Bag extends ArrayList<TokenFrequency> {
    /**
     * 
     */
    private static final long serialVersionUID = 1721183896451527542L;
    public int id;
    public int size;

    /**
     * @param id
     */
    public Bag(int id) {
        super();
        this.id = id;
        this.size =0;
    }
    
    public Bag(){
        super();
    }

    /**
     * @return the id
     */
    public int getId() {
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
        String returnString = "";
        for (TokenFrequency tokenFrequency : this) {
            returnString += tokenFrequency.getToken().toString() + "@@::@@"
                    + tokenFrequency.getFrequency() + ",";
        }
        return this.id+ "@#@"+ returnString.substring(0,returnString.length()-1) + System.lineSeparator();
    }

    
    public TokenFrequency get(TokenFrequency tokenFrequency) {
        for (TokenFrequency tf : this) {
            if (tf.equals(tokenFrequency)) {
                return tf;
            }
        }
        return null;
    }
    

    public int getSize() {
        if(this.size == 0){
        	for (TokenFrequency tf : this) {
                this.size += tf.getFrequency();
            }
        }
        return this.size;
    }
}
