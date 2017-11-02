package com.mondego.framework.controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jmatrix.eproperties.EProperties;

public class Controller {
    private static final Logger logger = LogManager
            .getLogger(Controller.class);
    public static EProperties properties = new EProperties();
    public static String ROOT_DIR;
    static {
        // load properties
        FileInputStream fis = null;
        String propertiesPath = System.getProperty("properties.location");
        logger.debug("propertiesPath: " + propertiesPath);
        try {
            fis = new FileInputStream(propertiesPath);
        } catch (FileNotFoundException e1) {
            logger.fatal(
                    "ERROR READING PROPERTIES FILE PATH, " + e1.getMessage());
            e1.printStackTrace();
            System.exit(1);
        }
        try {
            Controller.properties.load(fis);
        } catch (IOException e) {
            logger.fatal("ERROR READING PROPERTIES FILE, " + e.getMessage());
            System.exit(1);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error(
                            "ERROR CLOSING PROPERTIES FILE, " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        // set properties
        Controller.ROOT_DIR = System.getProperty("properties.rootDir");
        MainController.DATASET_DIR = MainController.ROOT_DIR
                + properties.getProperty("DATASET_DIR_PATH");
        MainController.NODE_PREFIX = properties.getProperty("NODE_PREFIX")
                .toUpperCase();
        MainController.OUTPUT_DIR = MainController.ROOT_DIR
                + properties.getProperty("OUTPUT_DIR");
        MainController.QUERY_DIR_PATH = MainController.ROOT_DIR
                + properties.getProperty("QUERY_DIR_PATH");
        logger.debug("Query path:" + MainController.QUERY_DIR_PATH);
        MainController.LOG_PROCESSED_LINENUMBER_AFTER_X_LINES = properties.getInt("LOG_PROCESSED_LINENUMBER_AFTER_X_LINES", 1000);
        MainController.min_tokens = properties.getInt("LEVEL_1_MIN_TOKENS", 65);
        MainController.max_tokens = properties.getInt("LEVEL_1_MAX_TOKENS", 500000);
        
        logger.debug(MainController.NODE_PREFIX + " MAX_TOKENS=" + max_tokens
                + " MIN_TOKENS=" + min_tokens);
    }
}
