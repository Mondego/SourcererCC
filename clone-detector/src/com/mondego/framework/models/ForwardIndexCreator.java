package com.mondego.framework.models;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

import com.mondego.framework.controllers.MainController;
import com.mondego.indexbased.DocumentMaker;

public class ForwardIndexCreator implements IListener, Runnable {
    private Bag bag;
    private static final Logger logger = LogManager
            .getLogger(ForwardIndexCreator.class);

    public ForwardIndexCreator(Bag bag) {
        super();
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
            logger.fatal(MainController.NODE_PREFIX
                    + ", something nasty in fwd indexing bags, exiting");
            System.exit(1);
        }
    }

    private void index(Bag bag) throws InterruptedException {
        return;
    }
}
