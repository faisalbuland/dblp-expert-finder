package edu.ucdavis.cs.dblp.data.viz;

import java.awt.Color;

import org.apache.log4j.Logger;

/**
 * Built from the Apache2GDL PERL script available on aisee.com's web site.
 * 
 * @author pfishero
 *
 */
public class Graph2GDL {
	private static final Logger logger = Logger.getLogger(Graph2GDL.class);
	private final String title;
	
	public Graph2GDL(String title) {
		this.title = title;
	}
	
//	#####
//
//	# Things you can easily customize:
	private int maxEdgeThickness = 40; // Useful values range from 10 to 80.
	private int minEdgeArrowSize =  8; // Useful values range from 0 to 12.

	private Color lightestNodeColor  = new Color(255,255,255); // An RGB triple, possible values ranging from (0,0,0) to (255,255,255).
	private Color darkestNodeColor   = new Color(55,105,155);  // An RGB triple, possible values ranging from (0,0,0) to (255,255,255).
	private int colorGradientSteps = 20;            // Useful values range from 2 to 30.

	private String displayEdgeLabels = "yes"; // Possible values: "yes" or "no".

	
	
}
