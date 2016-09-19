/**
 * 
 */
package utility;

import indexbased.SearchManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import models.Bag;
import models.TokenFrequency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * @author vaibhavsaini
 * 
 */
public class Util {
    static Random rand = new Random(5);
    public static final String CSV_DELIMITER = "~";
    public static final String INDEX_DIR = "index";
    public static final String INDEX_DIR_TEMP = "index_temp";
    public static final String GTPM_DIR = "gtpm";
    public static final String GLOBAL_WFM_DIR = "wfm";
    public static final String FWD_INDEX_DIR = "fwdindex";
    public static final String FWD_INDEX_DIR_TEMP = "fwdindex_temp";
    public static final String GTPM_INDEX_DIR = "gtpmindex";
    public static final String INDEX_DIR_NO_FILTER = "index_nofilter";

    /**
     * generates a random integer
     * 
     * @return
     */
    public static int getRandomNumber(int max, int min) {
        return rand.nextInt((max - min) + 1) + min;
    }

    /**
     * writes the given text to a file pointed by pWriter
     * 
     * @param pWriter
     *            handle to printWriter to write to a file
     * @param text
     *            text to be written in the file
     * @param isNewline
     *            whether to start from a newline or not
     */
    public static synchronized void writeToFile(Writer pWriter, final String text, final boolean isNewline) {
        if (isNewline) {
            try {
                pWriter.write(text + System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                pWriter.write(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            pWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * opens the outputfile for reporting clones
     * 
     * @param filename
     * @throws IOException
     * @return PrintWriter
     */
    public static Writer openFile(String filename, boolean append) throws IOException {
        try {
            Writer pWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filename, append), "UTF-8"));
            return pWriter;

        } catch (IOException e) {
            // IO exception caught
            System.err.println(e.getMessage());
            throw e;
        }
    }

    /**
     * closes the outputfile
     */
    public static void closeOutputFile(Writer pWriter) {
        if (null != pWriter) {
            try {
                pWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                pWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean createDirs(String dirname) {
        File dir = new File(dirname);
        if (!dir.exists()) {
            System.out.println("creating directory: " + dirname);
            return dir.mkdirs();
        } else {
            return true;
        }
    }

    public static boolean isSatisfyPosFilter(int similarity, int querySize, int termsSeenInQueryBlock,
            int candidateSize, int termsSeenInCandidate, int computedThreshold) {
        return computedThreshold <= similarity
                + Math.min(querySize - termsSeenInQueryBlock, candidateSize - termsSeenInCandidate);
    }

    public static void writeJsonStream(String filename, Map<String, Integer> gtpm) {
        Writer writer = null;
        try {
            writer = Util.openFile(filename, false);
            Gson gson = new GsonBuilder().create();
            String text = gson.toJson(gtpm);
            Util.writeToFile(writer, text, false);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String debug_thread() {
        return "  Thread_id: " + Thread.currentThread().getId() + " Thread_name: " + Thread.currentThread().getName();
    }

    public static Map<String, Integer> readJsonStream(String filename) {

        BufferedReader br = null;
        Map<String, Integer> gtpm = new HashMap<String, Integer>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"), 1024 * 1024 * 512);
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                Gson gson = new GsonBuilder().create();
                Type type = new TypeToken<Map<String, Integer>>() {
                }.getType();
                gtpm = gson.fromJson(line, type);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gtpm;

    }

    public static <K, V> Map<K, V> lruCache(final int maxSize) {
        return Collections.synchronizedMap(new LinkedHashMap<K, V>(maxSize * 4 / 3, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        });
    }

    // This cache is shared by all threads that call sortBag
    final static Map<String, Long> cache = lruCache(8000000);

    public static void sortBag(Bag bag) {
        List<TokenFrequency> bagAsList = new ArrayList<TokenFrequency>(bag);

        Collections.sort(bagAsList, new Comparator<TokenFrequency>() {
            public int compare(TokenFrequency tfFirst, TokenFrequency tfSecond) {
                Long frequency1 = 0l;
                Long frequency2 = 0l;
                String k1 = tfFirst.getToken().getValue();
                String k2 = tfSecond.getToken().getValue();
                if (cache.containsKey(k1)) {
                    frequency1 = cache.get(k1);
                } else {
                    frequency1 = SearchManager.gtpmSearcher.getFrequency(k1);
                    cache.put(k1, frequency1);
                }
                if (cache.containsKey(k2)) {
                    frequency2 = cache.get(k2);
                } else {
                    frequency2 = SearchManager.gtpmSearcher.getFrequency(k2);
                    cache.put(k2, frequency2);
                }
                int result = frequency1.compareTo(frequency2);
                if(result==0){
                    return k1.compareTo(k2);
                }else{
                    return result;
                }
            }
        });
        bag.clear();
        for (TokenFrequency tf : bagAsList) {
            bag.add(tf);
        }
    }

    /*
     * public static int getMinimumSimilarityThreshold(QueryBlock
     * queryBlock,float threshold) { return (int) Math.ceil((threshold *
     * queryBlock.getSize())/ (SearchManager.MUL_FACTOR*10)); } public static
     * int getMinimumSimilarityThreshold(Bag bag,float threshold) { return (int)
     * Math.ceil((threshold * bag.getSize())/ (SearchManager.MUL_FACTOR*10)); }
     */

    public static void writeMapToFile(String filename, Map<String, Long> map) {
        // TODO Auto-generated method stub
        Writer writer = null;
        try {
            System.out.println("writing to : " + filename);
            writer = Util.openFile(filename, false);
            for (Entry<String, Long> entry : map.entrySet()) {
                String text = entry.getKey() + ":" + entry.getValue();
                Util.writeToFile(writer, text, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Long> readMapFromFile(String filename) {
        // TODO Auto-generated method stub

        BufferedReader br = null;
        Map<String, Long> gtpm = new HashMap<String, Long>();
        try {
            System.out.println("here " + filename);
            File f = new File(filename);
            System.out.println("file is " + f);
            FileInputStream fis = new FileInputStream(f);
            System.out.println(fis);
            InputStreamReader ir = new InputStreamReader(fis, "UTF-8");
            System.out.println(ir);
            br = new BufferedReader(ir);
            System.out.println(br);
            // br = new BufferedReader(new InputStreamReader(new
            // FileInputStream(
            // new File(filename)), "UTF-8"), 1024 * 1024 * 512);

            System.out.println("hi");
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                String[] keyValPair = line.split(":");
                gtpm.put(keyValPair[0], Long.parseLong(keyValPair[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gtpm;

    }

    public static void populateProcessedWFMSet(String filename, Set<String> processedWFMset) {
        BufferedReader br = null;
        File f = new File(filename);
        System.out.println("file is " + f);
        FileInputStream fis;
        try {
            fis = new FileInputStream(f);
            System.out.println(fis);
            InputStreamReader ir = new InputStreamReader(fis, "UTF-8");
            System.out.println(ir);
            br = new BufferedReader(ir);
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                processedWFMset.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedReader getReader(File queryFile) throws FileNotFoundException {
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(queryFile));
        return br;
    }

    public static Writer openFile(File output, boolean append) throws IOException {
        // TODO Auto-generated method stub
        Writer pWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output, append), "UTF-8"));
        return pWriter;
    }

    /*
     * public static int getPrefixSize(QueryBlock queryBlock, float threshold) {
     * int prefixSize = (queryBlock.getSize() + 1) - computedThreshold;//
     * this.computePrefixSize(maxLength); return prefixSize; }
     */
    /*
     * public static int getPrefixSize(Bag bag, float threshold) { int
     * computedThreshold = getMinimumSimilarityThreshold(bag, threshold); int
     * prefixSize = (bag.getSize() + 1) - computedThreshold;//
     * this.computePrefixSize(maxLength); return prefixSize; }
     */
}
