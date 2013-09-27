/**
 * 
 */

/**
 * @author vaibhavsaini
 *
 */
public class Token {
    private int id; // unique id of this token
    private String value; // this is the face value of a token, e.g. 'for'
    private String type; // e.g. 'part_of_mehtod_name', 'part_of_method_body', 'variable', 'constant' etc.
    private String member_of; // 'class' or 'method'. 
}
