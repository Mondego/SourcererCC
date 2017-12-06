package com.mondego.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.models.ITokensFileProcessor;

public class TokensFileReader {
    private String nodeId;
    private File file;
    private ITokensFileProcessor processor;
    private int maxTokens;
    private static final Logger logger = LogManager
            .getLogger(TokensFileReader.class);

    public TokensFileReader(String node_id, File f, int max_tokens,
            ITokensFileProcessor p) throws IOException {
        this.nodeId = node_id;
        this.file = f;
        this.processor = p;
        this.maxTokens = max_tokens;
    }

    public void read()
            throws FileNotFoundException, IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        long lineNumber = 0;
        char[] buf = new char[40];
        while ((line = br.readLine()) != null) {
            this.processor.processLine(line);
            lineNumber++;
            logger.debug("queries processed: " + lineNumber);
        }
        try {
            br.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }
}
