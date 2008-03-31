package edu.ucdavis.cs.dblp.experts;


public interface SearchService {
	DblpResults fullTextSearch(String search);
	DblpResults refineSearch(Facet refiningFacet, DblpResults context);
}
