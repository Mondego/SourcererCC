package com.mondego.models;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

import com.mondego.indexbased.DocumentMaker;
import com.mondego.indexbased.SearchManager;

public class ForwardIndexCreator implements IListener, Runnable {
    private DocumentMaker documentMaker;
    private Document document;
    private Bag bag;
    private static final Logger logger = LogManager.getLogger(ForwardIndexCreator.class);
    public ForwardIndexCreator(Bag bag) {
        super();
        this.documentMaker = new DocumentMaker();
        this.bag = bag;
    }

    @Override
    public void run() {
        try {
            this.index(this.bag);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            logger.fatal(SearchManager.NODE_PREFIX + ", something nasty in fwd indexing bags, exiting");
            System.exit(1);
        }
    }

    private void index(Bag bag) throws InterruptedException {
	long startTime = System.nanoTime(); 
        List<Shard> shards = SearchManager.getShards(bag);
        this.document = this.documentMaker.prepareDocumentForFwdIndex(bag);
        for (Shard shard : shards) {
            try {
                shard.getForwardIndexWriter().addDocument(this.document);
            } catch (IOException e) {
                logger.error(SearchManager.NODE_PREFIX + ": error in indexing bag, " + bag);
                e.printStackTrace();
            }
        }
    	long estimatedTime = System.nanoTime() - startTime;

        logger.debug(SearchManager.NODE_PREFIX + " FI, Bag " + bag + " in " + estimatedTime/1000 + " micros");
    }

}
