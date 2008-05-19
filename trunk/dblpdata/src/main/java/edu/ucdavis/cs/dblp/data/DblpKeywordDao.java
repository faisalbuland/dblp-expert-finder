/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * Data Access Object for DBLP {@link Keyword}s.
 * 
 * @author pfishero
 */
public interface DblpKeywordDao {
	
	Keyword findById(Long id);
	Keyword findByName(String keyword);
	List<Keyword> findAll();
	
	void save(Keyword keyword);
	
	void update(Keyword keyword);
	
	void delete(Keyword keyword);
	
	EntityManager getEm();
}