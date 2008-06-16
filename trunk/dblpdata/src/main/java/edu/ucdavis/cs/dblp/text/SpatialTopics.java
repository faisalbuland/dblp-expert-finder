package edu.ucdavis.cs.dblp.text;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;
import y.view.NodeRealizer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Comparators;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.data.keywords.KeywordRecognizer;
import edu.ucdavis.cs.dblp.experts.DblpResults;
import edu.ucdavis.cs.dblp.experts.SearchService;
import edu.ucdavis.cs.dblp.service.ContentService;
import edu.ucdavis.cs.taxonomy.CategoryDao;

@Component
public class SpatialTopics {
	private static final Logger logger = Logger.getLogger(SpatialTopics.class);
	@Resource(name="dblpPubDaoImpl")
	private DblpPubDao pubDao;
	@Resource
	private CategoryDao catDao;
	@Resource
	private KeywordRecognizer recognizer;
	@Resource
	private SearchService searchService;
	private Set<SimplePub> simplePubs;
	@Resource
	private ClutoClusterSolution clusterSolution;
	
	@Autowired(required=false)
	public void setSerializedSimplePubs(@Qualifier("serSimplePubs") org.springframework.core.io.Resource serSimplePubs) {
		try {
			logger.info("loading serialized version of simplePubs");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(serSimplePubs.getInputStream()));
			this.simplePubs = (Set<SimplePub>)ois.readObject();
			ois.close();
			// filter out pubs with no authors -> i.e., main conference entries
			for (Iterator<SimplePub> iter = this.simplePubs.iterator(); iter.hasNext(); ) {
				SimplePub pub = iter.next();
				if (pub.getAuthorNames().size() == 0) {
					iter.remove();
					logger.info("removed pub with no authors - key:"+pub.getKey());
				}
			}			
		} catch (IOException e) {
			logger.error("error while loading simplePubs: "+e);
		} catch (ClassNotFoundException cnfe) {
			logger.error("error while loading simplePubs: "+cnfe);
		}
	}
	
	public Set<SimplePub> getSimplePubs() {
		return simplePubs;
	}
	
	/**
	 * Finds all pubs on spatial topics:
	 * 1. ACM category H.2.8 - Spatial Databases
	 * 2. full text search for "spatial database"
	 */
	public Set<String> allSpatialPubs() {
		Set<String> allPubKeys = Sets.newHashSet();
		{
			List<Publication> catPubs = pubDao.findByCategory(
					catDao.findByFreeTextSearch("Spatial Database").get(0));
			logger.info("docs from ACM category: "+catPubs.size());
			Iterable<Publication> solrPubs = DblpResults.allResults(
												searchService.fullTextSearch("spatial database"), 
												searchService);
			Iterables.addAll(allPubKeys, Iterables.transform(catPubs, Publication.FN_PUB_KEY));
			Iterables.addAll(allPubKeys, Iterables.transform(solrPubs, Publication.FN_PUB_KEY));
		}
		logger.info("retrieved "+allPubKeys.size()+" pubs relating to spatial dbs");
		
		loadAndSaveFullData(allPubKeys);
		return allPubKeys;
	}

	public void loadAndSaveFullData(Set<String> pubKeys) {
		ExecutorService execService = Executors.newFixedThreadPool(4);
		Semaphore lock = new Semaphore(4);
		int partition=0;
		for (Iterable<String> pubKeySlice : Iterables.partition(pubKeys, 100, false)) {
			try {
				logger.info("executing thread to process partition "+partition);
				execService.execute(new Runner(Sets.newHashSet(pubKeySlice), lock, partition));
				lock.acquire();
			} catch (InterruptedException e) {
				logger.error(e);
			}
			partition++;
		}
		execService.shutdown();
	}
	
	@Transactional
	private void processAndExport(Iterable<String> pubKeySlice, int partition) {
		Set<Publication> pubs = Sets.newHashSet();
		for (String key : pubKeySlice) {
			ContentService localContentService = (ContentService) ServiceLocator.getInstance().getAppContext().getBean("contentService");
			Publication pub = pubDao.findById(key);

			try {
				if (null == pub.getContent()) {
					// found but has no content
					localContentService.retrieveAll(pub);
					recognizer.produceControlledVocabulary(ImmutableList.of(pub));
					logger.info("was missing content: "+pub.getKey() + ' ' +
							(pub.getContent() == null?"no content": "has content"));
//					pubDao.update(pub);
				} else if (StringUtils.isBlank(pub.getContent().getAbstractText()) ||
						null == pub.getContent().getKeywords() || 
						pub.getContent().getKeywords().size() == 0) {
					localContentService.retrieveAll(pub);
					// found with no content (but maybe some keywords)
					recognizer.produceControlledVocabulary(ImmutableList.of(pub));
					logger.info("updating keywords on persisted pub with content "+pub.getKey());
//					pubDao.update(pub);
				} else {
					String origAbstractText = pub.getContent().getAbstractText();
					localContentService.retrieveAll(pub);
					if (!origAbstractText.equals(pub.getContent().getAbstractText())
							&& StringUtils.isNotBlank(pub.getContent().getAbstractText())) {
						// reprocess
						recognizer.produceControlledVocabulary(ImmutableList.of(pub));
						logger.info("updating keywords on persisted pub with existing content "+pub.getKey());
//						pubDao.update(pub);
					}
				}
				pubs.add(pub);
			} catch (RuntimeException rex) {
				// explicitly catching runtime exception until a resolution 
				// can be found to the constraint violation problem
				logger.error("error:"+rex);
				logger.error("error pub:"+pub.getKey()+" ee:"+pub.getEe());
			}
		}
		SpatialTopics.serialize(pubs, String.valueOf(partition));
	}
	
	private class Runner implements Runnable {
		private Iterable<String> items;
		private Semaphore lock;
		private int partitionId; 

		public Runner(Iterable<String> items, Semaphore lock, int partitionId) {
			super();
			this.items = items;
			this.lock = lock;
			this.partitionId = partitionId;
		}
		@Override
		public void run() {
			processAndExport(items, partitionId);
			lock.release();
		}
	}
	
	public static void serialize(Set<? extends Serializable> pubs, String qualifier) {
		File outputDir = new File("serialized");
		if (!outputDir.exists()) {
			logger.info("creating serialized output directory "+outputDir.toURI());
			outputDir.mkdir();
		}

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(
					new File(outputDir, "spatdbs-pubs"+qualifier+".ser.gz"))));
			oos.writeObject(pubs);
			oos.close();
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	public static Set<SimplePub> deserialize(File serFile) {
		Preconditions.checkState(serFile.exists() && serFile.canRead());
		Set<SimplePub> simplePubs = Sets.newHashSet();
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(serFile)));
			Set<Publication> pubs = (Set<Publication>)ois.readObject();
			ois.close();
			Iterables.addAll(simplePubs, Iterables.transform(pubs, new Function<Publication, SimplePub>(){
				@Override
				public SimplePub apply(Publication pub) {
					return new SimplePub(pub);
				}
			}));
			pubs = null;
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (ClassNotFoundException cnfe) {
			logger.error(cnfe);
		}
		
		return simplePubs;
	}
	
	/**
	 * combined the serialized Set<Publication>...s into one
	 */
	public void combineSerializedData() {
		File outputDir = new File("serialized");
		if (outputDir.exists() && outputDir.canRead() && outputDir.canWrite()) {
			logger.info("combining files from directory "+outputDir);
			File[] serFiles = outputDir.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return name.contains("spatdbs-pubs") && name.contains(".ser.gz");
				}
			});
			Set<SimplePub> allPubs = Sets.newHashSet();
			for (File serFile : serFiles) {
				logger.info("processing "+serFile);
				allPubs.addAll(deserialize(serFile));
			}
			logger.info("outputing combined set");
			SpatialTopics.serialize(allPubs, "");
		}
	}
	
	public Graph2D graphIt() {
		Graph2D graph = null;
		if (simplePubs != null) {
			logger.info("simple pub count = "+simplePubs.size());
			List<SimplePub> pubs = Lists.newArrayList(simplePubs);
			sortByYearDesc(pubs);
			recognizer.produceSimpleControlledVocabulary(simplePubs);
			/*logYearHistogram(simplePubs);
			logAuthorPubCount(simplePubs);
			logCoauthorCount(simplePubs);
			logVenueHistogram(simplePubs);*/
			logTopKeyphrasesPerYear(pubs);
//			logTopKeyphrases(pubs);
//			graph = generateKeyphraseGraph(getPubsForYear("2000", simplePubs));
			graph = generateClusterGraph(simplePubs);
		}
		return graph;
	}
	
	public void logTopKeyphrasesPerYear(Iterable<SimplePub> pubs) {
		// for all pubs, extract years
		// add keywords to a year bin
		final Map<String, Multiset<String>> yearKws = Maps.newHashMap();
		for(SimplePub pub : pubs) {
			// create-on-get (if not present)
			if (yearKws.get(pub.getYear()) == null) {
				yearKws.put(pub.getYear(), new HashMultiset<String>());
			}
			Iterables.addAll(yearKws.get(pub.getYear()),
					Iterables.transform(pub.getKeywords(), Keyword.FN_KEYWORD_STRINGS));
		}
		
		for (final String year : Comparators.naturalOrder().sortedCopy(yearKws.keySet())) {
			List<String> sortedKeyphrases = Lists.newLinkedList(yearKws.get(year).elementSet());
			Ordering<String> order = Comparators.fromFunction(new Function<String, Integer>(){
				@Override
				public Integer apply(String keyphrase) {
					return yearKws.get(year).count(keyphrase);
				}
			});
			order.reverseOrder().sort(sortedKeyphrases);
			logger.info("\ntop keyphrases for year "+year);
			for (int i=0; i < sortedKeyphrases.size() && i < 15; i++) {
//				logger.info(sortedKeyphrases.get(i)+','+yearKws.get(year).count(sortedKeyphrases.get(i)));
				// use sysout to enable easier importing into Excel
				System.out.println(year+','+sortedKeyphrases.get(i)+','+yearKws.get(year).count(sortedKeyphrases.get(i)));
			}
		}
	}
	
	public void logTopKeyphrases(Iterable<SimplePub> pubs) {
		Multimap<Integer, SimplePub> clusters = this.clusterSolution.getClustersFor(pubs);
		for(Integer clusterNum : clusters.keySet()) {
			final Multiset<String> keyphrases = new HashMultiset<String>();
			for(Iterable<String> kwStrings : Iterables.transform(clusters.get(clusterNum), 
					SimplePub.FN_SIMPLPUB_KEYWORDS)) {
				Iterables.addAll(keyphrases, kwStrings);
			}
			List<String> sortedKeyphrases = Lists.newLinkedList(keyphrases.elementSet());
			Ordering<String> order = Comparators.compound(Comparators.fromFunction(new Function<String, Integer>(){
				@Override
				public Integer apply(String keyphrase) {
					return keyphrases.count(keyphrase);
				}
			}), Comparators.fromFunction(new Function<String, Integer>(){
				@Override
				public Integer apply(String keyphrase) {
					return keyphrase.split("\\s+").length;
				}
			}));
			order.reverseOrder().sort(sortedKeyphrases);
			logger.info("top keyphrases for cluster "+clusterNum);
			for (int i=0; i < 20; i++) {
				logger.info(sortedKeyphrases.get(i)+','+keyphrases.count(sortedKeyphrases.get(i)));
			}
		}
	}
	
	public void logAuthorPubCount(Iterable<SimplePub> pubs) {
		Multiset<String> authorPubHistogram = new HashMultiset<String>();
		Iterables.addAll(authorPubHistogram, Iterables.concat(
				Iterables.transform(pubs, new Function<SimplePub, Iterable<String>>(){
			@Override
			public Iterable<String> apply(SimplePub pub) {
				return pub.getAuthorNames();
			}
		})));
		logger.info("authorName,pubCount");
		for(String authorName : authorPubHistogram.elementSet()) {
			logger.info(authorName+","+authorPubHistogram.count(authorName));
		}
	}
	
	public void logCoauthorCount(Iterable<SimplePub> pubs) {
		Multiset<Integer> authorPubHistogram = new HashMultiset<Integer>();
		Iterables.addAll(authorPubHistogram, Iterables.transform(pubs, new Function<SimplePub, Integer>(){
			@Override
			public Integer apply(SimplePub pub) {
				return pub.getAuthorNames().size();
			}
		}));
		logger.info("coauthorCount,pubCount");
		for(Integer count : authorPubHistogram.elementSet()) {
			logger.info(count+","+authorPubHistogram.count(count));
		}
	}
	
	public void logVenueHistogram(Iterable<SimplePub> pubs) {
		Multiset<String> venueHistogram = new HashMultiset<String>();
		Iterables.addAll(venueHistogram, Iterables.transform(pubs, new Function<SimplePub, String>(){
			@Override
			public String apply(SimplePub pub) {
				String[] keyTokens = pub.getKey().split("/");
				String venue = keyTokens[0]+'/'+keyTokens[1];
				return venue;
			}
		}));
		logger.info("venue,pubCount");
		for(String venue : venueHistogram.elementSet()) {
			logger.info(venue+","+venueHistogram.count(venue));
		}
	}
	
	public void logYearHistogram(Iterable<SimplePub> pubs) {
		Multiset<String> yearHistogram = new HashMultiset<String>();
		Iterables.addAll(yearHistogram, Iterables.transform(pubs, new Function<SimplePub, String>(){
			@Override
			public String apply(SimplePub pub) {
				return pub.getYear();
			}
		}));
		logger.info("year,pubCount");
		for(String year : yearHistogram.elementSet()) {
			logger.info(year+","+yearHistogram.count(year));
		}
	}
	
	public Iterable<SimplePub> getPubsForYear(final String year, Iterable<SimplePub> pubs) {
		return Iterables.filter(pubs, new Predicate<SimplePub>() { 
			@Override
			public boolean apply(SimplePub pub) {
				return year.equalsIgnoreCase(pub.getYear());
			}
		});
	}

	/**
	 * @param pubs
	 */
	private void sortByYearDesc(List<SimplePub> pubs) {
		Comparators.fromFunction(new Function<SimplePub, Integer>() {
			@Override
			public Integer apply(SimplePub pub) {
				return Integer.parseInt(pub.getYear());
			}
		}).sort(pubs);
		logger.debug("first pub year: "+pubs.get(0).getYear());
	}
	
	public Graph2D generateClusterGraph(Iterable<SimplePub> pubs) {
		Graph2D graph = new Graph2D();
		final String ROOT_ID="SpatDBs-ROOT";
		
		// one central node to bind all the clusters together
		// one "cluster root" node each
		// nodes for topics within each cluster, connected to each other also
		
		Multimap<Integer, SimplePub> clusters = this.clusterSolution.getClustersFor(pubs);
		// node per keyword
		Set<String> kws = Sets.newHashSet();
		for (Integer clusterNum : clusters.keySet()) {
			for(Iterable<String> kwStrings : Iterables.transform(clusters.get(clusterNum), 
												SimplePub.FN_SIMPLPUB_KEYWORDS)) {
				Iterables.addAll(kws, kwStrings);
			}
		}
		
		Node[] V = new Node[kws.size()+1+clusters.keySet().size()]; // kws+root+cluster nodes
		Map<String, Node> nodeMap = Maps.newHashMap();
		Map<EdgeKey, Edge> edgeMap = Maps.newHashMap();
		Multiset<Node> edgeCount = new HashMultiset<Node>();
	    
		int i=0;
		// root first
		Node rootNode = V[i] = graph.createNode();
		nodeMap.put(ROOT_ID, V[i]);
		NodeRealizer vr = graph.getRealizer(V[i]);
		vr.setLabelText(ROOT_ID);
		vr.setSize(125.0, 75.0);
		vr.setFillColor(Color.GREEN);
		i++;
		// clusters
	    for(Integer clusterNum : clusters.keySet()) {
	    	V[i] = graph.createNode();
	    	String clusterName = "Cluster: "+clusterNum; 
			nodeMap.put(clusterName, V[i]);
			NodeRealizer clusterVr = graph.getRealizer(V[i]);
			clusterVr.setLabelText("Cluster: "+clusterNum);
			clusterVr.setSize(75.0, 25.0);
			clusterVr.setFillColor(Color.LIGHT_GRAY);

			// add edge to root
			Node source = rootNode;
			Node sink = V[i];
			Edge edge = graph.createEdge(source, sink);
			EdgeKey edgeKey = new EdgeKey(ROOT_ID, clusterName);
			edgeMap.put(edgeKey, edge);
			edgeCount.add(source);
			edgeCount.add(sink);
		
			i++;
			
			// add cluster members
			// top keywords
			final Multiset<String> keyphrases = new HashMultiset<String>();
			for(Iterable<String> kwStrings : Iterables.transform(clusters.get(clusterNum), 
					SimplePub.FN_SIMPLPUB_KEYWORDS)) {
				Iterables.addAll(keyphrases, kwStrings);
			}
			List<String> sortedKeyphrases = Lists.newLinkedList(keyphrases.elementSet());
			Ordering<String> order = Comparators.compound(Comparators.fromFunction(new Function<String, Integer>(){
				@Override
				public Integer apply(String keyphrase) {
					return keyphrases.count(keyphrase);
				}
			}), Comparators.fromFunction(new Function<String, Integer>(){
				@Override
				public Integer apply(String keyphrase) {
					return keyphrase.split("\\s+").length;
				}
			}));
			order.reverseOrder().sort(sortedKeyphrases);
			for (int j=0; j < 20; j++) {
				  String kwStr = sortedKeyphrases.get(j);
				  if (!nodeMap.containsKey(kwStr)) {
					  logger.info("creating node for "+kwStr);
				      V[i] = graph.createNode();
				      nodeMap.put(kwStr, V[i]);
				      NodeRealizer vreal = graph.getRealizer(V[i]);
				      vreal.setLabelText(kwStr);
				      vreal.setSize(100.0, 25.0);
				      i++;
				  }
			      
			      // add edge to keyword
			      Node kwSource = nodeMap.get(clusterName);
			      Node kwSink = nodeMap.get(kwStr);
			      Edge kwEdge = graph.createEdge(kwSource, kwSink);
			      logger.info("creating edge from "+clusterName+" to "+kwStr);
			      EdgeKey kwEdgeKey = new EdgeKey(clusterName, kwStr);
			      edgeMap.put(kwEdgeKey, kwEdge);
			      edgeCount.add(kwSource);
			      edgeCount.add(kwSink);
			}
			
			// pub keywords
			for(SimplePub pub : clusters.get(clusterNum)) {
				for (Keyword kw : pub.getKeywords()) 
			    {
					for(Keyword coOccurringKw : pub.getKeywords()) {
		    			if (!coOccurringKw.equals(kw)) {
		    				EdgeKey kwEdgeKey = new EdgeKey(kw, coOccurringKw);
		    				if (!edgeMap.containsKey(kwEdgeKey)) {
		    					Node kwSource = nodeMap.get(kw.getKeyword());
		    					Node kwSink = nodeMap.get(coOccurringKw.getKeyword());
		    					// only add an edge if both kws were in the top keyword buckets
		    					if (kwSource != null && kwSink != null &&
		    							(edgeCount.count(source) <= 1 || 
		    							edgeCount.count(sink) <= 1)) {
			    					Edge kwEdge = graph.createEdge(kwSource, kwSink); 
			    					edgeMap.put(kwEdgeKey, kwEdge);
			    					edgeCount.add(kwSource);
			    					edgeCount.add(kwSink);
		    					}
		    				} // else, skip duplicate edge
		    			} // else, skip as we don't want self-loops
		    		}
			    }
			}
	    }
			
		return graph;
	}
	
	public Graph2D generateKeyphraseGraph(Iterable<SimplePub> pubs) {
		Graph2D graph = new Graph2D();
		
		// node per keyword
		Set<String> kws = Sets.newHashSet();
		for(Iterable<String> kwStrings : Iterables.transform(pubs, SimplePub.FN_SIMPLPUB_KEYWORDS)) {
			Iterables.addAll(kws, kwStrings);
		}
		Node[] V = new Node[kws.size()];
		Map<String, Node> nodeMap = Maps.newHashMap();
		Map<EdgeKey, Edge> edgeMap = Maps.newHashMap();
		Multiset<Node> edgeCount = new HashMultiset<Node>();
	    
		int i=0;
	    for(String kwStr : kws) 
	    {
	      V[i] = graph.createNode();
	      nodeMap.put(kwStr, V[i]);
	      NodeRealizer vr = graph.getRealizer(V[i]);
	      vr.setLabelText(kwStr);
	      i++;
	    }
	    // edges between co-occurring keywords
	    for(SimplePub pub : pubs) {
	    	for(Keyword kw : pub.getKeywords()) {
	    		for(Keyword coOccurringKw : pub.getKeywords()) {
	    			if (!coOccurringKw.equals(kw)) {
	    				EdgeKey edgeKey = new EdgeKey(kw, coOccurringKw);
	    				if (!edgeMap.containsKey(edgeKey)) {
	    					Node source = nodeMap.get(kw.getKeyword());
	    					Node sink = nodeMap.get(coOccurringKw.getKeyword());
	    					if (edgeCount.count(source) <= 1 || 
	    							edgeCount.count(sink) <= 1) {
		    					Edge edge = graph.createEdge(source, sink); 
		    					edgeMap.put(edgeKey, edge);
		    					edgeCount.add(source);
		    					edgeCount.add(sink);
	    					}
	    				} // else, skip duplicate edge
	    			} // else, skip as we don't want self-loops
	    		}
	    	}
	    }
		
		return graph;
	}
	
	private static final class EdgeKey {
		private String key1;
		private String key2;
		public EdgeKey(Keyword src, Keyword dest) {
			this(src.getKeyword(), dest.getKeyword());
		}
		public EdgeKey(String src, String dest) {
			if (src.hashCode() <= dest.hashCode()) {
				this.key1=src;
				this.key2=dest;				
			} else {
				this.key1=dest;
				this.key2=src;
			}
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key1 == null) ? 0 : key1.hashCode());
			result = prime * result + ((key2 == null) ? 0 : key2.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final EdgeKey other = (EdgeKey) obj;
			if (key1 == null) {
				if (other.key1 != null)
					return false;
			} else if (!key1.equals(other.key1))
				return false;
			if (key2 == null) {
				if (other.key2 != null)
					return false;
			} else if (!key2.equals(other.key2))
				return false;
			return true;
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = ServiceLocator.getInstance().getAppContext();
		final SpatialTopics spat = (SpatialTopics) ctx.getBean("spatialTopics");
//		spat.combineSerializedData();
//		Set<String> pubKeys = spat.allSpatialPubs();
		OrganicLayouterDemo.initLnF();
		
		final OrganicLayouterDemo demo = new OrganicLayouterDemo();
		demo.getView().setGraph2D(spat.graphIt());
		
/*		final SimpleIncrementalHierarchicLayouterDemo demo = new SimpleIncrementalHierarchicLayouterDemo() {
			protected JToolBar createToolBar()
			  {
				view.setGraph2D(spat.graphIt());
			    JToolBar tb = super.createToolBar();
			    tb.add(new LayoutAction());
			    tb.add(new LayoutFromSketchAction());
			    tb.add(new LayoutIncrementallyAction());
			    return tb;
			  }
		};
		final InteractiveOrganicDemo demo = new InteractiveOrganicDemo() {
			protected void initialize() {
			    layouter = new InteractiveOrganicLayouter();

			    view.setGraph2D(spat.graphIt());
			    //Reset the paths and the locations of the nodes.
			    LayoutTool.initDiagram( view.getGraph2D() );

			    view.updateWorldRect();
			    view.fitContent();
			    view.setZoom( 0.3 );
			  }
	    };*/
	    demo.start();
	}
	
}
