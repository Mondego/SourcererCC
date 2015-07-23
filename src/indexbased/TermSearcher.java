/**
 * 
 */
package indexbased;

import java.util.HashMap;
import java.util.Map;

import models.CandidateSimInfo;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;

import utility.Util;

/**
 * @author vaibhavsaini
 * 
 */
public class TermSearcher {
	private String searchTerm;
	private int freqTerm;
	private IndexReader reader;
	private Map<Long, CandidateSimInfo> simMap;
	private int querySize;
	private int computedThreshold;

	public TermSearcher() {
		this.simMap = new HashMap<Long, CandidateSimInfo>();
	}

	public void searchWithPosition(int queryTermsSeen) {
		for (AtomicReaderContext ctx : this.reader.getContext().leaves()) {
			int base = ctx.docBase;
			Term term = new Term("tokens", this.searchTerm);
			// SpanTermQuery spanQ = new SpanTermQuery(term);
			try {
				DocsAndPositionsEnum docEnum = MultiFields
						.getTermPositionsEnum(ctx.reader(),
								MultiFields.getLiveDocs(ctx.reader()),
								"tokens", term.bytes());
				if (null != docEnum) {
					int doc = DocsEnum.NO_MORE_DOCS;
					while ((doc = docEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
						long docId = doc + base;
						CandidateSimInfo simInfo = null;
						if (this.simMap.containsKey(docId)) {
							simInfo = this.simMap.get(docId);
							simInfo.similarity = simInfo.similarity
									+ Math.min(freqTerm, docEnum.freq());

						} else {
							simInfo = new CandidateSimInfo();
							simInfo.similarity = Math.min(freqTerm,
									docEnum.freq());
							this.simMap.put(docId, simInfo);
						}
						simInfo.queryMatchPosition = queryTermsSeen;
						int candidatePos = docEnum.nextPosition();
						simInfo.candidateMatchPosition = candidatePos + docEnum.freq();
						if(simInfo.candidateSize==0){
							simInfo.candidateSize = Integer
									.parseInt(SearchManager.searcher.getDocument(
											docId).get("size"));
						}
						if (!Util.isSatisfyPosFilter(this.simMap.get(docId).similarity,
								this.querySize, queryTermsSeen, simInfo.candidateSize,
								simInfo.candidateMatchPosition, this.computedThreshold)) {
							this.simMap.remove(docId);
						}
					}
				}
			} catch (Exception e) {
				System.out.println("exception caught" + e.getMessage());
			}

		}

	}

	/**
	 * @return the searchTerm
	 */
	public String getSearchTerm() {
		return searchTerm;
	}

	/**
	 * @param searchTerm
	 *            the searchTerm to set
	 */
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	/**
	 * @return the freqTerm
	 */
	public int getFreqTerm() {
		return freqTerm;
	}

	/**
	 * @param freqTerm
	 *            the freqTerm to set
	 */
	public void setFreqTerm(int freqTerm) {
		this.freqTerm = freqTerm;
	}

	/**
	 * @return the reader
	 */
	public IndexReader getReader() {
		return reader;
	}

	/**
	 * @param reader
	 *            the reader to set
	 */
	public void setReader(IndexReader reader) {
		this.reader = reader;
	}

	/**
	 * @return the simMap
	 */
	public Map<Long, CandidateSimInfo> getSimMap() {
		return simMap;
	}

	/**
	 * @param simMap
	 *            the simMap to set
	 */
	public void setSimMap(Map<Long, CandidateSimInfo> simMap) {
		this.simMap = simMap;
	}

	public void setQuerySize(int size) {
		this.querySize = size;

	}

	public void setComputedThreshold(int computedThreshold) {
		this.computedThreshold = computedThreshold;

	}

}
