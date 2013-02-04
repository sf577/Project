package lsi.noc.kernel;

import java.util.List;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.1 (York, 10/12/2009) 
 */

public class DirectedLink extends Link {

	public DirectedLink() {

		super();

	}

	public void link(Linkable a, Linkable b) {

		linked.add(a);
		linked.add(b);
	}

	public Linkable traverse(Linkable l1) {

		if (linked.get(0).equals(l1)) {
			return linked.get(1);
		}
		return null;
	}

	public List<Linkable> hyperTraverse(Linkable l1) {

		if (linked.get(0).equals(l1)) {

			return super.hypertraverse(l1);
		}
		return null;
	}

	public Linkable getSource() {
		return linked.get(0);
	}

	public Linkable getDest() {
		return linked.get(1);
	}

}
