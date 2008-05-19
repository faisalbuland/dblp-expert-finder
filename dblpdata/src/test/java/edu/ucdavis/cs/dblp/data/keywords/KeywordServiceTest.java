package edu.ucdavis.cs.dblp.data.keywords;

import static org.junit.Assert.*;

import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import edu.ucdavis.cs.dblp.data.Keyword;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
DependencyInjectionTestExecutionListener.class,
TransactionalTestExecutionListener.class})
@Transactional
@ContextConfiguration(
locations={"/spring/dblpApplicationContext.xml"})
public class KeywordServiceTest {
	private static final Logger logger = Logger.getLogger(KeywordServiceTest.class);
	
	@Resource
	private KeywordRecognizer recognizer;
	
	@Test
	public void checkKeywordRecognition() {
		String title="Towards a spatial multidimensional model";
		String paperAbstract = "Data warehouses and OLAP systems help to interactively analyze huge " +
				"volume of data. This data, extracted from transactional databases, frequently " +
				"contains spatial information which is useful for decision-making process. " +
				"Integration of spatial data in multidimensional models leads to the concept of " +
				"SOLAP (Spatial OLAP). Using a spatial measure as a geographical object, " +
				"i.e. with geometric and descriptive attributes, raises problems regarding the " +
				"aggregation operation in its semantic and implementation aspects. This paper " +
				"shows the requirements for a multidimensional spatial data model and presents " +
				"a multidimensional data model which is able to support complex objects as " +
				"measures, inter-dependent attributes for measures and aggregation functions, " +
				"use of ad-hoc aggregation functions and n to n relations between fact and " +
				"dimension, in order to handle geographical data, according to its particular " +
				"nature in an OLAP context.";
		Set<Keyword> expectedKeywords = Sets.newHashSet(ImmutableList.of(
											new Keyword("Spatial OLAP"),  
											new Keyword("multidimensional data"), 
											new Keyword("data warehouse")
											));
		Set<Keyword> foundKeywords = recognizer.findKeywordsIn(title+paperAbstract);
		logger.info("found keywords: "+foundKeywords);
		expectedKeywords.removeAll(foundKeywords);
		assertEquals("keyword(s) not found: "+expectedKeywords, expectedKeywords.size(), 0);
	}
}
