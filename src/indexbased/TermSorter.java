package indexbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;

import models.Bag;
import noindex.CloneHelper;
import utility.Util;

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
			TermSorter.globalTokenPositionMap = Util
					.readMapFromFile(Util.GTPM_DIR + "/gtpm.json");
			System.out.println("search size of GTPM: "
					+ TermSorter.globalTokenPositionMap.size());
			return;
		} else {
			System.out
					.println("GTPM files doesn't exist. reading from wfm files");
			File currentDir = new File(System.getProperty("user.dir"));

			this.populateGlobalWordFreqMapReccursive(currentDir);
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
			SearchManager.globalWordFreqMap = null;
			sortedMap = null;
		}
	}

	private void populateGlobalWordFreqMapReccursive(File root) {
		System.out.println("current Dir: " + root.getName());
		File[] files = root.listFiles();
		for (File currFile : files) {
			if (currFile.isFile()) {
				if (FilenameUtils.getExtension(currFile.getName())
						.equals("wfm")) {
					System.out
							.println("populating globalWordFreqMap, reading file: "
									+ currFile.getAbsolutePath());
					Map<String, Long> wordFreqMap = Util
							.readMapFromFile(currFile.getAbsolutePath());
					for (Entry<String, Long> entry : wordFreqMap.entrySet()) {

						long value = 0;
						if (SearchManager.globalWordFreqMap.containsKey(entry
								.getKey())) {
							value = SearchManager.globalWordFreqMap.get(entry
									.getKey()) + entry.getValue();
						} else {
							value = entry.getValue();
						}
						SearchManager.globalWordFreqMap.put(entry.getKey(),
								value);
					}
				}
			} else if (currFile.isDirectory()) {
				if (currFile.getName().contains("NODE_")
						|| currFile.getName().contains("gtpm")) {
					this.populateGlobalWordFreqMapReccursive(currFile);
				}
			}
		}
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
			System.out.println(SearchManager.NODE_PREFIX +" , GTPM line_number: " + lineNumber);
			lineNumber++;
		}
		br.close();
	}

}
