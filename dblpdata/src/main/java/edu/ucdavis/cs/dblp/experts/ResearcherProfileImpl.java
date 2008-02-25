/**
 * 
 */
package edu.ucdavis.cs.dblp.experts;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Join;
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
	
	@Override
	public Author getResearcher() {
		return this.researcher;
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
	
	public Map<Author, Integer> getCoAuthorsCounts() {
		Map<Author, Integer> countMap = 
				new HashMap<Author, Integer>(coAuthors.size());
		
		for (Author author : coAuthors.elementSet()) {
			countMap.put(author, coAuthors.count(author));
		}
		
		return countMap;
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
	
	public Map<Keyword, Integer> getKeywordsCounts() {
		Map<Keyword, Integer> countMap = 
				new HashMap<Keyword, Integer>(keywords.size());
		
		for (Keyword keyword : keywords.elementSet()) {
			countMap.put(keyword, keywords.count(keyword));
		}
		
		return countMap;
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
	
	public Map<Category, Integer> getLeafCategoriesCounts() {
		Map<Category, Integer> countMap = 
				new HashMap<Category, Integer>(leafCategories.size());
		
		for (Category cat: leafCategories.elementSet()) {
			countMap.put(cat, leafCategories.count(cat));
		}
		
		return countMap;
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
			str.append(keyword.getKeyword()+" ("+keywords.count(keyword)+"), ");
		}
		str.append("\nCo-Authors:\n");
		for (Author coAuthor : getSortedCoAuthors()) {
			str.append(coAuthor.getContent()+" ("+coAuthors.count(coAuthor)+")\n");
		}
		str.append("\nPublications:\n");
		for (Publication pub : pubs) {
			str.append(pub.getTitle()+'\n');
			Join.join(str, ", ", Iterables.transform(pub.getAuthor(), 
					new Function<Author, String>() {
				@Override
				public String apply(Author author) {
					return author.getContent();
				}
			}));
			str.append('\n'+pub.getCitationString()+'\n');
		}
		
		str.append("\nCategories:\n");
		for (Category cat : getSortedLeafCategories()) {
			String parentKey = cat.getKey().split(" Subjects: ")[0];
			str.append(parentKey+'\n');
			str.append(cat.getLabel()).append(" (").append(leafCategories.count(cat)).append(")\n");
		}
		
		return str.toString();
	}

}
