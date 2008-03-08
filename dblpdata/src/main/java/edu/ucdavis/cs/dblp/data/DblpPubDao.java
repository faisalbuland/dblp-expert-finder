/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.util.List;

import javax.persistence.EntityManager;

import edu.ucdavis.cs.taxonomy.Category;

/**
 * Data Access Object for DBLP {@link Publication}s.
 * 
 * @author pfishero
 *
 */
public interface DblpPubDao {
	/**
	 * @param key
	 * @return the Publication for the given <code>key</code>, if one is found.
	 * If one is not found in the persistent store, then null is returned.
	 */
	Publication findById(String key);
	List<Publication> findByAuthorName(String name);
	List<Publication> findByCategory(Category catId);
	List<SmeDTO> findSmes();
	
	List<Publication> findInText(String query);
	
	void save(Publication page);
	
	void update(Publication page);
	
	void delete(Publication page);
	
	EntityManager getEm();
}
