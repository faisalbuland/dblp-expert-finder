package edu.ucdavis.cs.dblp.experts;

import java.util.Collection;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.data.Publication;

/**
 * @author pfishero
 *
 */
public interface ResearcherDao {
	Collection<Author> findByName(String name);
	Collection<Author> findByNamePrefix(String prefix);
	Collection<Publication> findPublications(Author researcher);
	Collection<ResearcherProfile> buildProfile(String name);
	
	void save(Author researcher);
	
	void update(Author researcher);
	
	void delete(Author researcher);
}
