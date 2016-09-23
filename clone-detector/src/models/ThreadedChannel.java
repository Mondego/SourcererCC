package models;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;
import java.util.concurrent.RejectedExecutionException;

public class ThreadedChannel<E> {

    private ExecutorService executor;
    private Class<Runnable> workerType;
    private Semaphore semaphore;

    public ThreadedChannel(int nThreads, Class clazz) {
        this.executor = Executors.newFixedThreadPool(nThreads);
        this.workerType = clazz;
	this.semaphore = new Semaphore(nThreads+2);
    }

    public void send(E e) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        final Runnable o = this.workerType.getDeclaredConstructor(e.getClass()).newInstance(e);
	try {
	    semaphore.acquire();
	} catch (InterruptedException ex) {
	    System.out.println("Caught interrupted exception " + ex);
	}
	    	
	try {
	    executor.execute(new Runnable() {
		    public void run() {
			try { o.run(); } 
			finally { semaphore.release(); }
		    }
		});
	} catch (RejectedExecutionException ex){
	    semaphore.release();
	}
    }

    public void shutdown() {
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("inside catch, shutdown");
        }
    }
}
