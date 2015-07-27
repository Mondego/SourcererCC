/**
 * 
 */
package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.Bag;
import models.CandidateSimInfo;
import models.QueryBlock;
import models.TokenInfo;
import noindex.CloneHelper;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

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
	private static final String QUERY_DIR_PATH = "input/query/";
	public static final String DATASET_DIR2 = "input/dataset";
	private QueryBlock previousQueryBlock;
	private Writer clonesWriter; // writer to write the output
	public static float th; // args[2]
	private boolean isPrefixMode; // whether to do a prefix search or a normal
									// search
	private final static String ACTION_INDEX = "index";
	private final static String ACTION_SEARCH = "search";
	private CodeIndexer indexer;
	private long timeSpentInProcessResult;
	private long timeSpentInSearchingCandidates;
	private long timeIndexing;
	private long timeGlobalTokenPositionCreation;
	private long timeSearch;
	private long numCandidates;
	private Writer outputWriter;
	private long timeTotal;
	private String action;
	private boolean appendToExistingFile;
	TestGson testGson;
	private Writer cloneGroupWriter;
	private Writer cloneSiblingCountWriter;
	private int cloneSiblingCount;
	private Set<String> cloneSet;
	public static final Integer MUL_FACTOR = 100;
	int deletemeCounter = 0;
	private double ramBufferSizeMB;
	private int mergeFactor;
	private long bagsSortTime;

	public SearchManager(boolean mode) throws IOException {
		this.clonePairsCount = 0;
		this.isPrefixMode = mode;
		this.cloneHelper = new CloneHelper();
		this.timeSpentInProcessResult = 0;
		this.timeSpentInSearchingCandidates = 0;
		this.timeIndexing = 0;
		this.timeGlobalTokenPositionCreation = 0;
		this.timeSearch = 0;
		this.numCandidates = 0;
		this.timeTotal = 0;
		this.appendToExistingFile = true;
		this.cloneSiblingCount = 0;
		this.cloneSet = new HashSet<String>();
		this.ramBufferSizeMB = 1024 * 1;
		this.mergeFactor = 1000;
		this.bagsSortTime = 0;
	}

	public static void main(String[] args) throws IOException, ParseException {
		// set filePrefix
		// TODO: have two modes of execution. 1) detect all clones in a dataset,
		// 2) detect clones for a given query. here query can be independent of
		// the dataset
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
				searchManager.initIndexEnv();
				long begin_time = System.currentTimeMillis();
				searchManager.doIndex();
				searchManager.timeIndexing = System.currentTimeMillis()
						- begin_time;
			} else if (action.equalsIgnoreCase(ACTION_SEARCH)) {
				searchManager.initSearchEnv();
				long timeStartSearch = System.currentTimeMillis();
				searchManager.doSearch();
				searchManager.timeSearch = System.currentTimeMillis()
						- timeStartSearch;
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

	private void initIndexEnv() throws IOException, ParseException {
		TermSorter termSorter = new TermSorter();
		long timeGlobalPositionStart = System.currentTimeMillis();
		try {
			FileUtils.deleteDirectory(new File(Util.GTPM_DIR));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Util.createDirs(Util.GTPM_DIR);
		termSorter.populateGlobalPositionMap();
		this.timeGlobalTokenPositionCreation = System.currentTimeMillis()
				- timeGlobalPositionStart;
	}

	private void genReport() {
		String header = "";
		if (!this.appendToExistingFile) {
			header = "index_time, "
					+ "globalTokenPositionCreationTime,num_candidates, "
					+ "num_clonePairs, total_run_time, searchTime,"
					+ "timeSpentInSearchingCandidates,timeSpentInProcessResult,"
					+ "isPrefixmode,operation,sortTime_during_indexing\n";
		}
		header += this.timeIndexing + ",";
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
			header += this.bagsSortTime;
		} else {
			header += this.action;
		}

		Util.writeToFile(this.outputWriter, header, true);
	}

	private void doIndex() throws IOException, ParseException {

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
		// indexWriterConfig.setMergePolicy(mergePolicy);
		indexWriterConfig.setRAMBufferSizeMB(this.ramBufferSizeMB);
		TieredMergePolicy fwdMergePolicy = (TieredMergePolicy) fwdIndexWriterConfig
				.getMergePolicy();
		fwdIndexWriterConfig.setOpenMode(OpenMode.CREATE);
		fwdIndexWriterConfig.setRAMBufferSizeMB(this.ramBufferSizeMB);
		// fwdIndexWriterConfig.setMergePolicy(mergePolicy);
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
			fwdIndexer = new CodeIndexer(Util.FWD_INDEX_DIR, fwdIndexWriter,
					cloneHelper, this.isPrefixMode, this.th);
			File datasetDir = new File(SearchManager.DATASET_DIR2);

			if (datasetDir.isDirectory()) {
				System.out.println("Directory: " + datasetDir.getName());
				BufferedReader br = null;
				for (File inputFile : datasetDir.listFiles()) {
					try {
						br = new BufferedReader(new InputStreamReader(
								new FileInputStream(inputFile), "UTF-8"));
						String line;
						while ((line = br.readLine()) != null
								&& line.trim().length() > 0) {
							Bag bag = cloneHelper.deserialise(line);
							long startTime = System.currentTimeMillis();
							Util.sortBag(bag);
							this.bagsSortTime += System.currentTimeMillis()
									- startTime;
							fwdIndexer.fwdIndexCodeBlock(bag);
							this.indexer.indexCodeBlock(bag);
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
					// fwdIndexer.createFwdIndex(inputFile);
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
			this.indexer.closeIndexWriter();
		}
	}

	private void doSearch() {
		try {
			File queryDirectory = this.getQueryDirectory();
			File[] queryFiles = this.getQueryFiles(queryDirectory);
			for (File queryFile : queryFiles) {
				System.out.println("Query File: " + queryFile);
				String filename = queryFile.getName().replaceFirst("[.][^.]+$",
						"");
				try {

					this.clonesWriter = Util.openFile("output" + this.th
							/ this.MUL_FACTOR + "/" + filename
							+ "clones_index_WITH_FILTER.txt", false);
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
							// System.out.println("query prefix map size "+
							// queryBlock.getPrefixMapSize());
							/*
							 * System.out.println("Prefix map: ");
							 * System.out.println(queryBlock.getPrefixMap());
							 * 
							 * System.out.println("suffix map: ");
							 * System.out.println(queryBlock.getSuffixMap());
							 * 
							 * System.out.println("query size");
							 * System.out.println(queryBlock.getSize());
							 * 
							 * System.out.println("prefix size");
							 * System.out.println(queryBlock.getPrefixSize());
							 */

							this.cloneSiblingCount = 0;
							TermSearcher termSearcher = new TermSearcher();
							this.searcher.setTermSearcher(termSearcher);
							long searchCandidatesTimeStart = System
									.currentTimeMillis();
							this.searcher
									.search(queryBlock);
							this.timeSpentInSearchingCandidates += System
									.currentTimeMillis()
									- searchCandidatesTimeStart;
							long processResultTimeStart = System
									.currentTimeMillis();
							this.processResultWithFilter(termSearcher,
									queryBlock);
							this.timeSpentInProcessResult += System
									.currentTimeMillis()
									- processResultTimeStart;

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

				long timeGlobalPositionStart = System.currentTimeMillis();
				termSorter.populateGlobalPositionMap();
				this.timeGlobalTokenPositionCreation = System
						.currentTimeMillis() - timeGlobalPositionStart;
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
			QueryBlock queryBlock) {
		long numClonesFound = 0;
		Map<Long, CandidateSimInfo> codeBlockIds = result.getSimMap();
		this.numCandidates += codeBlockIds.size();
		// int prefixSize = this.getPrefixSize(bag);
		for (Entry<Long, CandidateSimInfo> entry : codeBlockIds.entrySet()) {
			Document doc = null;
			try {
				doc = this.searcher.getDocument(entry.getKey());
				CandidateSimInfo simInfo = entry.getValue();
				long candidateId = Long.parseLong(doc.get("id"));
				long functionIdCandidate = Long
						.parseLong(doc.get("functionId"));
				if ((candidateId <= queryBlock.getId())
					 || (functionIdCandidate == queryBlock.getFunctionId())) {
					continue; // we reject the candidate
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
							int similarity = -1;
							if (newCt != -1) {
								similarity = this.updateSimilarity(queryBlock,
										tokens, newCt, cadidateSize, simInfo);
							} else {
								similarity = this.updateSimilarity(queryBlock,
										tokens,
										queryBlock.getComputedThreshold(),
										cadidateSize, simInfo);
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

	private int updateSimilarity(QueryBlock queryBlock, String tokens,
			int computedThreshold, int candidateSize, CandidateSimInfo simInfo) {
		int tokensSeenInCandidate = 0;
		int similarity = simInfo.similarity;
		// if(candidateId == 614){
		try {
			for (String tokenfreqFrame : tokens.split("::")) {
				String[] tokenFreqInfo = tokenfreqFrame.split(":");
				/*
				 * System.out.println("tokenfreqinfo array "+ tokenFreqInfo[0] +
				 * " : " + tokenFreqInfo[1] + ": sim: "+ similarity);
				 * System.out.println("query size: "+ queryBlock.getSize() +
				 * ", tokensSeenInQueryBlock: "+ simInfo.queryMatchPosition);
				 * System.out.println("candidateSize: "+ candidateSize +
				 * ", tokensSeenInCandidate: "+ tokensSeenInCandidate);
				 * System.out.println("computedThreshold: "+ computedThreshold);
				 */
				if (Util.isSatisfyPosFilter(similarity, queryBlock.getSize(),
						simInfo.queryMatchPosition, candidateSize,
						simInfo.candidateMatchPosition, computedThreshold)) {
					// System.out.println("sim: "+ similarity);
					int candidatesTokenFreq = Integer
							.parseInt(tokenFreqInfo[1]);
					tokensSeenInCandidate += candidatesTokenFreq;
					if (tokensSeenInCandidate > simInfo.candidateMatchPosition) {
						TokenInfo tokenInfo = null;
						boolean matchFound = false;
						if (simInfo.queryMatchPosition < queryBlock
								.getPrefixMapSize()) {
							// check in prefix
							if (queryBlock.getPrefixMap().containsKey(
									tokenFreqInfo[0])) {
								matchFound= true;
								tokenInfo = queryBlock.getPrefixMap().get(
										tokenFreqInfo[0]);
								similarity = updateSimilarityHelper(simInfo,
										tokenInfo, similarity,
										candidatesTokenFreq);
							}
						}
						// check in suffix
						if (!matchFound && queryBlock.getSuffixMap().containsKey(
								tokenFreqInfo[0])) {
							tokenInfo = queryBlock.getSuffixMap().get(
									tokenFreqInfo[0]);
							similarity = updateSimilarityHelper(simInfo,
									tokenInfo, similarity, candidatesTokenFreq);
						}
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
		// }
		return -1;
	}

	private int updateSimilarityHelper(CandidateSimInfo simInfo, TokenInfo tokenInfo,
			int similarity, int candidatesTokenFreq) {
		simInfo.queryMatchPosition = tokenInfo.getPosition();
		similarity += Math.min(tokenInfo.getFrequency(), candidatesTokenFreq);
		// System.out.println("similarity: "+ similarity);
		return similarity;
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
		List<Entry<String, TokenInfo>> listOfTokens = new ArrayList<Entry<String, TokenInfo>>();
		QueryBlock queryBlock = this.cloneHelper.deserialiseToQueryBlock(line,
				listOfTokens);

		Collections.sort(listOfTokens,
				new Comparator<Entry<String, TokenInfo>>() {
					public int compare(Entry<String, TokenInfo> tfFirst,
							Entry<String, TokenInfo> tfSecond) {
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
		return queryBlock;
	}
}
