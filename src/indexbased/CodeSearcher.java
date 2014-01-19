/**
 * 
 */
package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import models.Bag;
import models.TokenFrequency;
import noindex.CloneHelper;

import org.apache.lucene.analysis.Analyzer;
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

    public CodeSearcher() {
        this.field = "tokens";
        this.indexDir = Util.INDEX_DIR;
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

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        CodeSearcher codeSearcher = new CodeSearcher();
        codeSearcher.searchIndex();
        // String test = "ada\"sdsd\'asds\"sdsdsd\'d";
        // System.out.println(codeSearcher.strip(test));
    }

    private void searchIndex() throws IOException {
        File datasetDir = new File("input/query/");
        if (datasetDir.isDirectory()) {
            System.out.println("Directory: " + datasetDir.getName());
            for (File inputFile : datasetDir.listFiles()) {
                String queryFile = "input/query/" + inputFile.getName();
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(queryFile));
                    String line;
                    while ((line = br.readLine()) != null
                            && line.trim().length() > 0) {
                        Bag bag = cloneHelper.deserialise(line);
                        for (TokenFrequency tf : bag) {
                            try {
                                Query query = queryParser.parse(tf.getToken()
                                        .getValue());
                                System.out.println("Searching for: "
                                        + query.toString(this.field) + " : "
                                        + tf.getToken().getValue());
                                CustomCollector result = new CustomCollector();
                                this.searcher.search(query, result);
                                this.processResults(result);
                            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                                // TODO Auto-generated catch block
                                // e.printStackTrace();
                                System.out.println("cannot parse "
                                        + e.getMessage());
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("File: " + datasetDir.getName()
                    + " is not a direcory. exiting now");
            System.exit(1);
        }
    }

    private String strip(String str) {
        return str.replaceAll("(\'|\")", "");
    }

    private void processResults(CustomCollector result) {
        if (null != result && result.getCodeBlockIds().size() == 0) {
            for (Integer docId : result.getCodeBlockIds()) {
                try {
                    Document doc = this.searcher.doc(docId);
                    System.out.println("match found, codeblock id :"
                            + doc.get("id"));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.out.println("IOEXception,  exiting");
                    System.exit(1);
                }
            }
        } else {
            System.out.println("no results");
        }
    }

}
