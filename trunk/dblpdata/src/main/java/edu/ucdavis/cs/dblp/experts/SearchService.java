package edu.ucdavis.cs.dblp.experts;

import java.util.List;


public interface SearchService {
	DblpResults fullTextSearch(String search);
	DblpResults refineSearch(DblpResults context);
	List<String> getFilterQueries();
	void addFilterQuery(String filterQuery);
	void removeFilterQuery(String filterQuery);
}
