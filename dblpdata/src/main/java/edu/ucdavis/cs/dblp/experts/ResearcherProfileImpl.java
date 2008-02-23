/**
 * 
 */
package edu.ucdavis.cs.dblp.experts;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.taxonomy.Categories;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * @author pfishero
 *
 */
public class ResearcherProfileImpl implements ResearcherProfile {
	public static final Logger logger = Logger.getLogger(ResearcherProfileImpl.class);

	private final Author researcher;
	private final Collection<Publication> pubs;
	private final Multiset<Author> coAuthors;
	private final Multiset<Keyword> keywords;
	private final Multiset<Category> leafCategories;
	
	public ResearcherProfileImpl(Author researcher) {
		ResearcherDao dao = ServiceLocator.getInstance().getResearcherDao();
		this.researcher = researcher;
		pubs = dao.findPublications(researcher);
		
		coAuthors = new HashMultiset<Author>(pubs.size(), 0.75f);
		keywords = new HashMultiset<Keyword>();
		leafCategories = new HashMultiset<Category>();
		
		for (Publication pub : pubs) {
			coAuthors.addAll(Lists.immutableList(
				Iterables.filter(pub.getAuthor(), new Predicate<Author>() {
				@Override
				public boolean apply(Author author) {
					if (ResearcherProfileImpl.this.researcher.equals(author)) {
						return false; // researcher cannot be its own co-author
					} else {
						return true;
					}
				}
			})));
			
			if (pub.getContent() != null) {
				keywords.addAll(pub.getContent().getKeywords());
				leafCategories.addAll(
						Lists.immutableList(
							Iterables.filter(
									pub.getContent().getCategories(), 
									Categories.ONLY_LEAF_NODES))
						);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.experts.ResearcherProfile#getCoAuthors()
	 */
	@Override
	public Multiset<Author> getCoAuthors() {
		return this.coAuthors;
	}
	
	public Collection<Author> getSortedCoAuthors() {
		Comparator<Author> comp = 
			new SortedComparator<Author>(this.coAuthors);
		
		List<Author> sortedCoAuthors = 
			Lists.sortedCopy(this.coAuthors.elementSet(), comp); 
		
		return sortedCoAuthors;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.experts.ResearcherProfile#getKeywords()
	 */
	@Override
	public Multiset<Keyword> getKeywords() {
		return this.keywords;
	}
	
	public Collection<Keyword> getSortedKeywords() {
		Comparator<Keyword> comp = 
			new SortedComparator<Keyword>(this.keywords);
		
		List<Keyword> sortedKeywords = 
			Lists.sortedCopy(this.keywords.elementSet(), comp); 
		
		return sortedKeywords;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.experts.ResearcherProfile#getPublications()
	 */
	@Override
	public Collection<Publication> getPublications() {
		return this.pubs;
	}
	
	@Override
	public Multiset<Category> getLeafCategories() {
		return leafCategories;
	}
	
	public Collection<Category> getSortedLeafCategories() {
		Comparator<Category> comp = 
			new SortedComparator<Category>(this.leafCategories);
		
		List<Category> sortedCategories = 
			Lists.sortedCopy(this.leafCategories.elementSet(), comp); 
		
		return sortedCategories;
	}
	
	private static final class SortedComparator<T> implements Comparator<T> {
		private final Multiset<T> multiset;
		public SortedComparator(Multiset<T> multiset) {
			this.multiset = multiset;
		}
		@Override
		public int compare(T o1, T o2) {
			int key1Count = multiset.count(o1); 
			int key2Count = multiset.count(o2);
			return key2Count - key1Count;
		}
	};
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("\nKeywords:\n");
		for (Keyword keyword : getSortedKeywords()) {
			str.append("<a href=\"#\">"+keyword.getKeyword()+"</a> ("+keywords.count(keyword)+"), ");
		}
		str.append("\nCo-Authors:\n");
		for (Author coAuthor : getSortedCoAuthors()) {
			str.append("<a href=\"#\">"+coAuthor.getContent()+"</a> ("+coAuthors.count(coAuthor)+")<br/>\n");
		}
		str.append("\nPublications:\n");
		for (Publication pub : pubs) {
			str.append("<div class=\"right_articles\">");
			str.append("<p><img src=\"images/image.gif\" alt=\"Image\" title=\"Image\" class=\"image\" />");
			str.append("<b>"+pub.getTitle()+"</b><br/>");
			str.append("Year: "+pub.getYear());
			str.append(pub.getJournal() != null ?
					" | Journal: "+pub.getJournal().getContent() : "");
			str.append("<br/>DBLP key: "+pub.getKey());
			str.append("</p></div>\n");
		}
		
		str.append("\nCategories:\n");
		for (Category cat : getSortedLeafCategories()) {
			str.append(cat+" ("+leafCategories.count(cat)+"), \n");
		}
		
		return str.toString();
	}

}
