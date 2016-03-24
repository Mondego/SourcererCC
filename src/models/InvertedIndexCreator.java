package models;

import indexbased.CodeIndexer;
import indexbased.SearchManager;

import java.util.NoSuchElementException;

public class InvertedIndexCreator implements IListener, Runnable {
    private CodeIndexer indexer;

    public InvertedIndexCreator(CodeIndexer indexer) {
        super();
        this.indexer = indexer;
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
        this.indexer.indexCodeBlock(bag);
        SearchManager.bagsToForwardIndexQueue.put(bag);
    }

    public CodeIndexer getIndexer() {
        return indexer;
    }
}