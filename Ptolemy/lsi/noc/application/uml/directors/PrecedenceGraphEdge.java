package lsi.noc.application.uml.directors;

import ptolemy.graph.Edge;

public class PrecedenceGraphEdge extends Edge {

	/**
	 * Construct an unweighted edge with a specified source node and sink node.
	 * 
	 * @param source
	 *            The source node.
	 * @param sink
	 *            The sink node.
	 */
	public PrecedenceGraphEdge(PrecedenceGraphNode source,
			PrecedenceGraphNode sink) {
		super(source, sink);
		_source = source;
		_sink = sink;
	}

	/**
	 * Construct a weighted edge with a specified source node, sink node, and
	 * edge weight.
	 * 
	 * @param source
	 *            The source node.
	 * @param sink
	 *            The sink node.
	 * @param weight
	 *            The edge weight.
	 * @exception IllegalArgumentException
	 *                If the specified weight is <code>null</code>.
	 */
	public PrecedenceGraphEdge(PrecedenceGraphNode source,
			PrecedenceGraphNode sink, Object weight) {
		this(source, sink);
		setWeight(weight);
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * A one-word description of the type of this graph element.
	 * 
	 * @return The description.
	 */
	public String descriptor() {
		return "PrecedenceGraphEdge";
	}

	/**
	 * Return the sink node of the edge.
	 * 
	 * @return The sink node.
	 */
	public PrecedenceGraphNode sink() {
		return _sink;
	}

	/**
	 * Return the source node of the edge.
	 * 
	 * @return The source node.
	 */
	public PrecedenceGraphNode source() {
		return _source;
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////
	// The sink node of the edge.
	private PrecedenceGraphNode _sink;

	// The source node of the edge.
	private PrecedenceGraphNode _source;
}