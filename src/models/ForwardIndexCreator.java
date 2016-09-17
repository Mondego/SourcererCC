package models;

import indexbased.DocumentMaker;
import indexbased.SearchManager;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.lucene.document.Document;

public class ForwardIndexCreator implements IListener, Runnable {
    private DocumentMaker documentMaker;
    private Document document;
    private Bag bag;

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
            System.out.println(SearchManager.NODE_PREFIX + ", something nasty in fwd indexing bags, exiting");
            System.exit(1);
        }
    }

    private void index(Bag bag) throws InterruptedException {
	long startTime = System.nanoTime(); 

        List<Shard> shards = SearchManager.getShardIdsForBag(bag);
        this.document = this.documentMaker.prepareDocumentForFwdIndex(bag);
        for (Shard shard : shards) {
            try {
                shard.getForwardIndexWriter().addDocument(this.document);
            } catch (IOException e) {
                System.out.println(SearchManager.NODE_PREFIX + ": error in indexing bag, " + bag);
                e.printStackTrace();
            }
        }
	long estimatedTime = System.nanoTime() - startTime;

        System.out.println(SearchManager.NODE_PREFIX + "FI, lines processed: " + SearchManager.statusCounter
                + ", Bag indexed: " + bag + " in " + estimatedTime/1000000 + "ms");
    }

}
