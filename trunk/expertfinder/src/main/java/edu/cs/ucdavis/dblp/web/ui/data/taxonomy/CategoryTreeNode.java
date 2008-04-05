/**
 * 
 */
package edu.cs.ucdavis.dblp.web.ui.data.taxonomy;

import java.util.Iterator;
import java.util.Map;

import org.richfaces.model.TreeNode;

import edu.ucdavis.cs.taxonomy.Category;

/**
 * @author pfishero
 *
 */
public class CategoryTreeNode<Category> implements TreeNode<Category> {

	private final Category cat;
	
	public CategoryTreeNode(Category cat) {
		super();
		this.cat = cat;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.TreeNode#addChild(java.lang.Object, org.richfaces.model.TreeNode)
	 */
	@Override
	public void addChild(Object identifier, TreeNode<Category> child) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.TreeNode#getChild(java.lang.Object)
	 */
	@Override
	public TreeNode<Category> getChild(Object id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.TreeNode#getChildren()
	 */
	@Override
	public Iterator<Map.Entry<Object,TreeNode<Category>>> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.TreeNode#getData()
	 */
	@Override
	public Category getData() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.TreeNode#getParent()
	 */
	@Override
	public TreeNode<Category> getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.TreeNode#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.TreeNode#removeChild(java.lang.Object)
	 */
	@Override
	public void removeChild(Object id) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.TreeNode#setData(java.lang.Object)
	 */
	@Override
	public void setData(Category data) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.TreeNode#setParent(org.richfaces.model.TreeNode)
	 */
	@Override
	public void setParent(TreeNode<Category> parent) {
		// TODO Auto-generated method stub

	}

}
