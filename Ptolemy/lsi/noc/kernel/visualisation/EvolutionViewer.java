package lsi.noc.kernel.visualisation;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class EvolutionViewer extends JPanel {

	protected int[] stages;
	private int max = 700;
	protected int currentStage = 0;
	private int xrange = 800;
	private int yrange = 600;

	public EvolutionViewer() {
		super();
		stages = new int[max];
	}

	public EvolutionViewer(int maxStages) {
		super();
		max = maxStages;
		stages = new int[max];
	}

	public synchronized void register(int value) {

		stages[currentStage] = value;
		currentStage++;
		repaint();
	}

	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);

		// draw y axis
		g.drawLine(getXOrigin(), getYOrigin(), getXOrigin(), getYMaximum());

		// draw x axis
		g.drawLine(getXOrigin(), getYOrigin(), getXMaximum(), getYOrigin());

		// draw values
		g.setColor(Color.RED);
		for (int i = 0; i < currentStage - 1; i++) {

			g.drawLine(getXOrigin() + i, getYOrigin() - stages[i], getXOrigin()
					+ i + 1, getYOrigin() - stages[i + 1]);

		}

	}

	protected void setXRange(int xrange) {
		this.xrange = xrange;
	}

	protected int getXRange() {
		return xrange;
	}

	protected void setYrange(int yrange) {
		this.yrange = yrange;
	}

	protected int getYrange() {
		return yrange;
	}

	protected int getXOrigin() {

		return 50;

	}

	protected int getYOrigin() {

		return getYrange() - 50;

	}

	protected int getYMaximum() {

		return 50;
	}

	protected int getXMaximum() {

		return getXRange() - 50;
	}

}
