/**
 * 
 */
package com.mondego.indexbased;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import com.mondego.models.Bag;
import com.mondego.models.Block;
import com.mondego.models.DocumentForInvertedIndex;
import com.mondego.models.TokenFrequency;
import com.mondego.utility.BlockInfo;

/**
 * @author vaibhavsaini
 * 
 */
public class DocumentMaker {
    private IndexWriter indexWriter;

    /**
     * @param indexDir
     * @param indexWriter
     * @param cloneHelper
     * @param isAppendIndex
     * @param isPrefixIndex
     */
    private static final Logger logger = LogManager.getLogger(DocumentMaker.class);
    public DocumentMaker(IndexWriter indexWriter) {
        super();
        this.indexWriter = indexWriter;
    }
    
    public DocumentMaker(){
        super();
    }

    /**
     * index the code block
     */
/*    public void indexCodeBlock(Bag bag) {
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
    }*/

/*    public void fwdIndexCodeBlock(Bag bag) {
        Document document = this.prepareDocumentForFwdIndex(bag);
        try {
            this.indexWriter.addDocument(document);
        } catch (IOException e) {
            System.out
                    .println("EXCEPTION caught while indexing document for bag "
                            + bag.getId());
            e.printStackTrace();
        }
    }*/

    private Document wfmEntry;
    private StringField wordField;
    private StoredField freqField;
    public void indexWFMEntry(String word, long frequency) {
	// Create the document and fields only once, for no GC
	if (wfmEntry == null) {
	    wfmEntry = new Document();
	    wordField = new StringField("key", word,
					       Field.Store.NO);
	    wfmEntry.add(wordField);
	    freqField = new StoredField("frequency", frequency);
	    wfmEntry.add(freqField);
	}
	else {
	    wordField.setStringValue(word);
	    freqField.setLongValue(frequency);
	}

        try {
	    this.indexWriter.updateDocument(new Term("key", word), wfmEntry);
        } catch (IOException e) {
            logger.error("EXCEPTION caught while indexing document for wfm entry "
                            + word + ":" + frequency);
            e.printStackTrace();
        }
    }

    public Document prepareDocumentForFwdIndex(Bag bag) {
        Document document = new Document();
        StringField idField = new StringField("id", bag.getId() + "",
                Field.Store.NO);
        //idField.fieldType().setIndexed(true);
        //idField.fieldType().freeze();
        document.add(idField);
        
        StringBuilder tokenString = new StringBuilder();
        for (TokenFrequency tf : bag) {
            // System.out.println(tf.getToken().getValue() +
            // ":"+tf.getFrequency());
            tokenString.append(tf.getToken().getValue() + ":" + tf.getFrequency() + "::");
        }
        StoredField strField = new StoredField("tokens", tokenString.toString().trim());
        document.add(strField);
        return document;
    }

    public DocumentForInvertedIndex prepareDocumentForII(Block block) {
        DocumentForInvertedIndex document = new DocumentForInvertedIndex();
        document.id = SearchManager.getNextId();
        document.fId = block.getId();
        document.pId = block.getFunctionId();
        document.size = block.getSize();
        //document.ct = BlockInfo.getMinimumSimilarityThreshold(bag.getSize(),
         //       SearchManager.th);
        //document.prefixSize = BlockInfo.getPrefixSize(bag.getSize(), document.ct);
        return document;
    }

    public void closeIndexWriter() {
        try {
            this.indexWriter.close();
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }

    }

    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    public void setIndexWriter(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

}
