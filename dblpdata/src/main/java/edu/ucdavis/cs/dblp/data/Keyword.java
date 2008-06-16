/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.ucdavis.cs.dblp.analyzers.TokenizerService;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
@Entity
@NamedQueries({
	@NamedQuery(name="Keyword.byId", query="FROM Keyword k WHERE k.id = :id"),
	@NamedQuery(name="Keyword.byName", query="FROM Keyword k WHERE UPPER(k.keyword) = UPPER(:keyword)"),
	@NamedQuery(name="Keyword.all", query="SELECT DISTINCT k FROM Keyword k ORDER BY k.keyword ASC"),
})
public class Keyword implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5944617676640723866L;	
	
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
	
	public static final Collection<Keyword> fromAll(Iterable<String> kws) {
		List<Keyword> keywords = Lists.newLinkedList();
		
		for (String kw : kws) {
			keywords.add(new Keyword(kw));
		}
		
		return keywords;
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
	
	private static final TokenizerService tokenizer = new TokenizerService();
	
	@Override
    public boolean equals(Object obj) {
    	if (obj instanceof Keyword == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		Keyword rhs = (Keyword) obj;
		// DANGER! XXX This is a major kludge to support reduction of keywords.
		// This may break many other things, but it is being implemented for the
		// purpose of SimpleKeywordRecognizer.produceSimpleControlledVocabulary(...).
		String otherKwStemLc = tokenizer.stemAllTokens(rhs.keyword.toLowerCase());
		String kwStemLc = tokenizer.stemAllTokens(this.keyword.toLowerCase());
		return new EqualsBuilder()
					.append(kwStemLc, otherKwStemLc)
					.isEquals();
    }
    
    @Override
    public int hashCode() {
    	return new HashCodeBuilder(117, 5)
        			.append(this.keyword)
        			.toHashCode();
    }
    
    // Google collections convenience methods for Keywords
	public static final Function<Keyword, String> FN_KEYWORD_STRINGS = 
		new Function<Keyword, String>() {
			@Override
			public String apply(Keyword keyword) {
				return keyword.getKeyword();
			}
		};
}
