/**
 * 
 */
package edu.ucdavis.cs.taxonomy;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.experts.NullAuthor;
import edu.ucdavis.cs.dblp.experts.ResearcherProfile;
import edu.ucdavis.cs.dblp.experts.ResearcherProfileImpl;
import edu.ucdavis.cs.dblp.service.AcmDlContentService;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
public class CategoryDaoImplTest {
	private final static Logger logger = Logger.getLogger(CategoryDaoImplTest.class);
	
	private static CategoryDao dao;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dao = ServiceLocator.getInstance().getCategoryDao();
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
	public void testFindByKey() {
		Category cat = dao.findByKey("K.8.0 General Subjects: Games");
		assertTrue(cat != null);
		assertEquals("K.8.0", cat.getParent().getId());
		assertEquals("Games", cat.getLabel());
		
		Category cat2 = dao.findByKey("H.5.2 User Interfaces (D.2.2, H.1.2, I.3.6)");
		assertNotNull(cat2);
		assertEquals("H.5.2", cat2.getId());
//		logger.info(dao.findById("H.5.2"));
	}
	
	@Test
	public void testFindById() {
		Category cat1 = dao.findById("D.2.2");
		logger.info(cat1);
		assertNotNull(cat1);
		
		List<String> catIds = ImmutableList.of(
									"B.2.4", 
									"B.4.3",
									"B.8",
									"C.5.1",
									"F.2.3",
									"H.2.8",
									"H.3.5",
									"H.5",
									"I.2.3",
									"I.3.1"
									);
		Iterable<Category> cats = Iterables.transform(catIds, 
				new Function<String, Category>() {
					@Override
					public Category apply(String catId) {
						logger.info("finding catId: "+catId);
						return dao.findById(catId);
					}
				});
		for (Category cat : cats) {
			logger.info("cat key="+cat.getKey());
			logger.info("cat children="+Join.join("\n",
						Iterables.transform(cat.getChildren(), 
								new Function<Category, String>() {
									public String apply(Category cat) {
										return cat.getKey();
									};
								})));
			assertNotNull(cat);
		}
	}
	
	/**
	 * Tests that lookups work for some of the categories from the ACM DL 
	 * that don't exactly match ACM CCS (1998) categories.
	 * 
	 * @see {@link AcmDlContentService#findCategoryFor(String)}
	 */
	@Test
	public void testFindIncorrectlyNamed() {		
		List<String> wrongCatNames = ImmutableList.of(
				"B.2.4 High-speed Arithmetic",
				"B.2.4 High-speed Arithmetic Subjects: Algorithms",
				"B.2.4 High-speed Arithmetic Subjects: Cost/performance",
				"B.4.3 Interconnections (subsystems)",
				"B.4.3 Interconnections (subsystems) Subjects: Asynchronous/synchronous operation",
				"B.4.3 Interconnections (subsystems) Subjects: Fiber optics",
				"B.4.3 Interconnections (subsystems) Subjects: Interfaces",
				"B.4.3 Interconnections (subsystems) Subjects: Topology (e.g., bus, point-to-point)",
				"B.8 Performance and Reliability",
				"C.5.1 Large and Medium (\"Mainframe\") Computers",
				"C.5.1 Large and Medium (\"Mainframe\") Computers Subjects: Super (very large) computers",
				"F.2.3 Tradeoffs among Complexity Measures",
				"H.2.8 Database applications",
				"H.2.8 Database applications Subjects: Data mining",
				"H.2.8 Database applications Subjects: Image databases",
				"H.2.8 Database applications Subjects: Scientific databases",
				"H.2.8 Database applications Subjects: Spatial databases and GIS",
				"H.2.8 Database applications Subjects: Statistical databases",
				"H.3.5 On-line Information Services",
				"H.3.5 On-line Information Services Subjects: Commercial services",
				"H.3.5 On-line Information Services Subjects: Data sharing",
				"H.3.5 On-line Information Services Subjects: Web-based services",
				"H.5 INFORMATION INTERFACES AND PRESENTATION (I.7)",
				"H.5 INFORMATION INTERFACES AND PRESENTATION",
				"I.2.3 Deduction and Theorem Proving Subjects: Uncertainty, \"fuzzy,\" and probabilistic reasoning",
				"I.3.1 Hardware architecture",
				"I.3.1 Hardware architecture Subjects: Graphics processors",
				"I.3.1 Hardware architecture Subjects: Hardcopy devices**",
				"I.3.1 Hardware architecture Subjects: Input devices",
				"I.3.1 Hardware architecture Subjects: Parallel processing",
				"I.3.1 Hardware architecture Subjects: Raster display devices"
									);
		Iterable<Category> cats = Iterables.transform(wrongCatNames, 
				new Function<String, Category>() {
					@Override
					public Category apply(String catKey) {
						logger.info("finding catKey: "+catKey);
						return AcmDlContentService.findCategoryFor(catKey);
					}
				});
		for (Category cat : cats) {
			assertNotNull(cat);
			if (null != cat) logger.info("cat key="+cat.getKey());
		}
	}
	
	@Test
	public void testFindAll() {
		List<Category> cats = dao.findAll();
		assertNotNull(cats);
	}
	
	@Test
	public void testFindLeafNodes() {
		List<Category> cats = dao.findLeafNodes();
		assertNotNull(cats);
	}

	@Test
	public void testFindByFreeTextSearch() {
		List<Category> cats = dao.findByFreeTextSearch("distributed data");
		assertNotNull(cats);
		logger.info(cats);
		
		List<Category> leaves = ImmutableList.copyOf(Iterables.filter(cats, Categories.ONLY_LEAF_NODES));
		DblpPubDao dao = ServiceLocator.getInstance().getDblpPubDao();
		Author noOpAuthor = new NullAuthor();
		for (Category cat : leaves) {
			List<Publication> pubs = dao.findByCategory(cat);
			ResearcherProfile profile = new ResearcherProfileImpl(noOpAuthor, pubs);
			logger.info("category="+cat);
			logger.info("keywords="+profile.getKeywords());
			logger.info("publications="+profile.getPublications());
			logger.info("authors="+profile.getCoAuthors());
		}
		
	}
}
