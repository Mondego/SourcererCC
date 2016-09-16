package utility;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

import java.text.ParseException;

import models.ITokensFileProcessor;

public class TokensFileReader {
    private File file;
    private ITokensFileProcessor processor;
    private int maxTokens;
    public TokensFileReader(File f, ITokensFileProcessor p, int max_tokens) throws IOException {
	this.file = f;
	this.processor = p;
	this.maxTokens = max_tokens;
    }

    public void read() throws FileNotFoundException, IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        long lineNumber = 0;
	char[] buf = new char[256];
        while (br.read(buf, 0, 256) != -1) {
	    String prefix = new String(buf);
	    String[] parts = prefix.split(",");
	    int ntokens = Integer.parseInt(parts[2]);
	    System.out.println("*** Reading file "+parts[1]+". Number of tokens is " + ntokens);
	    if (ntokens > this.maxTokens) {
		System.out.println("File " +parts[1]+ " is too big. Ignoring...");
		char c;
		while ((c = (char)br.read()) != '\n');
	    }
	    else {
		if ((line = br.readLine()) != null && line.trim().length() > 0) {
		    this.processor.processLine(prefix + line);
		}
	    }
	    lineNumber++;
        }
        br.close();
    }
}
