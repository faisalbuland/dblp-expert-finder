/**
 * 
 */
package edu.ucdavis.cs.taxonomy;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
@Entity
@NamedQueries({
	@NamedQuery(name="Category.allCategories", query="FROM Category c"),
	@NamedQuery(name="Category.allLeafNodes", query="FROM Category c WHERE c.children IS EMPTY"),
	@NamedQuery(name="Category.byId", query="FROM Category c WHERE c.id=:id"),
	@NamedQuery(name="Category.byKey", query="FROM Category c WHERE c.key=:key"),
	@NamedQuery(name="Category.byKeyUpperCase", query="FROM Category c WHERE UPPER(c.key)=UPPER(:key)"),
	@NamedQuery(name="Category.byFreeTextSearch", query="FROM Category c WHERE UPPER(c.key) LIKE '%' || UPPER(:freeText) || '%' AND UPPER(c.label) LIKE '%' || UPPER(:freeText) || '%'"),
})
public class Category implements Serializable {
	private static final Logger logger = Logger.getLogger(Category.class);
	
	private String label;
	private String id;
	@Id
	@Column(name="CAT_KEY")
	private String key; 
	@ManyToOne
	private Category parent;
	@OneToMany(mappedBy="parent",cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Collection<Category> children;
	@Transient
	// TODO make non-transient
	private Collection<Category> relatedCategories;
	private boolean active = true;
	
	public Category() {
		children = new LinkedList<Category>();
		relatedCategories = new LinkedList<Category>();
	}
	
	public Category(String id, String label, Category parent) {
		this();
		this.id = id;
		this.label = label;
		this.parent = parent;
		generateKey();
	}
	
	/**
	 * Generates the key field for this Category.
	 * 
	 * Nodes are identified by their ID and LABEL, unless they are a Subjects
	 * Node in which case they are identified by their parent node's key
	 * plus " Subjects: " and their label.
	 */
	public final void generateKey() {
		StringBuilder keyBuilder = new StringBuilder();
		// common case - id + label identify the node
		keyBuilder.append(id).append(' ').append(label);
		if (relatedCategories.size() > 0) {
			keyBuilder.append(" (");
			List<String> relatedCatIds = new LinkedList<String>(); 
			for (Category related : relatedCategories) {
				relatedCatIds.add(related.getId());
			}
			keyBuilder.append(StringUtils.join(relatedCatIds, ", ")).
						append(')');
			logger.trace(keyBuilder.toString());
		}

		// exception case - Subjects node (i.e. a node without an id)
		if (StringUtils.isBlank(id) && null != parent && children.size() == 0) {
			keyBuilder = new StringBuilder();
			keyBuilder.append(parent.getKey()).append(" Subjects: ").append(
					label);
		}

		this.key = keyBuilder.toString().trim();
	}
	
	/**
	 * @param parent
	 */
	public void setParent(Category parent) {
		this.parent = parent;
	}
	
	/**
	 * @return
	 */
	public Category getParent() {
		return parent;
	}
	
	public boolean isLeaf() {
		return (children.size() == 0);
	}
	
	public void addChild(Category category) {
		children.add(category);
	}
	
	public Collection<Category> getChildren() {
		return children;
	}
	
	public void addRelatedCategory(Category category) {
		relatedCategories.add(category);
	}
	
	public Collection<Category> getRelatedCategories() {
		return relatedCategories;
	}
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return a unique key identifying this Category node.
	 */
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * @return a description of this node's parent (e.g. H.2.2 This and That).
	 */
	public String getParentDescription() {
		return this.getKey().split(" Subjects: ")[0];
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("key", getKey())
					.append("id", this.id)
					.append("label", this.label)
					.append("active", this.active)
//					commented out as this leads to data explosion
//					.append("\nchildren", this.children)
					.toString();
	}
}
