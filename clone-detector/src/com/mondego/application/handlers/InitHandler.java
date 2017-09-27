package com.mondego.application.handlers;

import java.io.IOException;
import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.framework.handlers.interfaces.IActionHandler;
import com.mondego.indexbased.WordFrequencyStore;

public class InitHandler implements IActionHandler {
    private static final Logger logger = LogManager
            .getLogger(InitHandler.class);

    @Override
    public void handle(String action) {
        WordFrequencyStore wfs = new WordFrequencyStore();
        try {
            wfs.populateLocalWordFreqMap();
        } catch (IOException e) {
            logger.error("IO Exception ", e);
        } catch (ParseException e) {
            logger.error("Parse Exception ", e);
        }
    }

}
