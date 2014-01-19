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

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utility.Util;

/**
 * @author vaibhavsaini
 * 
 */
public class CodeIndexer {
    private String indexDir;
    private IndexWriter indexWriter;
    private CloneHelper cloneHelper;
    private boolean isAppendIndex;

    /**
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     */
    public CodeIndexer() throws IOException {
        this.indexDir = Util.INDEX_DIR;
        this.isAppendIndex=true;
        this.cloneHelper = new CloneHelper();
        WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_46);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_46,analyzer);
        if(isAppendIndex){
            indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);// add new docs to exisiting index
        }
        this.indexWriter = new IndexWriter(FSDirectory.open(new File(
                this.indexDir)), indexWriterConfig);
    }

    public static void main(String[] args) throws FileNotFoundException {
        CodeIndexer indexer = null;
        try {
            indexer = new CodeIndexer();
            File datasetDir = new File("input/dataset/");
            if (datasetDir.isDirectory()) {
                System.out.println("Directory: " + datasetDir.getName());
                for (File inputFile : datasetDir.listFiles()) {
                    String projectInputFile = "input/dataset/"
                            + inputFile.getName();
                    indexer.indexCodeBlocks(projectInputFile);
                }
            } else {
                System.out.println("File: " + datasetDir.getName()
                        + " is not a direcory. exiting now");
                System.exit(1);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("exception in constructor, exiting now.");
            e.printStackTrace();
            System.exit(1);
        }finally{
            try {
                indexer.indexWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * iterate over all the code blocks and index them
     */
    private void indexCodeBlocks(String filename) {
        BufferedReader br = null;
        System.out.println("Indexing file: "+ filename);
        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                this.indexCodeBlock(cloneHelper.deserialise(line));
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

    /**
     * index the code block
     */
    private void indexCodeBlock(Bag bag) {
        Document document = this.prepareDocument(bag);
        try {
            this.indexWriter.addDocument(document);
        } catch (IOException e) {
            System.out
                    .println("EXCEPTION caught while indexing document for bag "
                            + bag.getId());
            e.printStackTrace();
        }
    }

    private Document prepareDocument(Bag bag) {
        Document document = new Document();
        StoredField strField = new StoredField("id", bag.getId() + "");
        document.add(strField);
        String tokenString = "";
        for (TokenFrequency tf : bag) {
            tokenString += tf.getToken().getValue() + " ";
        }
        TextField textField = new TextField("tokens", tokenString.trim(),
                Field.Store.NO);
        document.add(textField);
        return document;
    }

}
