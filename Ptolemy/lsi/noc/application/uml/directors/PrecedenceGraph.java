package lsi.noc.application.uml.directors;

import ptolemy.graph.DirectedGraph;
import lsi.noc.application.uml.directors.PrecedenceGraphNode;

import ptolemy.graph.Edge;
import ptolemy.graph.ElementList;
import ptolemy.graph.GraphConstructionException;
import ptolemy.graph.GraphElementException;
import ptolemy.graph.GraphException;
import ptolemy.graph.GraphWeightException;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.Analysis;
import ptolemy.vergil.uml.CombinedFragment;
import ptolemy.vergil.uml.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class PrecedenceGraph extends DirectedGraph {

	public PrecedenceGraph() {

	}

	/**
	 * Add an unweighted node to this graph.
	 * 
	 * @return The node.
	 */
	public PrecedenceGraphNode addNode() {
		PrecedenceGraphNode node = new PrecedenceGraphNode();
		_registerNode(node);
		return node;
	}

}