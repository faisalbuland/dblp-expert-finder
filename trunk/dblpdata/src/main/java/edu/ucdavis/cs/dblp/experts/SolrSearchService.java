package edu.ucdavis.cs.dblp.experts;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * 
 * @author pfishero
 */
public class SolrSearchService implements SearchService {
	private static final Logger logger = Logger.getLogger(SolrSearchService.class);
	
	private SolrServer server;
	private String queryType;
	
	@Override
	public DblpResults fullTextSearch(String search) {
		DblpResults results = null;
		SolrQuery query = new SolrQuery();
		query.setQuery(search);
		query.setQueryType(queryType);
		query.set("facet.missing", "false");
		
		try {
			QueryResponse response = server.query(query);
			results = DblpResults.fromQueryResponse(response, search);
		} catch (SolrServerException e) {
			logger.error("error while executing search:"+search+" -"+e);
		} catch (IOException e) {
			logger.error("error while executing search:"+search+" -"+e);
		}
		
		return results;
	}

	@Override
	public DblpResults refineSearch(Facet refiningFacet, DblpResults context) {
		// TODO Auto-generated method stub
		return null;
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
}