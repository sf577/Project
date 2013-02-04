package ptolemy.vergil.uml;

import ptolemy.kernel.util.Location;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.DragInteractor;
import diva.graph.EdgeController;

/**
 * This interactor is responsible for dragging the messages up and down.
 * 
 * @author Andreas Thuy
 */
public class EdgeDragInteractor extends DragInteractor {

	private EdgeController controller = null;

	/**
	 * Constructor.
	 * 
	 * @param controller
	 *            - EdgeController
	 */
	public EdgeDragInteractor(EdgeController controller) {
		this.controller = controller;
	}

	public void mouseReleased(LayerEvent e) {
		if (e.getFigureSource() instanceof MessageConnector) {
			MessageConnector msgCon = (MessageConnector) e.getFigureSource();
			Message msg = (Message) msgCon.getUserObject();
			msg.getSendEvent().setTime(e.getLayerY());
			msg.getReceiveEvent().setTime(e.getLayerY());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * diva.canvas.interactor.DragInteractor#translate(diva.canvas.event.LayerEvent
	 * , double, double)
	 * 
	 * Only allow translation in y-direction
	 */
	public void translate(LayerEvent e, double x, double y) {
		super.translate(e, 0, y);
	}
}
