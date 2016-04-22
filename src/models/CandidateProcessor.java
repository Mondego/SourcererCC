package models;

import java.io.IOException;
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

    @Override
    public void run() {
        try {
            // System.out.println( "QCQ size: "+
            // SearchManager.queryCandidatesQueue.size() + Util.debug_thread());
            QueryCandidates qc = SearchManager.queryCandidatesQueue.remove();
            this.processResultWithFilter(qc.termSearcher, qc.queryBlock);
        } catch (NoSuchElementException e) {
            // e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void processResultWithFilter(TermSearcher result,
            QueryBlock queryBlock) throws InterruptedException {
        // System.out.println("HERE, thread_id: " + Util.debug_thread() +
        // ", query_id "+ queryBlock.getId());
        long sstart_time = System.currentTimeMillis();
        Map<Long, CandidateSimInfo> codeBlockIds = result.getSimMap();
        if (SearchManager.isGenCandidateStats) {
            SearchManager.updateNumCandidates(codeBlockIds.size());
        }
        System.out.println(SearchManager.NODE_PREFIX + ", num candidates: "
                + codeBlockIds.entrySet().size() + ", query: "
                + queryBlock.getFunctionId() + "," + queryBlock.getId());
        for (Entry<Long, CandidateSimInfo> entry : codeBlockIds.entrySet()) {
            // long start_time = System.currentTimeMillis();
            Document doc = null;
            try {
                doc = SearchManager.searcher.getDocument(entry.getKey());
                CandidateSimInfo simInfo = entry.getValue();
                long candidateId = Long.parseLong(doc.get("id"));
                long functionIdCandidate = Long
                        .parseLong(doc.get("functionId"));

                if ((candidateId <= queryBlock.getId())) {
                    // || (functionIdCandidate == queryBlock.getFunctionId())) {
                    continue; // we reject the candidate
                }
                int newCt = -1;
                int candidateSize = Integer.parseInt(doc.get("size"));
                if (candidateSize < queryBlock.getComputedThreshold()
                        || candidateSize > queryBlock.getMaxCandidateSize()) {
                    continue; // ignore this candidate
                }
                if (candidateSize > queryBlock.getSize()) {
                    newCt = Integer.parseInt(doc.get("ct"));
                }
                CustomCollectorFwdIndex collector = SearchManager.fwdSearcher
                        .search(doc);
                List<Integer> blocks = collector.getBlocks();
                if (!blocks.isEmpty()) {
                    if (blocks.size() == 1) {
                        Document document = SearchManager.fwdSearcher
                                .getDocument(blocks.get(0));
                        String tokens = document.get("tokens");
                        CandidatePair candidatePair = null;
                        if (newCt != -1) {
                            candidatePair = new CandidatePair(queryBlock,
                                    tokens, simInfo, newCt, candidateSize,functionIdCandidate,
                                    candidateId);
                        } else {
                            candidatePair = new CandidatePair(queryBlock,
                                    tokens, simInfo,
                                    queryBlock.getComputedThreshold(),
                                    candidateSize, functionIdCandidate,candidateId);
                        }

                        /*
                         * long end_time = System.currentTimeMillis(); Duration
                         * duration; try { duration =
                         * DatatypeFactory.newInstance().newDuration( end_time -
                         * start_time);
                         * System.out.printf(SearchManager.NODE_PREFIX +
                         * ", candidates processed: " +
                         * candidatePair.candidateId + "query: " +
                         * candidatePair.queryBlock.getFunctionId() + "," +
                         * candidatePair.queryBlock.getId() +
                         * " time taken: %02dh:%02dm:%02ds", duration.getDays()
                         * 24 + duration.getHours(), duration.getMinutes(),
                         * duration.getSeconds()); start_time = end_time;
                         * System.out.println(); } catch
                         * (DatatypeConfigurationException e) {
                         * e.printStackTrace(); }
                         */
                        SearchManager.verifyCandidateQueue.put(candidatePair);
                        entry = null;
                    } else {
                        System.out
                                .println(SearchManager.NODE_PREFIX
                                        + "ERROR: more than one doc found. some error here."
                                        + "," + doc.get("functionId") + ", "
                                        + doc.get("id"));
                    }

                } else {
                    System.out.println(SearchManager.NODE_PREFIX
                            + ", document not found in fwd index" + ","
                            + doc.get("functionId") + ", " + doc.get("id"));
                }
            } catch (NumberFormatException e) {
                System.out.println(SearchManager.NODE_PREFIX + e.getMessage()
                        + ", cant parse id for " + doc.get("functionId") + ", "
                        + doc.get("id"));
            } catch (IOException e) {
                System.out.println(e.getMessage()
                        + ", can't find document from searcher"
                        + entry.getKey());
            }
        }
        /* result = null; */
        System.out.println("here");
        long eend_time = System.currentTimeMillis();
        Duration duration;
        try {
            duration = DatatypeFactory.newInstance().newDuration(
                    eend_time - sstart_time);
            System.out.printf(
                    SearchManager.NODE_PREFIX
                            + ", TOTAL candidates processed status: "
                            + queryBlock.getFunctionId() + ","
                            + queryBlock.getId()
                            + " time taken: %02dh:%02dm:%02ds",
                    duration.getDays() * 24 + duration.getHours(),
                    duration.getMinutes(), duration.getSeconds());
            System.out.println();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        System.out.println("there");
    }

}
