package models;

import java.util.NoSuchElementException;

import utility.Util;
import indexbased.SearchManager;

public class CloneReporter implements IListener, Runnable {
    private ClonePair cp;
    public CloneReporter(ClonePair cp) {
        // TODO Auto-generated constructor stub
        this.cp = cp;
    }
    @Override
    public void run() {
        try {
            this.reportClone(this.cp);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    private void reportClone(ClonePair cp) {
        /*
         * System.out.println("QBQ: "+ SearchManager.queryBlockQueue.size()+
         * ", QCQ: "+ SearchManager.queryCandidatesQueue.size()+ ", VCQ: "+
         * SearchManager.verifyCandidateQueue.size()+ ", RCQ: "+
         * SearchManager.reportCloneQueue.size() );
         */
        long startTime = System.nanoTime();
        SearchManager.updateClonePairsCount(1);
        Util.writeToFile(SearchManager.clonesWriter, cp.toString(), true);
        cp = null;
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println(SearchManager.NODE_PREFIX + " CloneReporter, ClonePair " + cp + " in " + estimatedTime/1000 + " micros");
        
    }

}
