package models;

import indexbased.SearchManager;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

import utility.Util;

public class BagSorter implements IListener, Runnable {
    private Bag bag;

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
        Util.sortBag(bag);
        SearchManager.bagsToInvertedIndexQueue.send(bag);
    }
}