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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import models.Bag;
import noindex.CloneHelper;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utility.Util;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;

/**
 * for every project's input file (one file is one project) read all lines for
 * each line create a Bag. for each project create one output file, this file
 * will have all the tokens, in the bag.
 * 
 * @author vaibhavsaini
 * 
 */
public class TermSorter {
	private CloneHelper cloneHelper;
	public static Map<String, Long> wordFreq;
	public static String SORTED_FILES_DIR = "output/sortedFiles";
	public static Map<String, Long> globalTokenPositionMap = new HashMap<String, Long>();;

	public TermSorter() {
		TermSorter.wordFreq = new HashMap<String, Long>();
		Util.createDirs(SORTED_FILES_DIR);
		this.cloneHelper = new CloneHelper();

	}

	public static void main(String[] args) throws IOException, ParseException {
		TermSorter externalSort = new TermSorter();
		// externalSort.populateGlobalPositionMap();
	}

	public void populateLocalWordFreqMap() throws IOException, ParseException {

		File wfmFile = new File(SearchManager.GTPM_DIR_PATH
				+ "/wordFreqMap.wfm");
		if (wfmFile.exists()) {
			System.out.println("wfm file exists, not creating a new one");
			/*
			 * TermSorter.globalTokenPositionMap = Util
			 * .readMapFromFile(SearchManager.GTPM_DIR_PATH+"/gtpm.json");
			 * System.out.println("search size of GTPM: " +
			 * TermSorter.globalTokenPositionMap.size());
			 */
		} else {
			System.out
					.println("wfm file doesn't exist. Creating WFM from query File");
			File queryDir = new File(SearchManager.QUERY_DIR_PATH);
			if (queryDir.isDirectory()) {
				System.out.println("Directory: " + queryDir.getName());
				for (File inputFile : queryDir.listFiles()) {
					this.populateWordFreqMap(inputFile);
				}
				Util.createDirs(SearchManager.GTPM_DIR_PATH);
				Util.writeMapToFile(SearchManager.GTPM_DIR_PATH
						+ "/wordFreqMap.wfm", TermSorter.wordFreq);
			} else {
				System.out.println("File: " + queryDir.getName()
						+ " is not a direcory. exiting now");
				System.exit(1);
			}
		}
	}

	public void populateGlobalPositionMap() {
		File gtpmFile = new File(Util.GTPM_DIR + "/gtpm.json");
		if (gtpmFile.exists()) {
			System.out.println("GTPM file exists, reading from file");
			this.indexGPTM(gtpmFile);
			/*
			TermSorter.globalTokenPositionMap = Util
					.readMapFromFile(Util.GTPM_DIR + "/gtpm.json");
			System.out.println("search size of GTPM: "
					+ TermSorter.globalTokenPositionMap.size());*/
			return;
		} else {
			System.out
					.println("GTPM files doesn't exist. reading from wfm files");
			File currentDir = new File(System.getProperty("user.dir"));

			this.populateGlobalWordFreqMapIttrative(currentDir);
			System.out.println("sorting globalWordFreqMap to creat GTPM");

			Map<String, Long> sortedMap = ImmutableSortedMap
					.copyOf(SearchManager.globalWordFreqMap,
							Ordering.natural()
									.onResultOf(
											Functions
													.forMap(SearchManager.globalWordFreqMap))
									.compound(Ordering.natural()));
			Long count = 1l;
			for (Entry<String, Long> entry : sortedMap.entrySet()) {
				TermSorter.globalTokenPositionMap.put(entry.getKey(), count);
				count++;
			}
			System.out.println("index size of GTPM: "
					+ TermSorter.globalTokenPositionMap.size());
			Util.createDirs(Util.GTPM_DIR);
			Util.writeMapToFile(Util.GTPM_DIR + "/gtpm.json",
					TermSorter.globalTokenPositionMap);
			this.indexGPTM(gtpmFile);
			TermSorter.globalTokenPositionMap=null;
			SearchManager.globalWordFreqMap = null;
			sortedMap = null;
		}
	}

	private void indexGPTM(File gtpmFile) {
		KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
		IndexWriterConfig gtpmIndexWriterConfig = new IndexWriterConfig(
				Version.LUCENE_46, keywordAnalyzer);
		TieredMergePolicy mergePolicy = (TieredMergePolicy) gtpmIndexWriterConfig
				.getMergePolicy();
		mergePolicy.setNoCFSRatio(0);// what was this for?
		mergePolicy.setMaxCFSSegmentSizeMB(0); // what was this for?
		gtpmIndexWriterConfig.setOpenMode(OpenMode.CREATE);
		gtpmIndexWriterConfig.setRAMBufferSizeMB(1024);
		IndexWriter gtpmIndexWriter = null;
		CodeIndexer gtpmIndexer = null;
		try {
			gtpmIndexWriter = new IndexWriter(FSDirectory.open(new File(
					Util.GTPM_INDEX_DIR)), gtpmIndexWriterConfig);
			gtpmIndexer = new CodeIndexer(Util.GTPM_INDEX_DIR, gtpmIndexWriter,
					cloneHelper, SearchManager.th);

			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(gtpmFile), "UTF-8"));
				String line;
				while ((line = br.readLine()) != null
						&& line.trim().length() > 0) {
					gtpmIndexer.indexGtpmEntry(line);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage() + ", exiting now.");
			System.exit(1);
		} finally {
			gtpmIndexer.closeIndexWriter();
		}
	}

	private void populateGlobalWordFreqMapIttrative(File root) {
		Writer processedWFMfilesWriter = null;
		String processedWFMFilename = "processedWFMFiles.txt";
		try {
			processedWFMfilesWriter = Util.openFile(processedWFMFilename, true);
		} catch (IOException e) {
			System.out.println("cant open processedWFMFiles.txt");
			e.printStackTrace();
			System.exit(1);
		}
		File tempgtpm = new File("temp_gwfm.txt");
		if (tempgtpm.exists()) {
			SearchManager.globalWordFreqMap = Util
					.readMapFromFile("temp_gwfm.txt");
		}
		System.out.println("current Dir: " + root.getName());
		Set<String> processedWFMset = new HashSet<String>();
		Util.populateProcessedWFMSet(processedWFMFilename, processedWFMset);
		System.out.println("size of populateProcessedWFMSet "
				+ processedWFMset.size());
		Stack<File> fileStack = new Stack<File>();
		fileStack.push(root);
		while (!fileStack.isEmpty()) {
			File[] files = fileStack.pop().listFiles();
			for (File currFile : files) {
				if (currFile.isFile()) {
					if (FilenameUtils.getExtension(currFile.getName()).equals(
							"wfm")) {
						if (processedWFMset
								.contains(currFile.getAbsolutePath())) {
							System.out.println("ignore wfm file, "
									+ currFile.getAbsolutePath());
							continue;
						}
						System.out
								.println("populating globalWordFreqMap, reading file: "
										+ currFile.getAbsolutePath());
						Map<String, Long> wordFreqMap = Util
								.readMapFromFile(currFile.getAbsolutePath());
						for (Entry<String, Long> entry : wordFreqMap.entrySet()) {

							long value = 0;
							if (SearchManager.globalWordFreqMap
									.containsKey(entry.getKey())) {
								value = SearchManager.globalWordFreqMap
										.get(entry.getKey()) + entry.getValue();
							} else {
								value = entry.getValue();
							}
							SearchManager.globalWordFreqMap.put(entry.getKey(),
									value);
						}
						Util.writeMapToFile("temp_gwfm.txt",
								SearchManager.globalWordFreqMap);
						System.out
								.println("writing to processedWFMfilesWriter");
						Util.writeToFile(processedWFMfilesWriter,
								currFile.getAbsolutePath(), true);
						try {
							processedWFMfilesWriter.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else if (currFile.isDirectory()) {
					if (currFile.getName().contains("NODE_")
							|| currFile.getName().contains("gtpm")) {
						fileStack.push(currFile);
					}
				}

			}
		}
		Util.closeOutputFile(processedWFMfilesWriter);
	}

	private void populateWordFreqMap(File file) throws IOException,
			ParseException {
		// System.out.println("Sorting file: " + file.getName());
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(file));
		String line;
		System.out.println("populating GPTM");
		long lineNumber = 0;
		while ((line = br.readLine()) != null && line.trim().length() > 0) {
			Bag bag = cloneHelper.deserialise(line);

			if (null != bag && bag.getSize() > SearchManager.min_tokens
					&& bag.getSize() < SearchManager.max_tokens) {
				cloneHelper.populateWordFreqMap(bag);
			} else {
				if (null == bag) {
					System.out.println("empty block, ignoring");
				} else {
					System.out
							.println("not adding tokens of line to GPTM, REASON: "
									+ bag.getFunctionId()
									+ ", "
									+ bag.getId()
									+ ", size: " + bag.getSize());
				}
			}
			lineNumber++;
			System.out.println(SearchManager.NODE_PREFIX
					+ " , GTPM line_number: " + lineNumber);

		}
		br.close();
	}

}
