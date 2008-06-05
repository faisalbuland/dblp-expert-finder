package edu.ucdavis.cs.dblp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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
import org.htmlparser.filters.HasSiblingFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.data.PublicationContent;
import edu.ucdavis.cs.taxonomy.Category;
import edu.ucdavis.cs.taxonomy.CategoryDao;

public class AcmDlContentService implements ContentService {
	public static final Logger logger = Logger.getLogger(AcmDlContentService.class);
	private final ResourceFetcher fetcher;
	
	public AcmDlContentService(ResourceFetcher fetcher) {
		this.fetcher = fetcher;
	}
	
	
	private String retrieveAbstract(Publication pub, Parser parser) 
					throws ParserException {
		StringBuilder abstractText = new StringBuilder();
		
		NodeList list = new NodeList ();				
		NodeFilter baseFilter = 
			new AndFilter(
				new AndFilter (
			    		new TagNameFilter("p"),
			    		new HasAttributeFilter("class", "abstract")
			    		)
			    , 
			    	new NotFilter(
	    				new HasChildFilter(
								new AndFilter(
										new TagNameFilter("font"),
										new HasAttributeFilter("color", "Red")
										)
						)
					)); 
		NodeFilter filter = 
			new OrFilter(
				baseFilter,
				new AndFilter(
					new AndFilter(
						new TagNameFilter("p"),
						// handle the nested <p> problem with a sibling filter
						new HasSiblingFilter( 
								new AndFilter (
							    		new TagNameFilter("p"),
							    		new HasAttributeFilter("class", "abstract")
							    		)
							)
						)
					, 
			    	new NotFilter(
	    				new HasChildFilter(
								new AndFilter(
										new TagNameFilter("font"),
										new HasAttributeFilter("color", "Red")
										)
						)
	    		)
			));
			
		for (NodeIterator e = parser.elements(); e.hasMoreNodes (); ) {
		    e.nextNode().collectInto (list, filter);
		}
		logger.debug("processing abstract for pub id="+pub.getKey());
		for (NodeIterator e = list.elements(); e.hasMoreNodes (); ) {
			Node node = e.nextNode();
			if (StringUtils.isNotBlank(node.toPlainTextString())) {
				logger.debug("node:" + StringUtils.strip(node.toPlainTextString()));
				abstractText.append(StringUtils.strip(node.toPlainTextString()));
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

	@Override
	public Set<Category> retrieveClassification(Publication pub) {
		Set<Category> categories = new HashSet<Category>();
		
		if (StringUtils.isNotBlank(pub.getEe())) {
			try {
				Parser parser = new Parser(fetcher.fetchElectronicEdition(pub));
				retrieveClassification(pub, categories, parser);
			} catch (ParserException e) {
				String msg = "error while parsing electronic edition link";
				logger.error(msg+':'+e);
			}
		} else {
			logger.debug("no EE found for publication " + pub.getTitle() + 
					" - classification retrieval failed.");
		}
				
		return categories;
	}
	
	private Set<Category> retrieveClassification(Publication pub, Parser parser) {
		Set<Category> categories = new HashSet<Category>();
		
		if (StringUtils.isNotBlank(pub.getEe())) {
			try {
				retrieveClassification(pub, categories, parser);
			} catch (ParserException e) {
				String msg = "error while parsing electronic edition link";
				logger.error(msg+':'+e);
			}
		} else {
			logger.debug("no EE found for publication " + pub.getTitle() + 
					" - classification retrieval failed.");
		}
				
		return categories;
	}

	private void retrieveClassification(Publication pub,
			Set<Category> categories, Parser parser) 
			throws ParserException {
		NodeList list = new NodeList();
		NodeFilter classificationPFilter = 
			new AndFilter(
				new TagNameFilter("p"),
				new HasAttributeFilter("class", "Categories")
			);
		NodeFilter terms = new AndFilter(
 				new NodeClassFilter(TextNode.class),
				new HasParentFilter(new AndFilter(
					new TagNameFilter("a"),
					new HasAttributeFilter("NAME", "GenTerms")
				))
		);
		NodeFilter outlineNumber = new AndFilter(
 				new NodeClassFilter(TextNode.class),
				new HasParentFilter(new TagNameFilter("p"), true)
		);
		NodeFilter filter = new OrFilter(
			new AndFilter(
				new HasParentFilter(classificationPFilter, true),
				outlineNumber
				),
			new AndFilter(
				new HasParentFilter(classificationPFilter, true),
				terms
			)
		);
		
		for (NodeIterator e = parser.elements(); e.hasMoreNodes (); ) {
		    e.nextNode().collectInto (list, filter);
		}
		
		// TODO compile less frequently?
		Pattern ccsHeadingStart = Pattern.compile("^[A-K]\\..*");
		
		logger.debug("processing categories for pub id="+pub.getKey());
		String categoryName = "", prevCategoryName = "";
		NodeIterator e = list.elements();
		if (e.hasMoreNodes()) {
			Node node = e.nextNode();
			while (e.hasMoreNodes ()) {
				StringBuilder nodeText = new StringBuilder(StringUtils.strip(
									node.toPlainTextString().replace("&nbsp;", " ")));
				if ("Primary Classification:".equalsIgnoreCase(nodeText.toString()) ||
						"Additional Classification:".equalsIgnoreCase(nodeText.toString())) {
					logger.debug("skipping heading:"+nodeText);
					node = e.hasMoreNodes() ? e.nextNode() : node;
				} else if (StringUtils.isNotBlank(nodeText.toString())) {
					if (ccsHeadingStart.matcher(nodeText).matches()) {
						do { // skip whitespace
							node = e.nextNode();
						} while (e.hasMoreNodes() &&
								StringUtils.isBlank(
										node.toPlainTextString().
												replace("&nbsp;", " ")));
						nodeText.append(' ').
							append(StringUtils.strip(
									node.toPlainTextString().replace("&nbsp;", " ")));
						categoryName = nodeText.toString();
						node = e.hasMoreNodes() ? e.nextNode() : node;
						
						Category cat = AcmDlContentService.findCategoryFor(categoryName);
						if (null != cat) {
							logger.debug("category:" + cat);
							categories.add(cat);
						} else {
							logger.warn("unable to find category: "+categoryName);
						}
					} else if ("Subjects:".equalsIgnoreCase(nodeText.toString())) {
						// Subjects: entries may be ; delimited
						do {
							do { // skip whitespace
								node = e.nextNode();
							} while (e.hasMoreNodes() &&
									StringUtils.isBlank(
											node.toPlainTextString().
													replace("&nbsp;", " ")));
							nodeText = new StringBuilder(StringUtils.strip(
									node.toPlainTextString().replace("&nbsp;", " ")));
							if (!";".equals(nodeText.toString())) {
								categoryName = prevCategoryName + " Subjects: " + nodeText.toString();
								node = e.hasMoreNodes() ? e.nextNode() : node;
								
								Category cat = findCategoryFor(categoryName);
								if (null != cat) {
									logger.debug("category:" + cat);
									categories.add(cat);
								} else {
									logger.warn("unable to find category: "+categoryName);
								}
							}
						} while (";".equals(StringUtils.strip(
									node.toPlainTextString().replace("&nbsp;", " "))));
					} else {
						logger.warn("unknown category text: "+nodeText);
						node = e.hasMoreNodes() ? e.nextNode() : node;
					}
				} else { // node text is blank
					node = e.hasMoreNodes() ? e.nextNode() : node;
				}
				
				prevCategoryName = categoryName;
			}
		}
	}


	private static final Map<String, String> CAT_CONVERTS = new HashMap<String, String>(); {
		CAT_CONVERTS.put(
				"H.5 INFORMATION INTERFACES AND PRESENTATION (I.7)",
				"H.5 INFORMATION INTERFACES AND PRESENTATION (e.g., HCI) (I.7)");
		CAT_CONVERTS.put(
				"H.3.5 On-line Information Services",
				"H.3.5 Online Information Services");
		CAT_CONVERTS.put(
				"B.2.4 High-speed Arithmetic", 
				"B.2.4 High-Speed Arithmetic");
		CAT_CONVERTS.put(
				"B.4.3 Interconnections (subsystems)",
				"B.4.3 Interconnections (Subsystems)");
		CAT_CONVERTS.put(
				"B.8 Performance and Reliability",
				"B.8 PERFORMANCE AND RELIABILITY (C.4)");
		CAT_CONVERTS.put(
				"C.5.1 Large and Medium (\"Mainframe\") Computers",
				"C.5.1 Large and Medium (``Mainframe'') Computers");
		CAT_CONVERTS.put(
				"F.2.3 Tradeoffs among Complexity Measures",
				"F.2.3 Tradeoffs between Complexity Measures (F.1.3)");
		CAT_CONVERTS.put(
				"H.2.8 Database applications",
				"H.2.8 Database Applications");
		CAT_CONVERTS.put(
				"I.2.3 Deduction and Theorem Proving Subjects: Uncertainty, \"fuzzy,\" and probabilistic reasoning",
				"I.2.3 Deduction and Theorem Proving (F.4.1) Subjects: Uncertainty, ``fuzzy,'' and probabilistic reasoning");
		CAT_CONVERTS.put(
				"I.3.1 Hardware architecture",
				"I.3.1 Hardware Architecture (B.4.2)");
	}
	/**
	 * @param categories
	 * @param categoryName
	 * @return
	 */
	public static Category findCategoryFor(String categoryName) {
		categoryName = categoryName.replace("**", "").replace("*", "");
		
		// special case conversions - category names that differ between the
		// ACM DL (web) and the ACM CCS (1998)
		if (CAT_CONVERTS.containsKey(categoryName)) {
			categoryName = CAT_CONVERTS.get(categoryName);
		}
		
		CategoryDao dao = ServiceLocator.getInstance().getCategoryDao(); 
		Category cat = dao.findByKey(categoryName.trim());
		
		if (null == cat) { // still not found, try 2 other options
			// option 1 - all uppercase
			cat = dao.findByKeyIgnoreCase(categoryName);
			
			// option 2 - leaf nodes whose keys don't have their parents 
			// related nodes in them (i.e. (...))
			if (null == cat) {
				if (categoryName.contains("Subjects:")) {
					String parentId = categoryName.substring(0, 
													categoryName.indexOf(' '));
					final String subjectName = categoryName.substring(
											categoryName.indexOf("Subjects: ")+10);
					Category parent = dao.findById(parentId);
					
					cat = Iterables.getOnlyElement(Iterables.filter(parent.getChildren(), 
							new Predicate<Category>() {
								@Override
								public boolean apply(Category cat) {
									boolean match = false;
									if (cat.getLabel().equalsIgnoreCase(subjectName)) {
										match = true;
									}
									return match;
								}
							}));
				} else {
					String id = categoryName.substring(0, 
												categoryName.indexOf(' '));
					cat = dao.findById(id);
				}
			}
			
		}
		if (null == cat) {
			logger.warn("unable to find match for category name="+categoryName);
		}
		
		return cat;
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
		
		if (StringUtils.isNotBlank(pub.getEe())) {
			try {
				Parser parser = new Parser(fetcher.fetchElectronicEdition(pub));
				retrieveTerms(pub, "GenTerms", generalTerms, parser);
			} catch (ParserException e) {
				String msg = "error while parsing electronic edition link";
				logger.error(msg+':'+e);
			}
		} else {
			logger.debug("no EE found for publication " + pub.getTitle() + 
					" - general terms retrieval failed.");
		}
				
		return generalTerms;
	}
	
	private Set<String> retrieveGeneralTerms(Publication pub, Parser parser) {
		Set<String> generalTerms = new HashSet<String>();
		
		if (StringUtils.isNotBlank(pub.getEe())) {
			try {
				retrieveTerms(pub, "GenTerms", generalTerms, parser);
			} catch (ParserException e) {
				String msg = "error while parsing electronic edition link";
				logger.error(msg+':'+e);
			}
		} else {
			logger.debug("no EE found for publication " + pub.getTitle() + 
					" - general terms retrieval failed.");
		}
				
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
			if (ee.contains("acm.org")) accept = true;
			if (ee.contains("doi") && ee.contains("10.1145")) accept = true;
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
