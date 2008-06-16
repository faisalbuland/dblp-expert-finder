package edu.ucdavis.cs.dblp.data.keywords;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.aliasi.dict.Dictionary;
import com.google.common.collect.BiMap;

import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.text.SimplePub;

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
	
	Collection<Publication> removeLowInformationKeywords(Collection<Publication> pubs);
	
	List<Keyword> removeLowInformationKeywords(Iterable<Keyword> keywords);
	
	/**
	 * Optional method to implement.  If implemented it will return a map from keywords
	 * that are acronyms to their expanded form.  Example: WWW &lt;-&gt; World Wide Web. 
	 * @return the acronym map (acronyms &lt;-&gt; expanded form)
	 */
	BiMap<String, String> getAcronymMap();
	
	Dictionary<String> getKeywordDict();
	
	/**
	 * Produces a controlled vocabulary from <code>pubs</code> and updates
	 * each contained publication to have keywords only from this controlled vocabulary.
	 *  
	 * @param pubs
	 * @return
	 */
	Iterable<Publication> produceControlledVocabulary(Iterable<Publication> pubs);
	/**
	 * Produces a controlled vocabulary from <code>pubs</code> and updates
	 * each contained publication to have keywords only from this controlled vocabulary.
	 * This version operates on {@link SimplePub}s.
	 *  
	 * @param pubs
	 * @return
	 */
	Iterable<SimplePub> produceSimpleControlledVocabulary(Iterable<SimplePub> pubs);
}
