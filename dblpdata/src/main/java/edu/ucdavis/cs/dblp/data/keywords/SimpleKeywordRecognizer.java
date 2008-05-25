/**
 * 
 */
package edu.ucdavis.cs.dblp.data.keywords;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.Dictionary;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.TrieDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import edu.ucdavis.cs.dblp.analyzers.TokenizerService;
import edu.ucdavis.cs.dblp.data.DblpKeywordDao;
import edu.ucdavis.cs.dblp.data.Keyword;

/**
 * @author pfishero
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class SimpleKeywordRecognizer implements KeywordRecognizer {
	private static final Logger logger = Logger.getLogger(SimpleKeywordRecognizer.class);
	private static final String TYPE = "DBLP_KEYWORD";

	private Dictionary<String> keywordDict;
	private DblpKeywordDao dao;
	private TokenizerService tokenizer;
	private BiMap<String, String> acronymMap = Maps.newHashBiMap();
	
	@PostConstruct
	public void populateKeywordDictionary() {
		Preconditions.checkState(dao != null, 
				"keyword dao must be set prior to populating keyword dictonary");
		keywordDict = new TrieDictionary<String>();
		final double score = 0.0;

		Multiset<Integer> wordCounts = new HashMultiset<Integer>();
		Pattern trailingAcronym = Pattern.compile("^([^()]+)\\(([A-Z][a-zA-Z/-]+)\\)$");
		// acronyms/words to not add, as these produce too many false positive matches
		Set<String> stopAcronyms = Sets.newHashSet("IT", "USE", "IM", "ITS", "ROD", 
				"AS", "MR", "TE", "OU", "AN", "AD", "AND", "THE");
		
		for(Keyword keyword : Sets.newHashSet(dao.findAll())) {
			keyword.setKeyword(keyword.getKeyword().replace("&amp;", "&"));
			keyword.setKeyword(keyword.getKeyword().replace("&amp", "&"));
			keyword.setKeyword(keyword.getKeyword().replaceAll("&[^\\s]+", ""));
			keyword.setKeyword(keyword.getKeyword().replaceAll("\\s+", " "));
			
			Matcher m = trailingAcronym.matcher(keyword.getKeyword());
			if (m.matches()) {
				if (m.groupCount() == 2) {
					String expandedForm = m.group(1).trim();
					String acronym = m.group(2).trim();
					if (!stopAcronyms.contains(acronym.toUpperCase())) {
						logger.debug("acronym "+acronym+" = "+expandedForm);
						if (!acronymMap.containsKey(acronym) && 
								!acronymMap.containsValue(acronym) &&
								!acronymMap.containsKey(expandedForm) &&
								!acronymMap.containsValue(expandedForm)) {
							acronymMap.put(acronym, expandedForm);
						} else {
							logger.info("not inserting existing key into acronymMap.  key="+acronym);
						}
						addKwToDict(score, expandedForm);
						addKwToDict(score, acronym);
					}
				} else {
					logger.error("trailing acronym regex didn't work!!");
				}
			} else {
				if (keyword.getKeyword().length() > 2
						&& !stopAcronyms.contains(keyword.getKeyword().toUpperCase())) {
					addKwToDict(score, keyword.getKeyword());
					
					if (logger.isDebugEnabled()) {
						String[] words = keyword.getKeyword().split("\\s+");
						wordCounts.add(words.length);
						if (words.length > 5) {
							logger.debug(">5 words = "+Join.join(" ", words));
						}
					}
				} // else, skip one or two character keywords
			}
		}
		
		logger.debug("populated dictionary with "+keywordDict.size()+" keywords");
		if (logger.isDebugEnabled()) {
			for (Integer wordNum : wordCounts.elementSet()) {
				logger.debug("keywords with "+wordNum+" word(s) = "+wordCounts.count(wordNum));
			}
		}
	}

	/**
	 * @param score
	 * @param keyword
	 */
	private void addKwToDict(final double score, String keyword) {
		keywordDict.addEntry(new DictionaryEntry<String>(keyword.trim(), TYPE, score));
		logger.trace("adding keyword to dictionary: "+keyword);
	}
	
	@PostConstruct
	public void initTokenizerService() {
		tokenizer = new TokenizerService();
	}

	/**
	 * @param dao the Keyword DAO to set
	 */
	@Resource
	public void setDao(DblpKeywordDao dao) {
		this.dao = dao;
	}
	
	private static final Comparator<Keyword> LENGTH_COMPARATOR = new Comparator<Keyword>() {
		public int compare(Keyword kw1, Keyword kw2) {
			return kw2.getKeyword().length() - kw1.getKeyword().length();
		};
	};
	
	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.data.keywords.KeywordRecognizer#findKeywordsIn(java.lang.String)
	 */
	@Override
	public Set<Keyword> findKeywordsIn(String text) {
		Preconditions.checkArgument(text != null);
		Set<Keyword> foundKeywords = Sets.newHashSet();
		
		Chunker chunker = new ExactDictionaryChunker(keywordDict, 
								IndoEuropeanTokenizerFactory.FACTORY, 
								true, // return all matches 
								false); // case insensitive
		
		Chunking chunking = chunker.chunk(text);
		String theText = chunking.charSequence().toString();
		List<Chunk> chunks = Lists.newArrayList(chunking.chunkSet());
		Collections.sort(chunks, Chunk.LONGEST_MATCH_ORDER_COMPARATOR);

		for (Chunk chunk : chunks) {
			int start = chunk.start();
		    int end = chunk.end();
		    String chunkText = theText.substring(start,end).trim();
			chunkText = chunkText.replaceAll("\\s+", " ");
		    Keyword foundKeyword = new Keyword(chunkText);
		    foundKeywords.add(foundKeyword);
		}
		foundKeywords = disambiguateKeywords(foundKeywords);
		if (logger.isDebugEnabled()) {
			List<Keyword> keywordList = Lists.newArrayList(foundKeywords);
			Collections.sort(keywordList, LENGTH_COMPARATOR);
			logger.debug("found keywords: \n"+Join.join("\n", keywordList));
		}
		
		return foundKeywords;
	}
	
	private static final Function<Keyword, String> FN_KEYWORD_NAME = new Function<Keyword, String>() { 
		@Override
		public String apply(Keyword keyword) {
			return keyword.getKeyword();
		}
	};
	private static final Function<String, Keyword> FN_KEYWORD_FROM_STR = new Function<String, Keyword>() { 
		@Override
		public Keyword apply(String arg0) {
			return new Keyword(arg0);
		}
	};
	
	private Set<Keyword> disambiguateKeywords(Set<Keyword> keywords) {
		List<String> keywordsList = 
			Lists.newArrayList(Iterables.transform(keywords, FN_KEYWORD_NAME));

		Collections.sort(keywordsList, String.CASE_INSENSITIVE_ORDER);
		
		keywordsList = reduceKeywords(keywordsList);
		
		Set<Keyword> mungedKeywords = Sets.newHashSet(
				Iterables.transform(keywordsList, FN_KEYWORD_FROM_STR));
		
		return mungedKeywords;
	}
	
	/**
	 * Removes duplicates (stemmed form) and removes keywords that are contained within 
	 * other keywords, for example, 'spatial', 'spatial database', 'spatial databases' would 
	 * be reduced to 'spatial databases'.
	 * 
	 * @param keywords
	 * @return
	 */
	public List<String> reduceKeywords(List<String> keywords) {
		List<String> keywordsList = Lists.newArrayList();
		
		for (String keyword : keywords) {
			boolean keepKeyword = true;
			for (String otherKeyword : keywords) {
				if (!keyword.equals(otherKeyword)) {
					String otherKwStemLc = tokenizer.stemAllTokens(otherKeyword.toLowerCase());
					String kwStemLc = tokenizer.stemAllTokens(keyword.toLowerCase());
					if (otherKwStemLc.contains(kwStemLc) &&
							// if they are equal but differ in case, throw out the one with 
							// the lesser hash code
							!(otherKeyword.equalsIgnoreCase(keyword) && 
									otherKeyword.hashCode() <= keyword.hashCode()) &&
							// if their stemmed versions are equal but their normal forms are not,
							// throw out the shorter one or the one with a larger hash code 
							// if their lengths are equal
							!(!otherKeyword.equals(keyword) && 
									otherKeyword.length() < keyword.length()) ) {
						keepKeyword = false;
					}
				}
			}
			if (keepKeyword) keywordsList.add(keyword);
		}
		
		return keywordsList;
	}
	
	@Override
	public List<Keyword> removeLowInformationKeywords(Iterable<Keyword> keywords) {
		List<Keyword> refinedKeywords = Lists.newLinkedList(keywords);
		
		// remove non-informative keywords
		// * single token keywords that don't have the first two letters capitalized
		for (Iterator<Keyword> iter = refinedKeywords.iterator(); iter.hasNext(); ) {
			Keyword kw = iter.next();
			// strip punc
			String kwTemp = kw.getKeyword().replace("_", " ").replace("-", " ").replace("\\", " ").replace("/", " ");
			String[] tokens = kwTemp.split("\\s+");
			boolean firstTwoCharsCaps = kwTemp.length() >= 2 &&
										Character.isUpperCase(kwTemp.charAt(0)) &&
										Character.isUpperCase(kwTemp.charAt(1));
			if ((tokens.length < 2) && !firstTwoCharsCaps) {
				logger.debug("removing non-informative keyword: "+kwTemp);
				iter.remove();
			}
		}
		
		return refinedKeywords;
	}
	
	@Override
	public BiMap<String, String> getAcronymMap() {
		return acronymMap;
	}
	
	@Test
	public void testLongestMatch1() {
		initTokenizerService();
		List<String> testKws = ImmutableList.of("spatial", "spatial data", "spatial database", "spatial databases");
		List<String> processedKws = reduceKeywords(testKws);
		logger.info(processedKws);
	}
	
	@Test
	public void testLongestMatch2() {
		initTokenizerService();
		List<String> testKws = ImmutableList.of("spatial", "Spatial");
		List<String> processedKws = reduceKeywords(testKws);
		logger.info("dup with diff case = "+processedKws);
	}
	
	@Test
	public void testLongestMatch3() {
		initTokenizerService();
		List<String> testKws = ImmutableList.of("geographic information retrieval", "Geographic Information Retrieval (GIR)", "Geographical Information Retrieval");
		List<String> processedKws = reduceKeywords(testKws);
		logger.info("dup with extra acronym = "+processedKws);
	}
	
	@Test
	public void testLongestMatch4() {
		initTokenizerService();
		List<String> testKws = ImmutableList.of("geographic information systems", "Geographical Information Systems");
		List<String> processedKws = reduceKeywords(testKws);
		logger.info("dup with extra acronym = "+processedKws);
	}

}