package com.mondego.framework.services;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.framework.controllers.MainController;

public class RecoveryService {
    public Set<Long> completedQueries;
    private static RecoveryService instance;
    private static final Logger logger = LogManager
            .getLogger(RecoveryService.class);
    public long QUERY_LINES_TO_IGNORE;
    
    private RecoveryService(){
        //this.runtimeStateService = RuntimeStateService.getInstance();
        this.completedQueries = new HashSet<Long>();
    }
    
    public static synchronized RecoveryService getInstance(){
        if (null==instance){
            instance = new RecoveryService();
        }
        return instance;
    }
    
    private void populateCompletedQueries() {
        // TODO Auto-generated method stub
        BufferedReader br = null;
        String filename = MainController.OUTPUT_DIR
                + MainController.th / MainController.MUL_FACTOR
                + "/recovery.txt";
        try {
            br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filename), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    if (line.trim().length() > 0) {
                        this.QUERY_LINES_TO_IGNORE = Long
                                .parseLong(line.trim());
                    }
                } catch (NumberFormatException e) {
                    logger.error(
                            MainController.NODE_PREFIX + ", error in parsing:"
                                    + e.getMessage() + ", line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            logger.error(MainController.NODE_PREFIX + ", " + filename
                    + " not found");
        } catch (UnsupportedEncodingException e) {
            logger.error(MainController.NODE_PREFIX
                    + ", error in populateCompleteQueries" + e.getMessage());
            logger.error("stacktrace: ", e);
        } catch (IOException e) {
            logger.error(MainController.NODE_PREFIX
                    + ", error in populateCompleteQueries IO" + e.getMessage());
            logger.error("stacktrace: ", e);
        }
        logger.info("lines to ignore in query file: "
                + this.QUERY_LINES_TO_IGNORE);
    }

}
