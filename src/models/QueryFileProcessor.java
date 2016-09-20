package models;

import indexbased.SearchManager;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class QueryFileProcessor implements ITokensFileProcessor {

    
    private SearchManager searchManager;
    
    public QueryFileProcessor(SearchManager searchManager) {
        this.searchManager = searchManager;
    }
    
    @Override
    public void processLine(String line) throws ParseException {
        // TODO Auto-generated method stub
        long startTime = System.nanoTime();
        try {
            QueryBlock queryBlock = this.getNextQueryBlock(line);
            if (searchManager.appendToExistingFile
                    && searchManager.completedQueries.contains(queryBlock
                            .getId())) {
                System.out
                        .println("ignoring query, REASON: completed in previous run, "
                                + queryBlock.getFunctionId()
                                + ", "
                                + queryBlock.getId()
                                + ", "
                                + queryBlock.getSize());
                return;
            }
            if (queryBlock.getSize() < SearchManager.min_tokens
                    || queryBlock.getSize() > SearchManager.max_tokens) {
                System.out.println("ignoring query, REASON:  "
                        + queryBlock);
                return; // ignore this query
            }
            if (SearchManager.isStatusCounterOn) {
                SearchManager.statusCounter += 1;
                if ((SearchManager.statusCounter % SearchManager.printAfterEveryXQueries) == 0) {
                    long estimatedTime = System.nanoTime() - startTime;
                    System.out.println(SearchManager.NODE_PREFIX + " QueryBlockProcessor, QueryBlock " + queryBlock + " in " + estimatedTime/1000 + " micros");
                }
            }
            SearchManager.queryBlockQueue.send(queryBlock);
            // System.out.println(SearchManager.NODE_PREFIX +
            // ", line number: "+ count);
        } catch (ParseException e) {
            System.out.println("catching parseException, dont worry");
            System.out.println(e.getMessage()
                    + " skiping this query block, parse exception: " + line);
            // e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()
                    + " skiping this query block, illegal args: " + line);
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public QueryBlock getNextQueryBlock(String line) throws ParseException,
            IllegalArgumentException {
        List<Entry<String, TokenInfo>> listOfTokens = new ArrayList<Entry<String, TokenInfo>>();
        QueryBlock queryBlock = searchManager.cloneHelper.getSortedQueryBlock(
                line, listOfTokens);
        if (queryBlock.getSize() >= SearchManager.min_tokens
                && queryBlock.getSize() <= SearchManager.max_tokens) {
            int position = 0;
            for (Entry<String, TokenInfo> entry : listOfTokens) {
                TokenInfo tokenInfo = entry.getValue();
                if (position < queryBlock.getPrefixSize()) {
                    queryBlock.getPrefixMap().put(entry.getKey(), tokenInfo);
                    position += tokenInfo.getFrequency();
                    queryBlock.setPrefixMapSize(position);
                } else {
                    queryBlock.getSuffixMap().put(entry.getKey(), tokenInfo);
                    position += tokenInfo.getFrequency();
                }
                tokenInfo.setPosition(position);
            }
        }
        return queryBlock;
    }
}
