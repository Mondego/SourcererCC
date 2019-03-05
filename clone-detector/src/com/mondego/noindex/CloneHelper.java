package com.mondego.noindex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

import com.mondego.indexbased.CustomCollectorFwdIndex;
import com.mondego.indexbased.SearchManager;
import com.mondego.models.Bag;
import com.mondego.models.CandidateProcessor;
import com.mondego.models.QueryBlock;
import com.mondego.models.Shard;
import com.mondego.models.Token;
import com.mondego.models.TokenFrequency;
import com.mondego.models.TokenInfo;
import com.mondego.utility.Util;

/**
 * 
 */

/**
 * @author vaibhavsaini
 * 
 */
public class CloneHelper {
    private Writer clonesWriter; // writer to write the output
    private Bag previousBag; // the previous bag whose clones we were finding in
                             // other set
    private float threshold; // threshold for matching the clones.e.g. 80% or
                             // 90%
    private long comparisions;
    private int numClonesFound;
    private float th;
    private final Integer MUL_FACTOR = 100;
    private Map<String, Integer> globalIdentifierMap;
    private static final Logger logger = LogManager.getLogger(CloneHelper.class);
    /**
     * 
     */
    public CloneHelper() {
        super();
        this.comparisions = 0;
        this.numClonesFound = 0;
        this.globalIdentifierMap = new HashMap<String, Integer>();
    }

    /**
     * outputs the bagB as a clone of bagA
     * 
     * @param bagA
     * @param bagB
     */
    public void reportClone(Bag bagA, Bag bagB, Bag previousBag) {
        this.numClonesFound += 1;
        if (bagA.equals(previousBag)) { // TODO: can be optimized if we do not
                                        // compare here
            // System.out.println("equal");
            Util.writeToFile(this.clonesWriter, " ," + bagB.getId(), false);
        } else {
            // start a new line
            // System.out.println("different");
            Util.writeToFile(this.clonesWriter, "", true);
            Util.writeToFile(this.clonesWriter, "Clones of Bag " + bagA.getId(), true);
            Util.writeToFile(this.clonesWriter, bagB.getId() + "", false);
        }
    }

    /**
     * @return the threshold
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     * @param threshold
     *            the threshold to set
     */
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    /**
     * setA and setB represents two projects. Each element of the set is a Bag.
     * Each Bag is a Map that has Token as a key and token's frequency in the
     * method as value. the method compares two sets and outputs the clones.
     * 
     * @param setA
     *            set of Bags
     * @param setB
     *            set of Bags
     */
    public void detectClones(Set<Bag> setA, Set<Bag> setB, boolean useJaccardSimilarity) {
        // iterate on setA
        for (Bag bagInSetA : setA) {
            // compare this map with every map in setB and report clones
            // iterate on setB
            for (Bag bagInSetB : setB) {
                if (bagInSetA.getId() != bagInSetB.getId()) {
                    if (bagInSetA.getId() < bagInSetB.getId()) {
                        this.detectClones(bagInSetA, bagInSetB, useJaccardSimilarity);
                    }
                }
            }
        }
    }

    /**
     * the method compares two maps and reports if they are clones.
     * 
     * @param bagA
     *            map of token as key and it's frequency in a method as value
     * @param bagB
     *            map of token as key and it's frequency in a method as value
     */
    public void detectClones(Bag bagA, Bag bagB, boolean useJaccardSimilarity) {
        int computedThreshold = 0;
        if (useJaccardSimilarity) {
            int computedThreshold_jaccard = (int) Math
                    .ceil((this.th * (bagA.getSize() + bagB.getSize())) / (10 * this.MUL_FACTOR + this.th));
            computedThreshold = computedThreshold_jaccard;
        } else {
            int maxLength = Math.max(bagA.getSize(), bagB.getSize());
            int computedThreshold_overlap = (int) Math.ceil((this.th * maxLength) / (10 * this.MUL_FACTOR));
            computedThreshold = computedThreshold_overlap;
        }
        // threshold.
        // System.out.println("threshold is "+ computedThreshold +
        // " bagA: "+bagA.getId()+ " bagB: "+bagB.getId());
        // iterate on bagA
        int count = 0;
        for (TokenFrequency tokenFrequencyA : bagA) {
            // search this token in bagB
            TokenFrequency tokenFrequencyB = bagB.get(tokenFrequencyA);
            this.comparisions += bagB.getComparisions();
            if (null != tokenFrequencyB) {
                // token found.
                count += Math.min(tokenFrequencyA.getFrequency(), tokenFrequencyB.getFrequency());
                if (count >= computedThreshold) {
                    // report clone.
                    this.reportClone(bagA, bagB, this.previousBag);
                    this.previousBag = bagA;
                    break; // no need to iterate on other keys clone has been
                           // found
                }
            }
        }
    }

    /**
     * returns the string for the insuptSet.
     * 
     * @param inputSet
     * @return String
     */
    public String stringify(Set<Bag> inputSet) {
        String returnString = "";
        for (Bag bag : inputSet) {
            returnString += bag.toString();
        }
        return returnString;
    }

    public Bag deserialise(String s) throws ParseException {
        try {
            if (null != s && s.trim().length() > 0) {
                String[] bagAndTokens = s.split("@#@");
                String[] bagMetadata = bagAndTokens[0].split(",");
                String functionId = bagMetadata[0];
                String bagId = bagMetadata[1];
                int bagSize = Integer.parseInt(bagMetadata[2]);
                Bag bag = new Bag(Long.parseLong(bagId));
                bag.setFunctionId(Long.parseLong(functionId));
                bag.setSize(bagSize);
                if (bag.getSize() < SearchManager.min_tokens || bag.getSize() > SearchManager.max_tokens) {
                    return bag; // ignore this bag, do not process it further
                }
                String tokenString = bagAndTokens[1];
                this.parseAndPopulateBag(bag, tokenString);
                return bag;
            } else {
                throw new ParseException("parsing error at string: " + s, 0);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error(e.getMessage() + " possible parsing error at string: " + s);
            logger.error("ignoring this block");
        } catch (NumberFormatException e) {
            logger.error(e.getMessage() + ", ignoring this block");
        }
        return null;
    }

    private void parseAndPopulateBag(Bag bag, String inputString) {
        Scanner scanner = new Scanner(inputString);
        scanner.useDelimiter(",");
        while (scanner.hasNext()) {
            String tokenFreq = scanner.next();
            String[] tokenAndFreq = tokenFreq.split("@@::@@");
            String tokenStr = this.strip(tokenAndFreq[0]).trim();
            if (tokenStr.length() > 0) {
                Token token = new Token(tokenStr);
                TokenFrequency tokenFrequency = new TokenFrequency();
                tokenFrequency.setToken(token);
                try {
                    tokenFrequency.setFrequency(Integer.parseInt(tokenAndFreq[1]));
                    bag.add(tokenFrequency);
                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.error("EXCEPTION CAUGHT, token: " + token);
                    // System.out.println("EXCEPTION CAUGHT, tokenFreq: "+
                    // tokenAndFreq[1]);
                    logger.error("EXCEPTION CAUGHT: " + inputString);
                } catch (NumberFormatException e) {
                    logger.error("EXCEPTION CAUGHT: " + inputString + " " + e.getMessage());
                }
            }
        }
        scanner.close();
    }


    /*
     * public QueryBlock deserialiseToQueryBlock(String s, List<Entry<String,
     * TokenInfo>> listOfTokens) throws ParseException { try { if (null != s &&
     * s.trim().length() > 0) { String[] bagAndTokens = s.split("@#@"); String[]
     * functionIdAndBagId = bagAndTokens[0].split(","); String functionId =
     * functionIdAndBagId[0]; String bagId = functionIdAndBagId[1]; // int size
     * = Integer.parseInt(functionIdAndBagId[2]); // QueryBlock queryBlock = new
     * // QueryBlock(Long.parseLong((bagId))); //
     * queryBlock.setFunctionId(Long.parseLong(functionId)); String tokenString
     * = bagAndTokens[1]; int queryBlockSize = this.parseAndPopulateQueryBlock(
     * listOfTokens, tokenString,",","@@::@@"); QueryBlock queryBlock = new
     * QueryBlock(Long.parseLong((bagId)), queryBlockSize); try {
     * queryBlock.setFunctionId(Long.parseLong(functionId)); } catch
     * (NumberFormatException e) { throw e; } return queryBlock; } } catch
     * (ArrayIndexOutOfBoundsException e) {
     * System.out.println("EXCEPTION CAUGHT, string: " + s); } catch
     * (NumberFormatException e) { System.out.println(e.getMessage() +
     * ", ignoring query: " + s); } throw new ParseException("parsing error",
     * 0); }
     */

    public QueryBlock getSortedQueryBlock(String s, List<Entry<String, TokenInfo>> listOfTokens) throws ParseException {
        try {
            if (null != s && s.trim().length() > 0) {
                String[] bagAndTokens = s.split("@#@");
                String[] bagMetadata = bagAndTokens[0].split(",");
                String functionId = bagMetadata[0];
                String bagId = bagMetadata[1];
                QueryBlock queryBlock = null;
                try {
                    int bagSize = Integer.parseInt(bagMetadata[2]);
                    if (bagSize < SearchManager.min_tokens || bagSize > SearchManager.max_tokens) {
                        return null; // do not process it further. we need
                                           // to discard this query
                    }
                    queryBlock = new QueryBlock(Long.parseLong((bagId)), bagSize);
                    queryBlock.setFunctionId(Long.parseLong(functionId));
		    Shard shard = SearchManager.getShard(queryBlock);
		    if (shard == null) {
			logger.warn(SearchManager.NODE_PREFIX + " unable to find shard for query block " + queryBlock);
			return null;
		    }

		    queryBlock.setShardId(shard.getId()); 

                } catch (NumberFormatException e) {
                    throw e;
                }
                String tokenString = null;// bagAndTokens[1];
                CustomCollectorFwdIndex collector = SearchManager.fwdSearcher.get(queryBlock.getShardId()).search(bagId);
                List<Integer> blocks = collector.getBlocks();
                if (!blocks.isEmpty()) {
                    if (blocks.size() == 1) {
                        Document document = SearchManager.fwdSearcher.get(queryBlock.getShardId()).getDocument(blocks.get(0));
                        tokenString = document.get("tokens");
                        this.parseAndPopulateQueryBlock(listOfTokens, tokenString, "::", ":");
                        return queryBlock;
                    } else {
                        logger.error("blocks found in fwdIndex while parsing query: " + blocks.size());
                    }
                } else {
                    logger.warn("warning! " + bagId + " not in fwdindex, cant get query string");
                }

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("EXCEPTION CAUGHT, string: " + s);
        } catch (NumberFormatException e) {
            logger.error(e.getMessage() + ", ignoring query: " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new ParseException("parsing error", 0);
    }

    private void parseAndPopulateQueryBlock(List<Entry<String, TokenInfo>> listOfTokens, String inputString,
            String delimeterTokenFreq, String delimeterTokenAndFreq) {
        // int queryBlockSize = 0;
        Scanner scanner = new Scanner(inputString);
        scanner.useDelimiter(delimeterTokenFreq);
        String tokenFreq = null;
        String[] tokenAndFreq = null;
        String tokenStr = null;
        while (scanner.hasNext()) {
            tokenFreq = scanner.next();
            tokenAndFreq = tokenFreq.split(delimeterTokenAndFreq);
            tokenStr = this.strip(tokenAndFreq[0]).trim();
            if (tokenStr.length() > 0) {
                try {
                    TokenInfo tokenInfo = new TokenInfo(Integer.parseInt(tokenAndFreq[1]));
                    Entry<String, TokenInfo> entry = new AbstractMap.SimpleEntry<String, TokenInfo>(tokenStr,
                            tokenInfo);
                    listOfTokens.add(entry);
                    // queryBlockSize += tokenInfo.getFrequency();

                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.error("EXCEPTION CAUGHT, token: " + tokenStr + "," + e.getMessage());
                    // System.out.println("EXCEPTION CAUGHT, tokenFreq: "+
                    // tokenAndFreq[1]);
                    logger.error("EXCEPTION CAUGHT, inputString : " + inputString + "," + e.getMessage());
                } catch (NumberFormatException e) {
                    logger.error("EXCEPTION CAUGHT, inputString : " + inputString + "," + e.getMessage());
                }
            }

        }
        scanner.close();
    }

    private String strip(String str) {
        return str.replaceAll("(\'|\"|\\\\)", "");
    }

    /**
     * @return the clonesWriter
     */
    public Writer getClonesWriter() {
        return clonesWriter;
    }

    /**
     * @param clonesWriter
     *            the clonesWriter to set
     */
    public void setClonesWriter(Writer clonesWriter) {
        this.clonesWriter = clonesWriter;
    }

    public void bookKeepInputs(Set<Bag> setA, Set<Bag> setB, PrintWriter inputSetsWriter) {
        String setAString = this.stringify(setA);
        String setBString = this.stringify(setB);
        Util.writeToFile(inputSetsWriter, setAString, true);
        Util.writeToFile(inputSetsWriter, "********************************", true);
        Util.writeToFile(inputSetsWriter, "********************************", true);
        Util.writeToFile(inputSetsWriter, "********************************", true);
        Util.writeToFile(inputSetsWriter, setBString, true);
    }

    public void parseInputFileAndPopulateSet(File filename, Set<Bag> bagsSet) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                bagsSet.add(this.deserialise(line));
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private Integer getMappedInteger(String token) {
        if (this.globalIdentifierMap.containsKey(token)) {
            return this.globalIdentifierMap.get(token);
        }
        int size = this.globalIdentifierMap.size();
        this.globalIdentifierMap.put(token, size);
        return size;
    }

    /**
     * @return the comparisions
     */
    public long getComparisions() {
        return comparisions;
    }

    /**
     * @param comparisions
     *            the comparisions to set
     */
    public void setComparisions(long comparisions) {
        this.comparisions = comparisions;
    }

    /**
     * @return the numClonesFound
     */
    public int getNumClonesFound() {
        return numClonesFound;
    }

    /**
     * @param numClonesFound
     *            the numClonesFound to set
     */
    public void setNumClonesFound(int numClonesFound) {
        this.numClonesFound = numClonesFound;
    }

    public void setTh(float th) {
        this.th = th;

    }

    public void parseInputDirAndPopulateSet(File dir, Set<Bag> setA) {
        for (File filename : dir.listFiles()) {
            this.parseInputFileAndPopulateSet(filename, setA);
        }
    }

}
