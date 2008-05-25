/**
 * 
 */
package edu.ucdavis.cs.dblp.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * @author pfishero
 *
 */
public class NullContentService implements ContentService {
	private static final Logger logger = Logger.getLogger(NullContentService.class);

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.service.ContentService#accepts(edu.ucdavis.cs.dblp.data.Publication)
	 */
	@Override
	public boolean accepts(Publication pub) {
		// false as this content service doesn't accept any publications, but it can be used
		// when no other publications are accepted.
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.service.ContentService#retrieveAbstract(edu.ucdavis.cs.dblp.data.Publication)
	 */
	@Override
	public String retrieveAbstract(Publication pub) {
		String abstractText = "";
		return abstractText;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.service.ContentService#retrieveAll(edu.ucdavis.cs.dblp.data.Publication)
	 */
	@Override
	public void retrieveAll(Publication pub) {
		// do nothing
		logger.debug("(NullContentService) not retrieving any data for "+pub);
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.service.ContentService#retrieveClassification(edu.ucdavis.cs.dblp.data.Publication)
	 */
	@Override
	public Set<Category> retrieveClassification(Publication pub) {
		Set<Category> categories = new HashSet<Category>();
		return categories;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.service.ContentService#retrieveGeneralTerms(edu.ucdavis.cs.dblp.data.Publication)
	 */
	@Override
	public Set<String> retrieveGeneralTerms(Publication pub) {
		Set<String> generalTerms = new HashSet<String>();
		return generalTerms;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.service.ContentService#retrieveKeywords(edu.ucdavis.cs.dblp.data.Publication)
	 */
	@Override
	public Set<Keyword> retrieveKeywords(Publication pub) {
		Set<Keyword> keywords = new HashSet<Keyword>();
		return keywords;
	}

}
