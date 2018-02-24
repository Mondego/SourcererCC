package com.mondego.models;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;

public class CandidateSearcher implements IListener, Runnable {
    private Block queryBlock;
    private QueryCandidates qc;
    private static final Logger logger = LogManager.getLogger(CandidateSearcher.class);
    Set<Long> earlierDocs;
    
    

    public CandidateSearcher(Block queryBlock) {
        this.queryBlock = queryBlock;
        this.qc = new QueryCandidates(this.queryBlock);
        //this.earlierDocs = new HashSet<Long>();
    }

    @Override
    public void run() {
        try {
            this.searchCandidates();
        } catch (NoSuchElementException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("EXCEPTION CAUGHT::", e);
            e.printStackTrace();
        }
    }

    private void searchCandidates()
            throws IOException, InterruptedException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        long startTime = System.nanoTime();
        this.search(this.queryBlock.actionTokenFrequencySet,Block.ACTION_TOKENS);
        //this.search(this.queryBlock.stopwordActionTokenFrequencySet,Block.STOPWORD_ACTION_TOKENS);
        //this.search(this.queryBlock.methodNameActionTokenFrequencySet,Block.METHODNAME_ACTION_TOKENS);
        if (this.qc.simMap.size() > 0) {
            long estimatedTime = System.nanoTime() - startTime;
            logger.debug(SearchManager.NODE_PREFIX + " CandidateSearcher, QueryBlock " + this.queryBlock + " in "
                    + estimatedTime / 1000 + " micros");
            SearchManager.queryCandidatesQueue.send(this.qc);
        }
    }

    private void search(Set<TokenFrequency> tokenFrequencies, int whichTokens) {
        //Map<Long, CandidateSimInfo> simMap = new HashMap<Long, CandidateSimInfo>();
        for (TokenFrequency tokenFrequency : tokenFrequencies) {
            String searchTerm = tokenFrequency.getToken().getValue();
            int searchTermFreq = tokenFrequency.getFrequency();
            Map<Long, Integer> candidateDocsandFreq = SearchManager.invertedIndex.get(searchTerm);
            if (null != candidateDocsandFreq) {
                //logger.info("preparing simInfo for Candidates of searchTerm: "+ searchTerm);
                for (Entry<Long, Integer> docAndTermFreq : candidateDocsandFreq.entrySet()) {
                    CandidateSimInfo simInfo = null;
                    Block candidateDoc = SearchManager.documentsForII.get(docAndTermFreq.getKey());
                    if (null!=candidateDoc){
                        if (this.qc.simMap.containsKey(docAndTermFreq.getKey())) {
                            simInfo = this.qc.simMap.get(docAndTermFreq.getKey());
                            if (whichTokens == Block.ACTION_TOKENS){
                                simInfo.actionTokenSimilarity = simInfo.actionTokenSimilarity + Math.min(searchTermFreq, docAndTermFreq.getValue());
                            }else if(whichTokens==Block.STOPWORD_ACTION_TOKENS){
                                simInfo.stopwordActionTokenSimilarity = simInfo.stopwordActionTokenSimilarity + Math.min(searchTermFreq, docAndTermFreq.getValue());
                            }else{
                                simInfo.methodNameActionTokenSimilarity = simInfo.methodNameActionTokenSimilarity + Math.min(searchTermFreq, docAndTermFreq.getValue());
                            }
                            simInfo.totalActionTokenSimilarity = simInfo.stopwordActionTokenSimilarity+simInfo.actionTokenSimilarity+simInfo.methodNameActionTokenSimilarity;
                        } else {
                            /*if (this.earlierDocs.contains(candidateDoc.id)) {
                                continue;
                            }*/
                            if (candidateDoc.id >= this.queryBlock.id) {
                                //this.earlierDocs.add(candidateDoc.id);
                                continue; // we reject the candidate
                            }
                            simInfo = new CandidateSimInfo();
                            simInfo.doc = candidateDoc;
                            if (whichTokens == Block.ACTION_TOKENS){
                                try{
                                    simInfo.actionTokenSimilarity = Math.min(searchTermFreq, docAndTermFreq.getValue());
                                }catch(Exception e){
                                    logger.warn(simInfo.toString());
                                    logger.warn("docAndTermFreq: "+ docAndTermFreq);
                                }
                            }else if(whichTokens==Block.STOPWORD_ACTION_TOKENS){
                                simInfo.stopwordActionTokenSimilarity = Math.min(searchTermFreq, docAndTermFreq.getValue());
                            }else{
                                simInfo.methodNameActionTokenSimilarity = Math.min(searchTermFreq, docAndTermFreq.getValue());
                            }
                            simInfo.totalActionTokenSimilarity = simInfo.stopwordActionTokenSimilarity+simInfo.actionTokenSimilarity+simInfo.methodNameActionTokenSimilarity;
                            this.qc.simMap.put(docAndTermFreq.getKey(), simInfo);
                        }
                    }else{
                        logger.warn("candidate is null, docAndTermFreq is: "+ docAndTermFreq+ ", searchTerm is: "+ searchTerm);
                    }
                    
                }
            } else {
                // logger.debug("no docs found for searchTerm: " + searchTerm);
            }
        }
    }
}
