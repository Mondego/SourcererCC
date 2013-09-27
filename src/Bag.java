import java.util.HashMap;

/**
 * 
 */

/**
 * @author vaibhavsaini
 *
 */
public class Bag extends HashMap<Token,Integer> {
    private int id;

    /**
     * @param id
     */
    public Bag(int id) {
        super();
        this.id = id;
    }
    public String toString(){
        return this.id +"";
    }
}
