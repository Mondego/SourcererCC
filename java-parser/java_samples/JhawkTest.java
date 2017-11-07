package com.mondego.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mondego.models.Bag;
import com.mondego.models.TokenFrequency;

public class JhawkTest {

    int test =0;
    public static void sortBag(final Bag bag) {
        final Map<String, Long> cache = new HashMap<String, Long>();
        List<TokenFrequency> bagAsList = new ArrayList<TokenFrequency>(bag);
        try {
            Collections.sort(bagAsList, new Comparator<TokenFrequency>() {
                public void test1(){
                    int i = 0;
                    int j=0;
                    if (j<1){
                        for(i=j;i<100;i++){
                            System.out.println(i+j);
                        }
                    }
                }
                
                public int compare(TokenFrequency tfFirst,
                        TokenFrequency tfSecond) {
                    Long frequency1 = 0l;
                    Long frequency2 = 0l;
                    String k1 = tfFirst.getToken().getValue();
                    String k2 = tfSecond.getToken().getValue();
                    if (cache.containsKey(k1)) {
                        frequency1 = cache.get(k1);
                        if (null == frequency1) {
                            cache.put(k1, frequency1);
                        }
                    } else {
                        cache.put(k1, frequency1);
                    }
                    if (cache.containsKey(k2)) {
                        frequency2 = cache.get(k2);
                        if (null == frequency2) {
                            cache.put(k2, frequency2);
                        }
                    } else {
                        cache.put(k2, frequency2);
                    }
                    if (null == frequency1 || null == frequency2) {
                    }
                    int result = frequency1.compareTo(frequency2);
                    if (result == 0) {
                        return k1.compareTo(k2);
                    } else {
                        return result;
                    }
                }
            });
            bag.clear();
            for (TokenFrequency tf : bagAsList) {
                bag.add(tf);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public int compare2(TokenFrequency tfFirst, TokenFrequency tfSecond) {
        final Map<String, Long> cache = new HashMap<String, Long>();
        Long frequency1 = 0l;
        Long frequency2 = 0l;
        String k1 = tfFirst.getToken().getValue();
        String k2 = tfSecond.getToken().getValue();
        if (cache.containsKey(k1)) {
            frequency1 = cache.get(k1);
            if (null == frequency1) {
                cache.put(k1, frequency1);
            }
        } else {
            cache.put(k1, frequency1);
        }
        if (cache.containsKey(k2)) {
            frequency2 = cache.get(k2);
            if (null == frequency2) {
                cache.put(k2, frequency2);
            }
        } else {
            cache.put(k2, frequency2);
        }
        if (null == frequency1 || null == frequency2) {
        }
        int result = frequency1.compareTo(frequency2);
        if (result == 0) {
            return k1.compareTo(k2);
        } else {
            return result;
        }
    }
    
    public void test2(){
        int i = 0;
        int j=0;
        if (j<1){
            for(i=j;i<100;i++){
                System.out.println(i+j);
            }
        }
    }

}
