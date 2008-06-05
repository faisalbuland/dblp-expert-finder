/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.demos.RadialGraphView;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

import com.google.common.base.Join;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.keywords.KeywordRecognizer;

/**
 * @author pfishero
 *
 */
public class KeywordNetworkVisualization {
	private static final Logger logger = Logger.getLogger(KeywordNetworkVisualization.class);
	private static DblpPubDao dao;
	private static KeywordRecognizer recognizer;
	
	protected static final String SRC = Graph.DEFAULT_SOURCE_KEY;
    protected static final String TRG = Graph.DEFAULT_TARGET_KEY;
	
	public KeywordNetworkVisualization() {
		dao = ServiceLocator.getInstance().getDblpPubDao();
		recognizer = (KeywordRecognizer)ServiceLocator.getInstance().
											getAppContext().getBean("simpleKeywordRecognizer");
	}

	public List<Publication> getAuthorPubs(String authorName) throws Exception {
		List<Publication> pubs = dao.findByAuthorName(authorName);
		logger.info(pubs);
		return pubs;
	}
	
	private Graph generateSimpleGraph(final String authorName) {		
		List<Publication> pubs = dao.findByAuthorName(authorName);
		
		return generateSimpleGraph(pubs, authorName);
	}
	
	private Graph generateSimpleGraph(List<Publication> pubs, String centerNodeName) {
		Multimap<Keyword, Publication> kwsToPubs = Multimaps.newHashMultimap();
		Schema nodeSchema = new Schema();
		nodeSchema.addColumn("name", String.class);
		nodeSchema.addColumn("type", String.class);
		Schema edgeSchema = new Schema();
		edgeSchema.addColumn(SRC, int.class);
        edgeSchema.addColumn(TRG, int.class);
        
		Table nodes = nodeSchema.instantiate();
		Table edges = edgeSchema.instantiate();
		
		int pubIdSeq = 0;
		Map<Publication, Integer> pubIds = Maps.newHashMap();
//		Map<Keyword, Integer> pubIds = Maps.newHashMap();
		
		Set<Keyword> keywords = Sets.newHashSet();
		for (Publication pub : pubs) {
			StringBuilder rawText = new StringBuilder();
			rawText.append(pub.getTitle()).append(" ");
			if (pub.getContent() != null) {
				rawText.append(pub.getContent().getAbstractText()).append(" ");
				rawText.append(Join.join(" ", pub.getContent().getKeywords())).append(" ");
			}
			final Set<Keyword> pubKws = recognizer.findKeywordsIn(rawText.toString().trim());
			keywords.addAll(pubKws);
			for (Keyword kw : pubKws) {
				kwsToPubs.put(kw, pub);
			}
		}
		logger.info("all pubs keyword count="+keywords.size());
		// reduce one more time across the entire publication set
		List<Keyword> reducedKeywords = recognizer.removeLowInformationKeywords(
											recognizer.findKeywordsIn(Join.join(" ", keywords)));
		logger.info("after reduction - all pubs keyword count="+reducedKeywords.size());
		
		int rowId = nodes.addRow();
		nodes.set(rowId, "name", centerNodeName);
		nodes.set(rowId, "type", "author");
		
		Map<Publication, Integer> pubsToNodeIds = Maps.newHashMap();
		
		for (Keyword keyword : reducedKeywords) {
			int kwRowId = nodes.addRow();
			nodes.set(kwRowId, "name", keyword.getKeyword());
			nodes.set(kwRowId, "type", "keyword");

			for (Publication pub : kwsToPubs.get(keyword)) {
				if (!pubsToNodeIds.containsKey(pub)) {
					int pubRowId = nodes.addRow();
					nodes.set(pubRowId, "name", Integer.toString(pubIdSeq++));
					nodes.set(pubRowId, "type", "publication");
					pubsToNodeIds.put(pub, pubRowId);
					
					int pubToRootEdgeRowId = edges.addRow();
					edges.setInt(pubToRootEdgeRowId, SRC, rowId);
					edges.setInt(pubToRootEdgeRowId, TRG, pubRowId);
				}
				
				int edgeRowId = edges.addRow();
				edges.setInt(edgeRowId, SRC, pubsToNodeIds.get(pub));
				edges.setInt(edgeRowId, TRG, kwRowId);
			}		
		}
		
		return new Graph(nodes, edges, false);
	}
	
	public void radialVisualization(Graph graph, String nodeName) {
		UILib.setPlatformLookAndFeel();
        
        JFrame frame = new JFrame("p r e f u s e  |  r a d i a l g r a p h v i e w");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(RadialGraphView.demo(graph, nodeName));
        frame.pack();
        frame.setVisible(true);
	}
	
	public void radialVisualization() {
//		final String authorName = "Nick Bryan-Kinns";
		final String authorName = "Ian Davidson";
//		final String authorName = "Michael Gertz";
		radialVisualization(generateSimpleGraph(authorName), "name");		
	}
	
	public void graphVisualization() {
        // -- 1. load the data ------------------------------------------------
        
//		final String authorName = "Nick Bryan-Kinns";
//		final String authorName = "Ian Davidson";
		final String authorName = "Michael Gertz";
        Graph graph = generateSimpleGraph(authorName);

		graphVisualization(graph);
	}

	/**
	 * @param graph
	 */
	public void graphVisualization(Graph graph) {
		UILib.setPlatformLookAndFeel();
        
        // -- 2. the visualization --------------------------------------------
        
        // add the graph to the visualization as the data group "graph"
        // nodes and edges are accessible as "graph.nodes" and "graph.edges"
        Visualization vis = new Visualization();
        vis.add("graph", graph);
        vis.setInteractive("graph.edges", null, false);
        
        // -- 3. the renderers and renderer factory ---------------------------
        
        // draw the "name" label for NodeItems
        LabelRenderer r = new LabelRenderer("name");
        r.setRoundedCorner(8, 8); // round the corners
        
        // create a new default renderer factory
        // return our name label renderer as the default for all non-EdgeItems
        // includes straight line edges for EdgeItems by default
        vis.setRendererFactory(new DefaultRendererFactory(r));
        
        // -- 4. the processing actions ---------------------------------------
        
        // create our nominal color palette
        // pink, baby blue
        int[] palette = new int[] {
            ColorLib.rgb(255,180,180), ColorLib.rgb(190,190,255)
        };
     // map nominal data values to colors using our provided palette
        DataColorAction fill = new DataColorAction("graph.nodes", "type",
                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
        // use black for node text
        ColorAction text = new ColorAction("graph.nodes",
                VisualItem.TEXTCOLOR, ColorLib.gray(0));
        // use light grey for edges
        ColorAction edges = new ColorAction("graph.edges",
                VisualItem.STROKECOLOR, ColorLib.gray(200));
        
        // create an action list containing all color assignments
        ActionList color = new ActionList();
        color.add(fill);
        color.add(text);
        color.add(edges);
        
        // create an action list with an animated layout
        ActionList layout = new ActionList(Activity.INFINITY);
        layout.add(new ForceDirectedLayout("graph"));
        layout.add(new RepaintAction());
        
        // add the actions to the visualization
        vis.putAction("color", color);
        vis.putAction("layout", layout);
        
        
        // -- 5. the display and interactive controls -------------------------
        
        Display d = new Display(vis);
        d.setSize(720, 500); // set display size
        // drag individual items around
        d.addControlListener(new DragControl());
        // pan with left-click drag on background
        d.addControlListener(new PanControl()); 
        // zoom with right-click drag
        d.addControlListener(new ZoomControl());
        
        // -- 6. launch the visualization -------------------------------------
        
        // create a new window to hold the visualization
        JFrame frame = new JFrame("prefuse example");
        // ensure application exits when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(d);
        frame.pack();           // layout components in window
        frame.setVisible(true); // show the window
        
        // assign the colors
        vis.run("color");
        // start up the animated layout
        vis.run("layout");
	}
	
	/**
	 * @param args
	 */
/*	public static void main(String[] args) throws Exception {
		new NetworkVisualization().radialVisualization();
	}*/
	
    public static void main(String[] argv) {       
        new KeywordNetworkVisualization().radialVisualization();
    }

}
