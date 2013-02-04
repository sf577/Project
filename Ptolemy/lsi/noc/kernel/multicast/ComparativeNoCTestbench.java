package lsi.noc.kernel.multicast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import lsi.noc.kernel.Link;
import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DNoCFactory;
import lsi.noc.kernel.Mesh2DRouter;
import lsi.noc.kernel.Route;

public class ComparativeNoCTestbench extends Thread {

	public static void main(String[] args) {

		ComparativeNoCTestbench ntb = new ComparativeNoCTestbench();
		ntb.start();

	}

	public void run() {

		int x = 12;
		int y = 12;
		int hop = 50;

		JFrame frame = new JFrame("NoCTestbench");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(1200, 800));
		frame.pack();
		frame.setVisible(true);

		Random rnd = new Random();

		for (int i = 0; i < 1000000; i++) {

			int s1x = rnd.nextInt(x);
			int s1y = rnd.nextInt(y);

			int s2x = rnd.nextInt(x);
			int s2y = rnd.nextInt(y);

			int d1x = rnd.nextInt(x);
			int d1y = rnd.nextInt(y);

			int d2x = rnd.nextInt(x);
			int d2y = rnd.nextInt(y);

			int ds1s2 = Math.abs(s1x - s2x) + Math.abs(s1y - s2y);
			int dd1d2 = Math.abs(d1x - d2x) + Math.abs(d1y - d2y);
			int ds1d1 = Math.abs(s1x - d1x) + Math.abs(s1y - d1y);
			int ds1d2 = Math.abs(s1x - d2x) + Math.abs(s1y - d2y);
			int ds2d1 = Math.abs(s2x - d1x) + Math.abs(s2y - d1y);
			int ds2d2 = Math.abs(s2x - d2x) + Math.abs(s2y - d2y);

			int det1 = ds1s2 + dd1d2;
			int det2 = ds1d1 + ds2d2;
			int det3 = ds1d2 + ds2d1;

			if (det1 < det2 | det1 < det3) {

				ButterflyNetwork bn = new ButterflyNetwork(new Point(s1x, s1y),
						new Point(s2x, s2y), new Point(d1x, d1y), new Point(
								d2x, d2y));

				ConstructiveButterflyNetwork cbn = new ConstructiveButterflyNetwork(
						new Point(s1x, s1y), new Point(s2x, s2y), new Point(
								d1x, d1y), new Point(d2x, d2y));

				ImprovedConstructiveButterflyNetwork icbn = new ImprovedConstructiveButterflyNetwork(
						new Point(s1x, s1y), new Point(s2x, s2y), new Point(
								d1x, d1y), new Point(d2x, d2y));

				Point[] dests = { new Point(d1x, d1y), new Point(d2x, d2y) };

				Mesh2DNoCFactory factory = new Mesh2DNoCFactory();

				int maxx = x + 1;
				int maxy = y + 1;

				Mesh2DNoC network = (Mesh2DNoC) factory.createInterconnect(
						maxx, maxy);

				RPMRoute rpm1 = new RPMRoute(new Point(s1x, s1y), dests,
						network);
				RPMRoute rpm2 = new RPMRoute(new Point(s2x, s2y), dests,
						network);

				int uni = bn.getUnicastHopCount();
				int but = bn.getHopCount();
				int cbut = cbn.getHopCount();
				int icbut = icbn.getHopCount();
				int rpm = rpm1.getHopCount() + rpm2.getHopCount();

				int uniI = bn.getUnicastIntersectingLinksCount();
				int butI = bn.getButterflyIntersectingLinksCount();
				int cbutI = cbn.getButterflyIntersectingLinksCount();
				int icbutI = icbn.getButterflyIntersectingLinksCount();
				int rpmI = rpm1.getIntersectingLinksCount(rpm2);

				Graphics g = panel.getGraphics();

				g.clearRect(0, 0, 10000, 10000);

				g.setColor(Color.blue);
				g.fillRect(hop * s1x, hop * s1y, 10, 10);
				g.fillRect(hop * s2x, hop * s2y, 10, 10);

				g.drawString(new Integer(uni).toString(), 10, 10);
				g.drawString(new Integer(uniI).toString(), 30, 10);

				g.setColor(Color.red);
				g.fillRect(hop * d1x, hop * d1y, 10, 10);
				g.fillRect(hop * d2x, hop * d2y, 10, 10);

				// CBN

				g.setColor(Color.cyan);
				g.drawRect(hop * cbn.intermediateS.x,
						hop * cbn.intermediateS.y, 10, 10);
				g.drawRect(hop * cbn.intermediateD.x,
						hop * cbn.intermediateD.y, 10, 10);

				g.drawLine(hop * s1x, hop * s1y, hop * cbn.intermediateS.x, hop
						* cbn.intermediateS.y);
				g.drawLine(hop * s2x, hop * s2y, hop * cbn.intermediateS.x, hop
						* cbn.intermediateS.y);
				g.drawLine(hop * cbn.intermediateS.x,
						hop * cbn.intermediateS.y, hop * cbn.intermediateD.x,
						hop * cbn.intermediateD.y);
				g.drawLine(hop * d1x, hop * d1y, hop * cbn.intermediateD.x, hop
						* cbn.intermediateD.y);
				g.drawLine(hop * d2x, hop * d2y, hop * cbn.intermediateD.x, hop
						* cbn.intermediateD.y);

				g.drawString(new Integer(cbut).toString(), 10, 40);
				g.drawString(new Integer(cbutI).toString(), 30, 40);
				g.drawString(new Integer(cbn.disid).toString(), 50, 40);
				drawRoute(g, cbn, new Point(0, 0), hop, Color.cyan);

				// BN

				g.setColor(Color.black);
				g.drawRect(hop * bn.intermediateS.x, hop * bn.intermediateS.y,
						10, 10);
				g.drawRect(hop * bn.intermediateD.x, hop * bn.intermediateD.y,
						10, 10);

				g.drawLine(hop * s1x, hop * s1y, hop * bn.intermediateS.x, hop
						* bn.intermediateS.y);
				g.drawLine(hop * s2x, hop * s2y, hop * bn.intermediateS.x, hop
						* bn.intermediateS.y);
				g.drawLine(hop * bn.intermediateS.x, hop * bn.intermediateS.y,
						hop * bn.intermediateD.x, hop * bn.intermediateD.y);
				g.drawLine(hop * d1x, hop * d1y, hop * bn.intermediateD.x, hop
						* bn.intermediateD.y);
				g.drawLine(hop * d2x, hop * d2y, hop * bn.intermediateD.x, hop
						* bn.intermediateD.y);

				g.drawString(new Integer(but).toString(), 10, 25);
				g.drawString(new Integer(butI).toString(), 30, 25);
				g.drawString(new Integer(bn.disid).toString(), 50, 25);

				drawRoute(g, bn, new Point(0, 0), hop, Color.black);

				// ICBN

				g.setColor(Color.magenta);
				g.drawRect(hop * icbn.intermediateS.x, hop
						* icbn.intermediateS.y, 10, 10);
				g.drawRect(hop * icbn.intermediateD.x, hop
						* icbn.intermediateD.y, 10, 10);

				g.drawLine(hop * s1x, hop * s1y, hop * icbn.intermediateS.x,
						hop * icbn.intermediateS.y);
				g.drawLine(hop * s2x, hop * s2y, hop * icbn.intermediateS.x,
						hop * icbn.intermediateS.y);
				g.drawLine(hop * icbn.intermediateS.x, hop
						* icbn.intermediateS.y, hop * icbn.intermediateD.x, hop
						* icbn.intermediateD.y);
				g.drawLine(hop * d1x, hop * d1y, hop * icbn.intermediateD.x,
						hop * icbn.intermediateD.y);
				g.drawLine(hop * d2x, hop * d2y, hop * icbn.intermediateD.x,
						hop * icbn.intermediateD.y);

				g.drawString(new Integer(icbut).toString(), 10, 55);
				g.drawString(new Integer(icbutI).toString(), 30, 55);
				g.drawString(new Integer(icbn.disid).toString(), 50, 55);

				drawRoute(g, icbn, new Point(0, 0), hop, Color.magenta);

				// RPM

				drawRoute(g, rpm1, new Point(0, 0), hop, Color.PINK);
				drawRoute(g, rpm2, new Point(0, 0), hop, Color.PINK);
				g.setColor(Color.pink);
				g.drawString(new Integer(rpm).toString(), 10, 70);
				g.drawString(new Integer(rpmI).toString(), 30, 70);

				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					System.out.println(e);
				}

			}

		}

	}

	public void drawRoute(Graphics g, Route r, Point origin, int unitHop,
			Color c) {

		ArrayList<Link> links = r.getLinks();
		Mesh2DNoC mesh = (Mesh2DNoC) r.getInterconnect();

		for (int i = 0; i < links.size(); i++) {
			Link l = links.get(i);
			Mesh2DRouter r1 = (Mesh2DRouter) l.getLinked().get(0);
			Mesh2DRouter r2 = (Mesh2DRouter) l.getLinked().get(1);

			Point p1 = mesh.getMeshLocation(r1);
			Point p2 = mesh.getMeshLocation(r2);

			g.setColor(c);

			int x1 = origin.x + p1.x * unitHop;
			int y1 = origin.y + p1.y * unitHop;
			int x2 = origin.x + p2.x * unitHop;
			int y2 = origin.y + p2.y * unitHop;

			g.drawLine(x1, y1, x2, y2);

		}

	}

}
