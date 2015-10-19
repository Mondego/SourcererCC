/**
 * 
 */
package utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import indexbased.TermSorter;
import models.Bag;
import models.TokenFrequency;

/**
 * @author vaibhavsaini
 * 
 */
public class Util {
    static Random rand = new Random(5);
    public static final String CSV_DELIMITER = "~";
    public static final String INDEX_DIR = "index";
    public static final String GTPM_DIR = "gtpm";
    public static final String FWD_INDEX_DIR = "fwdindex";
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
    public static synchronized void writeToFile(Writer pWriter,
            final String text, final boolean isNewline) {
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
    }

    /**
     * opens the outputfile for reporting clones
     * 
     * @param filename
     * @throws IOException
     * @return PrintWriter
     */
    public static Writer openFile(String filename, boolean append)
            throws IOException {
        try {
            Writer pWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename, append), "UTF-8"),
                    1024 * 1000 * 2);
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
            return dir.mkdirs();
        } else {
            return true;
        }
    }

    public static boolean isSatisfyPosFilter(int similarity, int querySize,
            int termsSeenInQueryBlock, int candidateSize,
            int termsSeenInCandidate, int computedThreshold) {
        return computedThreshold <= similarity
                + Math.min(querySize - termsSeenInQueryBlock, candidateSize
                        - termsSeenInCandidate);
    }

    public static void writeJsonStream(String filename,
            Map<String, Integer> gtpm) {
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
        return "  Thread_id: " + Thread.currentThread().getId()
                + " Thread_name: " + Thread.currentThread().getName();
    }

    public static Map<String, Integer> readJsonStream(String filename) {

        BufferedReader br = null;
        Map<String, Integer> gtpm = new HashMap<String, Integer>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    filename), "UTF-8"));
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

    public static void sortBag(Bag bag) {
        List<TokenFrequency> bagAsList = new ArrayList<TokenFrequency>(bag);
        Collections.sort(bagAsList, new Comparator<TokenFrequency>() {
            public int compare(TokenFrequency tfFirst, TokenFrequency tfSecond) {
                long position1 = 0;
                long position2 = 0;
                try {
                    position1 = TermSorter.globalTokenPositionMap.get(tfFirst
                            .getToken().getValue());
                } catch (Exception e) {
                    position1 = -1;
                    System.out.println("Exception in sort "
                            + tfFirst.getToken().getValue());
                }
                try {
                    position2 = TermSorter.globalTokenPositionMap.get(tfSecond
                            .getToken().getValue());
                } catch (Exception e) {
                    position2 = -1;
                    System.out.println("Exception in sort "
                            + tfSecond.getToken().getValue());
                }
                if (position1 - position2 != 0) {
                    return (int) (position1 - position2);
                } else {
                    return 1;
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
