package com.mondego.framework.workers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.framework.pipeline.Pipe;

abstract public class Worker<T> implements Runnable{
    protected T dataObject;
    protected Pipe pipe;
    protected static final Logger logger = LogManager.getLogger(Worker.class);
    public Worker(T t){
        this.dataObject = t;
        this.pipe = Pipe.getInstance();
    }
    @Override
    public void run() {
        this.process();
    }
    
    abstract public void process();

}
