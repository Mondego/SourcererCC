/**
 * 
 */
package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.QueryBlock;
import noindex.CloneHelper;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utility.Util;

/**
 * @author vaibhavsaini
 * 
 */
public class SearchManager {
    private CodeSearcher searcher;
    private CodeSearcher fwdSearcher;
    private CloneHelper cloneHelper;
    private final String QUERY_DIR_PATH = "input/query/";
    private float threshold;
    private QueryBlock previousQueryBlock;
    private PrintWriter clonesWriter; // writer to write the output
    private String th; // args[2]
    private boolean isPrefixMode; // whether to do a prefix search or a normal
                                  // search
    private final static String ACTION_INDEX = "index";
    private final static String ACTION_SEARCH = "search";
    private CodeIndexer indexer;

    public SearchManager(boolean mode) {
        this.isPrefixMode = mode;
        this.cloneHelper = new CloneHelper();
    }

    public static void main(String[] args) throws IOException, ParseException {
        // set filePrefix
        if (args.length >= 3) {
            long start_time = System.currentTimeMillis();
            String action = args[0];
            SearchManager searchManager = null;
            searchManager = new SearchManager(Boolean.parseBoolean(args[1]));
            searchManager.threshold = Float.parseFloat(args[2]) / 10;
            searchManager.th = args[2];
            if (action.equalsIgnoreCase(ACTION_INDEX)) {
                searchManager.doIndex();
            } else if (action.equalsIgnoreCase(ACTION_SEARCH)) {
                Util.createDirs("output" + searchManager.th);
                searchManager.doSearch();
            }
            long end_time = System.currentTimeMillis();
            System.out.println("total run time in milliseconds:"
                    + (end_time - start_time));
        } else {
            System.out
                    .println("Please provide all 3 command line arguments, exiting now.");
            System.exit(1);
        }
    }

    private void doIndex() throws IOException, ParseException {

        if (this.isPrefixMode) {
            TermSorter termSorter = new TermSorter();
            termSorter.populateGlobalPositionMap();
            KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
            WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(
                    Version.LUCENE_46);
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
                    Version.LUCENE_46, whitespaceAnalyzer);
            IndexWriterConfig fwdIndexWriterConfig = new IndexWriterConfig(
                    Version.LUCENE_46, keywordAnalyzer);
            indexWriterConfig.setOpenMode(OpenMode.CREATE);// add new
                                                           // docs to
                                                           // exisiting
                                                           // index
            MergePolicy mergePolicy = indexWriterConfig.getMergePolicy();
            mergePolicy.setNoCFSRatio(0);// what was this for?
            mergePolicy.setMaxCFSSegmentSizeMB(0); // what was this for?
            indexWriterConfig.setMergePolicy(mergePolicy);
            fwdIndexWriterConfig.setOpenMode(OpenMode.CREATE);
            IndexWriter indexWriter;
            IndexWriter fwdIndexWriter = null;
            CodeIndexer fwdIndexer = null;
            try {
                indexWriter = new IndexWriter(FSDirectory.open(new File(
                        Util.INDEX_DIR)), indexWriterConfig);
                this.indexer = new CodeIndexer(Util.INDEX_DIR, indexWriter,
                        cloneHelper, this.isPrefixMode, this.threshold);
                fwdIndexWriter = new IndexWriter(FSDirectory.open(new File(
                        Util.FWD_INDEX_DIR)), fwdIndexWriterConfig);
                fwdIndexer = new CodeIndexer(Util.FWD_INDEX_DIR,
                        fwdIndexWriter, cloneHelper, this.isPrefixMode,
                        this.threshold);
                File datasetDir = new File(CodeIndexer.DATASET_DIR2);
                if (datasetDir.isDirectory()) {
                    System.out.println("Directory: " + datasetDir.getName());
                    for (File inputFile : datasetDir.listFiles()) {
                        fwdIndexer.createFwdIndex(inputFile);
                    }
                } else {
                    System.out.println("File: " + datasetDir.getName()
                            + " is not a direcory. exiting now");
                    System.exit(1);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage() + ", exiting now.");
                System.exit(1);
            } finally {
                fwdIndexer.closeIndexWriter();
            }
        } else {
            try {
                this.indexer = new CodeIndexer(this.isPrefixMode,
                        this.threshold);
            } catch (IOException e) {
                System.out.println(e.getMessage() + ", exiting now");
            }
        }
        this.index(this.indexer);
        this.indexer.closeIndexWriter();
    }

    private void index(CodeIndexer indexer) {
        File datasetDir = new File(CodeIndexer.DATASET_DIR2);
        if (datasetDir.isDirectory()) {
            System.out.println("Directory: " + datasetDir.getName());
            for (File inputFile : datasetDir.listFiles()) {
                indexer.indexCodeBlocks(inputFile);
            }
        } else {
            System.out.println("File: " + datasetDir.getName()
                    + " is not a direcory. exiting now");
            System.exit(1);
        }
    }

    private void doSearch() {
        this.initSearchEnv();
        try {

            File queryDirectory = this.getQueryDirectory();
            File[] queryFiles = this.getQueryFiles(queryDirectory);
            for (File queryFile : queryFiles) {
                System.out.println("Query File: " + queryFile);
                String filename = queryFile.getName().replaceFirst("[.][^.]+$",
                        "");
                try {
                    if (this.isPrefixMode) {

                        this.clonesWriter = Util.openFile("output" + this.th
                                + "/" + filename
                                + "clones_index_WITH_FILTER.txt", false);
                    } else {
                        this.clonesWriter = Util.openFile(
                                "output" + this.th + "/" + filename
                                        + "clones_index_NO_FILTER.txt", false);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage() + " exiting");
                    System.exit(1);
                }
                BufferedReader br = this.getReader(queryFile);
                String line = null;
                try {
                    while ((line = br.readLine()) != null
                            && line.trim().length() > 0) {
                        try {
                            QueryBlock queryBlock = this
                                    .getNextQueryBlock(line);// todo
                            // add
                            // filter
                            // support
                            int computedThreshold = Util
                                    .getMinimumSimilarityThreshold(queryBlock,
                                            this.threshold);
                            if (this.isPrefixMode) {
                                int prefixSize = Util.getPrefixSize(queryBlock,
                                        this.threshold);
                                /*
                                 * CustomCollector result =
                                 * this.searcher.search( queryBlock,
                                 * prefixSize);
                                 * this.processReultWithFilter(result,
                                 * queryBlock, computedThreshold);
                                 */
                                System.out.println("+++++++");
                                TermSearcher termSearcher = new TermSearcher();
                                this.searcher.setTermSearcher(termSearcher);
                                this.searcher.search2(queryBlock, prefixSize);
                                this.processResultWithFilter(termSearcher,
                                        queryBlock, computedThreshold);
                            } else {
                                /*
                                 * CustomCollector result = this.searcher
                                 * .search(queryBlock);
                                 * this.processResult(result, queryBlock);
                                 */
                                TermSearcher termSearcher = new TermSearcher();
                                this.searcher.setTermSearcher(termSearcher);
                                this.searcher.search2(queryBlock);
                                this.processReult(termSearcher,
                                        computedThreshold, queryBlock);
                            }

                        } catch (ParseException e) {
                            System.out.println(e.getMessage()
                                    + " skiping to next bag");
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    System.out
                            .println(e.getMessage() + " skiping to next file");
                }
                Util.closeOutputFile(this.clonesWriter);
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage() + "exiting");
            System.exit(1);
        }
    }

    private void processReult(TermSearcher termSearcher, int computedThreshold,
            QueryBlock queryBlock) {
        long numClonesFound = 0;
        Map<Long, Integer> codeBlockIds = termSearcher.getSimMap();
        for (Entry<Long, Integer> entry : codeBlockIds.entrySet()) {
            Document doc = null;
            try {
                doc = this.searcher.getDocument(entry.getKey());
                if (doc.get("id").equals(queryBlock.getId() + "")) {
                    continue;
                }
                if (Integer.parseInt(doc.get("size")) > queryBlock.getSize()) {
                    // reject this
                    continue;
                }
                if (entry.getValue() >= computedThreshold) {
                    this.reportClone(queryBlock, Long.parseLong(doc.get("id")),
                            this.previousQueryBlock);
                    this.previousQueryBlock = queryBlock;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage()
                        + ", can't find document from searcher"
                        + entry.getKey());
            }
        }
    }

    private void initSearchEnv() {
        if (this.isPrefixMode) {
            TermSorter termSorter = new TermSorter();
            try {
                termSorter.populateGlobalPositionMap();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.fwdSearcher = new CodeSearcher(true); // searches on fwd index
            this.searcher = new CodeSearcher(Util.INDEX_DIR);
        } else {
            this.searcher = new CodeSearcher(Util.INDEX_DIR_NO_FILTER);
        }

    }

    /*
     * private long processResult(CustomCollector result, QueryBlock queryBlock)
     * { long numClonesFound = 0; Map<Integer, Long> codeBlockIds =
     * result.getCodeBlockIds(); int similarityThreshold =
     * Util.getMinimumSimilarityThreshold( queryBlock, this.threshold); for
     * (Entry<Integer, Long> entry : codeBlockIds.entrySet()) { if
     * (entry.getValue() >= similarityThreshold) { long idB; Document doc =
     * null; try { doc = this.searcher.getDocument(entry.getKey()); idB =
     * Long.parseLong(doc.get("id")); this.reportClone(queryBlock, idB,
     * this.previousQueryBlock); numClonesFound += 1; this.previousQueryBlock =
     * queryBlock; } catch (NumberFormatException e) {
     * System.out.println(e.getMessage() + ", cant parse id for " +
     * doc.get("id")); } catch (IOException e) {
     * System.out.println(e.getMessage() + ", can't find document from searcher"
     * + entry.getKey()); }
     * 
     * } } return numClonesFound;
     * 
     * }
     */
    /*
     * private long processReultWithFilter(CustomCollector result, QueryBlock
     * queryBlock, int computedThreshold) { long numClonesFound = 0;
     * Map<Integer, Long> codeBlockIds = result.getCodeBlockIds(); // int
     * prefixSize = this.getPrefixSize(bag); for (Entry<Integer, Long> entry :
     * codeBlockIds.entrySet()) { Document doc = null; try { doc =
     * this.searcher.getDocument(entry.getKey()); CustomCollectorFwdIndex
     * collector = this.fwdSearcher .search(doc); List<Integer> blocks =
     * collector.getBlocks(); if (!blocks.isEmpty()) { if (blocks.size() == 1) {
     * Document document = this.fwdSearcher.getDocument(blocks .get(0)); String
     * tokens = document.get("tokens"); if (tokens != null &&
     * tokens.trim().length() > 0) { long similarity =
     * this.updateSimilarity(queryBlock, entry, tokens, computedThreshold); if
     * (similarity > 0) { // this is a clone. this.reportClone(queryBlock,
     * Integer.parseInt(doc.get("id")), this.previousQueryBlock); numClonesFound
     * += 1; this.previousQueryBlock = queryBlock; } } else {
     * System.out.println("tokens not found for document"); } // TODO: get the
     * tokens from this document. } else { System.out
     * .println("ERROR: more that one doc found. some error here."); }
     * 
     * } else { System.out.println("document not found in fwd index"); } } catch
     * (NumberFormatException e) { System.out.println(e.getMessage() +
     * ", cant parse id for " + doc.get("id")); } catch (IOException e) {
     * System.out.println(e.getMessage() + ", can't find document from searcher"
     * + entry.getKey()); } } return numClonesFound; }
     */

    private long processResultWithFilter(TermSearcher result,
            QueryBlock queryBlock, int computedThreshold) {
        long numClonesFound = 0;
        Map<Long, Integer> codeBlockIds = result.getSimMap();
        // int prefixSize = this.getPrefixSize(bag);
        for (Entry<Long, Integer> entry : codeBlockIds.entrySet()) {
            Document doc = null;
            try {
                doc = this.searcher.getDocument(entry.getKey());
                if (doc.get("id").equals(queryBlock.getId() + "")) {
                    continue;
                }
                if (Integer.parseInt(doc.get("size")) > queryBlock.getSize()) {
                    continue;
                }
                CustomCollectorFwdIndex collector = this.fwdSearcher
                        .search(doc);
                List<Integer> blocks = collector.getBlocks();
                if (!blocks.isEmpty()) {
                    if (blocks.size() == 1) {
                        Document document = this.fwdSearcher.getDocument(blocks
                                .get(0));
                        String tokens = document.get("tokens");
                        if (tokens != null && tokens.trim().length() > 0) {
                            long similarity = this.updateSimilarity(queryBlock,
                                    entry, tokens, computedThreshold,
                                    doc.get("id"));
                            if (similarity > 0) {
                                // this is a clone.
                                this.reportClone(queryBlock,
                                        Integer.parseInt(doc.get("id")),
                                        this.previousQueryBlock);
                                numClonesFound += 1;
                                this.previousQueryBlock = queryBlock;
                            }
                        } else {
                            System.out.println("tokens not found for document");
                        }
                        // TODO: get the tokens from this document.
                    } else {
                        System.out
                                .println("ERROR: more that one doc found. some error here.");
                    }

                } else {
                    System.out.println("document not found in fwd index");
                }
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage() + ", cant parse id for "
                        + doc.get("id"));
            } catch (IOException e) {
                System.out.println(e.getMessage()
                        + ", can't find document from searcher"
                        + entry.getKey());
            }
        }
        return numClonesFound;
    }

    private long updateSimilarity(QueryBlock queryBlock,
            Entry<Long, Integer> entry, String tokens, int computedThreshold,
            String idCandidate) {
        long similarity = entry.getValue();
        for (String tokenfreqFrame : tokens.split("::")) {
            String[] tokenFreqInfo = tokenfreqFrame.split(":");

            if (queryBlock.containsKey(tokenFreqInfo[0])) {
                similarity += Math.min(queryBlock.get(tokenFreqInfo[0]),
                        Integer.parseInt(tokenFreqInfo[1]));
                if (similarity >= computedThreshold) {
                    return similarity;
                }
            }
        }
        return -1;
    }

    private void reportClone(QueryBlock queryBlock, long idB,
            QueryBlock previousQueryBlock) {
        System.out.println("reporting " + idB);
        if (null != previousQueryBlock
                && queryBlock.getId() == previousQueryBlock.getId()) {
            // System.out.println("equal");
            Util.writeToFile(this.clonesWriter, " ," + idB, false);
        } else {
            // start a new line
            // System.out.println("different");
            Util.writeToFile(this.clonesWriter, "", true);
            Util.writeToFile(this.clonesWriter,
                    "Clones of Bag " + queryBlock.getId(), true);
            Util.writeToFile(this.clonesWriter, idB + "", false);
        }
    }

    private BufferedReader getReader(File queryFile)
            throws FileNotFoundException {
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(queryFile));
        return br;
    }

    private File getQueryDirectory() throws FileNotFoundException {
        File queryDir = new File(QUERY_DIR_PATH);
        if (!queryDir.isDirectory()) {
            throw new FileNotFoundException("directory not found.");
        } else {
            System.out.println("Directory: " + queryDir.getName());
            return queryDir;
        }
    }

    private File[] getQueryFiles(File queryDirectory) {
        return queryDirectory.listFiles();
    }

    private QueryBlock getNextQueryBlock(String line) throws ParseException {
        QueryBlock queryBlock = this.cloneHelper.deserialiseToQueryBlock(line);
        if (this.isPrefixMode) {
            // sort the queryBlock
            List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>();
            for (Entry<String, Integer> entry : queryBlock.entrySet()) {
                list.add(entry);
            }
            Collections.sort(list, new Comparator<Entry<String, Integer>>() {
                public int compare(Entry<String, Integer> tfFirst,
                        Entry<String, Integer> tfSecond) {
                    if (!TermSorter.globalTokenPositionMap.containsKey(tfFirst
                            .getKey())
                            || !TermSorter.globalTokenPositionMap
                                    .containsKey(tfSecond.getKey())) {
                        System.out.println("term not found in globalTokenPositionMap");
                    }
                    long position1 = TermSorter.globalTokenPositionMap
                            .get(tfFirst.getKey());
                    long position2 = TermSorter.globalTokenPositionMap
                            .get(tfSecond.getKey());
                    if (position1 - position2 != 0) {
                        return (int) (position1 - position2);
                    } else {
                        return 1;
                    }
                }
            });
            queryBlock.clear();
            for (Entry<String, Integer> entry : list) {
                queryBlock.put(entry.getKey(), entry.getValue());
            }
        }
        return queryBlock;
    }
}
