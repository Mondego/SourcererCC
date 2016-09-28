package models;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.lucene.document.Document;

import indexbased.CustomCollectorFwdIndex;
import indexbased.SearchManager;
import indexbased.TermSearcher;

public class CandidateProcessor implements IListener, Runnable {
    private QueryCandidates qc;

    public CandidateProcessor(QueryCandidates qc) {
        // TODO Auto-generated constructor stub
        this.qc = qc;
    }

    @Override
    public void run() {
        try {
            // System.out.println( "QCQ size: "+
            // SearchManager.queryCandidatesQueue.size() + Util.debug_thread());
            this.processResultWithFilter();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
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

    private void processResultWithFilter()
            throws InterruptedException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        // System.out.println("HERE, thread_id: " + Util.debug_thread() +
        // ", query_id "+ queryBlock.getId());
	TermSearcher result = this.qc.termSearcher;
	QueryBlock queryBlock = this.qc.queryBlock;
	int shard = queryBlock.getShardId();
	
        long sstart_time = System.currentTimeMillis();
        Map<Long, CandidateSimInfo> codeBlockIds = result.getSimMap();
        if (SearchManager.isGenCandidateStats) {
            SearchManager.updateNumCandidates(codeBlockIds.size());
        }
        System.out.println(SearchManager.NODE_PREFIX + ", num candidates: " + codeBlockIds.entrySet().size()
                + ", query: " + queryBlock.getFunctionId() + "," + queryBlock.getId());
        for (Entry<Long, CandidateSimInfo> entry : codeBlockIds.entrySet()) {
            long startTime = System.nanoTime();
            Document doc = null;
            try {
                doc = SearchManager.searcher.get(shard).getDocument(entry.getKey());
                CandidateSimInfo simInfo = entry.getValue();
                long candidateId = Long.parseLong(doc.get("id"));
                long functionIdCandidate = Long.parseLong(doc.get("functionId"));
                int newCt = -1;
                int candidateSize = Integer.parseInt(doc.get("size"));
                if (candidateSize < queryBlock.getComputedThreshold()
                        || candidateSize > queryBlock.getMaxCandidateSize()) {
                    continue; // ignore this candidate
                }
                if (candidateSize > queryBlock.getSize()) {
                    newCt = Integer.parseInt(doc.get("ct"));
                }
                CustomCollectorFwdIndex collector = SearchManager.fwdSearcher.get(shard).search(doc);
                List<Integer> blocks = collector.getBlocks();
                if (!blocks.isEmpty()) {
                    if (blocks.size() == 1) {
                        Document document = SearchManager.fwdSearcher.get(shard).getDocument(blocks.get(0));
                        String tokens = document.get("tokens");
                        CandidatePair candidatePair = null;
                        if (newCt != -1) {
                            candidatePair = new CandidatePair(queryBlock, tokens, simInfo, newCt, candidateSize,
                                    functionIdCandidate, candidateId);
                        } else {
                            candidatePair = new CandidatePair(queryBlock, tokens, simInfo,
                                    queryBlock.getComputedThreshold(), candidateSize, functionIdCandidate, candidateId);
                        }
                        long estimatedTime = System.nanoTime() - startTime;
                        //System.out.println(SearchManager.NODE_PREFIX + " CandidateProcessor, " + candidatePair + " in " + estimatedTime/1000 + " micros");
                        SearchManager.verifyCandidateQueue.send(candidatePair);
                        entry = null;
                    } else {
                        System.out
                                .println(SearchManager.NODE_PREFIX + "ERROR: more than one doc found. some error here."
                                        + "," + doc.get("functionId") + ", " + doc.get("id"));
                    }

                } else {
                    System.out.println(SearchManager.NODE_PREFIX + ", document not found in fwd index" + ","
                            + doc.get("functionId") + ", " + doc.get("id"));
                }
            } catch (NumberFormatException e) {
                System.out.println(SearchManager.NODE_PREFIX + e.getMessage() + ", cant parse id for "
                        + doc.get("functionId") + ", " + doc.get("id"));
            } catch (IOException e) {
                System.out.println(e.getMessage() + ", can't find document from searcher" + entry.getKey());
            }
        }
        /* result = null; */
        long eend_time = System.currentTimeMillis();
        Duration duration;
        try {
            duration = DatatypeFactory.newInstance().newDuration(eend_time - sstart_time);
            System.out.printf(
                    SearchManager.NODE_PREFIX + ", TOTAL candidates processed status: " + queryBlock.getFunctionId()
                            + "," + queryBlock.getId() + " time taken: %02dh:%02dm:%02ds",
                    duration.getDays() * 24 + duration.getHours(), duration.getMinutes(), duration.getSeconds());
            System.out.println();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }

}
