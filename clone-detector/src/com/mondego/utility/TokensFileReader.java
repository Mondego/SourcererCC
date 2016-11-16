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
    private static final Logger logger = LogManager.getLogger(TokensFileReader.class);
    public TokensFileReader(String node_id, File f, int max_tokens, ITokensFileProcessor p) throws IOException {
        this.nodeId = node_id;
        this.file = f;
        this.processor = p;
        this.maxTokens = max_tokens;
    }

    public void read() throws FileNotFoundException, IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        long lineNumber = 0;
        char[] buf = new char[40];
        while (br.read(buf, 0, 40) != -1) {
            String prefix = new String(buf);
            String[] parts = prefix.split(",");
            int ntokens = Integer.parseInt(parts[2]);

            if (ntokens > this.maxTokens) {
                logger.debug(
                        this.nodeId + " RL, file " + parts[1] + ", " + ntokens + " tokens is too big. Ignoring...");
                while (((char) br.read()) != '\n')
                    ;
            } else {
                long startTime = System.nanoTime();

                if ((line = br.readLine()) != null && line.trim().length() > 0) {
                    this.processor.processLine(prefix + line);
                }

                long estimatedTime = System.nanoTime() - startTime;
                logger.debug(this.nodeId + " RL " + lineNumber + ", file " + parts[1] + ", " + ntokens
                        + " tokens in " + estimatedTime / 1000 + " micros");
            }
            lineNumber++;
        }
        br.close();
    }
}
