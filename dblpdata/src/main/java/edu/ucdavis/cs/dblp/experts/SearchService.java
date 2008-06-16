package edu.ucdavis.cs.dblp.experts;

import java.util.List;


public interface SearchService {
	DblpResults fullTextSearch(String search);
	DblpResults refineSearch(DblpResults context);
	DblpResults fetchMoreResults(DblpResults context);
	DblpResults fetchResultsByRange(DblpResults context, int firstRow, int numberOfRows);
	int getDocFrequency(String phrase);
	List<String> getFilterQueries();
	void addFilterQuery(String filterQuery);
	void removeFilterQuery(String filterQuery);
	void clearFilterQueries();
}