/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import de.unitrier.dblp.Author;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
@Entity
public class Keyword implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;
	private String keyword;
	
	public Keyword() {	}
	
	/**
	 * @param keyword
	 */
	public Keyword(String keyword) {
		super();
		this.keyword = keyword;
	}

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
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
				append("Keyword", keyword).
				toString();
	}
	
	@Override
    public boolean equals(Object obj) {
    	if (obj instanceof Keyword == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		Keyword rhs = (Keyword) obj;
		return new EqualsBuilder()
					.append(this.keyword, rhs.keyword)
					.isEquals();
    }
    
    @Override
    public int hashCode() {
    	return new HashCodeBuilder(117, 5)
        			.append(this.keyword)
        			.toHashCode();
    }
}
