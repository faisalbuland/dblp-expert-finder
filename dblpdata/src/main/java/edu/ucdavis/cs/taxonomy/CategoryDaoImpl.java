package edu.ucdavis.cs.taxonomy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class CategoryDaoImpl implements CategoryDao {
	public static final Logger logger = Logger.getLogger(CategoryDaoImpl.class);

	@PersistenceContext
    private EntityManager em;

	@Override
	public Category findByKey(String key) {
		Query query = em.createNamedQuery("Category.byKey");
		query.setParameter("key", key);
		Category cat = null;
		try {
			cat = (Category)query.getSingleResult();
		} catch (javax.persistence.NoResultException nre) {
			logger.warn("didn't find a category for key: "+key);
		}
		return cat;
	}
	
	@Override
	public Category findByKeyIgnoreCase(String key) {
		Query query = em.createNamedQuery("Category.byKeyUpperCase");
		query.setParameter("key", key.toUpperCase());
		Category cat = null;
		try {
			cat = (Category)query.getSingleResult();
		} catch (javax.persistence.NoResultException nre) {
			logger.warn("didn't find a category for key: "+key.toUpperCase());
		}
		return cat;
	}
	
	@Override
	public Category findById(String id) {
		if (StringUtils.isBlank(id)) {
			throw new IllegalArgumentException("finding by blank id is not supported");
		}
		Query query = em.createNamedQuery("Category.byId");
		query.setParameter("id", id);
		Category cat = null;
		try {
			cat = (Category)query.getSingleResult();
		} catch (javax.persistence.NoResultException nre) {
			logger.warn("didn't find a category for id: "+id);
		}
		return cat;
	}
	
	@Override
	public List<Category> findByFreeTextSearch(String freeText) {
		Query query = em.createNamedQuery("Category.byFreeTextSearch");
		query.setParameter("freeText", freeText);
		
		List<Category> cats = (List<Category>)query.getResultList();
		
		return cats;
	}	
	
	@Override
	public List<Category> findAll() {
		return (List<Category>)em.createNamedQuery("Category.allCategories").getResultList();
	}
	
	@Override
	public List<Category> findLeafNodes() {
		return (List<Category>)em.createNamedQuery("Category.allLeafNodes").getResultList();
	}
	
	@Override
	public List<Category> findLeafParentNodes() {
		Set<Category> leafParentNodesSet = new HashSet<Category>();
		for (Category cat : findLeafNodes()) {
			leafParentNodesSet.add(cat.getParent());
		}
		
		return ImmutableList.copyOf(leafParentNodesSet);
	}

	@Override
	public void save(Category cat) {
		em.persist(cat);
	}

	@Override
	public void update(Category cat) {
		em.merge(cat);
	}
	
	@Override
	public void delete(Category cat) {
		em.remove(cat);
	}

}
