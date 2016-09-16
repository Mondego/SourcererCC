package models;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ThreadedChannel<E> {

    private ExecutorService executor;
    private Class<Runnable> workerType;

    public ThreadedChannel(int nThreads, Class clazz) {
        this.executor = Executors.newFixedThreadPool(nThreads);
        this.workerType = clazz;
    }

    public void send(E e) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Runnable o = this.workerType.getDeclaredConstructor(e.getClass()).newInstance(e);
        executor.execute((Runnable)o);
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
