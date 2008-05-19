package edu.ucdavis.cs.dblp.data.keywords;

import java.util.List;
import java.util.Set;

import edu.ucdavis.cs.dblp.data.Keyword;

public interface KeywordRecognizer {
	Set<Keyword> findKeywordsIn(String text);
	/**
	 * Removes duplicates (stemmed form) and removes keywords that are contained within 
	 * other keywords, for example, 'spatial', 'spatial database', 'spatial databases' would 
	 * be reduced to 'spatial databases'.
	 * 
	 * @param keywords
	 * @return the reduced keywords
	 */
	List<String> reduceKeywords(List<String> keywords);
}
