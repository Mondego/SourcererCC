package uci.mondego;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MapUtils {
    public static void subtractMaps(Map<String,Integer>map1, Map<String,Integer>map2){
        Iterator<Entry<String, Integer>> it =map1.entrySet().iterator();
        while (it.hasNext()){
            Entry<String,Integer> entry = it.next();
            if(map2.containsKey(entry.getKey())){
                int v2 = map2.get(entry.getKey());
                int newVal = entry.getValue()-v2;
                if (newVal>0){
                    entry.setValue(newVal);
                }else{
                    it.remove();
                }
                
            }
        }
    }
    
    public static int addValues(Map<String,Integer>map){
        int sum =0;
        for (Integer i : map.values()){
            sum+=i;
        }
        return sum;
    }
    
    public static void addOrUpdateMap(Map<String,Integer>map, String key){
        if (map.containsKey(key)){
            map.put(key, map.get(key)+1);
        }else{
            map.put(key, 1);
        }
    }

}
