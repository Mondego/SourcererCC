package models;

import indexbased.SearchManager;

import java.util.NoSuchElementException;

import utility.Util;

public class BagSorter implements IListener, Runnable {

	@Override
	public void run() {
		try {
			Bag bag = SearchManager.bagsToSortQueue.remove();
			this.sortBag(bag);
		} catch (NoSuchElementException e) {
			// e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sortBag(Bag bag) throws InterruptedException {
		Util.sortBag(bag);
		SearchManager.bagsToInvertedIndexQueue.put(bag);
	}
}