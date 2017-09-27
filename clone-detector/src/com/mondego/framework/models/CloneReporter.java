package com.mondego.framework.models;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.framework.controllers.MainController;
import com.mondego.framework.services.RuntimeStateService;
import com.mondego.utility.Util;

public class CloneReporter implements IListener, Runnable {
    private ClonePair cp;
    private RuntimeStateService runtimeStateService;
    private static final Logger logger = LogManager.getLogger(CloneReporter.class);
    public CloneReporter(ClonePair cp) {
        // TODO Auto-generated constructor stub
        this.cp = cp;
        this.runtimeStateService = RuntimeStateService.getInstance();
    }
    @Override
    public void run() {
        try {
            this.reportClone();
        } catch (NoSuchElementException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (Exception e){
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        }
    }

    private void reportClone() {
        /*
         * System.out.println("QBQ: "+ SearchManager.queryBlockQueue.size()+
         * ", QCQ: "+ SearchManager.queryCandidatesQueue.size()+ ", VCQ: "+
         * SearchManager.verifyCandidateQueue.size()+ ", RCQ: "+
         * SearchManager.reportCloneQueue.size() );
         */
        long startTime = System.nanoTime();
        this.runtimeStateService.updateClonePairsCount(1);
        Util.writeToFile(MainController.clonesWriter, cp.toString(), true);
        long estimatedTime = System.nanoTime() - startTime;
        logger.debug(MainController.NODE_PREFIX + " CloneReporter, ClonePair " + cp + " in " + estimatedTime/1000 + " micros");
        cp = null;
        
    }

}
