/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import edu.ucdavis.cs.taxonomy.Category;

/**
 * 
 * @author pfishero
 * @version $Id$
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
	List<Publication> findByCategoryId(Category catId);
	List<SmeDTO> findSmes();
	
	List<Publication> findInText(String query);
	
	void save(Publication page);
	
	void update(Publication page);
	
	void delete(Publication page);
	
	EntityManager getEm();
}
