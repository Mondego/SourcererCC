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
        long start_time = System.currentTimeMillis();
        TermSearcher termSearcher = new TermSearcher();
        SearchManager.searcher.search(queryBlock, termSearcher);
        QueryCandidates qc = new QueryCandidates();
        qc.queryBlock = queryBlock;
        qc.termSearcher = termSearcher;
        long end_time = System.currentTimeMillis();
        Duration duration;
        try {
            duration = DatatypeFactory.newInstance().newDuration(end_time - start_time);
            System.out.printf(
                    SearchManager.NODE_PREFIX + ", candidates Searched for query: " + queryBlock.getFunctionId() + ","
                            + queryBlock.getId() + " time taken: %02dh:%02dm:%02ds",
                    duration.getDays() * 24 + duration.getHours(), duration.getMinutes(), duration.getSeconds());
            start_time = end_time;
            System.out.println();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        SearchManager.queryCandidatesQueue.send(qc);
    }

}
