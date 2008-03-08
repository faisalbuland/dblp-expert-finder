package edu.ucdavis.cs.dblp.experts;

import de.unitrier.dblp.Author;

/**
 * NullAuthor implements a no-op replacement for an Author.  This instance 
 * is equal to none and has the empty string as its name.
 * 
 * @author pfishero
 */
public class NullAuthor extends Author {
	
	public NullAuthor() {
		this.content = "";
	}
	
	@Override
	public boolean equals(Object obj) {
		return false;
	}
}
