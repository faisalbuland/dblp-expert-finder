/**
 * 
 */
package edu.ucdavis.cs.dblp.service;

import java.util.Set;

import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
public interface ContentService {
	public String retrieveAbstract(Publication pub);
	public Set<Keyword> retrieveKeywords(Publication pub);
	public Set<Category> retrieveClassification(Publication pub);
	public Set<String> retrieveGeneralTerms(Publication pub);
	public void retrieveAll(Publication pub);
	/**
	 * @param pub the publication to check if the content service can retrieve content for
	 * @return true if the contentservice can retrieve content for <code>pub</code>, else false
	 */
	public boolean accepts(Publication pub);
}
