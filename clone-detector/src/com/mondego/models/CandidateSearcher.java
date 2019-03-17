package com.mondego.models;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.indexbased.TermSearcher;

public class CandidateSearcher implements IListener, Runnable {
    private QueryBlock queryBlock;
    private static final Logger logger = LogManager.getLogger(CandidateSearcher.class);
    public CandidateSearcher(QueryBlock queryBlock) {
        this.queryBlock = queryBlock;
    }

    @Override
    public void run() {
        try {
            this.searchCandidates(queryBlock);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void searchCandidates(QueryBlock queryBlock)
            throws IOException, InterruptedException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        long startTime = System.nanoTime();
	int shard = queryBlock.getShardId();
        TermSearcher termSearcher = new TermSearcher(shard, queryBlock.getId());

        SearchManager.searcher.get(shard).search(queryBlock, termSearcher);

        QueryCandidates qc = new QueryCandidates();
        qc.queryBlock = queryBlock;
        qc.termSearcher = termSearcher;
        long estimatedTime = System.nanoTime() - startTime;
        logger.debug(SearchManager.NODE_PREFIX + " CandidateSearcher, QueryBlock " + queryBlock + " in shard "+
			   shard+" in " + estimatedTime/1000 + " micros");
        SearchManager.queryCandidatesQueue.send(qc);
    }

}
