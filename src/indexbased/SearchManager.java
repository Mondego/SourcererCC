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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.QueryBlock;
import noindex.CloneHelper;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utility.BlockInfo;
import utility.Util;
import validation.TestGson;

/**
 * @author vaibhavsaini
 * 
 */
public class SearchManager {
	private long clonePairsCount;
	public static CodeSearcher searcher;
	private CodeSearcher fwdSearcher;
	private CloneHelper cloneHelper;
	private final String QUERY_DIR_PATH = "input/query/";
	private QueryBlock previousQueryBlock;
	private PrintWriter clonesWriter; // writer to write the output
	private float th; // args[2]
	private boolean isPrefixMode; // whether to do a prefix search or a normal
									// search
	private final static String ACTION_INDEX = "index";
	private final static String ACTION_SEARCH = "search";
	private CodeIndexer indexer;
	private long timeSpentInProcessResult;
	private long timeSpentInSearchingCandidates;
	private long timeFwdIndex;
	private long timeInvertedIndex;
	private long timeGlobalTokenPositionCreation;
	private long timeSearch;
	private long numCandidates;
	private PrintWriter outputWriter;
	private long timeTotal;
	private String action;
	private boolean appendToExistingFile;
	TestGson testGson;
	private PrintWriter cloneGroupWriter;
	private PrintWriter cloneSiblingCountWriter;
	private int cloneSiblingCount;
	private Set<String> cloneSet;
	public static final Integer MUL_FACTOR = 100;
	int deletemeCounter = 0;
	private double ramBufferSizeMB;
	private int mergeFactor;

	public SearchManager(boolean mode) throws IOException {
		this.clonePairsCount = 0;
		this.isPrefixMode = mode;
		this.cloneHelper = new CloneHelper();
		this.timeSpentInProcessResult = 0;
		this.timeSpentInSearchingCandidates = 0;
		this.timeFwdIndex = 0;
		this.timeInvertedIndex = 0;
		this.timeGlobalTokenPositionCreation = 0;
		this.timeSearch = 0;
		this.numCandidates = 0;
		this.timeTotal = 0;
		this.appendToExistingFile = true;
		this.cloneSiblingCount = 0;
		this.cloneSet = new HashSet<String>();
		this.ramBufferSizeMB = 1024 * 1;
		this.mergeFactor = 1000;
	}

	public static void main(String[] args) throws IOException, ParseException {
		// set filePrefix
		if (args.length >= 3) {
			long start_time = System.currentTimeMillis();
			String action = args[0];
			SearchManager searchManager = null;
			searchManager = new SearchManager(Boolean.parseBoolean(args[1]));
			searchManager.th = (Float.parseFloat(args[2]) * searchManager.MUL_FACTOR);
			searchManager.action = action;
			Util.createDirs("output" + searchManager.th
					/ searchManager.MUL_FACTOR);
			String reportFileName = "output" + searchManager.th
					/ searchManager.MUL_FACTOR + "/report.csv";
			File reportFile = new File(reportFileName);
			if (reportFile.exists()) {
				searchManager.appendToExistingFile = true;
			} else {
				searchManager.appendToExistingFile = false;
			}
			searchManager.outputWriter = Util.openFile(reportFileName,
					searchManager.appendToExistingFile);
			if (action.equalsIgnoreCase(ACTION_INDEX)) {

				searchManager.doIndex();
			} else if (action.equalsIgnoreCase(ACTION_SEARCH)) {
				long timeStartSearch = System.currentTimeMillis();
				searchManager.doSearch();
				searchManager.timeSearch = System.currentTimeMillis()
						- timeStartSearch;
				System.out.println("deletemeCounter "
						+ searchManager.deletemeCounter);
			}
			long end_time = System.currentTimeMillis();
			System.out.println("total run time in milliseconds:"
					+ (end_time - start_time));
			System.out.println("Search Candidates time: "
					+ searchManager.timeSpentInSearchingCandidates);
			System.out.println("Process Result  time: "
					+ searchManager.timeSpentInProcessResult);
			System.out.println("number of clone pairs detected: "
					+ searchManager.clonePairsCount);
			searchManager.timeTotal = end_time - start_time;
			searchManager.genReport();
			Util.closeOutputFile(searchManager.outputWriter);
			try {
				Util.closeOutputFile(searchManager.cloneGroupWriter);
			} catch (Exception e) {
				System.out
						.println("exception caught in main " + e.getMessage());
				// ignore.
			}

		} else {
			System.out
					.println("Please provide all 3 command line arguments, exiting now.");
			System.exit(1);
		}
	}

	private void genReport() {
		String header = "";
		if (!this.appendToExistingFile) {
			header = "fwd_index_time, inverted_index_time, "
					+ "globalTokenPositionCreationTime,num_candidates, "
					+ "num_clonePairs, total_run_time, searchTime,"
					+ "timeSpentInSearchingCandidates,timeSpentInProcessResult,"
					+ "isPrefixmode,operation,sortTime_during_indexing\n";
		}
		header += this.timeFwdIndex + ",";
		header += this.timeInvertedIndex + ",";
		header += this.timeGlobalTokenPositionCreation + ",";
		header += this.numCandidates + ",";
		header += this.clonePairsCount + ",";
		header += this.timeTotal + ",";
		header += this.timeSearch + ",";
		header += this.timeSpentInSearchingCandidates + ",";
		header += this.timeSpentInProcessResult + ",";
		header += this.isPrefixMode + ",";
		if (this.action.equalsIgnoreCase("index")) {
			header += this.action + ",";
			header += this.indexer.bagsSortTime;
		} else {
			header += this.action;
		}

		Util.writeToFile(this.outputWriter, header, true);
	}

	private void doIndex() throws IOException, ParseException {

		if (this.isPrefixMode) {
			TermSorter termSorter = new TermSorter();
			long timeGlobalPositionStart = System.currentTimeMillis();
			termSorter.populateGlobalPositionMap();
			this.timeGlobalTokenPositionCreation = System.currentTimeMillis()
					- timeGlobalPositionStart;
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
			TieredMergePolicy mergePolicy = (TieredMergePolicy) indexWriterConfig
					.getMergePolicy();

			mergePolicy.setNoCFSRatio(0);// what was this for?
			mergePolicy.setMaxCFSSegmentSizeMB(0); // what was this for?
			System.out.println("getSegmentsPerTier: "
					+ mergePolicy.getSegmentsPerTier());
			mergePolicy
					.setSegmentsPerTier(mergePolicy.getSegmentsPerTier() * .2);
			//indexWriterConfig.setMergePolicy(mergePolicy);
			indexWriterConfig.setRAMBufferSizeMB(this.ramBufferSizeMB);
			TieredMergePolicy fwdMergePolicy = (TieredMergePolicy) fwdIndexWriterConfig.getMergePolicy();
			fwdMergePolicy.setSegmentsPerTier(fwdMergePolicy.getSegmentsPerTier()*.2);
			fwdIndexWriterConfig.setOpenMode(OpenMode.CREATE);
			fwdIndexWriterConfig.setRAMBufferSizeMB(this.ramBufferSizeMB);
			//fwdIndexWriterConfig.setMergePolicy(mergePolicy);
			IndexWriter indexWriter;
			IndexWriter fwdIndexWriter = null;
			CodeIndexer fwdIndexer = null;
			try {
				indexWriter = new IndexWriter(FSDirectory.open(new File(
						Util.INDEX_DIR)), indexWriterConfig);
				this.indexer = new CodeIndexer(Util.INDEX_DIR, indexWriter,
						cloneHelper, this.isPrefixMode, this.th);
				fwdIndexWriter = new IndexWriter(FSDirectory.open(new File(
						Util.FWD_INDEX_DIR)), fwdIndexWriterConfig);
				fwdIndexer = new CodeIndexer(Util.FWD_INDEX_DIR,
						fwdIndexWriter, cloneHelper, this.isPrefixMode, this.th);
				File datasetDir = new File(CodeIndexer.DATASET_DIR2);

				if (datasetDir.isDirectory()) {
					long fwdIndexTimeStart = System.currentTimeMillis();
					System.out.println("Directory: " + datasetDir.getName());
					for (File inputFile : datasetDir.listFiles()) {
						fwdIndexer.createFwdIndex(inputFile);
					}
					this.timeFwdIndex = System.currentTimeMillis()
							- fwdIndexTimeStart;
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
				this.indexer = new CodeIndexer(this.isPrefixMode, this.th);
			} catch (IOException e) {
				System.out.println(e.getMessage() + ", exiting now");
			}
		}
		long invertedIndexTimeStart = System.currentTimeMillis();
		this.index(this.indexer);
		this.timeInvertedIndex = System.currentTimeMillis()
				- invertedIndexTimeStart;
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
								/ this.MUL_FACTOR + "/" + filename
								+ "clones_index_WITH_FILTER.txt", false);
					} else {
						this.clonesWriter = Util.openFile("output" + this.th
								/ this.MUL_FACTOR + "/" + filename
								+ "clones_index_NO_FILTER.txt", false);
					}
				} catch (IOException e) {
					System.out.println(e.getMessage() + " exiting");
					System.exit(1);
				}
				BufferedReader br = this.getReader(queryFile);
				String line = null;
				try {
					QueryBlock queryBlock = null;
					while ((line = br.readLine()) != null
							&& line.trim().length() > 0) {
						try {
							queryBlock = this.getNextQueryBlock(line);// todo
							this.cloneSiblingCount = 0;
							// add
							// filter
							// support
							BlockInfo queryBlockInfo = new BlockInfo(this.th,
									queryBlock);
							int computedThreshold = queryBlockInfo
									.getComputedThreshold();
							if (this.isPrefixMode) {
								int prefixSize = queryBlockInfo.getPrefixSize();
								/*
								 * CustomCollector result =
								 * this.searcher.search( queryBlock,
								 * prefixSize);
								 * this.processReultWithFilter(result,
								 * queryBlock, computedThreshold);
								 */
								// System.out.println("+++++++");
								TermSearcher termSearcher = new TermSearcher();
								this.searcher.setTermSearcher(termSearcher);
								long searchCandidatesTimeStart = System
										.currentTimeMillis();
								this.searcher.search2(queryBlock, prefixSize,
										computedThreshold);
								this.timeSpentInSearchingCandidates += System
										.currentTimeMillis()
										- searchCandidatesTimeStart;
								long processResultTimeStart = System
										.currentTimeMillis();
								this.processResultWithFilter(termSearcher,
										queryBlock, computedThreshold);
								this.timeSpentInProcessResult += System
										.currentTimeMillis()
										- processResultTimeStart;
							} else {
								/*
								 * CustomCollector result = this.searcher
								 * .search(queryBlock);
								 * this.processResult(result, queryBlock);
								 */
								TermSearcher termSearcher = new TermSearcher();
								this.searcher.setTermSearcher(termSearcher);
								long searchCandidatesTimeStart = System
										.currentTimeMillis();
								this.searcher.search2(queryBlock);
								this.timeSpentInSearchingCandidates += System
										.currentTimeMillis()
										- searchCandidatesTimeStart;
								long processResultTimeStart = System
										.currentTimeMillis();
								this.processReult(termSearcher,
										computedThreshold, queryBlock);
								this.timeSpentInProcessResult += System
										.currentTimeMillis()
										- processResultTimeStart;
							}

						} catch (ParseException e) {
							System.out.println(e.getMessage()
									+ " skiping to next bag");
							e.printStackTrace();
						}
						String siblingCount = queryBlock.getId() + ", "
								+ this.cloneSiblingCount;
						Util.writeToFile(this.cloneSiblingCountWriter,
								siblingCount, true);
					}
				} catch (IOException e) {
					System.out
							.println(e.getMessage() + " skiping to next file");
				}
				Util.closeOutputFile(this.clonesWriter);
				Util.closeOutputFile(this.cloneSiblingCountWriter);
			}
			/*
			 * System.out.println("unique clone pairs : " +
			 * this.cloneSet.size()); List<String> cloneList = new
			 * ArrayList<String>(this.cloneSet); Collections.sort(cloneList);
			 * for (String clonePair : cloneList) {
			 * System.out.println(clonePair); }
			 */
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage() + "exiting");
			System.exit(1);
		}
	}

	private void processReult(TermSearcher termSearcher, int computedThreshold,
			QueryBlock queryBlock) {

		Map<Long, Integer> codeBlockIds = termSearcher.getSimMap();
		this.numCandidates += codeBlockIds.size();
		for (Entry<Long, Integer> entry : codeBlockIds.entrySet()) {
			Document doc = null;
			try {
				doc = this.searcher.getDocument(entry.getKey());
				/*
				 * if (doc.get("id").equals(queryBlock.getId() + "")) {
				 * continue; }
				 */
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
		// testGson = new TestGson(); // remove this line later. for
		// validation only.
		// testGson.populateMap(); // this is for validation only, remove this
		// line.
		Util.createDirs("output" + this.th / this.MUL_FACTOR + "/cloneGroups/");
		try {
			this.cloneSiblingCountWriter = Util.openFile("output" + this.th
					/ this.MUL_FACTOR + "/cloneGroups/siblings_count.csv",
					false);
			Util.writeToFile(this.cloneSiblingCountWriter,
					"query_block_id,siblings", true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (this.isPrefixMode) {
			TermSorter termSorter = new TermSorter();
			try {
				termSorter.populateGlobalPositionMap();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				System.out.println("Error in Parsing: " + e.getMessage());
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
		this.numCandidates += codeBlockIds.size();
		// int prefixSize = this.getPrefixSize(bag);
		for (Entry<Long, Integer> entry : codeBlockIds.entrySet()) {
			Document doc = null;
			try {
				doc = this.searcher.getDocument(entry.getKey());
				long candidateId = Long.parseLong(doc.get("id"));
				if (candidateId <= queryBlock.getId()) {
					continue;
				}
				int newCt = -1;
				int cadidateSize = Integer.parseInt(doc.get("size"));
				if (cadidateSize > queryBlock.getSize()) {
					this.deletemeCounter++;

					newCt = Integer.parseInt(doc.get("ct"));
					/* System.out.println("stored ct: " + computedThreshold); */
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
							long similarity = -1;
							if (newCt != -1) {
								similarity = this.updateSimilarity(queryBlock,
										tokens, newCt, cadidateSize);
							} else {
								similarity = this
										.updateSimilarity(queryBlock, tokens,
												computedThreshold, cadidateSize);
							}
							if (similarity > 0) {
								// this is a clone.
								/*
								 * long blockId = Long.parseLong(doc.get("id"));
								 * String clonePair = ""; if (blockId <
								 * queryBlock.getId()) { clonePair = blockId +
								 * "::" + queryBlock.getId(); } else { clonePair
								 * = queryBlock.getId() + "::" + blockId; }
								 * this.cloneSet.add(clonePair);
								 */
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

	private long updateSimilarity(QueryBlock queryBlock, String tokens,
			int computedThreshold, int candidateSize) {
		// long similarity = entry.getValue();
		int similarity = 0;
		int tokensSeenInqueryBlock = 0;
		int tokensSeenInCandidate = 0;
		try {
			for (String tokenfreqFrame : tokens.split("::")) {
				String[] tokenFreqInfo = tokenfreqFrame.split(":");
				if (Util.isSatisfyPosFilter(similarity, queryBlock.getSize(),
						tokensSeenInqueryBlock, candidateSize,
						tokensSeenInCandidate, computedThreshold)) {
					if (queryBlock.containsKey(tokenFreqInfo[0])) {
						similarity += Math.min(
								queryBlock.get(tokenFreqInfo[0]),
								Integer.parseInt(tokenFreqInfo[1]));

						if (similarity >= computedThreshold) {
							return similarity;
						}
					}
				} else {
					break;
				}

			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("possible error in the format. tokens: "
					+ tokens);
		}
		return -1;
	}

	private void reportClone(QueryBlock queryBlock, long idB,
			QueryBlock previousQueryBlock) {
		this.clonePairsCount += 1;
		// System.out.println("reporting " + idB);
		if (null != previousQueryBlock
				&& queryBlock.getId() == previousQueryBlock.getId()) {
			this.cloneSiblingCount++;
			// System.out.println("equal");
			// Util.writeToFile(this.cloneGroupWriter,
			// this.testGson.idToCodeMap.get(idB+""), true);
			// Util.writeToFile(this.cloneGroupWriter,
			// "===================================================", true);
			Util.writeToFile(this.clonesWriter, " ," + idB, false);
		} else {
			// start a new line
			// System.out.println("different");
			try {
				Util.closeOutputFile(this.cloneGroupWriter);
			} catch (Exception e) {
				// ignore
				System.out.println("exception caught " + e.getMessage());
			}
			// try {
			// // Util.createDirs("output" + this.th+ "/cloneGroups/");
			// // this.cloneGroupWriter = Util.openFile("output" + this.th
			// // + "/cloneGroups/" + queryBlock.getId() + ".txt", false);
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			this.cloneSiblingCount++;
			// Util.writeToFile(this.cloneGroupWriter,
			// this.testGson.idToCodeMap.get(queryBlock.getId()+""), true);
			// Util.writeToFile(this.cloneGroupWriter,
			// "===================================================", true);
			// Util.writeToFile(this.cloneGroupWriter,
			// this.testGson.idToCodeMap.get(idB+""), true);
			// Util.writeToFile(this.cloneGroupWriter,
			// "===================================================", true);
			Util.writeToFile(this.clonesWriter, "", true);

			Util.writeToFile(this.clonesWriter, "Clones of Code Block "
					+ queryBlock.getId(), true);
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
					long position1 = 0;
					try {
						position1 = TermSorter.globalTokenPositionMap
								.get(tfFirst.getKey());
					} catch (Exception e) {
						position1 = -1;
					}
					long position2 = 0;
					try {
						position2 = TermSorter.globalTokenPositionMap
								.get(tfSecond.getKey());
					} catch (Exception e) {
						position2 = -1;
					}
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
