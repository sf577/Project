package ptolemy.vergil.uml;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Point2D;
import java.util.Iterator;

import ptolemy.kernel.undo.RedoChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Singleton;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.toolbox.PtolemyTransferable;
import ptolemy.vergil.toolbox.SnapConstraint;
import ptolemy.domains.uml.kernel.SDDirector;
import diva.graph.GraphController;
import diva.graph.GraphEvent;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;

/**
 * This class defines the DropTarget of a UML Editor so that DnD from the given
 * library into the graph of the editor can be done.
 * 
 * It would be better if this class was derived from DropTarget. However, a
 * BasicGraphFrame has an EditorDropTarget attribute, and now it can be set
 * easily and there are no ambigous attributes in the frame.
 * 
 * @author Andreas Thuy
 * 
 *         last review: 12.06.06
 */
public class UMLEditorDropTarget extends EditorDropTarget {

	private class UMLDropTargetListener implements DropTargetListener {

		public UMLDropTargetListener() {
			super();
		}

		public void dragEnter(DropTargetDragEvent dTDE) {
			if (dTDE.isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
				dTDE.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
			} else {
				dTDE.rejectDrag();
			}
		}

		// we do nothing
		public void dragOver(DropTargetDragEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void dropActionChanged(DropTargetDragEvent arg0) {
			// TODO Auto-generated method stub

		}

		// created with the help of the Copy & Paste antipattern
		// source: EditorDropTarget.DTListener.drop(...
		public void drop(DropTargetDropEvent dTDE) {
			NamedObj container = null;
			// See whether there is a container under the point.
			Point2D originalPoint = SnapConstraint.constrainPoint(dTDE
					.getLocation());

			GraphPane pane = ((JGraph) getComponent()).getGraphPane();

			// Find the default container for the dropped object
			GraphController controller = pane.getGraphController();
			GraphModel model = controller.getGraphModel();
			container = (NamedObj) model.getRoot();

			// Find the location for the dropped objects.
			// Account for the scaling in the pane.
			Point2D transformedPoint = new Point2D.Double();
			pane.getTransformContext().getInverseTransform()
					.transform(originalPoint, transformedPoint);

			// Get an iterator over objects to drop.
			Iterator iterator = null;
			if (dTDE.isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
				try {
					dTDE.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					iterator = (Iterator) dTDE
							.getTransferable()
							.getTransferData(PtolemyTransferable.namedObjFlavor);
				} catch (Exception e) {
					MessageHandler.error(
							"Can't find a supported data flavor for drop in "
									+ dTDE, e);
					return;
				}
			} else {
				dTDE.rejectDrop();
			}

			if (iterator == null) {
				// Nothing to drop!
				return;
			}

			// apply changes to the model => create a new Lifeline

			while (iterator.hasNext()) {
				final NamedObj dropObj = (NamedObj) iterator.next();

				if (container instanceof UMLSeqActor) {

					if (dropObj instanceof Lifeline) {

						// x-position of the new Lifeline, y-pos is fixed
						double[] time = {
								SnapConstraint.constrainPoint(transformedPoint)
										.getX(), 30 };
						try {
							String uniqueName = container
									.uniqueName("Lifeline");
							Lifeline lifeline = new Lifeline(container,
									uniqueName, time);
							lifeline.getLocation().addValueListener(
									(UMLGraphController) controller);
							model.dispatchGraphEvent(new GraphEvent(this,
									GraphEvent.STRUCTURE_CHANGED, container));
						} catch (IllegalActionException iAE) {
							iAE.printStackTrace();
						} catch (NameDuplicationException nDE) {
							nDE.printStackTrace();
						}
					} else if (dropObj instanceof SDDirector) {

						// Create the MoML change request to instantiate the new
						// objects.
						StringBuffer moml = new StringBuffer();
						moml.append("<group>");
						final String name = dropObj.getName();
						// Constrain point to snap to grid.
						Point2D newPoint = SnapConstraint
								.constrainPoint(transformedPoint);
						moml.append(dropObj.exportMoML(name));
						moml.append("<" + dropObj.getElementName() + " name=\""
								+ name + "\">\n");
						moml.append("<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{");
						moml.append((int) newPoint.getX());
						moml.append(", ");
						moml.append((int) newPoint.getY());
						moml.append("}\"/>\n</" + dropObj.getElementName()
								+ ">\n");
						moml.append("</group>");
						MoMLChangeRequest request = new MoMLChangeRequest(this,
								container, moml.toString());
						request.setUndoable(true);
						container.requestChange(request);

					}

				}

				else {
					return;
				}
			}
			dTDE.dropComplete(true); // success!
		}

		// we do nothing
		public void dragExit(DropTargetEvent arg0) {
			// TODO Auto-generated method stub

		}

	}

	public UMLEditorDropTarget(JGraph graph) {
		super(graph);

		registerAdditionalListener(new UMLDropTargetListener());
	}

}
