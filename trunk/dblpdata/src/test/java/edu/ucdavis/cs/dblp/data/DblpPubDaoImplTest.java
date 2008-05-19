/**
 * 
 */
package edu.ucdavis.cs.dblp.data;


import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
public class DblpPubDaoImplTest {
	private static final Logger logger = Logger.getLogger(DblpPubDaoImplTest.class);
	
	private static DblpPubDao dao;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dao = ServiceLocator.getInstance().getDblpPubDao();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

	}
	
	@Test
	@Ignore
	public void simplePersistence() throws Exception {
		Publication pub = new Publication();
		pub.setKey("test/test");
		pub.setAddress("test");
		pub.setTitle("test");
		pub.setEe("http://portal.acm.org/citation.cfm?id=1294308");
		Set<Author> authors = new HashSet<Author>();
		String[] authorNames = new String[]{
				"author1",
				"author2",
				"author three",
				"author1",
		};
		for(String name : authorNames) {
			authors.add(new Author(name));
		}
		pub.setAuthor(authors);
		dao.update(pub);
	}
	
	@Test
	@Ignore
	public void simplePersistence2() throws Exception {
		Publication pub = new Publication();
		pub.setKey("test/test");
		pub.setAddress("test");
		pub.setTitle("test");
		pub.setEe("http://portal.acm.org/citation.cfm?id=1295308");
		Set<Author> authors = new HashSet<Author>();
		String[] authorNames = new String[]{
				"author one",
				"author2",
				"author three",
		};
		for(String name : authorNames) {
			authors.add(new Author(name));
		}
		pub.setAuthor(authors);
		dao.update(pub);
	}
	
	@Test
	@Ignore
	public void simplePersistence3() throws Exception {
		Publication pub = new Publication();
		pub.setKey("test/test");
		pub.setAddress("test");
		pub.setTitle("test");
		pub.setEe("http://portal.acm.org/citation.cfm?id=1295308");
		Set<Author> authors = new HashSet<Author>();
		String[] authorNames = new String[]{
				"author one",
				"�uthor one",
				"�uthor2",
				"�uthor three",
		};
		for(String name : Arrays.asList(authorNames)) {
			authors.add(new Author(name));
		}
		pub.setAuthor(authors);
		assertEquals(4, pub.getAuthor().size());
		dao.update(pub);
		Publication pub2 = dao.getEm().find(Publication.class, "test/test");
		logger.info("pub authors retrieved from persistence context:");
		logger.info(pub2.getAuthor());
		assertEquals(4, pub2.getAuthor().size());
	}
	
	// TODO re-enable after building dist
	@Test
	public void testFindAuthorPubs() throws Exception {
		final String authorName = "Nick Bryan-Kinns";
		List<Publication> pubs = dao.findByAuthorName(authorName);
		logger.info(pubs);
		assertTrue(pubs.size() > 0);
	}	
	
	// TODO re-enable after building dist
	@Test
	@Ignore
	public void testFindByCategoryId() throws Exception {
		List<Publication> pubs = dao.findByCategory(
				ServiceLocator.getInstance().getCategoryDao().findByKey("B.3.1 Semiconductor Memories (B.7.1) Subjects: Dynamic memory (DRAM)"));
		logger.info("pubs for B.3.1:"+pubs);
		assertTrue(pubs.size() > 0);
	}
	
	// TODO re-enable after building dist
	@Ignore
	public void testFindSmes() throws Exception {
		List<SmeDTO> smes = dao.findSmes();
		logger.info("smes:"+smes);
		assertTrue(smes.size() > 0);
	}

}
