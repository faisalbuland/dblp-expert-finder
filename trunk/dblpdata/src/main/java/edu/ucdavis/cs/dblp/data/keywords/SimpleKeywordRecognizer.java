/**
 * 
 */
package edu.ucdavis.cs.dblp.data.keywords;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;

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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.ucdavis.cs.dblp.analyzers.TokenizerService;
import edu.ucdavis.cs.dblp.data.DblpKeywordDao;
import edu.ucdavis.cs.dblp.data.Keyword;

/**
 * @author pfishero
 *
 */
@Service
public class SimpleKeywordRecognizer implements KeywordRecognizer {
	private static final Logger logger = Logger.getLogger(SimpleKeywordRecognizer.class);
	private static final String TYPE = "DBLP_KEYWORD";

	private Dictionary<String> keywordDict;
	private DblpKeywordDao dao;
	private TokenizerService tokenizer;
	
	@PostConstruct
	public void populateKeywordDictionary() {
		Preconditions.checkState(dao != null, 
				"keyword dao must be set prior to populating keyword dictonary");
		keywordDict = new TrieDictionary<String>();
		final double score = 0.0;

		for(Keyword keyword : Sets.newHashSet(dao.findAll())) {
			if (keyword.getKeyword().length() > 2) {
				keywordDict.addEntry(new DictionaryEntry<String>(keyword.getKeyword().trim(), TYPE, score));
				logger.trace("adding keyword to dictionary: "+keyword);
			} // else, skip one or two character keywords
		}
		
		logger.debug("populated dictionary with "+keywordDict.size()+" keywords");
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
		    String chunkText = theText.substring(start,end);
		    Keyword foundKeyword = new Keyword(chunkText);
		    foundKeywords.add(foundKeyword);
		}
		foundKeywords = disambiguateKeywords(foundKeywords);
		logger.debug("found keywords: \n"+Join.join("\n", foundKeywords));
		
		return foundKeywords;
	}
	
	private final Function<Keyword, String> FN_KEYWORD_NAME = new Function<Keyword, String>() { 
		@Override
		public String apply(Keyword keyword) {
			return keyword.getKeyword();
		}
	};
	
	private Set<Keyword> disambiguateKeywords(Set<Keyword> keywords) {
		List<String> keywordsList = 
			Lists.newArrayList(Iterables.transform(keywords, FN_KEYWORD_NAME));

		Collections.sort(keywordsList, String.CASE_INSENSITIVE_ORDER);
		
		keywordsList = reduceKeywords(keywordsList);
		
		Set<Keyword> mungedKeywords = Sets.newHashSet(Iterables.transform(keywordsList, new Function<String, Keyword>() { 
			@Override
			public Keyword apply(String arg0) {
				return new Keyword(arg0);
			}
		}));
		
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
							// throw out the shorter one
							!(!otherKeyword.equals(keyword) && 
									otherKeyword.length() <= keyword.length())) {
						keepKeyword = false;
					}
				}
			}
			if (keepKeyword) keywordsList.add(keyword);
		}
		
		return keywordsList;
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
