package com.mondego.application.workers;

import com.mondego.application.models.ClonePair;
import com.mondego.framework.controllers.MainController;
import com.mondego.framework.services.RuntimeStateService;
import com.mondego.framework.utility.Util;
import com.mondego.framework.workers.Worker;

public class CloneReporterWorker extends Worker<ClonePair> {
    private RuntimeStateService runtimeStateService;
    public CloneReporterWorker(ClonePair t) {
        super(t);
        this.runtimeStateService = RuntimeStateService.getInstance();
    }

    @Override
    public void process() {
        long startTime = System.nanoTime();
        this.runtimeStateService.updateClonePairsCount(1);
        Util.writeToFile(MainController.clonesWriter, this.dataObject.toString(), true);
        long estimatedTime = System.nanoTime() - startTime;
        logger.debug(MainController.NODE_PREFIX + " CloneReporter, ClonePair " + this.dataObject + " in " + estimatedTime/1000 + " micros");
        this.dataObject = null;
        
    }

}
