/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import static edu.ucdavis.cs.taxonomy.Categories.ONLY_LEAF_NODES;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.common.base.Join;
import com.google.common.collect.Iterables;

import edu.ucdavis.cs.taxonomy.Category;

/**
 * PublicationContent is the content external to a DBLP entry for a 
 * {@link Publication}.
 *  
 * @author pfishero
 * @version $Id$
 */
@Entity
public class PublicationContent implements Serializable {	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;
	@Lob
	@Column(length=1048576)
	protected String abstractText;
	@ManyToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	@JoinTable(
		name="PUBCONTENT_KWS"
	)
	protected Set<Keyword> keywords;
	@ManyToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST},fetch=FetchType.EAGER)
	@JoinTable(
		name="PUBCONTENT_CATS"
	)
	protected Set<Category> categories;
	
	public PublicationContent() {	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the abstractText
	 */
	public String getAbstractText() {
		return abstractText;
	}

	/**
	 * @param abstractText the abstractText to set
	 */
	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	/**
	 * @return the keywords
	 */
	public Set<Keyword> getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(Set<Keyword> keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return the categories
	 */
	public Set<Category> getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
			append("Keywords", this.keywords).
			append("Categories", Join.join(", ", 
					Iterables.filter(this.categories, ONLY_LEAF_NODES))).
			append("TruncatedAbstract", 
					this.abstractText != null ? 
							(this.abstractText.length() > 30 ?
							this.abstractText.substring(0, 30)
							: this.abstractText)
							: "").
			toString();
	}
}
