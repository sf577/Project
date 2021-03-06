package lsi.noc.kernel.multicast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class NoCTestbench extends Thread {

	/**
	 * @param args
	 */

	public static void main(String[] args) {

		NoCTestbench ntb = new NoCTestbench();
		ntb.start();

	}

	public void run() {

		int x = 5;
		int y = 5;

		JFrame frame = new JFrame("NoCTestbench");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(800, 600));
		frame.pack();
		frame.setVisible(true);

		// Mesh2DNoCFactory factory = new Mesh2DNoCFactory();
		// Mesh2DNoC noc = (Mesh2DNoC) factory.createInterconnect(x, y);
		// Mesh2DXYRouting routing = new Mesh2DXYRouting();

		Random rnd = new Random();

		// System.out.println("S1    S2    D1    D2    R11  R12  R21  R22");
		// System.out.println("S1    S2    D1    D2    Uni  Butterfly");
		// System.out.println("==========================================");

		for (int i = 0; i < 1000000; i++) {

			int s1x = rnd.nextInt(x);
			int s1y = rnd.nextInt(y);

			int s2x = rnd.nextInt(x);
			int s2y = rnd.nextInt(y);

			int d1x = rnd.nextInt(x);
			int d1y = rnd.nextInt(y);

			int d2x = rnd.nextInt(x);
			int d2y = rnd.nextInt(y);
			/*
			 * 
			 * int s1x = 0; int s1y = 0;
			 * 
			 * int s2x = i; int s2y = 0;
			 * 
			 * int d1x = i; int d1y = 1+i;
			 * 
			 * int d2x = 1+i; int d2y = i;
			 */

			/*
			 * Route r11 =
			 * routing.route(noc.getRouter(s1x,s1y),noc.getRouter(d1x,d1y),noc);
			 * Route r12 =
			 * routing.route(noc.getRouter(s1x,s1y),noc.getRouter(d2x,d2y),noc);
			 * Route r21 =
			 * routing.route(noc.getRouter(s2x,s2y),noc.getRouter(d1x,d1y),noc);
			 * Route r22 =
			 * routing.route(noc.getRouter(s2x,s2y),noc.getRouter(d2x,d2y),noc);
			 */

			// System.out.println(s1x+","+s1y+" "+s2x+","+s2y+" "+d1x+","+d1y+" "+d2x+","+d2y+" "+r11.getHopCount()+" "+r12.getHopCount()+" "+r21.getHopCount()+" "+r22.getHopCount());

			ButterflyNetwork bn = new ButterflyNetwork(new Point(s1x, s1y),
					new Point(s2x, s2y), new Point(d1x, d1y), new Point(d2x,
							d2y));

			ConstructiveButterflyNetwork cbn = new ConstructiveButterflyNetwork(
					new Point(s1x, s1y), new Point(s2x, s2y), new Point(d1x,
							d1y), new Point(d2x, d2y));

			ImprovedConstructiveButterflyNetwork icbn = new ImprovedConstructiveButterflyNetwork(
					new Point(s1x, s1y), new Point(s2x, s2y), new Point(d1x,
							d1y), new Point(d2x, d2y));

			int uni = bn.getUnicastHopCount();
			int but = bn.getButterflyHopCount();
			int cbut = cbn.getButterflyHopCount();
			int icbut = icbn.getButterflyHopCount();

			int uniI = bn.getUnicastIntersectingLinksCount();
			int butI = bn.getButterflyIntersectingLinksCount();
			int cbutI = cbn.getButterflyIntersectingLinksCount();
			int icbutI = icbn.getButterflyIntersectingLinksCount();

			Graphics g = panel.getGraphics();

			g.clearRect(0, 0, 10000, 10000);

			g.setColor(Color.blue);
			g.fillRect(40 * s1x, 40 * s1y, 10, 10);
			g.fillRect(40 * s2x, 40 * s2y, 10, 10);

			g.drawString(new Integer(uni).toString(), 10, 10);
			g.drawString(new Integer(uniI).toString(), 30, 10);

			g.setColor(Color.red);
			g.fillRect(40 * d1x, 40 * d1y, 10, 10);
			g.fillRect(40 * d2x, 40 * d2y, 10, 10);

			// CBN

			g.setColor(Color.cyan);
			g.fillRect(40 * cbn.intermediateS.x, 40 * cbn.intermediateS.y, 10,
					10);
			g.fillRect(40 * cbn.intermediateD.x, 40 * cbn.intermediateD.y, 10,
					10);

			g.drawLine(40 * s1x, 40 * s1y, 40 * cbn.intermediateS.x,
					40 * cbn.intermediateS.y);
			g.drawLine(40 * s2x, 40 * s2y, 40 * cbn.intermediateS.x,
					40 * cbn.intermediateS.y);
			g.drawLine(40 * cbn.intermediateS.x, 40 * cbn.intermediateS.y,
					40 * cbn.intermediateD.x, 40 * cbn.intermediateD.y);
			g.drawLine(40 * d1x, 40 * d1y, 40 * cbn.intermediateD.x,
					40 * cbn.intermediateD.y);
			g.drawLine(40 * d2x, 40 * d2y, 40 * cbn.intermediateD.x,
					40 * cbn.intermediateD.y);

			g.drawString(new Integer(cbut).toString(), 10, 40);
			g.drawString(new Integer(cbutI).toString(), 30, 40);
			g.drawString(new Integer(cbn.disid).toString(), 50, 40);

			// BN

			g.setColor(Color.black);
			g.drawRect(40 * bn.intermediateS.x, 40 * bn.intermediateS.y, 10, 10);
			g.drawRect(40 * bn.intermediateD.x, 40 * bn.intermediateD.y, 10, 10);

			g.drawLine(40 * s1x, 40 * s1y, 40 * bn.intermediateS.x,
					40 * bn.intermediateS.y);
			g.drawLine(40 * s2x, 40 * s2y, 40 * bn.intermediateS.x,
					40 * bn.intermediateS.y);
			g.drawLine(40 * bn.intermediateS.x, 40 * bn.intermediateS.y,
					40 * bn.intermediateD.x, 40 * bn.intermediateD.y);
			g.drawLine(40 * d1x, 40 * d1y, 40 * bn.intermediateD.x,
					40 * bn.intermediateD.y);
			g.drawLine(40 * d2x, 40 * d2y, 40 * bn.intermediateD.x,
					40 * bn.intermediateD.y);

			g.drawString(new Integer(but).toString(), 10, 25);
			g.drawString(new Integer(butI).toString(), 30, 25);
			g.drawString(new Integer(bn.disid).toString(), 50, 25);

			// ICBN

			g.setColor(Color.magenta);
			g.drawRect(40 * icbn.intermediateS.x, 40 * icbn.intermediateS.y,
					10, 10);
			g.drawRect(40 * icbn.intermediateD.x, 40 * icbn.intermediateD.y,
					10, 10);

			g.drawLine(40 * s1x, 40 * s1y, 40 * icbn.intermediateS.x,
					40 * icbn.intermediateS.y);
			g.drawLine(40 * s2x, 40 * s2y, 40 * icbn.intermediateS.x,
					40 * icbn.intermediateS.y);
			g.drawLine(40 * icbn.intermediateS.x, 40 * icbn.intermediateS.y,
					40 * icbn.intermediateD.x, 40 * icbn.intermediateD.y);
			g.drawLine(40 * d1x, 40 * d1y, 40 * icbn.intermediateD.x,
					40 * icbn.intermediateD.y);
			g.drawLine(40 * d2x, 40 * d2y, 40 * icbn.intermediateD.x,
					40 * icbn.intermediateD.y);

			g.drawString(new Integer(icbut).toString(), 10, 55);
			g.drawString(new Integer(icbutI).toString(), 30, 55);
			g.drawString(new Integer(icbn.disid).toString(), 50, 55);

			g.setColor(Color.orange);
			int ds1s2 = Math.abs(s1x - s2x) + Math.abs(s1y - s2y);
			int dd1d2 = Math.abs(d1x - d2x) + Math.abs(d1y - d2y);

			int det1 = ds1s2 + dd1d2;
			int det2 = bn.ds1d1 + bn.ds2d2;
			int det3 = bn.ds1d2 + bn.ds2d1;

			g.drawString(new Integer(det1).toString(), 50, 70);
			g.drawString(new Integer(det2).toString(), 50, 85);
			g.drawString(new Integer(det3).toString(), 50, 100);

			g.drawString(new Integer(i).toString(), 50, 200);

			// if(det1<det2 | det1<det3){

			// if (icbut>uni){
			try {
				Thread.sleep(25000);
			} catch (Exception e) {
				System.out.println(e);
			}
			// }

			// }

		}

	}

}
