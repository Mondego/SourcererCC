package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadedChannel<E> {

    private ExecutorService executor;
    private Class<Runnable> workerType;
    private Semaphore semaphore;
    private static final Logger logger = LogManager.getLogger(ThreadedChannel.class);

    public ThreadedChannel(int nThreads, Class clazz) {
        this.executor = Executors.newFixedThreadPool(nThreads);
        this.workerType = clazz;
        this.semaphore = new Semaphore(nThreads);
    }

    public void send(E e) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        final Runnable o = this.workerType.getDeclaredConstructor(e.getClass()).newInstance(e);
        try {
            semaphore.acquire();
        } catch (InterruptedException ex) {
            logger.error("Caught interrupted exception " + ex);
        }

        try {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        o.run();
                    } finally {
                        semaphore.release();
                    }
                }
            });
        } catch (RejectedExecutionException ex) {
            semaphore.release();
        }
    }

    public void shutdown() {
        long start_time = System.nanoTime();
        
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)){
                logger.error("Pool did not terminate");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("inside catch, shutdown");
        }
        long end_time = System.nanoTime();
        logger.info("time taken for shutdown: "+ (end_time-start_time)/1000 +" micro seconds");
    }
}
