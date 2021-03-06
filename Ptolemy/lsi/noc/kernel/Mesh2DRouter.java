package lsi.noc.kernel;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.0 (York, 25/09/2009) 
 */

public class Mesh2DRouter extends Router {

	public static final int EAST = 0;
	public static final int WEST = 1;
	public static final int NORTH = 2;
	public static final int SOUTH = 3;
	public static final int LOCAL = 4;

	public Mesh2DRouter() {

		linksIn = new DirectedLink[5];
		linksOut = new DirectedLink[5];

	}

	public void setInputLink(DirectedLink link, int direction) {

		if (linksIn[direction] != null) {
			linksIn[direction].unlink(this);
		} // unlink previous link, if any
		linksIn[direction] = link;
		link.link(this);

	}

	public void setOutputLink(DirectedLink link, int direction) {

		if (linksOut[direction] != null) {
			linksOut[direction].unlink(this);
		} // unlink previous link, if any
		linksOut[direction] = link;
		link.link(this);

	}

	public DirectedLink getInputLink(int direction) {
		return (DirectedLink) linksIn[direction];
	}

	public DirectedLink getOutputLink(int direction) {
		return (DirectedLink) linksOut[direction];
	}

}
