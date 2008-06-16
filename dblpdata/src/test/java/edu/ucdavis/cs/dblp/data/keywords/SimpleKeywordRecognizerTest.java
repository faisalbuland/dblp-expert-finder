package edu.ucdavis.cs.dblp.data.keywords;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

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

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
DependencyInjectionTestExecutionListener.class,
TransactionalTestExecutionListener.class})
@Transactional
@ContextConfiguration(
locations={"/spring/dblpApplicationContext.xml"})
public class SimpleKeywordRecognizerTest {
	private static final Logger logger = Logger.getLogger(SimpleKeywordRecognizerTest.class); 
	
	@Resource
	private KeywordRecognizer recognizer;
	
	@Test
	public void testLongestMatch1() {		
		List<String> testKws = ImmutableList.of("spatial", "spatial data", "spatial database", "spatial databases");
		List<String> processedKws = recognizer.reduceKeywords(testKws);
		logger.info(processedKws);
	}
	
	@Test
	public void testLongestMatch2() {
		List<String> testKws = ImmutableList.of("spatial", "Spatial");
		List<String> processedKws = recognizer.reduceKeywords(testKws);
		logger.info("dup with diff case = "+processedKws);
	}
	
	@Test
	public void testLongestMatch3() {
		List<String> testKws = ImmutableList.of("geographic information retrieval", "Geographic Information Retrieval (GIR)", "Geographical Information Retrieval");
		List<String> processedKws = recognizer.reduceKeywords(testKws);
		logger.info("dup with extra acronym = "+processedKws);
	}
	
	@Test
	public void testLongestMatch4() {
		List<String> testKws = ImmutableList.of("geographic information systems", "Geographical Information Systems", "GIS");
		List<String> processedKws = recognizer.reduceKeywords(testKws);
		logger.info("dup with extra acronym = "+processedKws);
	}
	
	@Test
	public void testLongestMatch5() {
		List<String> testKws = ImmutableList.of("123", "R-tree", "R-Tree", "R-Trees", "R-Tree family", "abc", "random",
				"WWW", "world wide web", "Geographic Information Systems", "GIS");
		List<String> processedKws = recognizer.reduceKeywords(testKws);
		logger.info("R-tree variants = "+processedKws);
	}
	
	@Test
	public void testShouldNotMerge() {
		List<String> testKws = ImmutableList.of("3D face recognition", "face recognition");
		List<String> processedKws = recognizer.reduceKeywords(testKws);
		logger.info("should not be merged = "+processedKws);
		assertThat(processedKws.size(), is(2));
	}
	
	@Test
	public void testClosePhrases() {
		List<String> testKws = ImmutableList.of("Sample Selection", "Sample Selection Bias");
		List<String> processedKws = recognizer.reduceKeywords(testKws);
		logger.info("should not be merged = "+processedKws);
		assertThat(processedKws.size(), is(2));
	}
}
