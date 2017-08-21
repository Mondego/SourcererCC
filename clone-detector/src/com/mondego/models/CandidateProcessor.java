package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;

public class CandidateProcessor implements IListener, Runnable {
	private QueryCandidates qc;
	private static final Logger logger = LogManager.getLogger(CandidateProcessor.class);

	public CandidateProcessor(QueryCandidates qc) {
		// TODO Auto-generated constructor stub
		this.qc = qc;
	}

	@Override
	public void run() {
		try {
			// System.out.println( "QCQ size: "+
			// SearchManager.queryCandidatesQueue.size() + Util.debug_thread());
			this.processCandidates();
		} catch (NoSuchElementException e) {
			logger.error("EXCEPTION CAUGHT::", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
			e.printStackTrace();
		}
	}

	/*
	 * private void processCandidates() throws InterruptedException,
	 * InstantiationException, IllegalAccessException, IllegalArgumentException,
	 * InvocationTargetException, NoSuchMethodException, SecurityException { //
	 * CandidatePair candidatePair = new CandidatePair(queryBlock, simInfo, //
	 * computedThreshold, candidateSize, functionIdCandidate, candidateId)
	 * logger.debug("num candidates before: " + (this.qc.candidateUpperIndex -
	 * this.qc.candidateLowerIndex));
	 * 
	 * if (SearchManager.ijaMapping.containsKey(this.qc.queryBlock.fqmn)) {
	 * logger.debug("num candidates: " + (this.qc.candidateUpperIndex -
	 * this.qc.candidateLowerIndex)); for (int i = this.qc.candidateLowerIndex;
	 * i <= this.qc.candidateUpperIndex; i++) { // long startTime =
	 * System.nanoTime(); Block candidateBlock =
	 * SearchManager.candidatesList.get(i); if (candidateBlock.rowId <
	 * this.qc.queryBlock.rowId) { if (candidateBlock.size >=
	 * this.qc.queryBlock.computedThreshold && candidateBlock.size <=
	 * this.qc.queryBlock.maxCandidateSize) { if
	 * (SearchManager.ijaMapping.containsKey(candidateBlock.fqmn)) { if
	 * (this.getJaccard(candidateBlock.uniqueChars,
	 * this.qc.queryBlock.uniqueChars) > 0.5) { String[] features =
	 * this.getLineToWrite(qc.queryBlock, candidateBlock); String line =
	 * this.getLineToSend(features); try { //
	 * SearchManager.reportCloneQueue.send(new // ClonePair(line));
	 * SearchManager.socketWriter.writeToSocket(line); } catch (Exception e) {
	 * e.printStackTrace(); } } } } } } //
	 * SearchManager.verifyCandidateQueue.send(candidatePair); } }
	 */

	private double getJaccard(String s1, String s2) {
		if (s1 == null) {
			throw new NullPointerException("s1 must not be null");
		}

		if (s2 == null) {
			throw new NullPointerException("s2 must not be null");
		}

		if (s1.equals(s2)) {
			return 1;
		}

		// Map<String, Integer> profile1 = getProfile(s1);
		// Map<String, Integer> profile2 = getProfile(s2);

		Set<Character> union = new HashSet<Character>();
		for (int i = 0; i < s1.length(); i++) {
			union.add(s1.charAt(i));
		}
		for (int i = 0; i < s2.length(); i++) {
			union.add(s2.charAt(i));
		}

		// s1.chars().collect(java.util.stream.Collectors.toSet());
		// Collectors.toSet()
		// union.addAll();
		// union.addAll(profile2.keySet());

		int inter = s1.length() + s2.length() - union.size();

		return 1.0 * inter / union.size();
	}

	private String[] getLineToWrite(Block queryBlock, Block candiadteBlock) {
		String output[] = new String[30+27];
		// output[0]=firstLine[0]+"."+firstLine[1]+"."+firstLine[2]+"."+firstLine[27];
		// output[1]=secondLine[0]+secondLine[1]+secondLine[2]+secondLine[27];
		// output[2]=isClone?"1":"0";
		output[0] = queryBlock.projectName + "," + queryBlock.fileName + "," + queryBlock.startLine + ","
				+ queryBlock.endLine;
		output[1] = candiadteBlock.projectName + "," + candiadteBlock.fileName + "," + candiadteBlock.startLine + ","
				+ candiadteBlock.endLine;

		String cp = queryBlock.functionId + "," + queryBlock.id + "," + candiadteBlock.functionId + ","
				+ candiadteBlock.id;
		if (SearchManager.clonePairs.contains(cp)) {
			output[2] = 1 + "";
		} else {
			cp = candiadteBlock.functionId + "," + candiadteBlock.id + "," + queryBlock.functionId + ","
					+ queryBlock.id;
			if (SearchManager.clonePairs.contains(cp)) {
				output[2] = 1 + "";
			} else {
				output[2] = 0 + "";
			}
		}

		for (int i = 0; i < queryBlock.metrics.size(); i++) {

			output[i+3] = roundTwoDecimal(
					getPercentageDiff(queryBlock.metrics.get(i), candiadteBlock.metrics.get(i))).toString();
			output[i+30]=  roundTwoDecimal(Math.abs(queryBlock.metrics.get(i)-candiadteBlock.metrics.get(i)))+"";
		}
		return output;
	}

	private Double getPercentageDiff(double firstValue, double secondValue) {
		return (Math.abs(firstValue - secondValue) / Math.max(firstValue, secondValue)) * 100;
	}

	private Double roundTwoDecimal(double param) {
		return Double.valueOf(Math.round(param * 100.0) / 100.0);
	}

	private String getLineToSend(String[] lineParams) {
		String line = "";
		for (int i = 0; i < lineParams.length; i++) {
			line += lineParams[i] + "~~";
		}
		line = line.substring(0, line.length() - 2);// changed to 2 becasue ~~
													// has to chars
		return line;
	}

	private void processCandidates() throws InterruptedException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// System.out.println("HERE, thread_id: " + Util.debug_thread() +
		// ", query_id "+ queryBlock.getId());
		if (SearchManager.ijaMapping.containsKey(this.qc.queryBlock.fqmn)) {
			Map<Long, CandidateSimInfo> simMap = this.qc.simMap;
			Block queryBlock = this.qc.queryBlock;
			logger.debug(SearchManager.NODE_PREFIX + ", num candidates: " + simMap.entrySet().size() + ", query: "
					+ queryBlock);
			for (Entry<Long, CandidateSimInfo> entry : simMap.entrySet()) {
				Block candidateBlock = entry.getValue().doc;
				if (candidateBlock.size >= this.qc.queryBlock.computedThreshold
						&& candidateBlock.size <= this.qc.queryBlock.maxCandidateSize) {
					String type="3.2";
					if(candidateBlock.thash.equals(this.qc.queryBlock.thash)){
						type = "1";
					}
					if (SearchManager.ijaMapping.containsKey(candidateBlock.fqmn)) {
						String[] features = this.getLineToWrite(qc.queryBlock, candidateBlock);
						String line = this.getLineToSend(features);
						try {
							// SearchManager.reportCloneQueue.send(new
							// ClonePair(line));
							//logger.debug("FEATURE ROW: "+ line);
							SearchManager.socketWriter.writeToSocket(type+"#$#"+line);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}
		}

	}
}
