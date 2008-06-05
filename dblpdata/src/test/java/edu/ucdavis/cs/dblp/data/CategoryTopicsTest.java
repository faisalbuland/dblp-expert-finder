package edu.ucdavis.cs.dblp.data;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.base.Predicate;
import com.google.common.collect.Comparators;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.keywords.KeywordRecognizer;
import edu.ucdavis.cs.taxonomy.CategoryDao;

public class CategoryTopicsTest {
	private static final Logger logger = Logger.getLogger(CategoryTopicsTest.class);
	
	private static DblpPubDao dao;
	private static CategoryDao catDao;
	private static KeywordRecognizer recognizer;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception { 
		final ServiceLocator locator = ServiceLocator.getInstance();
		dao = locator.getDblpPubDao();
		catDao = locator.getCategoryDao();
		recognizer = (KeywordRecognizer) locator.getAppContext().getBean("simpleKeywordRecognizer");
	}
	
	public Multimap<String, Publication> topicsForCategory() throws Exception {
		List<Publication> pubs = dao.findByCategory(
				catDao.findByFreeTextSearch("Spatial Database").get(0));
		logger.info("pub count for spatial databases:"+pubs.size());
		assertTrue(pubs.size() > 0);
		
		for (Publication pub : pubs) {
			PublicationContent content;
			if (pub.getContent() != null) {
				content = pub.getContent();
			} else {
				content = new PublicationContent();
				content.setKeywords(new HashSet<Keyword>());
				pub.setContent(content);
			}
			
			Set<Keyword> keywords = content.getKeywords();
			Set<Keyword> recognizedKws = recognizer.findKeywordsIn(pub.getTitle()+' '+content.getAbstractText());

			Set<Keyword> allFoundKeywords = Sets.newHashSet();
			allFoundKeywords.addAll(keywords);
			allFoundKeywords.addAll(recognizedKws);
			content.setKeywords(allFoundKeywords);
		}
		
		Collections.sort(pubs, Comparators.fromFunction(
				Publication.FN_PUB_YEAR));
		logger.info("after sort - first pub year ="+pubs.get(0).getYear());
		logger.info("after sort - last pub year ="+pubs.get(pubs.size()-1).getYear());
		
		extractKeywords(pubs);
		
		// Multimap for the years
		Multimap<String, Publication> yearPubs = new LinkedHashMultimap<String, Publication>();
		for (Publication pub : pubs) {
			yearPubs.put(pub.getYear(), pub);
		}
		for (String year : yearPubs.keySet()) {
			logger.info("year: "+year+" ("+yearPubs.get(year).size()+')');
			extractKeywords(yearPubs.get(year));
		}
		
		return yearPubs;
	}
	
	public Multimap<String, String> extractYearKeywords() throws Exception {
		List<Publication> pubs = dao.findByCategory(
				catDao.findByFreeTextSearch("Spatial Database").get(0));
		logger.info("pub count for spatial databases:"+pubs.size());
		assertTrue(pubs.size() > 0);
		
		for (Publication pub : pubs) {
			PublicationContent content;
			if (pub.getContent() != null) {
				content = pub.getContent();
			} else {
				content = new PublicationContent();
				content.setKeywords(new HashSet<Keyword>());
				pub.setContent(content);
			}
			
			Set<Keyword> keywords = content.getKeywords();
			Set<Keyword> recognizedKws = recognizer.findKeywordsIn(pub.getTitle()+' '+content.getAbstractText());

			Set<Keyword> allFoundKeywords = Sets.newHashSet();
			allFoundKeywords.addAll(keywords);
			allFoundKeywords.addAll(recognizedKws);
			content.setKeywords(allFoundKeywords);
		}
		
		// TODO build a (subset of the main) controlled vocabulary for this category
		// and re-populate the publications keywords from this vocabulary
		
		Collections.sort(pubs, Comparators.fromFunction(
				Publication.FN_PUB_YEAR));
		logger.info("after sort - first pub year ="+pubs.get(0).getYear());
		logger.info("after sort - last pub year ="+pubs.get(pubs.size()-1).getYear());
		
		// extract keywords for all of the category data
//		extractKeywords(pubs);
		
		// Multimap for the years
		Multimap<String, Publication> yearPubs = new LinkedHashMultimap<String, Publication>();
		for (Publication pub : pubs) {
			yearPubs.put(pub.getYear(), pub);
		}
		// Multimap for the years keywords
		Multimap<String, String> yearKeywords = new LinkedHashMultimap<String, String>();
		for (String year : yearPubs.keySet()) {
			logger.info("year: "+year+" ("+yearPubs.get(year).size()+')');
			yearKeywords.putAll(year, extractKeywords(yearPubs.get(year)));
		}
		
		return yearKeywords;
	}
	
	public static void main(String ... argv) throws Exception {
		CategoryTopicsTest.setUpBeforeClass();
		CategoryTopicsTest ctt = new CategoryTopicsTest();
		new NetworkVisualization().radialVisualization(ctt.asGraph(ctt.extractYearKeywords()), "name");
//		new NetworkVisualization().radialVisualization(ctt.asGraph(ctt.topicsForCategory(), 1), "name");
//		new NetworkVisualization().radialVisualization(ctt.asGraph(ctt.topicsForCategory(), 2), "name");
//		new NetworkVisualization().graphVisualization(ctt.asGraph(ctt.topicsForCategory(), 2));
	}
	
	public Multiset<String> extractKeywords(final Iterable<Publication> pubs) {
		final Multiset<String> keywordPubs = new HashMultiset<String>();
		final List<String> allKeywords = Lists.newLinkedList();
		Iterables.addAll(allKeywords, 
				Iterables.concat(
						Iterables.transform(
								Iterables.filter(pubs, Publication.PRED_HAS_CONTENT), 
								Publication.FN_PUB_KEYWORDS)));
		Iterables.addAll(keywordPubs, 
				Iterables.transform(
							recognizer.removeLowInformationKeywords(
								recognizer.findKeywordsIn(Join.join(" ", allKeywords))),
								Keyword.FN_KEYWORD_STRINGS
								));
		if (logger.isDebugEnabled()) {
			logger.debug(Join.join("\n", Iterables.transform(
					Sets.newHashSet(Iterables.filter(keywordPubs, 
						new Predicate<String>() {
							@Override
							public boolean apply(String keyword) {
								return keywordPubs.count(keyword) > 1;
							}
						})),
						new Function<String, String>() {
							@Override
							public String apply(String str) {
								return str+" ("+keywordPubs.count(str)+')';
							}
						})));
		}
		
		return keywordPubs;
	}
	
	protected static final String SRC = Graph.DEFAULT_SOURCE_KEY;
    protected static final String TRG = Graph.DEFAULT_TARGET_KEY;
    
	public Graph asGraph(Multimap<String, Publication> yearPubs, int minFrequency) {		
		Schema nodeSchema = new Schema();
		nodeSchema.addColumn("name", String.class);
		nodeSchema.addColumn("type", String.class);
		Schema edgeSchema = new Schema();
		edgeSchema.addColumn(SRC, int.class);
        edgeSchema.addColumn(TRG, int.class);
        
		Table nodes = nodeSchema.instantiate();
		Table edges = edgeSchema.instantiate();
		
		Map<String, Integer> keywordIds = Maps.newHashMap();
		
		final int rootRowId = nodes.addRow();
		nodes.set(rootRowId, "name", "Years");
		nodes.set(rootRowId, "type", "year");
		
		for (String year : yearPubs.keySet()) {
			final Multiset<String> keywordPubs = new HashMultiset<String>();
			Iterables.addAll(keywordPubs, 
					Iterables.concat(
							Iterables.transform(
									Iterables.filter(yearPubs.get(year), Publication.PRED_HAS_CONTENT), 
									Publication.FN_PUB_KEYWORDS)));
			
			final int rowId = nodes.addRow();
			nodes.set(rowId, "name", year);
			nodes.set(rowId, "type", "year");
			
			// connect all years to the root year
			final int toRootRowId = edges.addRow();
			edges.setInt(toRootRowId, SRC, rootRowId);
			edges.setInt(toRootRowId, TRG, rowId);
			
			for (String keyword : extractKeywords(yearPubs.get(year)).elementSet() ) {
				if (keywordPubs.count(keyword) >= minFrequency) {
					// add a node for this keyword, if one does not exist already
					final int keywordRowId;
					if (!keywordIds.containsKey(keyword.toLowerCase())) {
						keywordRowId = nodes.addRow();
						nodes.set(keywordRowId, "name", keyword);
						nodes.set(keywordRowId, "type", "keyword");
						keywordIds.put(keyword.toLowerCase(), keywordRowId);
					} else {
						keywordRowId = keywordIds.get(keyword.toLowerCase());
					}
					
					// add an edge
					final int edgeRowId = edges.addRow();
					edges.setInt(edgeRowId, SRC, rowId);
					edges.setInt(edgeRowId, TRG, keywordRowId);
				}
			}
		}
		
		return new Graph(nodes, edges, false);
	}
	
	public Graph asGraph(Multimap<String, String> yearKeywords) {		
		Schema nodeSchema = new Schema();
		nodeSchema.addColumn("name", String.class);
		nodeSchema.addColumn("type", String.class);
		Schema edgeSchema = new Schema();
		edgeSchema.addColumn(SRC, int.class);
        edgeSchema.addColumn(TRG, int.class);
        
		Table nodes = nodeSchema.instantiate();
		Table edges = edgeSchema.instantiate();
		
		Map<String, Integer> keywordIds = Maps.newHashMap();
		
		final int rootRowId = nodes.addRow();
		nodes.set(rootRowId, "name", "Years");
		nodes.set(rootRowId, "type", "year");
		
		for (String year : yearKeywords.keySet()) {
			final Collection<String> keywords = yearKeywords.get(year);
			
			final int rowId = nodes.addRow();
			nodes.set(rowId, "name", year);
			nodes.set(rowId, "type", "year");
			
			// connect all years to the root year
			final int toRootRowId = edges.addRow();
			edges.setInt(toRootRowId, SRC, rootRowId);
			edges.setInt(toRootRowId, TRG, rowId);
			
			for (String keyword : yearKeywords.get(year) ) {
				// add a node for this keyword, if one does not exist already
				final int keywordRowId;
				if (!keywordIds.containsKey(keyword.toLowerCase())) {
					keywordRowId = nodes.addRow();
					nodes.set(keywordRowId, "name", keyword);
					nodes.set(keywordRowId, "type", "keyword");
					keywordIds.put(keyword.toLowerCase(), keywordRowId);
				} else {
					keywordRowId = keywordIds.get(keyword.toLowerCase());
				}
				
				// add an edge
				final int edgeRowId = edges.addRow();
				edges.setInt(edgeRowId, SRC, rowId);
				edges.setInt(edgeRowId, TRG, keywordRowId);
			}
		}
		
		return new Graph(nodes, edges, false);
	}

}
