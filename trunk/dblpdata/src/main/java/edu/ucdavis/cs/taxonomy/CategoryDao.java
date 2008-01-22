package edu.ucdavis.cs.taxonomy;

import java.util.List;


public interface CategoryDao {
	Category findByKey(String key);
	Category findByKeyIgnoreCase(String key);
	Category findById(String id);
	List<Category> findAll();
	List<Category> findLeafNodes();
	
	void save(Category cat);
	
	void update(Category cat);
	
	void delete(Category cat);
}