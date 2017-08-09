package com.mondego.models;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.utility.Util;

public class CandidateSearcher implements IListener, Runnable {
	private QueryBlock queryBlock;
	private static final Logger logger = LogManager.getLogger(CandidateSearcher.class);

	public CandidateSearcher(QueryBlock queryBlock) {
		// TODO Auto-generated constructor stub
		this.queryBlock = queryBlock;
	}

	@Override
	public void run() {
		try {
			this.searchCandidates(queryBlock);

		} catch (NoSuchElementException e) {
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("EXCEPTION CAUGHT::", e);
		}
	}

	private void searchCandidates(QueryBlock queryBlock)
			throws IOException, InterruptedException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		long startTime = System.nanoTime();
		int lowerIndex = this.getLowerIndex();
		int higherIndex = this.getHigherIndex();

		QueryCandidates qc = new QueryCandidates(queryBlock, lowerIndex, higherIndex);
		long estimatedTime = System.nanoTime() - startTime;
		logger.debug(SearchManager.NODE_PREFIX + " CandidateSearcher, QueryBlock " + queryBlock + " in "
				+ estimatedTime / 1000 + " micros");
		SearchManager.queryCandidatesQueue.send(qc);
	}

	private int getLowerIndex() {
		double lowerRange = this.queryBlock.getComputedThreshold();
		Double uniqueTokens = Double.parseDouble(SearchManager.candidatesList.get(0)[7]);
		if (uniqueTokens < lowerRange) {
			int low = 0;
			int high = SearchManager.candidatesList.size() - 1;
			int mid;
			int bestLow = -1;
			while (low <= high) {
				mid = (low + high) / 2;
				uniqueTokens = Double.parseDouble(SearchManager.candidatesList.get(mid)[7]);
				if (uniqueTokens > lowerRange) {
					high = mid - 1;
				} else if (uniqueTokens <= lowerRange) {
					bestLow = mid;
					low = mid + 1;
				} else {
					// medians are equal
					bestLow = mid;
					break;
				}
			}
			double tempUniqueTokens = Double.parseDouble(SearchManager.candidatesList.get(bestLow)[7]);
			int index = --bestLow;
			while (index > -1 && Double.parseDouble(SearchManager.candidatesList.get(index)[7]) == tempUniqueTokens) {
				index--;
			}
			return ++index;
		}
		return 0;
	}

	private int getHigherIndex() {
		double higherRange = this.queryBlock.getMaxCandidateSize();
		Double uniqueTokens = Double
				.parseDouble(SearchManager.candidatesList.get(SearchManager.candidatesList.size() - 1)[7]);
		if (uniqueTokens > higherRange) {
			int low = 0;
			int high = SearchManager.candidatesList.size() - 1;
			int mid = 0;
			int bestHigh = -1;
			while (low <= high) {
				mid = (low + high) / 2;
				if (Double.parseDouble(SearchManager.candidatesList.get(mid)[7]) >= higherRange) {
					bestHigh = mid;
					high = mid - 1;
				} else if (Double.parseDouble(SearchManager.candidatesList.get(mid)[7]) < higherRange) {
					low = mid + 1;
				} else {
					// medians are equal
					bestHigh = mid;
					break;
				}
			}
			double temp = Double.parseDouble(SearchManager.candidatesList.get(bestHigh)[7]);
			int index = ++bestHigh;
			while (index < SearchManager.candidatesList.size()
					&& Double.parseDouble(SearchManager.candidatesList.get(index)[7]) == temp) {
				index++;
			}
			return --index;
		} else
			return SearchManager.candidatesList.size() - 1;
	}

}
