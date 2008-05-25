package edu.ucdavis.cs.dblp.data.keywords;

import java.util.List;
import java.util.Set;

import com.google.common.collect.BiMap;

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
	
	List<Keyword> removeLowInformationKeywords(Iterable<Keyword> keywords);
	
	/**
	 * Optional method to implement.  If implemented it will return a map from keywords
	 * that are acronyms to their expanded form.  Example: WWW &lt;-&gt; World Wide Web. 
	 * @return the acronym map (acronyms &lt;-&gt; expanded form)
	 */
	BiMap<String, String> getAcronymMap();
}
