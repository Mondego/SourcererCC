package com.mondego.models;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

import com.mondego.indexbased.DocumentMaker;
import com.mondego.indexbased.SearchManager;
import com.mondego.indexbased.WordFrequencyStore;

public class InvertedIndexCreator implements IListener, Runnable {
    private DocumentMaker documentMaker;
    private Document document;
    private Bag bag;
    private static final Logger logger = LogManager.getLogger(InvertedIndexCreator.class);
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
            this.index(this.bag);
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

    private void index(Bag bag) throws InterruptedException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        long startTime = System.nanoTime();
        List<Shard> shards = SearchManager.getShards(bag);
	    StringBuilder sid = new StringBuilder();
        this.document = this.documentMaker.prepareDocument(bag);
        for (Shard shard : shards) {
	    sid.append(shard.getId() + ":");
            try {
                shard.getInvertedIndexWriter().addDocument(this.document);
            } catch (IOException e) {
                logger.error(SearchManager.NODE_PREFIX + ": error in indexing bag, " + bag);
                e.printStackTrace();
            }
        }
        long estimatedTime = System.nanoTime() - startTime;
        logger.debug(SearchManager.NODE_PREFIX + " II, Bag " + bag + " in shards " + sid.toString()
			   + " in " + estimatedTime / 1000 + " micros");

        SearchManager.bagsToForwardIndexQueue.send(bag);
    }

    public DocumentMaker getIndexer() {
        return documentMaker;
    }
}
