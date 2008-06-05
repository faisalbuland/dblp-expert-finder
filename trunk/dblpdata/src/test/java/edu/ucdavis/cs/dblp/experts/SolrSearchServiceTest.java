package edu.ucdavis.cs.dblp.experts;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
DependencyInjectionTestExecutionListener.class,
TransactionalTestExecutionListener.class})
@Transactional
@ContextConfiguration(
locations={"/spring/dblpApplicationContext.xml"})
public class SolrSearchServiceTest {
	private static final Logger logger = Logger.getLogger(SolrSearchServiceTest.class);
	
	@Autowired(required=true)
	private SearchService searchService;
	
	@Test
	public void searchAndVerify() {
		String[][] params = new String[][]{
							// {"search phrase", "refinement filter query"}
								{"constrained clustering","author:Ian+Davidson"},
								{"spatial databases","author:\"Ralf Hartmut Güting\""},
								};
		for (int i=0; i < params.length; i++) {
			DblpResults results = searchService.fullTextSearch(params[i][0]);
			logger.info("total found="+results.getResultsCount()+" in "+results.getQueryTime()+" msecs");
			
			results = verifyResults(results);
			
			searchService.addFilterQuery(params[i][1]);
			results = verifyResults(searchService.refineSearch(results));
			searchService.clearFilterQueries();
		}
	}

	/**
	 * @param results
	 * @return
	 */
	private DblpResults verifyResults(DblpResults results) {
		int retrievedCount = 0;
		do {
			if (retrievedCount > 0) {
				results = searchService.fetchMoreResults(results);
			}
			assertNotNull(results.getPubs());
			logger.info("found "+results.getPubs().size()+" matching publications");
			retrievedCount += results.getPubs().size();
			logger.info("retrieved "+retrievedCount+" so far");
			assertTrue(results.getPubs().size() >= 0);
		} while (results.hasMore() && retrievedCount > 0); 
		assertEquals("did not retrieve the correct number of pubs", 
				retrievedCount, results.getResultsCount());
		return results;
	}
}
