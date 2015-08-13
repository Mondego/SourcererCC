/**
 * 
 */
package indexbased;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import models.QueryBlock;
import models.TokenInfo;
import noindex.CloneHelper;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import utility.Util;

/**
 * @author vaibhavsaini
 * 
 */
public class CodeSearcher {
    private String indexDir;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private IndexReader reader;
    private String queryDir;
    private CloneHelper cloneHelper;
    private QueryParser queryParser;
    private String field;

    public CodeSearcher(String indexDir) {
        this.field = "tokens";
        this.indexDir = indexDir;
        RAMDirectory directory = null;
        try {
        	FSDirectory fsDirectory = FSDirectory.open(new File(
                    this.indexDir));
        	directory = new RAMDirectory(fsDirectory, null);
            this.reader = DirectoryReader.open(directory);
        } catch (IOException e) {
            System.out.println("cant get the reader to index dir, exiting");
            e.printStackTrace();
            System.exit(1);
        }
        this.searcher = new IndexSearcher(this.reader);
        this.analyzer = new WhitespaceAnalyzer(Version.LUCENE_46);
        this.cloneHelper = new CloneHelper();
        this.queryParser = new QueryParser(Version.LUCENE_46, this.field,
                analyzer);
    }

    public CodeSearcher(boolean searchOnForwardIndex) {
        this.field = "id";
        this.indexDir = Util.FWD_INDEX_DIR;
        try {
            this.reader = DirectoryReader.open(FSDirectory.open(new File(
                    this.indexDir)));
        } catch (IOException e) {
            System.out.println("cant get the reader to fwdindex dir, exiting");
            e.printStackTrace();
            System.exit(1);
        }
        this.searcher = new IndexSearcher(reader);
        this.analyzer = new KeywordAnalyzer();
        this.cloneHelper = new CloneHelper();
        this.queryParser = new QueryParser(Version.LUCENE_46, this.field,
                analyzer);
    }


    public void search(QueryBlock queryBlock, TermSearcher termSearcher)
            throws IOException {
//        List<String> tfsToRemove = new ArrayList<String>();
        termSearcher.setReader(this.reader);
       // System.out.println("setting reader: "+this.reader + Util.debug_thread());
        termSearcher.setQuerySize(queryBlock.getSize());
        termSearcher.setComputedThreshold(queryBlock.getComputedThreshold());
        int termsSeenInQuery =0;
        StringBuilder prefixTerms = new StringBuilder();
        for (Entry<String, TokenInfo> entry : queryBlock.getPrefixMap().entrySet()) {
            try {
            	prefixTerms.append(entry.getKey()+" ");
            	Query query = null;
            	synchronized (this) {
            		query = queryParser.parse("\""+entry.getKey()+"\"");
				}
                termSearcher.setSearchTerm(query.toString(this.field));
                termSearcher.setFreqTerm(entry.getValue().getFrequency());
                termsSeenInQuery+=entry.getValue().getFrequency();
                termSearcher.searchWithPosition(termsSeenInQuery);
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                System.out.println("cannot parse " + e.getMessage());
            }
        }
    }


    public CustomCollectorFwdIndex search(Document doc) throws IOException {
        CustomCollectorFwdIndex result = new CustomCollectorFwdIndex();
        Query query;
        try {
        	synchronized (this) {
        		query = queryParser.parse(doc.get("id"));
			}
            /*
             * System.out.println("Searching for: " + query.toString(this.field)
             * + " : " + doc.get("id"));
             */
            this.searcher.search(query, result);
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            System.out.println("cannot parse " + e.getMessage());
        }
        return result;
    }
    
    public synchronized CustomCollectorFwdIndex search(Document doc, int i) throws IOException {
        CustomCollectorFwdIndex result = new CustomCollectorFwdIndex();
        Query query;
        String s = "sdsdasdasdsd";
        try {
    		 query = queryParser.parse(doc.get("id"));
            /*
             * System.out.println("Searching for: " + query.toString(this.field)
             * + " : " + doc.get("id"));
             */
            this.searcher.search(query, result);
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            System.out.println("cannot parse " + e.getMessage());
        }
        return result;
    }
    
    public Document getDocument(long docId) throws IOException {
        return this.searcher.doc((int) docId);
    }

    /**
     * @return the reader
     */
    public IndexReader getReader() {
        return reader;
    }

    /**
     * @param reader
     *            the reader to set
     */
    public void setReader(IndexReader reader) {
        this.reader = reader;
    }

}
