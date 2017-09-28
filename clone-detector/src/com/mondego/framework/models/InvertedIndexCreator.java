package com.mondego.framework.models;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.application.handlers.SearchActionHandler;
import com.mondego.indexbased.DocumentMaker;

public class InvertedIndexCreator implements Runnable {
    private DocumentMaker documentMaker;
    private Bag bag;
    private static final Logger logger = LogManager
            .getLogger(InvertedIndexCreator.class);

    public InvertedIndexCreator(Bag bag) {
        super();
        this.documentMaker = new DocumentMaker();
        this.bag = bag;
    }

    @Override
    public void run() {
        try {
            /*
             * System.out.println(SearchManager.NODE_PREFIX +
             * ", size of bagsToInvertedIndexQueue " +
             * SearchManager.bagsToInvertedIndexQueue.size());
             */
            this.process();
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

    private void process() throws InterruptedException,
            InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        DocumentForInvertedIndex documentForII = this.documentMaker.prepareDocumentForII(this.bag);
        SearchActionHandler.documentsForII.put(documentForII.id, documentForII);
        Set<Long> docs = null;
        int prefixLength = documentForII.prefixSize;
        int pos = 0;
        TermInfo termInfo = null;
        for (TokenFrequency tf : this.bag) {
            if (prefixLength > 0) {
                String term = tf.getToken().getValue();
                if (SearchActionHandler.invertedIndex.containsKey(term)){
                    docs= SearchActionHandler.invertedIndex.get(term);
                }else{
                    docs = new HashSet<Long>();
                    SearchActionHandler.invertedIndex.put(term, docs);
                }
                docs.add(documentForII.id);
                termInfo = new TermInfo();
                termInfo.frequency=tf.getFrequency();
                termInfo.position = pos;
                pos = pos + tf.getFrequency();
                documentForII.termInfoMap.put(term, termInfo);
                prefixLength -= tf.getFrequency();
            }
            documentForII.tokenFrequencies.add(tf);
        }
    }

    public DocumentMaker getIndexer() {
        return documentMaker;
    }
}
