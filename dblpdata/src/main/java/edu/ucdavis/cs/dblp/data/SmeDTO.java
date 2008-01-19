/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
public class SmeDTO {
	private String catKey;
	private Author author;
	private Long count;
	
	/**
	 * @param cat
	 * @param author
	 * @param count
	 */
	public SmeDTO(String catKey, Author author, Long count) {
		super();
		this.catKey = catKey;
		this.author = author;
		this.count = count;
	}
	
	
	/**
	 * @param author
	 * @param count
	 */
	public SmeDTO(Author author, Long count) {
		super();
		this.author = author;
		this.count = count;
	}


	/**
	 * @return the author
	 */
	public Author getAuthor() {
		return author;
	}
	/**
	 * @param author the author to set
	 */
	public void setAuthor(Author author) {
		this.author = author;
	}
	/**
	 * @return the count
	 */
	public Long getCount() {
		return count;
	}
	/**
	 * @param count the count to set
	 */
	public void setCount(Long count) {
		this.count = count;
	}
}
