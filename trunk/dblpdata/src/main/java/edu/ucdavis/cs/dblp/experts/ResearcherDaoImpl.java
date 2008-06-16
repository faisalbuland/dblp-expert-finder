/**
 * 
 */
package edu.ucdavis.cs.dblp.experts;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.Publication;

/**
 * @author pfishero
 *
 */
@Transactional(propagation = Propagation.REQUIRED)
public class ResearcherDaoImpl implements ResearcherDao {
	public static final Logger logger = Logger.getLogger(ResearcherDaoImpl.class);

	@PersistenceContext
    private EntityManager em;
	
	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.experts.ResearcherDao#findAuthors(java.lang.String)
	 */
	@Override
	public Collection<Author> findByName(String name) {
		Query query = em.createNamedQuery("Author.byName");
		query.setParameter("name", name);
		
		List<Author> authors = (List<Author>)query.getResultList();
		
		return authors;
	}
	
	@Override
	public Collection<Author> findByNamePrefix(String prefix) {
		Query query = em.createNamedQuery("Author.byNamePrefix");
		query.setParameter("namePrefix", prefix);
		
		List<Author> authors = (List<Author>)query.getResultList();
		
		return authors;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.experts.ResearcherDao#findPublications(de.unitrier.dblp.Author)
	 */
	@Override
	public Collection<Publication> findPublications(Author researcher) {
		return ServiceLocator.getInstance().
				getDblpPubDao().findByAuthorName(researcher.getContent());
	}
	
	@Override
	public Collection<ResearcherProfile> buildProfile(String name) {
		Collection<ResearcherProfile> profiles = Lists.newLinkedList();
		Collection<Author> authors = findByName(name);
		
		for (Author author : authors) {
			ResearcherProfile profile = new ResearcherProfileImpl(author);
			profiles.add(profile);
		}
		
		return profiles;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.experts.ResearcherDao#save(de.unitrier.dblp.Author)
	 */
	@Override
	public void save(Author researcher) {
		em.persist(researcher);
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.experts.ResearcherDao#update(de.unitrier.dblp.Author)
	 */
	@Override
	public void update(Author researcher) {
		em.merge(researcher);
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.experts.ResearcherDao#delete(de.unitrier.dblp.Author)
	 */
	@Override
	public void delete(Author researcher) {
		em.remove(researcher);
	}
}
