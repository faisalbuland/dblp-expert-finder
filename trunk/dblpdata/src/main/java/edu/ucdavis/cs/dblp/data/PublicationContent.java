/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

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
/*	@OneToOne(optional=false, mappedBy="publicationContent")
	protected Publication publication;*/
	@Lob
	@Column(length=1048576)
	protected String abstractText;
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(
		name="PUBCONTENT_KWS"
	)
	protected Set<Keyword> keywords;
	@ManyToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST})
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
	 * @return the publication
	 *//*
	public Publication getPublication() {
		return publication;
	}

	*//**
	 * @param publication the publication to set
	 *//*
	public void setPublication(Publication publication) {
		this.publication = publication;
	}*/

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
}
