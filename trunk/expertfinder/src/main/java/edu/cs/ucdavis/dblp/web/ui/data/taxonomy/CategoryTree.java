package edu.cs.ucdavis.dblp.web.ui.data.taxonomy;

import java.util.Map;

import org.apache.log4j.Logger;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import edu.ucdavis.cs.taxonomy.Category;

public class CategoryTree {
	private static final Logger logger = Logger.getLogger(CategoryTree.class);
	
	private final Map<Category, TreeNode<Category>> catsInTree = Maps.newHashMap(); 
	private TreeNode<Category> root;
	
	public TreeNode<Category> asTree(Category leafCat) {
		Preconditions.checkArgument(leafCat.isLeaf(), "leafCat ("+leafCat+") must be a leaf");
		buildTreeNodeFor(leafCat);
		
		assert root != null : "no root set after building TreeNode for all ancestors of: "+leafCat;
		return root;
	}
	
	public TreeNode<Category> asTree(Iterable<Category> leafCategories) {
		for (Category cat : leafCategories) {
			asTree(cat);
		}
		
		return root;
	}
	
	public TreeNode<Category> buildTreeNodeFor(Category cat) {
		Preconditions.checkNotNull(cat);
		TreeNode<Category> treeNode = new TreeNodeImpl<Category>();
		
		for (Category childCat : cat.getChildren()) {
			// skip children not already in tree
			if (catsInTree.containsKey(childCat)) {
				treeNode.addChild(childCat.getKey(), catsInTree.get(childCat));
			} else {
				logger.debug("skipping child that not in catsInTreeMap: "+childCat);
			}
		}
		treeNode.setData(cat);
		catsInTree.put(cat, treeNode);
		
		if (cat.getParent() == null) {
			root = treeNode;
		} else {
			treeNode.setParent(buildTreeNodeFor(cat.getParent()));
		}
		
		return treeNode;
	}
	
	public TreeNode<Category> getRoot() {
		return root;
	}
}
