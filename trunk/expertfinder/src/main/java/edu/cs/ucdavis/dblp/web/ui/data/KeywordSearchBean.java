package edu.cs.ucdavis.dblp.web.ui.data;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.experts.DblpResults;
import edu.ucdavis.cs.dblp.experts.SearchService;

/**
 * TODO unify with {@link SearchBean}.
 * @author pfishero
 */
public class KeywordSearchBean {
	private static final Logger logger = Logger.getLogger(KeywordSearchBean.class);
	
	private String searchText;
	private DblpResults results;
	private String refinementValue;
	private String refinementName;
	private String refinementFacetName;
	private SearchService searchService;
	private Map<String, Refinement> addedRefinements;
	
	private int currentPage = 1;
	
	public KeywordSearchBean() {
		searchService = ServiceLocator.getInstance().getSearchService();
		addedRefinements = Maps.newHashMap();
	}

	// ---- Action handlers ----
	public String doSearch() {		
		this.results = searchService.fullTextSearch(searchText);
		this.currentPage = 1;
		return null;
	}
	
	public boolean isResultsFound() {
		boolean resultsFound;
		
		if (results == null || results.getResultsCount() == 0) {
			resultsFound = false;
		} else {
			resultsFound = true;
		}
		
		return resultsFound;
	}
	
	public String refine() {
		searchService.addFilterQuery(refinementValue);
		addedRefinements.put(refinementValue, 
							new Refinement(getRefinementName(), getRefinementFacetName()));
		this.results = searchService.refineSearch(this.results);
		this.currentPage = 1;
		
		return null;
	}
	
	public String clearSearchText() {
		this.searchText = null;
		if (searchService.getFilterQueries() == null || 
				searchService.getFilterQueries().size() == 0) {
			this.results = null;
			this.refinementValue = null;
		}
		
		return "TOPIC_SEARCH_CLEARED";
	}
	
	public String clearFacet() {
		searchService.removeFilterQuery(refinementValue);
		addedRefinements.remove(refinementValue);
		this.results = searchService.refineSearch(this.results);
		
		return null;
	}
	
	// ---- Getters/Setters ----
	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public DblpResults getResults() {
		return results;
	}
	
	public DblpDataModel getDataModel() {
		return new DblpDataModel(searchService, results, 
					ServiceLocator.getInstance().getDblpPubDao());
	}

	public String getRefinementValue() {
		return refinementValue;
	}

	public void setRefinementValue(String refinementValue) {
		this.refinementValue = refinementValue;
	}
	
	public String getRefinementName() {
		return refinementName;
	}
	
	public void setRefinementName(String refinementName) {
		this.refinementName = refinementName;
	}
	
	public String getRefinementFacetName() {
		return refinementFacetName;
	}
	
	public void setRefinementFacetName(String refinementFacetName) {
		this.refinementFacetName = refinementFacetName;
	}
	
	public List<String> getRefinements() {
		return searchService.getFilterQueries();
	}
	
	public Map<String, Refinement> getAddedRefinements() {
		return addedRefinements;
	}
	
	public static class Refinement {
		private final String name;
		private final String facetName;
		
		public Refinement(String name, String facetName) {
			super();
			this.name = name;
			this.facetName = facetName;
		}

		public String getName() {
			return name;
		}

		public String getFacetName() {
			return facetName;
		}		
	}
	
	public int getPage() {
		return currentPage;
	}
	
	public void setPage(int page) {
		this.currentPage = page;
	}
}