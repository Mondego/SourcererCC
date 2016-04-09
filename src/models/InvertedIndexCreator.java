package models;

import indexbased.DocumentMaker;
import indexbased.SearchManager;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.lucene.document.Document;

public class InvertedIndexCreator implements IListener, Runnable {
    private DocumentMaker documentMaker;
    private Document document;

    public InvertedIndexCreator() {
        super();
        this.documentMaker = new DocumentMaker();
    }

    @Override
    public void run() {
        try {
            /*System.out.println(SearchManager.NODE_PREFIX
                    + ", size of bagsToInvertedIndexQueue "
                    + SearchManager.bagsToInvertedIndexQueue.size());*/
            Bag bag = SearchManager.bagsToInvertedIndexQueue.remove();
            this.index(bag);
        } catch (NoSuchElementException e) {
            // e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void index(Bag bag) throws InterruptedException {
        List<Shard> shards = SearchManager.getShardIdsForBag(bag);
        this.document = this.documentMaker.prepareDocument(bag);
        for (Shard shard : shards){
            try {
                shard.getInvertedIndexWriter().addDocument(this.document);
            } catch (IOException e) {
                System.out.println(SearchManager.NODE_PREFIX+ ": error in indexing bag, "+ bag);
                e.printStackTrace();
            }
        }
        SearchManager.bagsToForwardIndexQueue.put(bag);
    }

    public DocumentMaker getIndexer() {
        return documentMaker;
    }
}