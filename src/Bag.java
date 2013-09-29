import java.util.HashSet;
import java.util.Set;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class Bag extends HashSet<TokenFrequency> {
    private int id;

    /**
     * @param id
     */
    public Bag(int id) {
        super();
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
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
            returnString += tokenFrequency.getToken().toString()
                    + System.lineSeparator();
        }
        return "Bag [id=" + id + "]" + System.lineSeparator() + returnString;
    }
    
    public TokenFrequency get(TokenFrequency tokenFrequency){
        for (TokenFrequency tf : this){
            if(tf.equals(tokenFrequency)){
                return tf;
            }
        }
        return null;
    }
}
