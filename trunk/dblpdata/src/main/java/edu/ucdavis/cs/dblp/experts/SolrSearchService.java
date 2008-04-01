package edu.ucdavis.cs.dblp.experts;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.google.common.base.Join;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * 
 * @author pfishero
 */
public class SolrSearchService implements SearchService {
	private static final Logger logger = Logger.getLogger(SolrSearchService.class);
	
	private SolrServer server;
	private String queryType;
	private List<String> filterQueries;
	
	public SolrSearchService() {
		filterQueries = Lists.newLinkedList();
	}
	
	@Override
	public DblpResults fullTextSearch(String search) {
		DblpResults results = null;
		SolrQuery query = new SolrQuery();
		query.setQuery(search);
		query.setQueryType(queryType);
		
		try {
			QueryResponse response = server.query(query);
			results = DblpResults.fromQueryResponse(response, search);
		} catch (SolrServerException e) {
			logger.error("error while executing search:"+search+" -"+e);
		} catch (IOException e) {
			logger.error("error while executing search:"+search+" -"+e);
		}
		
		filterQueries.clear();
		
		return results;
	}

	@Override
	public DblpResults refineSearch(DblpResults context) {
		logger.info("refining search:"+context.getSearchText()+
				" with filters: "+Join.join(", ",filterQueries));
		
		DblpResults results = null;
		SolrQuery query = new SolrQuery();
		query.setQuery(context.getSearchText());
		query.setQueryType(queryType);
		query.setFilterQueries(filterQueries.toArray(new String[filterQueries.size()]));
		
		try {
			QueryResponse response = server.query(query);
			results = DblpResults.fromQueryResponse(response, context.getSearchText());
		} catch (SolrServerException e) {
			logger.error("error while executing search:"+context.getSearchText()+" -"+e);
		} catch (IOException e) {
			logger.error("error while executing search:"+context.getSearchText()+" -"+e);
		}
		
		return results;
	}
	
	@Override
	public void addFilterQuery(String filterQuery) {
		filterQueries.add(filterQuery);		
	}
	
	@Override
	public void removeFilterQuery(String filterQuery) {
		filterQueries.remove(filterQuery);
	}

	public SolrServer getServer() {
		return server;
	}

	public void setServer(SolrServer server) {
		this.server = server;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public List<String> getFilterQueries() {
		return filterQueries;
	}
	public void setFilterQueries(List<String> filterQueries) {
		this.filterQueries = filterQueries;
	}
}