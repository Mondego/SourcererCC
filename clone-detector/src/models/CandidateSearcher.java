package models;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import indexbased.SearchManager;
import indexbased.TermSearcher;

public class CandidateSearcher implements IListener, Runnable {
    private QueryBlock queryBlock;

    public CandidateSearcher(QueryBlock queryBlock) {
        // TODO Auto-generated constructor stub
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
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
        System.out.println(SearchManager.NODE_PREFIX + " CandidateSearcher, QueryBlock " + queryBlock + " in shard "+
			   shard+" in " + estimatedTime/1000 + " micros");
        SearchManager.queryCandidatesQueue.send(qc);
    }

}
