package lsi.noc.kernel;

import java.awt.Point;

/* 
 * @author      Leandro Soares Indrusiak
 * @version 1.1 (York, 10/12/2009) 
 */

public class Mesh2DNoC extends NoC {

	public Mesh2DRouter[][] index;

	public Point getMeshLocation(Mesh2DRouter r) {

		for (int i = 0; i < index.length; i++) {

			for (int j = 0; j < index[i].length; j++) {

				if (r.equals(index[i][j]))
					return new Point(i, j);

			}

		}

		return null;
	}

	public Point getMeshLocation(ProcessingCore p) {

		return getMeshLocation((Mesh2DRouter) p.getOutput().traverse(p));

	}

	public Point getMeshLocation(Linkable p) {

		if (p instanceof ProcessingCore)
			return getMeshLocation((ProcessingCore) p);
		else if (p instanceof Mesh2DRouter)
			return getMeshLocation((Mesh2DRouter) p);
		else
			return null;

	}

	public Mesh2DRouter getRouter(int x, int y) {
		return index[x][y];
	}

	public ProcessingCore getCore(int x, int y) {

		Mesh2DRouter r = getRouter(x, y);
		return (ProcessingCore) r.linksOut[Mesh2DRouter.LOCAL].traverse(r);

	}

}
