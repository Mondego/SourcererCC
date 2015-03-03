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
import org.apache.lucene.store.FSDirectory;
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
    private TermSearcher termSearcher;

    public CodeSearcher(String indexDir) {
        this.field = "tokens";
        this.indexDir = indexDir;
        try {
            this.reader = DirectoryReader.open(FSDirectory.open(new File(
                    this.indexDir)));
        } catch (IOException e) {
            System.out.println("cant get the reader to index dir, exiting");
            e.printStackTrace();
            System.exit(1);
        }
        this.searcher = new IndexSearcher(reader);
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

    public CustomCollector search(QueryBlock queryBlock) throws IOException {
        CustomCollector result = new CustomCollector(this.searcher);
        for (Entry<String, Integer> entry : queryBlock.entrySet()) {
            try {
                Query query = queryParser.parse(entry.getKey());
                /*
                 * System.out.println("Searching for: " +
                 * query.toString(this.field) + " : " +
                 * tf.getToken().getValue());
                 */

                this.searcher.search(query, result);

            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                System.out.println("cannot parse " + e.getMessage());
            }
        }
        return result;
    }

    public CustomCollector search(QueryBlock queryBlock, int prefixSize)
            throws IOException {
        CustomCollector result = new CustomCollector(this.searcher);
        List<String> tfsToRemove = new ArrayList<String>();
        for (Entry<String, Integer> entry : queryBlock.entrySet()) {
            try {
                Query query = queryParser.parse(entry.getKey());
                /*
                 * System.out.println("Searching for: " +
                 * query.toString(this.field) + " : " +
                 * tf.getToken().getValue());
                 */
                result.setSearchTerm(query.toString(this.field));
                result.setFreqOfSearchTerm(entry.getValue());
                this.searcher.search(query, result);
                // String term = query.toString(this.field);
                tfsToRemove.add(entry.getKey()); // remove this tf
                prefixSize = prefixSize - entry.getValue();
                if (prefixSize <= 0) {
                    break;
                }
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                System.out.println("cannot parse " + e.getMessage());
            }
        }
        for (String key : tfsToRemove) {
            queryBlock.remove(key);
        }
        tfsToRemove = null; // just making sure to remove all references
        return result;
    }

    public void search2(QueryBlock queryBlock, int prefixSize)
            throws IOException {
//        List<String> tfsToRemove = new ArrayList<String>();
        this.termSearcher.setReader(this.reader);
        for (Entry<String, Integer> entry : queryBlock.entrySet()) {
            try {
                Query query = queryParser.parse("\""+entry.getKey()+"\"");
                this.termSearcher.setSearchTerm(query.toString(this.field));
                this.termSearcher.setFreqTerm(entry.getValue());
                this.termSearcher.search();
                // String term = query.toString(this.field);
                // tfsToRemove.add(entry.getKey()); // remove this tf
                prefixSize = prefixSize - entry.getValue();
                if (prefixSize <= 0) {
                    break;
                }
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                System.out.println("cannot parse " + e.getMessage());
            }
        }
        // for (String key : tfsToRemove) {
        // queryBlock.remove(key);
        // }

    }

    public void search2(QueryBlock queryBlock) throws IOException {
        this.termSearcher.setReader(this.reader);
        for (Entry<String, Integer> entry : queryBlock.entrySet()) {
            try {
                Query query = queryParser.parse("\""+entry.getKey()+"\"");
                this.termSearcher.setSearchTerm(query.toString(this.field));
                this.termSearcher.setFreqTerm(entry.getValue());
                this.termSearcher.search();
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                System.out.println("cannot parse " + e.getMessage());
            }
        }
    }

    public CustomCollectorFwdIndex search(Document doc) throws IOException {
        CustomCollectorFwdIndex result = new CustomCollectorFwdIndex();
        Query query;
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

    /**
     * @return the termSearcher
     */
    public TermSearcher getTermSearcher() {
        return termSearcher;
    }

    /**
     * @param termSearcher
     *            the termSearcher to set
     */
    public void setTermSearcher(TermSearcher termSearcher) {
        this.termSearcher = termSearcher;
    }
}
