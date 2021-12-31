package com.mondego.models;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.utility.Util;

public class BagSorter implements IListener, Runnable {
    private Bag bag;
    private static final Logger logger = LogManager.getLogger(BagSorter.class);
    public BagSorter(Bag bag) {
        // TODO Auto-generated constructor stub
        this.bag = bag;
    }

    @Override
    public void run() {
        try {
            /*
             * System.out.println(SearchManager.NODE_PREFIX +
             * ", size of bagsToSortQueue " +
             * SearchManager.bagsToSortQueue.size());
             */
            this.sortBag(this.bag);
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

    private void sortBag(Bag bag) throws InterruptedException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
	    long startTime = System.nanoTime();
        Util.sortBag(bag);
	    long estimatedTime = System.nanoTime() - startTime;
	    logger.info(SearchManager.NODE_PREFIX + " SB, Bag " + bag+ " in " + estimatedTime/1000 + " micros");
            SearchManager.bagsToInvertedIndexQueue.send(bag);
    }
}
