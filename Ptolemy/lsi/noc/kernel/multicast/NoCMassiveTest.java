package lsi.noc.kernel.multicast;

import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import lsi.noc.kernel.Mesh2DNoC;
import lsi.noc.kernel.Mesh2DNoCFactory;

public class NoCMassiveTest extends Thread {

	/**
	 * @param args
	 */

	public static void main(String[] args) {

		NoCMassiveTest nmt = new NoCMassiveTest();
		nmt.start();

	}

	public void run() {

		Random rnd = new Random();

		for (int d = 3; d <= 12; d++) {

			int x = d;
			int y = d;
			File f = new File("test" + d + ".csv");
			FileWriter fw;
			try {
				fw = new FileWriter(f);
				int i = 0;
				while (i < 5000) {

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

						ButterflyNetwork bn = new ButterflyNetwork(new Point(
								s1x, s1y), new Point(s2x, s2y), new Point(d1x,
								d1y), new Point(d2x, d2y));

						ConstructiveButterflyNetwork cbn = new ConstructiveButterflyNetwork(
								new Point(s1x, s1y), new Point(s2x, s2y),
								new Point(d1x, d1y), new Point(d2x, d2y));

						ImprovedConstructiveButterflyNetwork icbn = new ImprovedConstructiveButterflyNetwork(
								new Point(s1x, s1y), new Point(s2x, s2y),
								new Point(d1x, d1y), new Point(d2x, d2y));

						Point[] dests = { new Point(d1x, d1y),
								new Point(d2x, d2y) };

						Mesh2DNoCFactory factory = new Mesh2DNoCFactory();

						int maxx = x + 1;
						int maxy = y + 1;

						Mesh2DNoC network = (Mesh2DNoC) factory
								.createInterconnect(maxx, maxy);

						RPMRoute rpm1 = new RPMRoute(new Point(s1x, s1y),
								dests, network);
						RPMRoute rpm2 = new RPMRoute(new Point(s2x, s2y),
								dests, network);

						int uni = bn.getUnicastHopCount();
						int but = bn.getButterflyHopCount();
						int cbut = cbn.getButterflyHopCount();
						int icbut = icbn.getButterflyHopCount();
						int rpm = rpm1.getHopCount() + rpm2.getHopCount();

						int uniI = bn.getUnicastIntersectingLinksCount();
						int butI = bn.getButterflyIntersectingLinksCount();
						int cbutI = cbn.getButterflyIntersectingLinksCount();
						int icbutI = icbn.getButterflyIntersectingLinksCount();
						int rpmI = rpm1.getIntersectingLinksCount(rpm2);

						fw.write(uni + "," + uniI + "," + but + "," + butI
								+ "," + bn.disid + "," + cbut + "," + cbutI
								+ "," + cbn.disid + "," + icbut + "," + icbutI
								+ "," + icbn.disid + "," + rpm + "," + rpmI
								+ System.getProperty("line.separator"));
					}

					else
						i++;

				}
				fw.flush();
				fw.close();
				System.out.println("test" + d + ".csv written");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
