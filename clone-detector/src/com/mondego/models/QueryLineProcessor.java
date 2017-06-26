package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;

public class QueryLineProcessor implements Runnable {
    private String line;
    private SearchManager searchManager;
    private static final Logger logger = LogManager.getLogger(QueryLineProcessor.class);

    public QueryLineProcessor(String line) {
        this.line = line;
        this.searchManager = SearchManager.theInstance;
    }

    public void run() {
        try {
            processLine();
        } catch (Exception e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        }
    }

    public void processLine() {
        // TODO Auto-generated method stub
        long startTime = System.nanoTime();
        try {
            QueryBlock queryBlock = this.getNextQueryBlock(line);
            if (queryBlock == null)
                return;
            if (searchManager.appendToExistingFile && searchManager.completedQueries.contains(queryBlock.getId())) {
                logger.debug("ignoring query, REASON: completed in previous run, " + queryBlock);
                return;
            }

            if (SearchManager.isStatusCounterOn) {
                SearchManager.statusCounter += 1;
            }
            long estimatedTime = System.nanoTime() - startTime;
            logger.debug(SearchManager.NODE_PREFIX + " QLP processed QueryBlock " + queryBlock + " in "
                    + estimatedTime / 1000 + " micros");
            SearchManager.queryBlockQueue.send(queryBlock);
            // System.out.println(SearchManager.NODE_PREFIX +
            // ", line number: "+ count);
        } catch (InstantiationException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage() + " skiping this query block, illegal args: " + line.substring(0, 40));
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (ParseException e) {
            logger.error("catching parseException, dont worry");
            logger.error(e.getMessage() + " skiping this query block, parse exception: " + line.substring(0, 40));
            // e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        }
    }

    public QueryBlock getNextQueryBlock(String line) throws ParseException, IllegalArgumentException {
        List<Entry<String, TokenInfo>> listOfTokens = new ArrayList<Entry<String, TokenInfo>>();
        QueryBlock queryBlock = searchManager.cloneHelper.getSortedQueryBlock(line, listOfTokens);
        if (queryBlock == null) {
            logger.debug(SearchManager.NODE_PREFIX + " QLP, Invalid QueryBlock " + line.substring(0, 40));
            return null;
        }

        int position = 0;
        for (Entry<String, TokenInfo> entry : listOfTokens) {
            TokenInfo tokenInfo = entry.getValue();
            if (position < queryBlock.getPrefixSize()) {
                queryBlock.getPrefixMap().put(entry.getKey(), tokenInfo);
                position += tokenInfo.getFrequency();
                queryBlock.setPrefixMapSize(position);
            } else {
                queryBlock.getSuffixMap().put(entry.getKey(), tokenInfo);
                position += tokenInfo.getFrequency();
            }
            tokenInfo.setPosition(position);
        }

        return queryBlock;
    }
}
