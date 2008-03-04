/**
 * 
 */
package edu.ucdavis.cs.dblp.experts;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Iterators;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.timeline.EventData;

/**
 * Tests the ResearcherDaoImpl.  Note: this requires the data store
 * to be loaded prior to running the tests or else the tests will fail.
 * 
 * @author pfishero
 */
public class ResearcherDaoImplTest {
	private static final Logger logger = Logger.getLogger(ResearcherDaoImplTest.class);
	
	private static ResearcherDao dao;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dao = ServiceLocator.getInstance().getResearcherDao();
	}
	
	/**
	 * Test method for {@link edu.ucdavis.cs.dblp.experts.ResearcherDaoImpl#findByName(java.lang.String)}.
	 */
	@Test
	public final void testFindByName() {
		String[] names = new String[]{
			"Ian Davidson",
			"Michael Gertz",
			"Nick Bryan-Kinns",
		};
		for (String name : names) {
			Collection<Author> authors = dao.findByName(name);
			assertTrue(authors.size() >= 1);
			logger.info("for name:"+name+
					"found author(s):\n"+StringUtils.join(authors, '\n'));
		}
	}
	
	@Test
	public final void testFindByNamePrefix() {
		String[] namePrefixes = new String[]{
			"Ian Dav",
			"Michael Ge",
			"Nick Brya",
		};
		for (String namePrefix : namePrefixes) {
			Collection<Author> authors = dao.findByNamePrefix(namePrefix);
			assertTrue(authors.size() >= 1);
			logger.info("for namePrefix:"+namePrefix+
					" found author(s):\n"+StringUtils.join(authors, '\n'));
		}
	}

	/**
	 * Test method for {@link edu.ucdavis.cs.dblp.experts.ResearcherDaoImpl#findPublications(de.unitrier.dblp.Author)}.
	 */
	@Test
	public final void testFindPublications() {
//		Collection<Author> authors = dao.findByName("Hector Garcia-Molina");
		Collection<Author> authors = dao.findByName("Ian Davidson");
		assertTrue(authors.size() >= 1);
		ResearcherProfile profile = new ResearcherProfileImpl(
									Iterators.getOnlyElement(authors.iterator()));
		assertTrue(profile.getPublications().size() > 0);
		logger.info("for author(s):"+StringUtils.join(authors, ',')+
				" profile:"+profile);
		logger.info("pubs as xml: " + 
				EventData.fromPublications(profile.getPublications()).
					toXML());
	}

}
