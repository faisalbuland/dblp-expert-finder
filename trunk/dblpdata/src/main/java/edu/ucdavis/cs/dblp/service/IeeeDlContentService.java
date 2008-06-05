package edu.ucdavis.cs.dblp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.StringFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;

import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.data.PublicationContent;
import edu.ucdavis.cs.taxonomy.Category;

public class IeeeDlContentService implements ContentService {
	public static final Logger logger = Logger.getLogger(IeeeDlContentService.class);
	private ResourceFetcher fetcher;
	
	public IeeeDlContentService() {
		
	}
	
	public IeeeDlContentService(ResourceFetcher fetcher) {
		this.fetcher = fetcher;
	}
	
	
	private String retrieveAbstract(Publication pub, Parser parser) 
					throws ParserException {
		StringBuilder abstractText = new StringBuilder();
		
		NodeList list = new NodeList ();
		NodeFilter filter =
			new OrFilter(
				new AndFilter(
					new AndFilter (
				    		new TagNameFilter("td"),
				    		new HasAttributeFilter("class", "bodyCopyBlackLargeSpaced")
				    		),
				    new AndFilter(
		    		new HasChildFilter(new StringFilter("Abstract")),
		    		// exclude matches from login/pw prompt (with AbstractPlus in them)
		    		new NotFilter(new HasChildFilter(new StringFilter("AbstractPlus")))
					)
				),
				new AndFilter(
						new AndFilter (
					    		new TagNameFilter("td"),
					    		new HasAttributeFilter("class", "bodyCopyBlackLargeSpaced")
					    ),
			    		new HasChildFilter(
			    			new AndFilter(
			    				new AndFilter (
						    		new TagNameFilter("span"),
						    		new HasAttributeFilter("class", "sectionHeaders")
						    	),
						    	new HasChildFilter(new StringFilter("Abstract"))
			    			)
			    		)
					)
				);
			
		for (NodeIterator e = parser.elements(); e.hasMoreNodes (); ) {
		    e.nextNode().collectInto (list, filter);
		}
		logger.debug("processing abstract for pub id="+pub.getKey());
		for (NodeIterator e = list.elements(); e.hasMoreNodes (); ) {
			Node node = e.nextNode();
			if (StringUtils.isNotBlank(node.toPlainTextString())) {
				String nodeText = StringUtils.strip(node.toPlainTextString());
				final String abstractStartWord = nodeText.contains("Abstract:") ?
															"Abstract:" :
															"Abstract";
				abstractText.append(nodeText.substring(
						nodeText.lastIndexOf(abstractStartWord)+abstractStartWord.length()
						).trim());
				logger.debug("abstract = "+abstractText);
			}
		}
		
		return abstractText.toString();
	}
	
	@Override
	public String retrieveAbstract(Publication pub) {
		String abstractText = "";
		
		if (StringUtils.isNotBlank(pub.getEe())) {
			try {
				Parser parser = new Parser(fetcher.fetchElectronicEdition(pub));
				abstractText = retrieveAbstract(pub, parser);
			} catch (ParserException e) {
				String msg = "error while parsing electronic edition link";
				logger.error(msg+':'+e);
			}
		} else {
			logger.debug("no EE found for publication " + pub.getTitle() + 
					" - abstract retrieval failed.");
		}
		
		return abstractText;
	}
	
	@Test
	public void testIeeeRetrieval() {
		this.fetcher = new WebResourceFetcher();
		Publication testPub = new Publication();
		testPub.setEe("http://192.168.1.22/acm/conf/isi/AbbasiC07.html");
		testPub.setKey("conf/isi/AbbasiC07");
		this.retrieveAbstract(testPub);
	}

	@Override
	public Set<Category> retrieveClassification(Publication pub) {
		Set<Category> categories = new HashSet<Category>();
		
		// ignore classification - not relevant in our context (IEEE)
		
		return categories;
	}
	
	private Set<Category> retrieveClassification(Publication pub, Parser parser) {
		Set<Category> categories = new HashSet<Category>();
		
		// ignore categories - not relevant in our context (IEEE)
				
		return categories;
	}


	@Override
	public Set<Keyword> retrieveKeywords(Publication pub) {
		Set<Keyword> keywords = new HashSet<Keyword>();
		
		if (StringUtils.isNotBlank(pub.getEe())) {
			try {
				keywords = retrieveKeywords(pub, 
							new Parser(fetcher.fetchElectronicEdition(pub)));
			} catch (ParserException e) {
				String msg = "error while parsing electronic edition link";
				logger.error(msg+':'+e);
			}
		} else {
			logger.debug("no EE found for publication " + pub.getTitle() + 
					" - keyword retrieval failed.");
		}
		
		return keywords;
	}
	
	private Set<Keyword> retrieveKeywords(Publication pub, Parser parser) {
		Set<Keyword> keywords = new HashSet<Keyword>();
		Set<String> keywordsStr = new HashSet<String>();
		
		if (StringUtils.isNotBlank(pub.getEe())) {
			try {
				retrieveTerms(pub, "keywords", keywordsStr, parser);
				for (String keyword : keywordsStr) {
					keywords.add(new Keyword(keyword));
				}
			} catch (ParserException e) {
				String msg = "error while parsing electronic edition link";
				logger.error(msg+':'+e);
			}
		} else {
			logger.debug("no EE found for publication " + pub.getTitle() + 
					" - keyword retrieval failed.");
		}
				
		return keywords;
	}
	
	@Override
	public Set<String> retrieveGeneralTerms(Publication pub) {
		Set<String> generalTerms = new HashSet<String>();
		
		// ignore general terms - not relevant in our context (IEEE)
				
		return generalTerms;
	}
	
	private Set<String> retrieveGeneralTerms(Publication pub, Parser parser) {
		Set<String> generalTerms = new HashSet<String>();

		// ignore classification - not relevant in our context (IEEE)
				
		return generalTerms;
	}

	private void retrieveTerms(Publication pub, 
								String termClass, Set<String> terms,
								Parser parser) 
								throws ParserException {
		NodeList list = new NodeList();
		NodeFilter keywordsPFilter = 
			new AndFilter(
				new TagNameFilter("p"),
				new HasAttributeFilter("class", termClass)
			);
		NodeFilter filter = 
			new AndFilter(
				new AndFilter(
					new TagNameFilter("a"),
					new HasAttributeFilter("target", "_self")
					),
				new HasParentFilter(keywordsPFilter, true)
			);
		
		for (NodeIterator e = parser.elements(); e.hasMoreNodes (); ) {
		    e.nextNode().collectInto (list, filter);
		}
		logger.debug("processing "+termClass+" for pub id="+pub.getKey());
		for (NodeIterator e = list.elements(); e.hasMoreNodes (); ) {
			Node node = e.nextNode();
			if (StringUtils.isNotBlank(node.toPlainTextString())) {
				logger.debug(termClass+" term:" + StringUtils.strip(node.toPlainTextString()));
				terms.add(StringUtils.strip(node.toPlainTextString()));
			}
		}
	}
	
	@Override
	public void retrieveAll(Publication pub) {
		logger.debug("retrieving all information for pub id="+pub.getTitle()+'-'+pub.getEe());
		if (StringUtils.isNotBlank(pub.getEe())) {
			try {
				final String eeContents = fetcher.fetchElectronicEdition(pub);
				if (StringUtils.isNotBlank(eeContents)) {
					Parser parser = new Parser(eeContents);
					PublicationContent content = new PublicationContent();
					content.setAbstractText(retrieveAbstract(pub, parser));
					parser.reset();
					// ignoring General Terms for now as they are too general
					retrieveGeneralTerms(pub, parser);
					parser.reset();
					content.setKeywords(retrieveKeywords(pub, parser));
					parser.reset();
					content.setCategories(retrieveClassification(pub, parser));
					// put the content in the pub unless it is totally empty
					if (StringUtils.isBlank(content.getAbstractText()) && 
							content.getKeywords().size() == 0 && 
							content.getCategories().size() == 0) {
						logger.debug("not setting content in pub "+
								pub.getTitle()+" as no content was found");
					} else {
						pub.setContent(content);
					}
				} else {
					logger.debug("no contents fetched for " + 
									pub.getEe());
				}
			} catch (ParserException e) {
				String msg = "error while parsing electronic edition link";
				logger.error(msg+':'+e);
			}
		} else {
			logger.debug("no EE found for publication " + pub.getTitle() + 
					" - retrieveAll failed.");
		}
	}
	
	@Override
	public boolean accepts(Publication pub) {
		boolean accept = false;
		String ee = pub.getEe();
		if (StringUtils.isNotEmpty(ee)) {
			if (ee.contains("ieeexplore")) accept = true;
			if (ee.contains("dx.doi.org") && ee.contains("10.1109")) accept = true;
		}
		return accept;
	}

	public static void main(String ... args) throws Exception {
		File inputFile = new File(args[0]);
		File outputDir = new File("converted");

		if (!outputDir.exists()) {
			logger.info("creating converted output directory");
			outputDir.mkdir();
		}
		
		if (!inputFile.exists() || 
				!inputFile.canRead()) {
			throw new RuntimeException("error with input file: "+inputFile);
		}
		
		ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(inputFile)));
		List<Publication> pubs = (List<Publication>)ois.readObject();
		ois.close();
		
		ContentService service = ServiceLocator.getInstance().getContentService();
		for (Publication pub : pubs) {
			service.retrieveAll(pub);
		}
		
		ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(new File(outputDir, args[1]))));
		oos.writeObject(pubs);
		oos.close();
	}

}
