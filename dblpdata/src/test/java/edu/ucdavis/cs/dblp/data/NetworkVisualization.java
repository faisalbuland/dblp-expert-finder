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
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.demos.RadialGraphView;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;

/**
 * @author pfishero
 *
 */
public class NetworkVisualization {
	private static final Logger logger = Logger.getLogger(NetworkVisualization.class);
	private static DblpPubDao dao;
	
	protected static final String SRC = Graph.DEFAULT_SOURCE_KEY;
    protected static final String TRG = Graph.DEFAULT_TARGET_KEY;
    
    private final Set<Author> firstLevelAuthors = Sets.newHashSet(); 
	
	public NetworkVisualization() {
		dao = ServiceLocator.getInstance().getDblpPubDao();
	}

	public List<Publication> getAuthorPubs(String authorName) throws Exception {
		List<Publication> pubs = dao.findByAuthorName(authorName);
		logger.info(pubs);
		return pubs;
	}
	
	private Graph generateSimpleGraph(final String authorName) {
		firstLevelAuthors.clear();
		
		Set<Publication> allPubs = Sets.newHashSet();
		List<Publication> pubs = dao.findByAuthorName(authorName);
		allPubs.addAll(pubs);
		
		for (Publication pub : pubs) {
			for (Author author : pub.getAuthor()) {
				firstLevelAuthors.add(author);
				allPubs.addAll(dao.findByAuthorName(author.getContent()));
			}
		}
		return generateSimpleGraph(Lists.newLinkedList(allPubs));
	}
	
	private Graph generateSimpleGraph(List<Publication> pubs) {
		Schema nodeSchema = new Schema();
		nodeSchema.addColumn("name", String.class);
		nodeSchema.addColumn("type", String.class);
		Schema edgeSchema = new Schema();
		edgeSchema.addColumn(SRC, int.class);
        edgeSchema.addColumn(TRG, int.class);
        
		Table nodes = nodeSchema.instantiate();
		Table edges = edgeSchema.instantiate();
		
		Map<Author, Integer> authIds = Maps.newHashMap();
		Map<AuthorEdge, Integer> edgeIds = Maps.newHashMap();
		
		for (Publication pub : pubs) {
			addAuthors(nodes, edges, authIds, edgeIds, pub);
		}
		
		return new Graph(nodes, edges, false);
	}

	/**
	 * @param nodes
	 * @param edges
	 * @param authIds
	 * @param edgeIds
	 * @param pub
	 */
	private void addAuthors(Table nodes, Table edges,
			Map<Author, Integer> authIds, Map<AuthorEdge, Integer> edgeIds,
			Publication pub) {
		for (Author author : pub.getAuthor()) {
			if (!authIds.containsKey(author)) {
				int rowId = nodes.addRow();
				nodes.set(rowId, "name", author.getContent());
				nodes.set(rowId, "type", "author");
				authIds.put(author, rowId);
			}
		}
		for (Author author : pub.getAuthor()) {
			for (Author author2 : pub.getAuthor()) {
				if (firstLevelAuthors.contains(author) || firstLevelAuthors.contains(author2))
				if (!author.equals(author2)) {
					AuthorEdge authEdge = new AuthorEdge(author, author2);
					if (!edgeIds.containsKey(authEdge)) {
						// add an edge
						int rowId = edges.addRow();
						edges.setInt(rowId, SRC, authIds.get(author));
						edges.setInt(rowId, TRG, authIds.get(author2));
						edgeIds.put(authEdge, rowId);
					}
				}
			}
		}
	}
	
	private static final class AuthorEdge {
		private final Author author1;
		private final Author author2;
		public AuthorEdge(Author author1, Author author2) {
			if (author1.getContent().hashCode() <= author2.getContent().hashCode()) {
				this.author1=author1;
				this.author2=author2;				
			} else {
				this.author1=author2;
				this.author2=author1;
			}
		}
		@Override
	    public boolean equals(Object obj) {
	    	if (obj instanceof AuthorEdge == false) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			AuthorEdge rhs = (AuthorEdge ) obj;
			return new EqualsBuilder()
						.append(author1, rhs.author1)
						.append(author2, rhs.author2)
						.isEquals() 
						||
					new EqualsBuilder()
						.append(author1, rhs.author2)
						.append(author2, rhs.author1)
						.isEquals();
	    }
	    
	    @Override
	    public int hashCode() {
	    	return new HashCodeBuilder(5, 13)
	        			.append(author1).append(author2)
	        			.toHashCode();
	    }
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
        new NetworkVisualization().graphVisualization();
    }

}
