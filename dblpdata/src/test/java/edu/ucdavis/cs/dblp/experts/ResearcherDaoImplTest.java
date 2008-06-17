/**
 * 
 */
package edu.ucdavis.cs.dblp.experts;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Join;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.data.timeline.EventData;
import edu.ucdavis.cs.dblp.text.ClutoClusterer;
import edu.ucdavis.cs.dblp.text.SimplePub;

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
	@Test @Ignore
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
	
	@Test @Ignore
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
	@Test @Ignore
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
	
	@Test
	public final void verifyAuthorProfile() {
//		final String researcherName = "Ian Davidson";
		final String researcherName = "Shyhtsun Felix Wu";
//		final String researcherName = "Michael Gertz";
		ResearcherProfile profile = Iterables.getOnlyElement(dao.buildProfile(researcherName));
		assertTrue(profile.getPublications().size() > 0);
		List<Keyword> expertiseAreas = profile.identifyExpertiseAreas();
		assertThat(expertiseAreas.size(), is(greaterThanOrEqualTo(1)));
		
		final int numClusters = 8;
		Multimap<Integer, Publication> clusters = 
							ClutoClusterer.getClustersFor(profile.getPublications(), numClusters);
		assertThat(clusters.keySet().size(), is(numClusters));
		for(int clusterId : clusters.keySet()) {
			logger.info("\ncluster "+clusterId+" members:");
			for (Publication pub : clusters.get(clusterId)) {
				logger.info(pub.getTitle());
			}
			logger.info("\nkeywords:");
			logger.info(
					Join.join("\n", Join.join(", ", 
							Iterables.partition(profile.simpleTopKeyphrases(
									SimplePub.fromAll(clusters.get(clusterId))), 10, false))));
		}
	}

}
