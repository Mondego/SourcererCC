package com.mondego.models;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.utility.Util;

public class CloneReporter implements IListener, Runnable {
    private ClonePair cp;
    private static final Logger logger = LogManager.getLogger(CloneReporter.class);
    public CloneReporter(ClonePair cp) {
        // TODO Auto-generated constructor stub
        this.cp = cp;
    }
    @Override
    public void run() {
        try {
            this.reportClone(this.cp);
        } catch (NoSuchElementException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (Exception e){
            logger.error("EXCEPTION CAUGHT::", e);
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
        //long startTime = System.nanoTime();
        //SearchManager.updateClonePairsCount(1);
        try {
            Util.writeToFile(SearchManager.clonesWriter, cp.outline, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //long estimatedTime = System.nanoTime() - startTime;
        //logger.debug(SearchManager.NODE_PREFIX + " CloneReporter, ClonePair " + cp + " in " + estimatedTime/1000 + " micros");
        cp = null;
        
    }

}
