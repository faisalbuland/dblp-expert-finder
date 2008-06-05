/**
 * 
 */
package edu.ucdavis.cs.dblp.text.io;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.taxonomy.Categories;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * Export all publications (and associated content) to Solr.
 * 
 * @author pfishero
 */
@Component
public class SolrExportPubs {
	public static final Logger logger = Logger.getLogger(SolrExportPubs.class);
	
	private static int COMMIT_FREQ = 500;
	
	@Resource(name="dblpPubDaoImpl")
	private DblpPubDao dao;
	private SolrServer server;
	
	public SolrExportPubs() {
		try {
			server = new CommonsHttpSolrServer( "http://localhost:8983/solr" );
		} catch (MalformedURLException e) {
			logger.error(e);
			throw new IllegalStateException(e);
		}
		
		logger.info("created data exporter for all publications");
	}
	
	private AtomicInteger totalCount = new AtomicInteger();
	
	public void exportData() {
		int firstResult = 1;
		int pageBy = 500;
		boolean continuePaging = true;

		while (continuePaging) {
			List<Publication> results = query(firstResult, pageBy);
			processResults(results);
			continuePaging = results.size() > 0;
			firstResult = firstResult+pageBy;
		}
	}

	/**
	 * @param firstResult
	 * @param pageBy
	 * @return
	 */
	@Transactional
	private List<Publication> query(int firstResult, int pageBy) {
		Query q = dao.getEm().createNamedQuery("Publication.all");
		List<Publication> results = q.setFirstResult(firstResult).setMaxResults(pageBy).getResultList();
		return results;
	}
	
	
		
	public void processResults(List<Publication> allPubData) {
		logger.info("exporting data to Solr");
		int count=0;
		List<SolrInputDocument> docs = Lists.newLinkedList();
		
		try {
			for(Publication pub : allPubData) {
				count++;
				SolrInputDocument doc = new SolrInputDocument();
				
				if (pub.getContent() != null && pub.getContent().getCategories() != null) {
					for (Category cat : 
							Iterables.filter(pub.getContent().getCategories(), Categories.ONLY_LEAF_NODES)) {					
						doc.addField("leafCategoryId", cat.getKey());
						
						Category parent = cat.getParent();
						while (parent != null) {
							doc.addField("categoryId", parent.getKey());
							parent = parent.getParent();
						}
					}
				}
				doc.addField("id", pub.getKey());
				doc.addField("publishYear", pub.getYear());
				doc.addField("title", pub.getTitle());
				
				if (pub.getContent() != null && pub.getContent().getKeywords() != null) {
					for (Keyword keyword : pub.getContent().getKeywords()) {
						doc.addField("keyword", keyword.getKeyword());
					}
				}
				if (pub.getContent() != null && pub.getContent().getAbstractText() != null) {
					doc.addField("abstract", pub.getContent().getAbstractText());
				}
				for (Author author : pub.getAuthor()) {
					doc.addField("author", author.getContent());
				}
				
				docs.add(doc);
				
				if (count % COMMIT_FREQ == 0) {
					logger.info("totalCount="+totalCount.get());
					server.add(docs);
					server.commit();
					
					docs.clear();
				}
			}
			
			if (docs.size() > 0) {
				server.add(docs);
				server.commit();
			}
		} catch (SolrServerException e) {
			logger.error("count ="+count+" error="+e);
		} catch (IOException e) {
			logger.error("count ="+count+" error="+e);
		}
		totalCount.addAndGet(count);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
										new String[]{"/spring/dblpApplicationContext.xml"});
		((SolrExportPubs)ctx.getBean("solrExportPubs")).exportData();
	}

}
