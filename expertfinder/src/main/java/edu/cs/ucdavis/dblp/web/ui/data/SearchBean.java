package edu.cs.ucdavis.dblp.web.ui.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.faces.component.UIComponent;

import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.taxonomy.Categories;
import edu.ucdavis.cs.taxonomy.Category;
import edu.ucdavis.cs.taxonomy.CategoryDao;

public class SearchBean implements Serializable {
	public static final Logger logger = Logger.getLogger(SearchBean.class);
	
	private String searchText;
	private Collection matches;
	
	public SearchBean() { }

	// ---- Action handlers ----
	public String doSearch() {
		CategoryDao catDao = ServiceLocator.getInstance().getCategoryDao();
		
		List<Category> cats = catDao.findByFreeTextSearch(searchText);
		List<Category> leaves = Lists.immutableList(Iterables.filter(cats, Categories.ONLY_LEAF_NODES));
		this.matches = leaves;
				
		return "NODES_SEARCHED";
	}		
	
	public boolean isMatchesFound() {
		boolean matchesFound;
		
		if (matches == null || matches.size() == 0) {
			matchesFound = false;
		} else {
			matchesFound = true;
		}
		
		return matchesFound;
	}
	
	// ---- Getters/Setters ----
	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
	
	public Collection getMatches() {
		return matches;
	}

	public void setMatches(Collection matches) {
		this.matches = matches;
	}

	public int getMatchesCount() {
		return matches == null ? 0 : matches.size();
	}
}
