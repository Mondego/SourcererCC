package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.DocumentMaker;
import com.mondego.indexbased.SearchManager;

public class InvertedIndexCreator implements IListener, Runnable {
	private DocumentMaker documentMaker;
	private Block block;
	private static final Logger logger = LogManager.getLogger(InvertedIndexCreator.class);

	public InvertedIndexCreator(Block block) {
		super();
		this.documentMaker = new DocumentMaker();
		this.block = block;
	}

	@Override
	public void run() {
		try {
			/*
			 * System.out.println(SearchManager.NODE_PREFIX +
			 * ", size of bagsToInvertedIndexQueue " +
			 * SearchManager.bagsToInvertedIndexQueue.size());
			 */
			this.index(this.block);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void index(Block block) throws InterruptedException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		//DocumentForInvertedIndex documentForII = this.documentMaker.prepareDocumentForII(block);
		
		SearchManager.documentsForII.put(block.rowId, block);
		Map<Long,Integer> documentsAndTermFrequencyMap= new HashMap<Long,Integer>();
		//Set<Long> docs = null;
		// int prefixLength = documentForII.prefixSize;
		for (TokenFrequency tf : block.actionTokenFrequencySet) {
			// if (prefixLength > 0) {
			String term = tf.getToken().getValue();
			if (SearchManager.invertedIndex.containsKey(term)) {
				documentsAndTermFrequencyMap = SearchManager.invertedIndex.get(term);
			} else {
				documentsAndTermFrequencyMap = new HashMap<Long,Integer>();
				SearchManager.invertedIndex.put(term, documentsAndTermFrequencyMap);
			}
			documentsAndTermFrequencyMap.put(block.rowId,tf.getFrequency());
			//termInfo = new TermInfo();
			//termInfo.frequency = tf.getFrequency();
			//termInfo.position = pos;
			//pos = pos + tf.getFrequency();
			//documentForII.termInfoMap.put(term, termInfo);
			// prefixLength -= tf.getFrequency();
			// }
			//documentForII.tokenFrequencies.add(tf);
		}
	}

	public DocumentMaker getIndexer() {
		return documentMaker;
	}
}
