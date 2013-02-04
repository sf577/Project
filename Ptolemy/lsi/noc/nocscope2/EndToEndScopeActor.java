//Copyright (c) 2008 Leandro Moller
//All rights reserved.
//Darmstadt, 29/10/2008.
//Version 2.0

package lsi.noc.nocscope2;

import java.awt.Point;
import javax.swing.JFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class EndToEndScopeActor extends NoCScopeActor {

	private Point middle;
	private double rad;
	private int numberOfNodes;
	private int width, height;
	private EndToEndScope endToEndScope;

	public EndToEndScopeActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		_frame = new JFrame("EndToEndScope");
		_frame.setSize(1000, 1000);
	}

	public void initialize() throws IllegalActionException {
		super.initialize();
		numberOfNodes = getXDimension() * getYDimension();
		width = _frame.getWidth();
		height = _frame.getHeight();
		setNodeCoordinates();
		endToEndScope.setFirst();

		_frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (width != _frame.getWidth() || height != _frame.getHeight()) {
					width = _frame.getWidth();
					height = _frame.getHeight();
					setNodeCoordinates();
					endToEndScope.setFirst();
				}
			}
		});
	}

	public void paintInput(int x, int y, int biggest, int input) {
		((EndToEndScope) _nocscope).getSwitch(x, y).setMax(biggest);
		((EndToEndScope) _nocscope).getSwitch(x, y).setInput(input);
		_nocscope.repaint();
	}

	public void paintOutput(int x, int y, int biggest, int output) {
		((EndToEndScope) _nocscope).getSwitch(x, y).setMax(biggest);
		((EndToEndScope) _nocscope).getSwitch(x, y).setOutput(output);
		_nocscope.repaint();
	}

	public void update(double timeNow) {
		// visit each node
		for (int x = 0; x < getXDimension(); x++) {
			for (int y = 0; y < getYDimension(); y++) {
				// get off the vector old connections
				((EndToEndScope) _nocscope).getSwitch(x, y).cleanConnections(
						timeNow);
			}
		}
		// repaint the window
		_nocscope.repaint();
	}

	public EndToEndSwitch getSwitch(int x, int y) {
		return ((EndToEndScope) _nocscope).getSwitch(x, y);
	}

	public int getWindowSize() {
		return windowSize;
	}

	/**
	 * Set the node coordinates inside the canvas; usually called on
	 * initialization and possibly if the window is resized (not yet
	 * implemented)
	 */
	public void setNodeCoordinates() {
		// get the canvas size
		int width = ((EndToEndScope) _nocscope).getWidth();
		int height = ((EndToEndScope) _nocscope).getHeight();
		// get the middle point of the canvas
		middle = new Point((int) (width / 2), (int) (height / 2));

		// setting different distances between circles depending on how many
		// circles you have to draw
		if (numberOfNodes > 5)
			rad = (double) (width >= height ? (height * 4) / 5
					: (width * 4) / 5) / 2D;
		else
			rad = (double) (width >= height ? (height * 4) / 8
					: (width * 4) / 8) / 2D;
		// thickness of each node
		int thick = (int) ((rad / numberOfNodes) * 3.5);
		double d1 = (2 * Math.PI) / numberOfNodes;
		double d = 0;

		// create the central point of each node
		for (int x = 0; x < getXDimension(); x++) {
			for (int y = 0; y < getYDimension(); y++) {
				((EndToEndScope) _nocscope).getSwitch(x, y).setPoint(
						(int) (rad * Math.sin(d)) + middle.x,
						(int) (rad * Math.cos(d)) + middle.y);
				((EndToEndScope) _nocscope).getSwitch(x, y).setDegree(d);
				((EndToEndScope) _nocscope).getSwitch(x, y).setThick(thick);
				((EndToEndScope) _nocscope).getSwitch(x, y).setName(x + "" + y);
				((EndToEndScope) _nocscope).getSwitch(x, y).setWindowSize(
						windowSize);
				d = d + d1;
			}
		}
		_nocscope.repaint();
	}

	public NoCScope createScope() {
		endToEndScope = new EndToEndScope(getXDimension(), getYDimension());
		return endToEndScope;
	}

}
