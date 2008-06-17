package edu.ucdavis.cs.dblp.experts;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Comparators;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.text.SimplePub;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * ResearcherProfile aggregates information related to a Researcher,
 * for example keywords, categories, co-authors, and publications.
 *  
 * @author pfishero
 */
public interface ResearcherProfile {
	Author getResearcher();
	Collection<Publication> getPublications();
	Multiset<Author> getCoAuthors();
	Multiset<Keyword> getKeywords();
	Multiset<Category> getLeafCategories();
	Collection<Author> getSortedCoAuthors();
	Collection<Keyword> getSortedKeywords();
	Collection<Category> getSortedLeafCategories();
	/**
	 * @return a ranked list of expertise areas (ranked in desc order of expertise)
	 */
	List<Keyword> identifyExpertiseAreas();
	Collection<String> simpleTopKeyphrases(Iterable<SimplePub> pubs);
	
}
