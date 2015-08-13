package models;

import java.io.IOException;
import java.util.NoSuchElementException;

import indexbased.SearchManager;
import indexbased.TermSearcher;

public class CandidateSearcher implements IListener, Runnable {

	@Override
	public void run() {
		try {
			QueryBlock queryBlock = SearchManager.queryBlockQueue.remove();
			this.searchCandidates(queryBlock);
		} catch (NoSuchElementException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void searchCandidates(QueryBlock queryBlock) throws IOException,
			InterruptedException {
		TermSearcher termSearcher = new TermSearcher();
		// SearchManager.searcher.setTermSearcher(termSearcher);
		long searchCandidatesTimeStart = System.currentTimeMillis();
		SearchManager.searcher.search(queryBlock, termSearcher);
		SearchManager.timeSpentInSearchingCandidates += System
				.currentTimeMillis() - searchCandidatesTimeStart;
		QueryCandidates qc = new QueryCandidates();
		qc.queryBlock = queryBlock;
		qc.termSearcher = termSearcher;
		// System.out.println("before putting in qcq "+ Util.debug_thread() +
		// " query_id "+ queryBlock.getId());
		SearchManager.queryCandidatesQueue.put(qc);
		// System.out.println("after putting in qcq "+ Util.debug_thread() +
		// " query_id "+ queryBlock.getId());
	}

}
