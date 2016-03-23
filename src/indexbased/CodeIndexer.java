/**
 * 
 */
package indexbased;

import java.io.IOException;

import models.Bag;
import models.TokenFrequency;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import utility.BlockInfo;

/**
 * @author vaibhavsaini
 * 
 */
public class CodeIndexer {
    private IndexWriter indexWriter;

    /**
     * @param indexDir
     * @param indexWriter
     * @param cloneHelper
     * @param isAppendIndex
     * @param isPrefixIndex
     */
    public CodeIndexer(IndexWriter indexWriter) {
        super();
        this.indexWriter = indexWriter;
    }

    /**
     * index the code block
     */
    public void indexCodeBlock(Bag bag) {
        Document document;
        document = this.prepareDocument(bag);
        try {
            this.indexWriter.addDocument(document);
        } catch (IOException e) {
            System.out
                    .println("EXCEPTION caught while indexing document for bag "
                            + bag.getId());
            e.printStackTrace();
        }
    }

    public void fwdIndexCodeBlock(Bag bag) {
        Document document = this.prepareDocumentForFwdIndex(bag);
        try {
            this.indexWriter.addDocument(document);
        } catch (IOException e) {
            System.out
                    .println("EXCEPTION caught while indexing document for bag "
                            + bag.getId());
            e.printStackTrace();
        }
    }

    public void indexGtpmEntry(String line) {
        Document document = this.prepareDocumentForGTPMIndex(line);
        try {
            if (null != document) {
                this.indexWriter.addDocument(document);
            }
        } catch (IOException e) {
            System.out
                    .println("EXCEPTION caught while indexing document for gtpm entry "
                            + line);
            e.printStackTrace();
        }
    }

    private Document prepareDocumentForFwdIndex(Bag bag) {
        Document document = new Document();
        TextField textField = new TextField("id", bag.getId() + "",
                Field.Store.NO);
        document.add(textField);
        String tokenString = "";
        System.out.println("fwdindex: creating tokenString ");
        for (TokenFrequency tf : bag) {
            // System.out.println(tf.getToken().getValue() +
            // ":"+tf.getFrequency());
            tokenString += tf.getToken().getValue() + ":" + tf.getFrequency()
                    + "::";
        }
        System.out.println("fwdindex:  tokenString created ");
        StoredField strField = new StoredField("tokens", tokenString.trim());
        document.add(strField);
        System.out.println("fwdindex: document added");
        return document;
    }

    private Document prepareDocumentForGTPMIndex(String line) {
        Document document = new Document();
        String[] keyValPair = line.split(":");
        if (keyValPair.length == 2) {
            TextField textField = new TextField("key", keyValPair[0] + "",
                    Field.Store.NO);
            document.add(textField);
            StoredField strField = new StoredField("position", keyValPair[1]);
            document.add(strField);
            return document;
        }
        return null;

    }

    private Document prepareDocument(Bag bag) {
        Document document = new Document();
        StoredField strField = new StoredField("id", bag.getId() + "");
        document.add(strField);
        StoredField functionId = new StoredField("functionId",
                bag.getFunctionId() + "");
        document.add(functionId);
        StoredField sizeField = new StoredField("size", bag.getSize() + "");
        document.add(sizeField);
        String tokenString = "";
        int ct = BlockInfo.getMinimumSimilarityThreshold(bag.getSize(),
                SearchManager.th);
        StoredField computedThresholdField = new StoredField("ct", ct + "");
        int lct = BlockInfo.getMinimumSimilarityThreshold(bag.getSize(),
                (SearchManager.th - 0.5f));
        StoredField lenientComputedThresholdField = new StoredField("lct", lct
                + "");
        document.add(sizeField);
        document.add(computedThresholdField);
        document.add(lenientComputedThresholdField);
        int prefixLength = BlockInfo.getPrefixSize(bag.getSize(), ct);
        System.out.println("inverted index: creating tokenString");
        for (TokenFrequency tf : bag) {
            for (int i = 0; i < tf.getFrequency(); i++) {
                tokenString += tf.getToken().getValue() + " ";
                // System.out.println(tf.getToken().getValue());
            }
            prefixLength -= tf.getFrequency();
            if (prefixLength <= 0) {
                break;
            }
        }
        System.out.println("inverted index: tokenString created");
        @SuppressWarnings("deprecation")
        Field field = new Field("tokens", tokenString.trim(), Field.Store.NO,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS);
        document.add(field);
        System.out.println("inverted index: document added");
        return document;
    }

    public void closeIndexWriter() {
        try {
            this.indexWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    public void setIndexWriter(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

}
