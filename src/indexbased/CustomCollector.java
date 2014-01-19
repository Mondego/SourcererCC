/**
 * 
 */
package indexbased;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

/**
 * @author vaibhavsaini
 *
 */
public class CustomCollector extends Collector {
    private List<Integer> codeBlockIds;
    private int docBase;
    
    public CustomCollector(){
        this.codeBlockIds = new ArrayList<Integer>();
    }
    
    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }
    @Override
    public void collect(int doc) throws IOException {
        this.codeBlockIds.add(doc+docBase);
    }

    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        this.docBase = context.docBase;
        System.out.println(this.docBase);
    }

    @Override
    public void setScorer(Scorer arg0) throws IOException {
        // TODO Auto-generated method stub

    }
    /**
     * @return the codeBlockIds
     */
    public List<Integer> getCodeBlockIds() {
        return codeBlockIds;
    }
    /**
     * @param codeBlockIds the codeBlockIds to set
     */
    public void setCodeBlockIds(List<Integer> codeBlockIds) {
        this.codeBlockIds = codeBlockIds;
    }
}
