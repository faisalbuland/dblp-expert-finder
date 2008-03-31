package edu.ucdavis.cs.dblp.experts;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Timed;
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
	
	@Autowired
	private SearchService searchService;
	
	@Test
	@Timed(millis=15000)
	public void searchAndVerify() {
		DblpResults results = searchService.fullTextSearch("constrained clustering");
		logger.info("total found="+results.getResultsCount()+" in "+results.getQueryTime()+" msecs");
		if (results.getPubs() != null) {
			logger.info("found "+results.getPubs().size()+" matching publications");
		}
	}
}
