package models;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Queue<E> extends LinkedBlockingQueue<E>{

	private List<IListener> listeners;
	private ExecutorService executor;
	
	public Queue(int nThreads, int capacity) {
		super(capacity);
		this.executor = Executors.newFixedThreadPool(nThreads);
	}

	@Override
	public void put(E e) throws InterruptedException {
		super.put(e);
		notifyListeners();
	}

	private void notifyListeners() {
		for(IListener listener : this.listeners){
			executor.execute((Runnable) listener);
		}
	}

	public List<IListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<IListener> listeners) {
		this.listeners = listeners;
	}
	
	public void shutdown(){
		this.executor.shutdown();
		try {
			this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("inside catch, shutdown");
		}
	}
}
