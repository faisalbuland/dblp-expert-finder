/**
 * 
 */
package edu.ucdavis.cs.dblp.text.io;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.taxonomy.Categories;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * Export all publications for all categories to Solr.
 * 
 * @author pfishero
 */
public class SolrExportPubsForCategories {
	public static final Logger logger = Logger.getLogger(SolrExportPubsForCategories.class);
	
	private static final int COMMIT_FREQ = 1000;
	
	private final DblpPubDao dao;
	private final Iterable<Publication> allPubData;
	private final List<Category> categories;
	private final SolrServer server;
	
	public SolrExportPubsForCategories(List<Category> categories) {		
		this.dao = ServiceLocator.getInstance().getDblpPubDao();
		this.categories = categories;
		
		Iterable<List<Publication>> pubsForCats = Iterables.transform(categories, 
				new Function<Category, List<Publication>>() {
					@Override
					public List<Publication> apply(Category cat) {
						logger.info("Finding pubs for cat: "+cat);
						return dao.findByCategory(cat);
					}
				});
		allPubData = Iterables.concat(Iterables.concat(pubsForCats));
		
		try {
			server = new CommonsHttpSolrServer( "http://localhost:8983/solr" );
		} catch (MalformedURLException e) {
			logger.error(e);
			throw new IllegalStateException(e);
		}
		
		logger.info("created data exporter for "+categories.size()+" categories");
	}
	
	public void exportData() {
		Multiset<String> catPubCounts = Multisets.newHashMultiset();
		logger.info("exporting data to Solr");
		int count=0;
		List<SolrInputDocument> docs = Lists.newLinkedList();
		
		try {
			for(Publication pub : allPubData) {
				count++;
				SolrInputDocument doc = new SolrInputDocument();
				
				for (Category cat : 
						Iterables.filter(pub.getContent().getCategories(), Categories.ONLY_LEAF_NODES)) {					
					catPubCounts.add(cat.getKey());
					doc.addField("leafCategoryId", cat.getKey());
					
					Category parent = cat.getParent();
					while (parent != null) {
						doc.addField("categoryId", parent.getKey());
						parent = parent.getParent();
					}
				}
				doc.addField("id", pub.getKey());
				doc.addField("publishYear", pub.getYear());
				doc.addField("title", pub.getTitle());
				
				for (Keyword keyword : pub.getContent().getKeywords()) {
					doc.addField("keyword", keyword.getKeyword());
				}
				doc.addField("abstract", pub.getContent().getAbstractText());
				for (Author author : pub.getAuthor()) {
					doc.addField("author", author.getContent());
				}
				
				docs.add(doc);
				
				if (count % COMMIT_FREQ == 0) {
					logger.info("count="+count);
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
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Category> cats = ServiceLocator.getInstance().getCategoryDao().findLeafParentNodes();
		SolrExportPubsForCategories dumper = new SolrExportPubsForCategories(cats);
		dumper.exportData();
	}

}
