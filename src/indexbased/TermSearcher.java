/**
 * 
 */
package indexbased;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;

/**
 * @author vaibhavsaini
 * 
 */
public class TermSearcher {
    private String searchTerm;
    private int freqTerm;
    private IndexReader reader;
    private Map<Long, Integer> simMap;

    public TermSearcher() {
        this.simMap = new HashMap<Long, Integer>();
    }

    public void search() {
        for (AtomicReaderContext ctx : this.reader.getContext().leaves()) {
            int base = ctx.docBase;
            Term term = new Term("tokens", this.searchTerm);
            try {
                DocsEnum docEnum = MultiFields.getTermDocsEnum(ctx.reader(),
                        MultiFields.getLiveDocs(ctx.reader()), "tokens",
                        term.bytes());
                if (null != docEnum) {
                    int doc = DocsEnum.NO_MORE_DOCS;
                    while ((doc = docEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {

                        long docId = doc + base;
                        // System.out.println("docbase is: " + base + " doc: " +
                        // doc + " globDocId: "+docId);
                        if (this.simMap.containsKey(docId)) {
                            this.simMap.put(docId, this.simMap.get(docId)
                                    + Math.min(freqTerm, docEnum.freq()));
                        } else {
                            this.simMap.put(docId,
                                    Math.min(freqTerm, docEnum.freq()));
                        }
                    }
                } else {
                    System.out.println("term not found: " + this.searchTerm);
                }
            } catch (Exception e) {
                System.out.println("" + e.getMessage());
            }

        }

    }

    /**
     * @return the searchTerm
     */
    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * @param searchTerm
     *            the searchTerm to set
     */
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    /**
     * @return the freqTerm
     */
    public int getFreqTerm() {
        return freqTerm;
    }

    /**
     * @param freqTerm
     *            the freqTerm to set
     */
    public void setFreqTerm(int freqTerm) {
        this.freqTerm = freqTerm;
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
     * @return the simMap
     */
    public Map<Long, Integer> getSimMap() {
        return simMap;
    }

    /**
     * @param simMap
     *            the simMap to set
     */
    public void setSimMap(Map<Long, Integer> simMap) {
        this.simMap = simMap;
    }

}
