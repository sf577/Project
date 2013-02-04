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

public class NoCTestbench3Destinations extends Thread {

	public static void main(String[] args) {

		NoCTestbench3Destinations ntb = new NoCTestbench3Destinations();
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

			int d3x = rnd.nextInt(x);
			int d3y = rnd.nextInt(y);

			Point[] dests = { new Point(d1x, d1y), new Point(d2x, d2y),
					new Point(d3x, d3y) };

			Mesh2DNoCFactory factory = new Mesh2DNoCFactory();
			int maxx = x + 1;
			int maxy = y + 1;
			Mesh2DNoC network = (Mesh2DNoC) factory.createInterconnect(maxx,
					maxy);

			ConstructiveButterflyNetworkwithThreeDestinations cbn3 = new ConstructiveButterflyNetworkwithThreeDestinations(
					new Point(s1x, s1y), new Point(s2x, s2y), new Point(d1x,
							d1y), new Point(d2x, d2y), new Point(d3x, d3y),
					network);

			RPMRoute rpm1 = new RPMRoute(new Point(s1x, s1y), dests, network);
			RPMRoute rpm2 = new RPMRoute(new Point(s2x, s2y), dests, network);

			int cbn3c = cbn3.getHopCount();
			int uni = cbn3.getUnicastHopCount();
			int rpmc = rpm1.getHopCount() + rpm2.getHopCount();

			int rpmI = rpm1.getIntersectingLinksCount(rpm2);

			Graphics g = panel.getGraphics();

			g.clearRect(0, 0, 10000, 10000);

			g.setColor(Color.blue);
			g.fillRect(hop * s1x, hop * s1y, 10, 10);
			g.fillRect(hop * s2x, hop * s2y, 10, 10);

			g.drawString(new Integer(uni).toString(), 10, 10);
			// g.drawString(new Integer(uniI).toString(),30,10);

			g.setColor(Color.red);
			g.fillRect(hop * d1x, hop * d1y, 10, 10);
			g.fillRect(hop * d2x, hop * d2y, 10, 10);
			g.fillRect(hop * d3x, hop * d3y, 10, 10);

			g.setColor(Color.black);
			g.drawRect(hop * cbn3.intermediateS.x, hop * cbn3.intermediateS.y,
					10, 10);
			g.setColor(Color.pink);
			g.drawRect(hop * cbn3.intermediateD.x, hop * cbn3.intermediateD.y,
					10, 10);

			// CBN3

			drawRoute(g, cbn3, new Point(0, 0), hop, Color.green);
			g.setColor(Color.green);

			g.drawString(new Integer(cbn3c).toString(), 10, 55);
			// g.drawString(new Integer(icbutI).toString(),30,55);
			// g.drawString(new Integer(icbn.disid).toString(),50,55);

			// RPM

			// drawRoute(g,rpm1,new Point(0,0),hop,Color.PINK);
			// drawRoute(g,rpm2,new Point(0,0),hop,Color.PINK);
			g.setColor(Color.pink);
			g.drawString(new Integer(rpmc).toString(), 10, 70);
			g.drawString(new Integer(rpmI).toString(), 30, 70);

			try {
				Thread.sleep(10000);
			} catch (Exception e) {
				System.out.println(e);
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
