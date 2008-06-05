package edu.ucdavis.cs.dblp.experts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.data.PublicationType;

/**
 * @author pfishero
 */
@Transactional(propagation = Propagation.REQUIRED)
public class DblpResults {
	private static final Logger logger = Logger.getLogger(DblpResults.class);

	private int DEFAULT_FETCH_SIZE = Integer.MAX_VALUE;
	
	private String searchText;
	private long resultsCount;
	private int queryTime;
	private List<FacetField> facetFields;
	private List<Publication> pubs;
	private int start;
	private final Map<String, String> facetsToDisplayNames = Maps.immutableMap(
															"author_exact", "Authors", 
															"keyword_exact", "Keywords", 
															"categoryId_exact", "Categories", 
															"publishYear", "Years");
	
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
		results.start = response.getResults().getStart();
		results.queryTime = response.getQTime();
		
		for (SolrDocument doc : (List<SolrDocument>)response.getResults()) {
			String docId = doc.getFieldValue("id").toString();
			Publication pub = new Publication();
			pub.setKey(docId);
			pub.setYear((String)doc.getFieldValue("publicationYear"));
			pub.setTitle(doc.getFieldValue("title").toString());
			pub.setType(PublicationType.ARTICLE);
			Set<Author> authors = Sets.newHashSet();
			if (doc.getFieldValues("author") != null) {
				for (Object authorName : doc.getFieldValues("author") ) {
					authors.add(new Author(authorName.toString()));
				}
				pub.setAuthor(authors);
			} else {
				logger.error("no authors returned for pub "+pub.getKey());
			}
			results.pubs.add(pub);
			/*Publication pub = ServiceLocator.getInstance().getDblpPubDao().findById(docId);
			if (pub != null) {
				results.pubs.add(pub);
			}*/
		}
		
		return results;
	}
	
	public static Iterable<Publication> allResults(DblpResults initialResults, SearchService searchService) {
		Iterable<Publication> allPubs = initialResults.getPubs();
		int retrievedCount = initialResults.getPubs().size();
		DblpResults results = initialResults;

		while (results.hasMore()) {
			assert results.hasMore() : "code error - results did not have more";
			results = searchService.fetchMoreResults(results);
			retrievedCount += results.getPubs().size();
			allPubs = Iterables.concat(allPubs, results.getPubs());
		}
		
		assert retrievedCount == initialResults.getResultsCount() :
			"retrieved count != reported result count";
		return allPubs;
	}
	
	/**
	 * Resets the pagination state to go back to the start of the results.
	 */
	public void resetPagination() {
		start = 0;
		pubs.clear();
	}
	
	/**
	 * @return true if there are more results that can be retrieved from SOLR
	 * for the same query.
	 */
	public boolean hasMore() {
		boolean more = true;
		
		if (pubs.size()+start >= resultsCount) { 
			more = false;
		}
		
		return more;
	}
	
	/**
	 * Note: clients should call the state testing method {@link #hasMore()}
	 * before calling this method to determine if there exists more results.
	 * 
	 * @return the index of the next "starting" record to retrieve
	 */
	public int getNextStart() {
		return pubs.size()+start;
	}
	
	public int getRowFetchSize() {
		if (pubs.size() == 0) {
			return DEFAULT_FETCH_SIZE;
		} else {
			return pubs.size();
		}
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
	public List<FacetField> getFacetFields() {
		return facetFields;
	}
	
	public Map<String, FacetField> getFacetFieldsMap() {
		Map<String, FacetField> ffMap = new HashMap<String, FacetField>();
		
		for (FacetField field : getFacetFields()) {
			ffMap.put(facetsToDisplayNames.get(field.getName()), field);
		}
		
		return ffMap;
	}
}
