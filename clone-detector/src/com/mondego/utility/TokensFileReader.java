package com.mondego.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;
import com.mondego.models.ITokensFileProcessor;
import com.mondego.models.ProgressMonitor;

public class TokensFileReader {
    private String nodeId;
    private File file;
    private ITokensFileProcessor processor;
    private int maxTokens;
    private static final Logger logger = LogManager.getLogger(TokensFileReader.class);
    private ProgressMonitor progressMonitor;

    public TokensFileReader(String node_id, File f, int max_tokens, ITokensFileProcessor p) throws IOException {
        this.nodeId = node_id;
        this.file = f;
        this.processor = p;
        this.maxTokens = max_tokens;
        this.progressMonitor = ProgressMonitor.getInstance();
    }

    public void read() throws FileNotFoundException, IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        char[] buf = new char[40];
        this.progressMonitor.queriesProcessed = 0;
        while ((line = br.readLine()) != null) {
            this.processor.processLine(line);
            SearchManager.incrementProcessedQueriesCounter();
            this.progressMonitor.queriesProcessed++;
            logger.debug("queries processed: " + SearchManager.queriesProcessed + ", ProgressMonitor: "
                    + this.progressMonitor);
            if (this.progressMonitor.queriesProcessed
                    % SearchManager.properties.getInt("LOG_PROCESSED_LINENUMBER_AFTER_X_LINES") == 0) {
                logger.info("Search Progress" + this.progressMonitor.toString());
            }
        }
        try {
            br.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }
}
