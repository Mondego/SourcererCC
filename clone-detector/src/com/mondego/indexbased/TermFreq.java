/**
 * 
 */
package com.mondego.indexbased;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public class TermFreq {
    private String searchTerm;
    private IndexReader reader;
    private Map<String, Long> TermFreqMap;

    public TermFreq() {
        this.TermFreqMap = new HashMap<String, Long>();
    }

    private void dummy() throws IOException {
        Fields fields = MultiFields.getFields(this.reader);
        Terms terms = fields.terms("field");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef = null;
        while ((byteRef = iterator.next()) != null) {
            String term = new String(byteRef.bytes, byteRef.offset,
                    byteRef.length);
            Term termInstance = new Term("tokens", term);
            long termFreq = this.reader.totalTermFreq(termInstance);
            this.TermFreqMap.put(term, termFreq);
            System.out.println(termFreq);
        }
    }
    
    public static void main (String [] args) throws IOException{
        TermFreq freq = new TermFreq();
        freq.dummy();
    }
}
