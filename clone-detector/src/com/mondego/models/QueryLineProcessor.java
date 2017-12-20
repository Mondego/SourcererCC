package com.mondego.models;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;

public class QueryLineProcessor implements Runnable {
    private String line;
    private static final Logger logger = LogManager
            .getLogger(QueryLineProcessor.class);
    private Shard shard;

    public QueryLineProcessor(QueryLineWrapper lineWrapper) {
        this.line = lineWrapper.line;
        this.shard = lineWrapper.shard;
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
            Block queryBlock = new Block(line);
            queryBlock.shard = this.shard;
            long estimatedTime = System.nanoTime() - startTime;
            logger.debug(SearchManager.NODE_PREFIX
                    + " QLP processed QueryBlock " + queryBlock + " in "
                    + estimatedTime / 1000 + " micros");
            SearchManager.queryBlockQueue.send(queryBlock);
            // System.out.println(SearchManager.NODE_PREFIX +
            // ", line number: "+ count);
        } catch (InstantiationException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.error(
                    e.getMessage() + " skiping this query block, illegal args: "
                            + line.substring(0, 40));
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
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        }
    }
}
