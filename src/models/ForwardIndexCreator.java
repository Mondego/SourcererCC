package models;

import indexbased.CodeIndexer;
import indexbased.SearchManager;

import java.util.NoSuchElementException;

public class ForwardIndexCreator implements IListener, Runnable {
    private CodeIndexer indexer;

    public ForwardIndexCreator(CodeIndexer indexer) {
        super();
        this.indexer = indexer;
    }

    @Override
    public void run() {
        try {
            Bag bag = SearchManager.bagsToForwardIndexQueue.remove();
            this.index(bag);
        } catch (NoSuchElementException e) {
            // e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void index(Bag bag) throws InterruptedException {
        this.indexer.fwdIndexCodeBlock(bag);
        System.out.println(SearchManager.NODE_PREFIX + ", lines processed: "
                + SearchManager.statusCounter + ", Bag indexed: "
                + bag.getFunctionId() + ", " + bag.getId() + ", size: "
                + bag.getSize() + "");
    }

}