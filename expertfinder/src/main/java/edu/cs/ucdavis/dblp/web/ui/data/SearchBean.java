package edu.cs.ucdavis.dblp.web.ui.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.richfaces.model.TreeNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import edu.cs.ucdavis.dblp.web.ui.data.taxonomy.CategoryTree;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.taxonomy.Categories;
import edu.ucdavis.cs.taxonomy.Category;
import edu.ucdavis.cs.taxonomy.CategoryDao;

public class SearchBean implements Serializable {
	public static final Logger logger = Logger.getLogger(SearchBean.class);
	
	private String searchText;
	private Collection matches;
	private CategoryTree catTree;
	
	public SearchBean() { }

	// ---- Action handlers ----
	public String doSearch() {
		CategoryDao catDao = ServiceLocator.getInstance().getCategoryDao();
		
		List<Category> cats = catDao.findByFreeTextSearch(searchText);
		List<Category> leaves = ImmutableList.copyOf(Iterables.filter(cats, Categories.ONLY_LEAF_NODES));
		this.matches = leaves;
		
		catTree = new CategoryTree();
		catTree.asTree(this.matches);
		
		return "NODES_SEARCHED";
	}
	
	public TreeNode<Category> getCategoryTree() {
		TreeNode<Category> rootNode = null;
		if (catTree != null) {
			rootNode = catTree.getRoot();
		} 
		return rootNode;
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
	
	public Boolean isNodeExpanded(org.richfaces.component.UITree node) {
		return Boolean.TRUE;
	}
}
