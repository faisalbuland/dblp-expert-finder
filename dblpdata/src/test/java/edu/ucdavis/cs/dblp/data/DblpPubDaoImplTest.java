/**
 * 
 */
package edu.ucdavis.cs.dblp.data;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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

import com.google.common.collect.ImmutableList;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.keywords.KeywordRecognizer;

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
//		recognizer = (KeywordRecognizer) ServiceLocator.getInstance().
//						getAppContext().getBean("keywordRecognizer");
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
				"âuthor one",
				"äuthor2",
				"àuthor three",
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
	@Ignore
	public void testFindAuthorPubs() throws Exception {
		final String authorName = "Nick Bryan-Kinns";
		List<Publication> pubs = dao.findByAuthorName(authorName);
		logger.info(pubs);
		assertTrue(pubs.size() > 0);
	}
	
	@Test
	public void analyzeMissingKeywords() throws Exception {
//		String id = "conf/icde/TungHH01";
//		String id = "conf/pakdd/ZhangYXD07";
		String id = "conf/icat/Yu06";
//		String id = "conf/jcis/BecerraRD02";
		Publication pub = dao.findById(id);
		assertThat(pub.getContent(), is(notNullValue()));
		logger.info(pub);
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
	@Test
	@Ignore
	public void testFindSmes() throws Exception {
		List<SmeDTO> smes = dao.findSmes();
		logger.info("smes:"+smes);
		assertTrue(smes.size() > 0);
	}

}
