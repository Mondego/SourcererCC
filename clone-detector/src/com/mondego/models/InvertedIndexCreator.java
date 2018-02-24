package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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
            this.index();
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

    private void index() throws InterruptedException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // DocumentForInvertedIndex documentForII =
        // this.documentMaker.prepareDocumentForII(block);
        synchronized (SearchManager.theInstance) {
            SearchManager.documentsForII.put(this.block.id, this.block);
            //Map<Long, Integer> documentsAndTermFrequencyMap = new HashMap<Long, Integer>();
            this.indexTokensInSet(this.block.actionTokenFrequencySet);
          //this.indexTokensInSet(this.block.stopwordActionTokenFrequencySet, documentsAndTermFrequencyMap);
            //this.indexTokensInSet(this.block.methodNameActionTokenFrequencySet, documentsAndTermFrequencyMap);
        }
    }
    private void indexTokensInSet(Set<TokenFrequency> tokenSet){
        for (TokenFrequency tf : tokenSet) {
            // if (prefixLength > 0) {
            String term = tf.getToken().getValue();
            if (SearchManager.invertedIndex.containsKey(term)) {
                Map<Long, Integer> documentsAndTermFrequencyMap = SearchManager.invertedIndex.get(term);
                documentsAndTermFrequencyMap.put(this.block.id, tf.getFrequency());
            } else {
                Map<Long, Integer> documentsAndTermFrequencyMap = new HashMap<Long, Integer>();
                SearchManager.invertedIndex.put(term, documentsAndTermFrequencyMap);
                documentsAndTermFrequencyMap.put(this.block.id, tf.getFrequency());
            }
        }
    }

    public DocumentMaker getIndexer() {
        return documentMaker;
    }
}
