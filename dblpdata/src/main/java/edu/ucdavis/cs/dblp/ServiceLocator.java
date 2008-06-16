/**
 * 
 */
package edu.ucdavis.cs.dblp;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.ucdavis.cs.dblp.data.DataLoader;
import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.keywords.KeywordRecognizer;
import edu.ucdavis.cs.dblp.experts.ResearcherDao;
import edu.ucdavis.cs.dblp.experts.SearchService;
import edu.ucdavis.cs.dblp.service.ContentService;
import edu.ucdavis.cs.taxonomy.Category;
import edu.ucdavis.cs.taxonomy.CategoryDao;

/**
 * 
 * @author pfishero
 * @version $Id$
 *
 */
public class ServiceLocator {
	private static final Logger logger = Logger.getLogger(ServiceLocator.class);
	
	private static final ServiceLocator instance = new ServiceLocator();
	private final ApplicationContext appContext;
	
	private ServiceLocator() {
		appContext = new ClassPathXmlApplicationContext(
				new String[] {"spring/dblpApplicationContext.xml"});
		initACMTaxonomy();
	}
	
	private final void initACMTaxonomy() {
		Category root = getCategoryDao().findById("acmccs98");
		if (null == root) {
			logger.warn("ACM Taxonomy was not found.  Creating...");
			try {
				DataLoader dataLoader = 
							(DataLoader)appContext.getBean("ccsDataLoader");
				dataLoader.doLoad();
			} catch (Exception e) {
				throw new RuntimeException("error loading ACM Taxonomy", e);
			}
		} // else it exists, so nothing else needs to be done to init it
	}
	
	public static final ServiceLocator getInstance() {
		return instance;
	}
	
	public ApplicationContext getAppContext() {
		return appContext;
	}
	
	public DblpPubDao getDblpPubDao() {
		return (DblpPubDao)appContext.getBean("dblpPubDao");
	}
	
	public ResearcherDao getResearcherDao() {
		return (ResearcherDao)appContext.getBean("researcherDao");
	}
	
	public CategoryDao getCategoryDao() {
		return (CategoryDao)appContext.getBean("categoryDao");
	}
	
	public ContentService getContentService() {
		return (ContentService)appContext.getBean("contentService");
	}
	
	public SearchService getSearchService() {
		return (SearchService)appContext.getBean("searchService");
	}
	
	public SolrServer getSolrServer() {
		return (SolrServer)appContext.getBean("solrServer");
	}
	
	public KeywordRecognizer getKeywordRecognizer() {
		return (KeywordRecognizer)appContext.getBean("keywordRecognizer");
	}
}
