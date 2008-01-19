package edu.ucdavis.cs.dblp.data;

import de.unitrier.dblp.Article;
import de.unitrier.dblp.Book;
import de.unitrier.dblp.Incollection;
import de.unitrier.dblp.Inproceedings;
import de.unitrier.dblp.Mastersthesis;
import de.unitrier.dblp.Phdthesis;
import de.unitrier.dblp.Proceedings;
import de.unitrier.dblp.Www;

public enum PublicationType {
	ARTICLE, 
	INPROCEEDINGS, 
	PROCEEDINGS, 
	BOOK, 
	INCOLLECTION, 
	PHDTHESIS, 
	MASTERSTHESIS, 
	WWW;
	
	public static PublicationType typeOf(Object publication) {
		PublicationType type;
		
		if (publication instanceof Article) {
			type = ARTICLE;
		} else if (publication instanceof Inproceedings) {
			type = INPROCEEDINGS;
		} else if (publication instanceof Proceedings) {
			type = PROCEEDINGS;
		} else if (publication instanceof Book) {
			type = BOOK;
		} else if (publication instanceof Incollection) {
			type = INCOLLECTION;
		} else if (publication instanceof Phdthesis) {
			type = PHDTHESIS;
		} else if (publication instanceof Mastersthesis) {
			type = MASTERSTHESIS;
		} else if (publication instanceof Www) {
			type = WWW;
		} else {
			throw new IllegalArgumentException("invalid Object to identify the " +
					"PublicationType for: "+publication);
		}
		
		return type;
	}
}
