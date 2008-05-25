package edu.ucdavis.cs.dblp.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * @author pfishero
 *
 */
@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class DblpKeywordDaoImpl implements DblpKeywordDao {
	private static final Logger logger = Logger.getLogger(DblpKeywordDaoImpl.class);
	
	@PersistenceContext
    private EntityManager em;
	
	@Override
	public List<Keyword> findAll() {
		Query query = em.createNamedQuery("Keyword.all");
		List<Keyword> allKeywords = (List<Keyword>)query.getResultList();
		
		return allKeywords;
	}

	@Override
	public Keyword findById(Long id) {
		Keyword keyword = null;
		
		try {
			Query query = em.createNamedQuery("Keyword.byId");
			query.setParameter("id", id);
			keyword = (Keyword)query.getSingleResult();
		} catch (javax.persistence.NoResultException nre) {
			logger.warn("didn't find keyword with id: "+id);
		}
		
		return keyword;
	}

	@Override
	public Keyword findByName(String keyword) {
		Keyword foundKeyword = null;
		
		try {
			Query query = em.createNamedQuery("Keyword.byName");
			query.setParameter("keyword", keyword);
			foundKeyword = (Keyword)query.getSingleResult();
		} catch (javax.persistence.NoResultException nre) {
			logger.warn("didn't find keyword: "+keyword);
		}
		
		return foundKeyword;
	}

	@Override
	public EntityManager getEm() {
		return em;
	}

	@Override
	public void save(Keyword keyword) {
		em.persist(keyword);
	}

	@Override
	public void update(Keyword keyword) {
		em.merge(keyword);
	}
	
	@Override
	public void delete(Keyword keyword) {
		// warning msg as most use cases shouldn't require deleting keywords
		logger.info("caution - remove keyword called for "+keyword);
		em.remove(keyword);
	}

}
