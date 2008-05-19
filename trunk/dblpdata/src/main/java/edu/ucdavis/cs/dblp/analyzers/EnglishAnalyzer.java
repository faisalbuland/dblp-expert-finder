package edu.ucdavis.cs.dblp.analyzers;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

public class EnglishAnalyzer extends SnowballAnalyzer {

	public EnglishAnalyzer() {
		super("English");
	}
}