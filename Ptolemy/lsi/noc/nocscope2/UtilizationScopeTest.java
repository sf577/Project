package lsi.noc.nocscope2;

/* Copyright (c) 2007, 2011 Leandro Soares Indrusiak
 * All rights reserved.
 * Fulford, 04/07/2011.
 * Version 1.0
 */

import java.util.Random;

import javax.swing.JFrame;

public class UtilizationScopeTest extends Thread {

	JFrame frame;
	UtilizationScope scope;

	public static void main(String[] args) {
		UtilizationScopeTest test = new UtilizationScopeTest(5, 5);
		test.start();

	}

	public UtilizationScopeTest(int x, int y) {
		scope = new UtilizationScope(x, y);

		// creates a window to hold the scope
		frame = new JFrame("UtilizationScope");
		frame.getContentPane().add(scope);
		frame.setSize(x * 100 + 10, y * 100 + 28);
		frame.setVisible(true);

	}

	public void run() {

		Random rnd = new Random();
		while (true) {

			for (int i = 0; i < scope.xdimension; i++) {
				for (int j = 0; j < scope.ydimension; j++) {

					UtilizationSwitch s = scope.getSwitch(i, j); // selects one
																	// of the
																	// network
																	// switches,
																	// given its
																	// location
					s.setEast(rnd.nextInt(105)); // sets the utilization of the
													// outgoing link to the East
													// direction
					s.setWest(rnd.nextInt(105)); // sets the utilization of the
													// outgoing link to the West
													// direction
					s.setNorth(rnd.nextInt(105)); // sets the utilization of the
													// outgoing link to the
													// North direction
					s.setSouth(rnd.nextInt(105)); // sets the utilization of the
													// outgoing link to the
													// South direction
					s.setLocal(rnd.nextInt(105)); // sets the utilization of the
													// local processing core
					s.setIn(rnd.nextInt(105)); // sets the utilization of the
												// incoming link from the local
												// core
					s.setOut(rnd.nextInt(105)); // sets the utilization of the
												// outgoing link to the local
												// core

				}

			}

			scope.repaint();
			try {
				sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
