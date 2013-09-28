import java.util.HashMap;
import java.util.Set;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class Bag extends HashMap<Token, Integer> {
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
        Set<Token> tokens = this.keySet();
        for (Token token : tokens) {
            returnString += token.toString() + System.lineSeparator();
        }
        return "Bag [id=" + id + "]" + System.lineSeparator() + returnString;
    }
}
