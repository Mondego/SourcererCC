package com.mondego.noindex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mondego.models.Bag;
import com.mondego.models.Token;
import com.mondego.models.TokenFrequency;
import com.mondego.utility.Util;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class CloneTestHelper {

    /**
     * returns a set of 10 bags
     * 
     * @return Set<Bag>
     */
    public static HashSet<Bag> getTestSet(int start, int stop) {
        HashSet<Bag> set = new HashSet<Bag>();
        for (int i = start; i < stop; i++) {
            set.add(getTestBag(i));
        }
        return set;
    }

    /**
     * 
     * @param i
     *            integer to create value of a token
     * @return Token
     */
    public static Token getTestToken() {
        return new Token("t" + Util.getRandomNumber(21, 1));
    }

    /**
     * creates and return a bag of 10 tokens
     * 
     * @param i
     *            id of the bag
     * @return Bag
     */
    public static Bag getTestBag(int i) {
        Bag bag = new Bag(i);
        for (int j = 0; j < 10; j++) {
            Token t = getTestToken();
            TokenFrequency tFrequency = new TokenFrequency();
            tFrequency.setToken(t);
            tFrequency.setFrequency(Util.getRandomNumber(1, 1));
            bag.add(tFrequency);
        }
        return bag;
    }

    public static Map<String, Integer> getGlobalTokenPositionMap(
            Set<Bag> setA, Set<Bag> setB) {
        Map<String, Integer> tokenPositionMap = new HashMap<String, Integer>();
        Map<TokenFrequency, TokenFrequency> map = new HashMap<TokenFrequency, TokenFrequency>();
        fetchTokenFrequencyList(map, setA);
        fetchTokenFrequencyList(map, setB);
        List<TokenFrequency> list = new ArrayList<TokenFrequency>( map.values());
        Collections.sort(list, new Comparator<TokenFrequency>() {
            public int compare(TokenFrequency tfFirst, TokenFrequency tfSecond) {
                return tfFirst.getFrequency() - tfSecond.getFrequency();
            }
        });
        int position = 0;
        for (TokenFrequency tokenFrequency : list) {
            tokenPositionMap.put(tokenFrequency.getToken().getValue(), position);
            position++;
        }
        return tokenPositionMap;
    }

    private static void fetchTokenFrequencyList(
            Map<TokenFrequency, TokenFrequency> map, Set<Bag> setA) {
        for (Bag bag : setA) {
            mergeCollections(map, bag);
        }
    }

    private static void mergeCollections(
            Map<TokenFrequency, TokenFrequency> map,
            Collection<TokenFrequency> listB) {
        for (TokenFrequency tf : listB) {
            if (map.containsKey(tf)) {
                TokenFrequency tokenFrequency = map.get(tf);
                tokenFrequency.setFrequency(tokenFrequency.getFrequency()
                        + tf.getFrequency());
            } else {
                TokenFrequency tokenFrequency = new TokenFrequency();
                Token token = new Token(tf.getToken().getValue());
                tokenFrequency.setToken(token);
                tokenFrequency.setFrequency(tf.getFrequency());
                map.put(tokenFrequency, tokenFrequency);
            }
        }
    }
}
