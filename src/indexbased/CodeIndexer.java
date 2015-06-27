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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.Bag;
import models.TokenFrequency;
import noindex.CloneHelper;

import org.apache.lucene.analysis.Analyzer;
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
    private boolean isPrefixIndex;
    private float threshold;
    //public final static String DATASET_DIR = "input/dataset";
    public static String DATASET_DIR2 = "input/dummy";
    public long bagsSortTime;
    /**
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     */
    public CodeIndexer(boolean isPrefixIndex,float threshold) throws IOException {
        this.threshold = threshold;
        this.bagsSortTime=0;
        this.isPrefixIndex = isPrefixIndex;
        this.indexDir = Util.INDEX_DIR_NO_FILTER;
        this.cloneHelper = new CloneHelper();
        Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_46);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_46, analyzer);
        indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);// add new
                                                                 // docs to
                                                                 // exisiting
                                                                 // index
        this.indexWriter = new IndexWriter(FSDirectory.open(new File(
                this.indexDir)), indexWriterConfig);
    }

    /**
     * @param indexDir
     * @param indexWriter
     * @param cloneHelper
     * @param isAppendIndex
     * @param isPrefixIndex
     */
    public CodeIndexer(String indexDir, IndexWriter indexWriter,
            CloneHelper cloneHelper, boolean isPrefixIndex,float threshold) {
        super();
        this.bagsSortTime=0;
        this.threshold = threshold;
        this.indexDir = indexDir;
        this.indexWriter = indexWriter;
        this.cloneHelper = cloneHelper;
        this.isPrefixIndex = isPrefixIndex;
    }

    /**
     * iterate over all the code blocks and index them
     */
    public void indexCodeBlocks(File file) {
        BufferedReader br = null;
        System.out.println("Indexing file: " + file.getName());
        try {
            br = new BufferedReader(new FileReader(file));
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

    public void createFwdIndex(File file) {
        BufferedReader br = null;
        System.out.println("fwd Indexing file: " + file.getName());
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                this.fwdIndexCodeBlock(cloneHelper.deserialise(line));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * index the code block
     */
    private void indexCodeBlock(Bag bag) {
        Document document;
        if (this.isPrefixIndex) {
            long startTime = System.currentTimeMillis();
            this.sortBag(bag);
            this.bagsSortTime += System.currentTimeMillis()-startTime;
            document = this.prepareDocument(bag, this.isPrefixIndex);
        } else {
            document = this.prepareDocument(bag);
        }
        try {
            this.indexWriter.addDocument(document);
        } catch (IOException e) {
            System.out
                    .println("EXCEPTION caught while indexing document for bag "
                            + bag.getId());
            e.printStackTrace();
        }
    }
    
    private void sortBag(Bag bag){
        long bagId = bag.getId();
        List<TokenFrequency> bagAsList = new ArrayList<TokenFrequency>(bag);
        Collections.sort(bagAsList, new Comparator<TokenFrequency>() {
            public int compare(TokenFrequency tfFirst, TokenFrequency tfSecond) {
                long position1 = TermSorter.globalTokenPositionMap.get(tfFirst.getToken().getValue());
                long position2 = TermSorter.globalTokenPositionMap.get(tfSecond.getToken().getValue());
                if(position1-position2!=0){
                    return (int) (position1 - position2);
                }else{
                    return 1;
                }
            }
        });
        bag.clear();
        for(TokenFrequency tf : bagAsList){
            bag.add(tf);
        }
    }

    private void fwdIndexCodeBlock(Bag bag) {
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

    private Document prepareDocumentForFwdIndex(Bag bag) {
        Document document = new Document();
        TextField textField  = new TextField("id", bag.getId() + "",Field.Store.NO);
        document.add(textField);
        String tokenString = "";
        for (TokenFrequency tf : bag) {
            tokenString += tf.getToken().getValue() + ":"+tf.getFrequency()+"::";
        }
        StoredField strField = new StoredField("tokens", tokenString.trim());
        document.add(strField);
        return document;
    }

    private Document prepareDocument(Bag bag) {
        Document document = new Document();
        StoredField strField = new StoredField("id", bag.getId() + "");
        document.add(strField);
        StoredField sizeField = new StoredField("size", bag.getSize() + "");
        document.add(sizeField);
        String tokenString = "";
        for (TokenFrequency tf : bag) {
            for(int i=0;i<tf.getFrequency();i++){
                tokenString += tf.getToken().getValue() + " ";
            }
        }
        Field field = new Field("tokens",tokenString.trim(),Field.Store.NO,Field.Index.ANALYZED,Field.TermVector.YES);
        document.add(field);
        return document;
    }

    private Document prepareDocument(Bag bag, boolean isPrefixIndex) {
        Document document = new Document();
        StoredField strField = new StoredField("id", bag.getId() + "");
        document.add(strField);
        StoredField sizeField = new StoredField("size", bag.getSize() + "");
        document.add(sizeField);
        String tokenString = "";
        int prefixLength = Util.getPrefixSize(bag,this.threshold);
        for (TokenFrequency tf : bag) {
            for(int i=0;i<tf.getFrequency();i++){
                tokenString += tf.getToken().getValue() + " ";
                
            }
            prefixLength -=tf.getFrequency();
            if(prefixLength<=0){
                break;
            }
        }
        Field field = new Field("tokens",tokenString.trim(),Field.Store.NO,Field.Index.ANALYZED,Field.TermVector.WITH_POSITIONS);
        document.add(field);
        return document;
    }
    
    public void closeIndexWriter(){
        try{
            this.indexWriter.close();
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        
    }

}
