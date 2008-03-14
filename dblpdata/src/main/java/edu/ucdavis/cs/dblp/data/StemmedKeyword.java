/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.util.StringTokenizer;

import net.sf.snowball.ext.PorterStemmer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

/**
 * {@link Keyword} that considers itself equal to other Keywords whose content
 * matches after stemming has been applied.
 * 
 * @author pfishero
 *
 */
public class StemmedKeyword extends Keyword {
	public static final Logger logger = Logger.getLogger(StemmedKeyword.class);
	
	public StemmedKeyword(Keyword toCopy) {
		this.setId(toCopy.getId());
		this.setKeyword(toCopy.getKeyword());
	}
	
    @Override
    public int hashCode() {
    	return new HashCodeBuilder(107, 13)
        			.append(stem(this.getKeyword()))
        			.toHashCode();
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
					.append(stem(this.getKeyword()), stem(rhs.getKeyword()))
					.isEquals();
    }
	
	private final String stem(String in) {
		StringBuilder str = new StringBuilder();
		PorterStemmer stemmer = new PorterStemmer();
		StringTokenizer tokenizer = new StringTokenizer(in);
		
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			stemmer.setCurrent(token);
			stemmer.stem();
			str.append(stemmer.getCurrent()).append(' ');			
		}
		
		logger.debug("stemmed "+in+" down to "+str.toString());
		
		return str.toString();
	}
}
