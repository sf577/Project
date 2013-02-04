package ptolemy.vergil.uml;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLUndoEntry;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.LocatableNodeController;
import ptolemy.vergil.basic.LocatableNodeDragInteractor;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.SelectionModel;

public class CombFragNodeInteractor extends LocatableNodeDragInteractor {

	/*
	 * @Override public void mouseReleased(LayerEvent e) { // We should factor
	 * out the common code in this method and in // transform(). // Work out the
	 * transform the drag performed. double[] dragEnd = _getConstrainedPoint(e);
	 * double[] transform = new double[2]; transform[0] = _dragStart[0] -
	 * dragEnd[0]; transform[1] = _dragStart[1] - dragEnd[1];
	 * 
	 * if ((transform[0] == 0.0) && (transform[1] == 0.0)) { return; }
	 * 
	 * BasicGraphController graphController = (BasicGraphController) _controller
	 * .getController(); BasicGraphFrame frame = graphController.getFrame();
	 * 
	 * SelectionModel model = graphController.getSelectionModel();
	 * AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel)
	 * graphController .getGraphModel(); Object[] selection =
	 * model.getSelectionAsArray(); Object[] userObjects = new
	 * Object[selection.length];
	 * 
	 * // First get the user objects from the selection. for (int i = 0; i <
	 * selection.length; i++) { userObjects[i] = ((Figure)
	 * selection[i]).getUserObject(); }
	 * 
	 * // First make a set of all the semantic objects as they may // appear
	 * more than once HashSet namedObjSet = new HashSet();
	 * 
	 * for (int i = 0; i < selection.length; i++) { if (selection[i] instanceof
	 * Figure) { Object userObject = ((Figure) selection[i]).getUserObject();
	 * 
	 * if (graphModel.isEdge(userObject) || graphModel.isNode(userObject)) {
	 * NamedObj actual = (NamedObj) graphModel.getSemanticObject(userObject);
	 * 
	 * if (actual != null) { namedObjSet.add(actual); } else { // Special case,
	 * may need to handle by not going to // MoML and which may not be undoable.
	 * // FIXME: This is no way to handle it... System.out.println(
	 * "Object with no semantic object , class: " +
	 * userObject.getClass().getName()); } } } }
	 * 
	 * // Generate the MoML to carry out move. // Note that the location has
	 * already been set by the mouseMoved() // call, but we need to do this so
	 * that the undo is generated and // so that the change propagates. // The
	 * toplevel is the container being edited. final NamedObj toplevel =
	 * (NamedObj) graphModel.getRoot();
	 * 
	 * StringBuffer moml = new StringBuffer(); StringBuffer undoMoml = new
	 * StringBuffer(); moml.append("<group>\n"); undoMoml.append("<group>\n");
	 * 
	 * Iterator elements = namedObjSet.iterator();
	 * 
	 * while (elements.hasNext()) { NamedObj element = (NamedObj)
	 * elements.next(); List locationList =
	 * element.attributeList(Locatable.class);
	 * 
	 * if (locationList.isEmpty()) { // Nothing to do as there was no previous
	 * location // attribute (applies to "unseen" relations) continue; }
	 * 
	 * // Set the new location attribute. Locatable locatable = (Locatable)
	 * locationList.get(0);
	 * 
	 * // Give default values in case the previous locations value // has not
	 * yet been set double[] newLocation = new double[] { 0, 0 };
	 * 
	 * if (locatable.getLocation() != null) { newLocation =
	 * locatable.getLocation(); }
	 * 
	 * // NOTE: we use the transform worked out for the drag to // set the
	 * original MoML location double[] oldLocation = new double[2];
	 * oldLocation[0] = newLocation[0] + transform[0]; oldLocation[1] =
	 * newLocation[1] + transform[1];
	 * 
	 * // Create the MoML, wrapping the new location attribute // in an element
	 * refering to the container String containingElementName =
	 * element.getElementName(); String elementToMove = "<" +
	 * containingElementName + " name=\"" + element.getName() + "\" >\n";
	 * moml.append(elementToMove); undoMoml.append(elementToMove);
	 * 
	 * // NOTE: use the moml info element name here in case the // location is a
	 * vertex String momlInfo = ((NamedObj) locatable).getElementName();
	 * moml.append("<" + momlInfo + " name=\"" + locatable.getName() +
	 * "\" value=\"[" + newLocation[0] + ", " + newLocation[1] + "]\" />\n");
	 * undoMoml.append("<" + momlInfo + " name=\"" + locatable.getName() +
	 * "\" value=\"[" + oldLocation[0] + ", " + oldLocation[1] + "]\" />\n");
	 * moml.append("</" + containingElementName + ">\n"); undoMoml.append("</" +
	 * containingElementName + ">\n"); }
	 * 
	 * moml.append("</group>\n"); undoMoml.append("</group>\n");
	 * 
	 * final String finalUndoMoML = undoMoml.toString();
	 * 
	 * // Request the change. MoMLChangeRequest request = new
	 * MoMLChangeRequest(this, toplevel, moml.toString()) { protected void
	 * _execute() throws Exception { super._execute();
	 * 
	 * // Next create and register the undo entry; // The MoML by itself will
	 * not cause an undo // to register because the value is not changing. //
	 * Note that this must be done inside the change // request because write
	 * permission on the // workspace is required to push an entry // on the
	 * undo stack. If this is done outside // the change request, there is a
	 * race condition // on the undo, and a deadlock could result if // the
	 * model is running. MoMLUndoEntry newEntry = new MoMLUndoEntry(toplevel,
	 * finalUndoMoML.toString()); UndoStackAttribute undoInfo =
	 * UndoStackAttribute .getUndoInfo(toplevel); undoInfo.push(newEntry); } };
	 * 
	 * toplevel.requestChange(request);
	 * 
	 * if (frame != null) { // NOTE: Use changeExecuted rather than directly
	 * calling // setModified() so that the panner is also updated.
	 * frame.changeExecuted(null); } }
	 */

	@Override
	public void translate(LayerEvent e, double x, double y) {
		// TODO Auto-generated method stub
		super.translate(e, 0, y);
	}

	public CombFragNodeInteractor(LocatableNodeController controller) {
		super(controller);
		this._controller = controller;
	}

	private LocatableNodeController _controller = null;

	private double[] _dragStart;

	private SnapConstraint _snapConstraint;
}
