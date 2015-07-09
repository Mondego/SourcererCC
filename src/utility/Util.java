/**
 * 
 */
package utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author vaibhavsaini
 * 
 */
public class Util {
	static Random rand = new Random(5);
	public static final String CSV_DELIMITER = "~";
	public static final String INDEX_DIR = "index";
	public static final String GTPM_DIR = "gtpm";
	public static final String FWD_INDEX_DIR = "fwdindex";
	public static final String INDEX_DIR_NO_FILTER = "index_nofilter";

	/**
	 * generates a random integer
	 * 
	 * @return
	 */
	public static int getRandomNumber(int max, int min) {
		return rand.nextInt((max - min) + 1) + min;
	}

	/**
	 * writes the given text to a file pointed by pWriter
	 * 
	 * @param pWriter
	 *            handle to printWriter to write to a file
	 * @param text
	 *            text to be written in the file
	 * @param isNewline
	 *            whether to start from a newline or not
	 */
	public static void writeToFile(Writer pWriter, final String text,
			final boolean isNewline) {
		if (isNewline) {
			try {
				pWriter.write(text + System.lineSeparator());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				pWriter.write(text);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * opens the outputfile for reporting clones
	 * 
	 * @param filename
	 * @throws IOException
	 * @return PrintWriter
	 */
	public static Writer openFile(String filename, boolean append)
			throws IOException {
		try {
			Writer pWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename, append), "UTF-8"));
			return pWriter;

		} catch (IOException e) {
			// IO exception caught
			System.err.println(e.getMessage());
			throw e;
		}
	}

	/**
	 * closes the outputfile
	 */
	public static void closeOutputFile(Writer pWriter) {
		if (null != pWriter) {
			try {
				pWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				pWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static boolean createDirs(String dirname) {
		File dir = new File(dirname);
		if (!dir.exists()) {
			return dir.mkdirs();
		} else {
			return true;
		}
	}

	public static boolean isSatisfyPosFilter(int similarity, int querySize,
			int termsSeenInQueryBlock, int candidateSize,
			int termsSeenInCandidate, int computedThreshold) {
		return computedThreshold <= similarity
				+ Math.min(querySize - termsSeenInQueryBlock, candidateSize
						- termsSeenInCandidate);
	}

	public static void writeJsonStream(String filename,
			Map<String, Integer> gtpm) {
		JsonWriter writer = null;
		try {
			writer = new JsonWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8"));
			Gson gson = new GsonBuilder().create();
			gson.toJson(gtpm, gtpm.getClass(), writer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Map<String,Integer> readJsonStream(String filename){
		JsonReader reader = null;
		Map<String,Integer> gtpm = new HashMap<String, Integer>();
		try {
			reader = new JsonReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			Gson gson = new GsonBuilder().create();
			gtpm =  gson.fromJson(reader, gtpm.getClass());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return gtpm;
	}
	/*
	 * public static int getMinimumSimilarityThreshold(QueryBlock
	 * queryBlock,float threshold) { return (int) Math.ceil((threshold *
	 * queryBlock.getSize())/ (SearchManager.MUL_FACTOR*10)); } public static
	 * int getMinimumSimilarityThreshold(Bag bag,float threshold) { return (int)
	 * Math.ceil((threshold * bag.getSize())/ (SearchManager.MUL_FACTOR*10)); }
	 */

	/*
	 * public static int getPrefixSize(QueryBlock queryBlock, float threshold) {
	 * int prefixSize = (queryBlock.getSize() + 1) - computedThreshold;//
	 * this.computePrefixSize(maxLength); return prefixSize; }
	 */
	/*
	 * public static int getPrefixSize(Bag bag, float threshold) { int
	 * computedThreshold = getMinimumSimilarityThreshold(bag, threshold); int
	 * prefixSize = (bag.getSize() + 1) - computedThreshold;//
	 * this.computePrefixSize(maxLength); return prefixSize; }
	 */
}
