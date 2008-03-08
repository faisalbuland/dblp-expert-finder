/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.reader.ReaderProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.cs.dblp.EnglishAnalyzer;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * 
 * @author pfishero
 * @version $Id$
 *
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class DblpPubDaoImpl implements DblpPubDao {
	public static final Logger logger = Logger.getLogger(DblpPubDaoImpl.class);

	@PersistenceContext
    private EntityManager em;
	
	public EntityManager getEm() {
		return em;
	}
	
	public void delete(Publication page) {
		em.remove(page);
	}

	public Publication findById(String key) {
		Publication pub = null;
		
		try {
			Query query = em.createNamedQuery("Publication.byId");
			query.setParameter("key", key);
			pub = (Publication)query.getSingleResult();
		} catch (javax.persistence.NoResultException nre) {
			logger.warn("didn't find pub with id: "+key);
		}
		
		return pub;
	}
	
	public List<Publication> findByAuthorName(String name) {
		Query query = em.createNamedQuery("Publication.byAuthorName");
		query.setParameter("name", name);
		
		List<Publication> pubs = (List<Publication>)query.getResultList();
		
		return pubs;
	}
	
	public List<Publication> findByCategory(Category cat) {
		Query query = em.createNamedQuery("Publication.byCategory");
		query.setParameter("catKey", cat.getKey());
		
		List<Publication> pubs = (List<Publication>)query.getResultList();
		
		return pubs;
	}
	
	public List<SmeDTO> findSmes() {
		Query query = em.createNamedQuery("allSmes");
		
		List<SmeDTO> smes = (List<SmeDTO>)query.getResultList();
		
		return smes;
	}

	public void save(Publication page) {
		em.persist(page);
	}

	/**
	 * Note: page must have its Id set or else this may fail with an 
	 * "entity already exists exception".
	 * 
	 * @param page
	 */
	public void update(Publication page) {
		em.merge(page);
	}

	public List<Publication> findInText(String query) {
		List<Publication> result = fullTextSearch(query, new SimpleQueryCreator("text"));
		
		return result;
	}
	
	private List<Publication> fullTextSearch(String query,  
											LuceneQueryCreator queryCreator) {
		FullTextEntityManager fullTextEntityManager =
		    org.hibernate.search.jpa.Search.createFullTextEntityManager(em);

		SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();

		ReaderProvider readerProvider = searchFactory.getReaderProvider();
		IndexReader indexReader = readerProvider.openReader(searchFactory.getDirectoryProviders(Publication.class )[0]);
		List<Publication> result = null;
		
		try {
			org.apache.lucene.search.Query luceneQuery = queryCreator.createQuery(query);
			javax.persistence.Query fullTextQuery = fullTextEntityManager.createFullTextQuery(luceneQuery,
														Publication.class );
			fullTextQuery.setMaxResults(10);
			fullTextQuery.setFirstResult(0);
	
			logger.debug("found "+fullTextQuery.getResultList().size()+" matches to full text query");
			result = fullTextQuery.getResultList(); // return a list of managed objects
		} finally {
		    readerProvider.closeReader(indexReader);
		}
		return result;
	}
	
	private static interface LuceneQueryCreator {
		public org.apache.lucene.search.Query createQuery(String query);
	}
	
	private static class SimpleQueryCreator implements LuceneQueryCreator {
		private String defaultField;
		
		public SimpleQueryCreator(String defaultField) {
			this.defaultField = defaultField;
		}
		
		@Override
		public org.apache.lucene.search.Query createQuery(String query) {
			org.apache.lucene.search.Query luceneQuery = null;
			
			try {
				org.apache.lucene.queryParser.QueryParser parser = new QueryParser(defaultField, new EnglishAnalyzer());
				luceneQuery = parser.parse(query);
			} catch (ParseException e) {
				logger.error("error while searching for "+query+"- in field '"+defaultField+"': "+e);
			}
			
			return luceneQuery;
		}
	}
}
