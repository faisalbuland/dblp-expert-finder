package edu.ucdavis.cs.dblp.experts;

import java.util.Collection;

import com.google.common.collect.Multiset;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * ResearcherProfile aggregates information related to a Researcher,
 * for example keywords, categories, co-authors, and publications.
 *  
 * @author pfishero
 */
public interface ResearcherProfile {
	Multiset<Author> getCoAuthors();
	Collection<Publication> getPublications();
	Multiset<Keyword> getKeywords();
	Multiset<Category> getLeafCategories();
	// TODO add getACMCCSNodes();
}
