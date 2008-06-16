package edu.ucdavis.cs.dblp.text;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;

/**
 * Simple representation of a {@link Publication} for use in text analysis/visualization.
 * 
 * @author pfishero
 */
public class SimplePub implements Serializable {
//	this is the ID for the object prior to having author names / abstracts
//	private static final long serialVersionUID = -4222535796036860492L;
	private static final long serialVersionUID = 1715527380252479027L;
	
	private String title;
	private String key;
	private String year;
	private String abstractText;
	private List<String> authorNames;
	private Set<Keyword> keywords = Sets.newHashSet();
	private static final transient Map<Keyword, Keyword> KW_CACHE = Maps.newConcurrentHashMap();
	
	public SimplePub(Publication pubToCopy) {
		this.title = pubToCopy.getTitle();
		this.key = pubToCopy.getKey();
		this.year = pubToCopy.getYear();
		authorNames = Lists.newLinkedList(Iterables.transform(pubToCopy.getAuthor(), Author.FN_AUTHOR_NAME));
		if (pubToCopy.getContent() != null) {
			if (pubToCopy.getContent().getKeywords() != null) {
				for(Keyword kw : pubToCopy.getContent().getKeywords()) {
					if (!KW_CACHE.containsKey(kw)) {
						KW_CACHE.put(kw, kw);
					}
					keywords.add(KW_CACHE.get(kw));
				}
			}
			if (StringUtils.isNotBlank(pubToCopy.getContent().getAbstractText())) {
				this.abstractText = pubToCopy.getContent().getAbstractText();
			}
		}
	}
	
	public static final Iterable<SimplePub> fromAll(Iterable<Publication> pubs) {
		List<SimplePub> simplePubs = Lists.newLinkedList();
		
		for (Publication pub : pubs) {
			simplePubs.add(new SimplePub(pub));
		}
		
		return simplePubs;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the keywords
	 */
	public Set<Keyword> getKeywords() {
		return keywords;
	}
	
	public void setKeywords(Set<Keyword> keywords) {
		this.keywords = keywords;
	}
	
	/**
	 * @return the abstractText
	 */
	public String getAbstractText() {
		return abstractText;
	}

	/**
	 * @return the authorNames
	 */
	public List<String> getAuthorNames() {
		return authorNames;
	}

	/**
	 * @return the publication year
	 */
	public String getYear() {
		return year;
	}
	
	public static final Function<SimplePub, Iterable<String>> FN_SIMPLPUB_KEYWORDS = 
		new Function<SimplePub, Iterable<String>>() {
			@Override
			public Iterable<String> apply(SimplePub pub) {
				return Iterables.transform(pub.getKeywords(), 
						new Function<Keyword, String>() {
							@Override
							public String apply(Keyword keyword) {
								return keyword.getKeyword();
							}
						});
			}
		};
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 17;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SimplePub other = (SimplePub) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
				append("key", key).
				append("title", title).
				append("year", year).
				append("keywords", keywords).
				append("abstract", abstractText).
				append("authors", authorNames).
				toString();
	}
}
