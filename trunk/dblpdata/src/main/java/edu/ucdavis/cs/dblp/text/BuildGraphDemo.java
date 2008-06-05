/****************************************************************************
 **
 ** This file is part of yFiles-2.6-rc1. 
 ** 
 ** yWorks proprietary/confidential. Use is subject to license terms.
 **
 ** Redistribution of this file or of an unauthorized byte-code version
 ** of this file is strictly forbidden.
 **
 ** Copyright (c) 2000-2008 by yWorks GmbH, Vor dem Kreuzberg 28, 
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ***************************************************************************/
package edu.ucdavis.cs.dblp.text;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import y.base.Node;
import y.base.Edge;

import y.view.Graph2DView;
import y.view.EditMode;

import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.NodeRealizer;
import y.view.ImageNodeRealizer;
import y.view.NodeLabel;
import y.view.EdgeLabel;
import y.view.Graph2D;

/** 
 *  Demonstrates simple usage of the Graph2DView.
 *  This demo shows how to add nodes and edges 
 *  with API call to the view.
 *  It further shows how some graphical node and edge
 *  properties can be set.
 *
 *  Additionally it is shown how the appearance 
 *  of the default nodes and edges can be set.
 *
 */
public abstract class BuildGraphDemo extends JPanel 
{
  final Graph2DView view;
  
  public BuildGraphDemo()
  {
    setLayout(new BorderLayout());  
    view = new Graph2DView();
    EditMode mode = new EditMode();
    view.addViewMode(mode);
    add(view);
    
    buildGraph();
    setDefaultGraphics();
    
  }
  
  void setDefaultGraphics()
  {
    Graph2D graph = view.getGraph2D();
    
    //change the looks of the default edge 
    EdgeRealizer er = graph.getDefaultEdgeRealizer();
    //a standard (target) arrow
    er.setArrow(Arrow.STANDARD); 
    //set diamond source arrow
    er.setSourceArrow(Arrow.WHITE_DIAMOND); 
  }

  public abstract void buildGraph();
  
  public void buildGraphIt()  {
	    Graph2D graph = view.getGraph2D();
	    
	    Node v = graph.createNode();
	    // get the "graphics realizer" of v2 from the graph
	    NodeRealizer vr = graph.getRealizer(v);
	    vr.setLocation(60,200);
	    vr.setSize(100,30);
	    vr.setFillColor(Color.cyan);
	    vr.setLabelText("wow! Cyan");
	    
	    // create some edges and new nodes
	    for(int i = 0; i < 5; i++)
	    {
	      Node w = graph.createNode(200, 100 + i*50, "" + (i+1));
	      Edge e = graph.createEdge(v,w);
	    }
	  };

  public void start()
  {
    JFrame frame = new JFrame(getClass().getName());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addContentTo(frame.getRootPane());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public final void addContentTo( final JRootPane rootPane )
  {
    rootPane.setContentPane(this);
  }

  public static void main(String args[])
  {
    final BuildGraphDemo demo = new BuildGraphDemo() {
    	@Override
    	public void buildGraph() {
    		buildGraphIt();
    	}
    };
    demo.start();
  }
}

      
