package models;

import java.util.NoSuchElementException;

import utility.Util;
import indexbased.SearchManager;

public class CloneReporter implements IListener, Runnable {

	@Override
	public void run() {
		try {
			ClonePair cp = SearchManager.reportCloneQueue.remove();
			this.reportClone(cp);
		} catch (NoSuchElementException e) {

		}
	}

	private void reportClone(ClonePair cp) {
		SearchManager.updateClonePairsCount(1);
		String text = cp.qid + "," + cp.cid;
		Util.writeToFile(SearchManager.clonesWriter, text, true);
	}

}
