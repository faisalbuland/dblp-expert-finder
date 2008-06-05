package edu.ucdavis.cs.dblp.analyzers;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import com.google.common.base.Join;
import com.google.common.collect.Maps;

public class TokenizerService {
	public static final Logger logger = Logger.getLogger(TokenizerService.class);
	public TokenizerService() { }
	private Analyzer analyzer = new EnglishAnalyzer();
	
	public List<String> tokenize(String input) {
		List<String> tokens = new ArrayList<String>();
		TokenStream stream = analyzer.tokenStream("text", new StringReader(input));
		try {
			Token token;
			while ((token = stream.next()) != null) {
				tokens.add(token.termText());
			}
		} catch (IOException e) {
			logger.error(e);
		}

		return tokens;
	}
	
	private final Map<String, String> cacheKludge = Maps.newHashMap();
	
	/**
	 * @param input text to tokenize and stem and then return in stemmed form.
	 * @return whitespace joined stemmed version of all tokens in <code>input</code>.
	 */
	public String stemAllTokens(String input) {
		if (!cacheKludge.containsKey(input)) {
			cacheKludge.put(input, Join.join(" ", tokenize(input)));
		}
		return cacheKludge.get(input);
	}

	public static void main(String[] args) {
		TokenizerService service = new TokenizerService();
		logger.info(service.tokenize("This is a test testing testable tests"));
		logger.info(service.tokenize("geographic information retrieval geographical information retrieval"));
	}
}