package models;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import indexbased.SearchManager;
import indexbased.TermSearcher;

public class CandidateSearcher implements IListener, Runnable {

    @Override
    public void run() {
        try {
            QueryBlock queryBlock = SearchManager.queryBlockQueue.remove();
            this.searchCandidates(queryBlock);
        } catch (NoSuchElementException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void searchCandidates(QueryBlock queryBlock) throws IOException,
            InterruptedException {
        long start_time = System.currentTimeMillis();
        TermSearcher termSearcher = new TermSearcher();
        SearchManager.searcher.search(queryBlock, termSearcher);
        QueryCandidates qc = new QueryCandidates();
        qc.queryBlock = queryBlock;
        qc.termSearcher = termSearcher;
        SearchManager.queryCandidatesQueue.put(qc);
        
        long end_time = System.currentTimeMillis();
        Duration duration;
        try {
            duration = DatatypeFactory.newInstance().newDuration(
                    end_time - start_time);
            System.out.printf(SearchManager.NODE_PREFIX + ", candidates Searched for query: "
                    + queryBlock.getFunctionId() + ","
                    + queryBlock.getId()
                    + " time taken: %02dh:%02dm:%02ds", duration.getDays()
                    * 24 + duration.getHours(), duration.getMinutes(),
                    duration.getSeconds());
            start_time = end_time;
            System.out.println();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }


    }

}
