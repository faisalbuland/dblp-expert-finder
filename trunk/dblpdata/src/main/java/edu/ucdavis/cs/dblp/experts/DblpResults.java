package edu.ucdavis.cs.dblp.experts;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.data.Publication;

/**
 * @author pfishero
 */
@Transactional(propagation = Propagation.REQUIRED)
public class DblpResults {
	private String searchText;
	private long resultsCount;
	private int queryTime;
	private List<FacetField> facetFields;
	private List<Publication> pubs;
	
	public DblpResults(String searchText) {
		super();
		this.searchText = searchText;
		pubs = Lists.newLinkedList();
	}
	/**
	 * @param response
	 * @return
	 */
	public static final DblpResults fromQueryResponse(QueryResponse response, String searchText) {
		DblpResults results = new DblpResults(searchText);
		results.facetFields = response.getFacetFields();
		results.resultsCount = response.getResults().getNumFound();
		results.queryTime = response.getQTime();
		
		for (SolrDocument doc : (List<SolrDocument>)response.getResults()) {
			String docId = doc.getFieldValue("id").toString();
			Publication pub = new Publication();
			pub.setKey(docId);
			pub.setYear((String)doc.getFieldValue("publicationYear"));
			pub.setTitle(doc.getFieldValue("title").toString());
			Set<Author> authors = Sets.newHashSet();
			for (Object authorName : doc.getFieldValues("author") ) {
				authors.add(new Author(authorName.toString()));
			}
			pub.setAuthor(authors);
			results.pubs.add(pub);
			/*Publication pub = ServiceLocator.getInstance().getDblpPubDao().findById(docId);
			if (pub != null) {
				results.pubs.add(pub);
			}*/
		}
		
		return results;
	}
	
	public String getSearchText() {
		return searchText;
	}
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
	public long getResultsCount() {
		return resultsCount;
	}
	public int getQueryTime() {
		return queryTime;
	}
	public List<Publication> getPubs() {
		return pubs;
	}
}
