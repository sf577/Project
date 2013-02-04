package ptolemy.vergil.uml;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.vergil.basic.ParameterizedNodeController;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.NodeInteractor;
import diva.graph.NodeRenderer;

/**
 * Controller of Lifeline nodes in a UML Sequence Diagram. Dragging of the
 * Lifelines is supported, so that changes are also forwarded to the model.
 * 
 * @author Andreas Thuy, Leandro Soares Indrusiak
 * 
 *         last review: 10.05.07
 */
public class LifelineNodeController extends ParameterizedNodeController {

	/**
	 * Defines how those nodes controlled by this controller are rendered.
	 * 
	 * => UML Lifelines
	 * 
	 * @author Andreas Thuy
	 */
	public class LifelineNodeRenderer implements NodeRenderer {

		public LifelineNodeRenderer() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see diva.graph.NodeRenderer#render(java.lang.Object)
		 */
		public Figure render(Object node) {
			LifelineFigure lifelineFigure = null;
			if (node != null) {
				if (node instanceof Location) {
					Location loc = (Location) node;
					if (loc.getContainer() instanceof Lifeline) {
						Lifeline lifeline = (Lifeline) ((Location) node)
								.getContainer();
						lifelineFigure = new LifelineFigure(
								lifeline.getUMLName(), loc.getLocation()[0], 30); // hard-coded
																					// y-pos
					}
				}
			}
			return lifelineFigure;
		}
	} // end of LifelineNodeRenderer-class

	/**
	 * Constructor. Set interactor and renderer for the Lifeline nodes supported
	 * by this NodeController. Add a RenameAction, so that the name of a
	 * Lifeline can be changed.
	 * 
	 * @param controller
	 *            - controller of the whole graph
	 */
	public LifelineNodeController(GraphController controller) {
		super(controller);
		// a LocatableNodeDragInteractor is set as DragInteractor in
		// LocatableNodeController

		// add RenameDialog
		_menuFactory = new PtolemyMenuFactory(controller);
		_menuFactory.addMenuItemFactory(new UMLRenameDialogFactory());
		_menuCreator.setMenuFactory(_menuFactory);

		setNodeRenderer(new LifelineNodeRenderer());

		((NodeInteractor) getNodeInteractor())
				.setPrototypeDecorator(new LifelineHighlighter());
	}

	/**
	 * Only set the x-position of the Lifeline
	 * 
	 * @see ptolemy.vergil.basic.LocatableNodeController#setLocation(java.lang.Object,
	 *      double[])
	 */
	public void setLocation(Object node, double[] location)
			throws IllegalActionException {
		double[] oldLoc = ((Location) node).getLocation();
		// don't change the y-position of the Lifeline
		double[] newLoc = { location[0], oldLoc[1] };
		super.setLocation(node, newLoc);

	}
}
