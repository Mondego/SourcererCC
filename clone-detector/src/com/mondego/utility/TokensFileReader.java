package com.mondego.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.framework.controllers.MainController;
import com.mondego.framework.models.Bag;
import com.mondego.framework.models.ITokensFileProcessor;
import com.mondego.framework.models.QueryBlock;
import com.mondego.framework.models.Token;
import com.mondego.framework.models.TokenFrequency;
import com.mondego.framework.models.TokenInfo;
import com.mondego.framework.services.RecoveryService;

public class TokensFileReader {
    private String nodeId;
    private File file;
    private ITokensFileProcessor processor;
    private int maxTokens;
    private RecoveryService recoveryService;
    private static final Logger logger = LogManager.getLogger(TokensFileReader.class);

    public TokensFileReader(String node_id, File f, int max_tokens, ITokensFileProcessor p) throws IOException {
        this.nodeId = node_id;
        this.file = f;
        this.processor = p;
        this.maxTokens = max_tokens;
        this.recoveryService = RecoveryService.getInstance();
    }

    public void read() throws FileNotFoundException, IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        long lineNumber = 0;
        char[] buf = new char[40];
        while (br.read(buf, 0, 40) != -1) {
            if (MainController.ACTION_SEARCH.equals(MainController.ACTION)
                    && lineNumber < this.recoveryService.QUERY_LINES_TO_IGNORE) {
                logger.debug("RECOVERY: ignoring this line, as it was covered in previous run");
                while (((char) br.read()) != '\n') {
                    ; // ignore the line
                }
                lineNumber++;
                continue;
            }
            String prefix = new String(buf);
            String[] parts = prefix.split(",");
            int ntokens = Integer.parseInt(parts[2]);

            if (ntokens > this.maxTokens) {
                logger.debug(
                        this.nodeId + " RL, file " + parts[1] + ", " + ntokens + " tokens is too big. Ignoring...");
                while (((char) br.read()) != '\n')
                    ;
            } else {
                long startTime = System.nanoTime();

                if ((line = br.readLine()) != null && line.trim().length() > 0) {
                    long estimatedTime = System.nanoTime() - startTime;
                    logger.debug(this.nodeId + " RL " + lineNumber + ", file " + prefix + " in " + estimatedTime / 1000
                            + " micros");
                    this.processor.processLine(prefix + line);

                }
            }
            lineNumber++;
            if (MainController.ACTION_SEARCH.equals(MainController.ACTION)
                    && MainController.LOG_PROCESSED_LINENUMBER_AFTER_X_LINES > 0
                    && lineNumber % MainController.LOG_PROCESSED_LINENUMBER_AFTER_X_LINES == 0) {
                // Util.writeToFile(SearchManager.recoveryWriter, lineNumber +
                // "",
                // true);
            }
        }
        if (MainController.ACTION_SEARCH.equals(MainController.ACTION)) {
            // Util.writeToFile(SearchManager.recoveryWriter, lineNumber + "",
            // true);
        }
        br.close();
    }
    public static Bag deserialise(String s) {
        try {
            if (null != s && s.trim().length() > 0) {
                String[] bagAndTokens = s.split("@#@");
                String[] bagMetadata = bagAndTokens[0].split(",");
                // pid,fid,tokens,uniquetokens,separators,assignments,statements,expressions,thash
                String functionId = bagMetadata[0];
                String bagId = bagMetadata[1];
                int bagSize = Integer.parseInt(bagMetadata[2]);
                Bag bag = new Bag(Long.parseLong(bagId));
                bag.setFunctionId(Long.parseLong(functionId));
                bag.setSize(bagSize);
                int numUniqueTokens = Integer.parseInt(bagMetadata[3]);
                bag.setNumUniqueTokens(numUniqueTokens);
                if (bag.getSize() < MainController.min_tokens || bag.getSize() > MainController.max_tokens) {
                    return bag; // ignore this bag, do not process it further
                }
                for (int index = 0; index < Util.METRICS_ORDER_IN_INPUT_FILE.size(); index++) {
                    bag.metrics.put(Util.METRICS_ORDER_IN_INPUT_FILE.get(index),
                            Long.parseLong(bagMetadata[index + 2]));
                }
                String tokenString = bagAndTokens[1];
                TokensFileReader.parseAndPopulateBag(bag, tokenString);
                return bag;
            } else {
                logger.warn("parsing error at string: " + s, 0);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error(e.getMessage() + " possible parsing error at string: " + s);
            logger.error("ignoring this block");
        } catch (NumberFormatException e) {
            logger.error(e.getMessage() + ", ignoring this block");
        }
        return null;
    }

    private static void parseAndPopulateBag(Bag bag, String inputString) {
        Scanner scanner = new Scanner(inputString);
        scanner.useDelimiter(",");
        while (scanner.hasNext()) {
            String tokenFreq = scanner.next();
            String[] tokenAndFreq = tokenFreq.split("@@::@@");
            String tokenStr = TokensFileReader.strip(tokenAndFreq[0]).trim();
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

    public static QueryBlock getSortedQueryBlock(String s, List<Entry<String, TokenInfo>> listOfTokens) throws ParseException {
        try {
            if (null != s && s.trim().length() > 0) {
                String[] bagAndTokens = s.split("@#@");
                String[] bagMetadata = bagAndTokens[0].split(",");
                String functionId = bagMetadata[0];
                String bagId = bagMetadata[1];
                QueryBlock queryBlock = null;
                try {
                    int bagSize = Integer.parseInt(bagMetadata[2]);
                    int numUniqueTokens = Integer.parseInt(bagMetadata[3]);
                    if (bagSize < MainController.min_tokens || bagSize > MainController.max_tokens) {
                        return null; // do not process it further. we need
                                     // to discard this query
                    }
                    queryBlock = new QueryBlock(Long.parseLong((bagId)), bagSize);
                    queryBlock.setFunctionId(Long.parseLong(functionId));
                    queryBlock.setNumUniqueTokens(numUniqueTokens);
                    // logger.debug("setting metrics data for "+
                    // s.substring(0,40));

                    for (int index = 0; index < Util.METRICS_ORDER_IN_INPUT_FILE.size(); index++) {
                        queryBlock.metrics.put(Util.METRICS_ORDER_IN_INPUT_FILE.get(index),
                                Long.parseLong(bagMetadata[index + 2]));
                    }

                } catch (NumberFormatException e) {
                    logger.error(MainController.NODE_PREFIX + "NumberFormatException: " + e.getMessage());
                    throw e;
                } catch (Exception e) {
                    logger.error("EXCEPTION CAUGHT::", e);
                    System.exit(1);
                }
                String tokenString = bagAndTokens[1];
                TokensFileReader.parseAndPopulateQueryBlock(listOfTokens, tokenString, ",", "@@::@@");
                // Util.sortList(listOfTokens);
                return queryBlock;

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("EXCEPTION CAUGHT, string: " + s);
        } catch (NumberFormatException e) {
            logger.error(e.getMessage() + ", ignoring query: " + s);
        }
        throw new ParseException("parsing error", 0);
    }

    private static void parseAndPopulateQueryBlock(List<Entry<String, TokenInfo>> listOfTokens, String inputString,
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
            tokenStr = TokensFileReader.strip(tokenAndFreq[0]).trim();
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

    private static String strip(String str) {
        return str.replaceAll("(\'|\"|\\\\)", "");
    }


    public static void parseInputFileAndPopulateSet(File filename, Set<Bag> bagsSet) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                bagsSet.add(TokensFileReader.deserialise(line));
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
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
}
