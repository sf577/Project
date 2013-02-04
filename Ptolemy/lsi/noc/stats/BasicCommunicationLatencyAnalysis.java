/**
 * 
 */
package lsi.noc.stats;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * @author Leandro Soares Indrusiak
 * @version 1.0 (York, 17/08/2009)
 * 
 * 
 *          Simple implementation of CommunicationLatencyAnalysis. All calls to
 *          the notification methods add the latency result to a list, sorted by
 *          message name. Basic analysis methods include maximum, minimum and
 *          average latency of each message.
 * 
 */
public class BasicCommunicationLatencyAnalysis extends
		CommunicationLatencyAnalysis implements ActionListener {

	public BasicCommunicationLatencyAnalysis() {

		frame = new JFrame("Basic Communication Latency Analysis");
		tabbedPane = new JTabbedPane();

		JButton but = new JButton("Generate report");
		but.addActionListener(this);
		but.setActionCommand("generate");
		JButton res = new JButton("Reset stats");
		res.addActionListener(this);
		res.setActionCommand("reset");
		JButton mon = new JButton("Toggle message monitoring");
		mon.addActionListener(this);
		mon.setActionCommand("monitor");

		commands = new JPanel();
		commands.setLayout(new GridLayout(0, 1));
		commands.add(but);
		commands.add(res);
		commands.add(mon);

		textarea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textarea);
		textarea.setEditable(false);

		tabbedPane.addTab("status", scrollPane);
		tabbedPane.addTab("commands", commands);

		frame.getContentPane().add(tabbedPane);
		frame.setSize(400, 300);

		frame.setVisible(true);

		reset();

	}

	public void notifyReceipt(int id, String name, double latency) {
		this.notifyReceipt(name, latency);

	}

	public void notifyReceipt(int id, String name, double time, double latency) {
		this.notifyReceipt(name, time, latency);

	}

	public void notifyReceipt(String name, double time, double latency) {

		boolean contains = latencies.containsKey(name);
		if (!contains) { // first entry of a given message
			latencies.put(name, new Vector<Double>());
			deliveries.put(name, new Vector<Double>());
		}
		// adds new latency value to the list of the respective message
		latencies.get(name).add(new Double(latency));
		deliveries.get(name).add(new Double(time));

		if (monitor)
			textarea.setText(textarea.getText()
					+ System.getProperty("line.separator") + name + "," + time
					+ "," + latency);
	}

	public void notifyReceipt(String name, double latency) {

		boolean contains = latencies.containsKey(name);
		if (!contains) { // first entry of a given message
			latencies.put(name, new Vector<Double>());
			deliveries.put(name, new Vector<Double>());
		} else { // adds new latency value to the list of the respective message
			latencies.get(name).add(new Double(latency));
			deliveries.get(name).add(new Double(Double.NaN));

		}

	}

	@Override
	public String[] report() {
		return report(",");
	}

	public String[] report(String sep) {

		StringBuffer report = new StringBuffer();

		for (Enumeration<String> k = latencies.keys(); k.hasMoreElements();) {

			String name = k.nextElement();
			Vector<Double> v = latencies.get(name);

			double maxi = 0; // Double.MIN_VALUE;
			double mini = 0; // Double.MAX_VALUE;
			double sum = 0.0;
			int n = 0;

			for (Enumeration<Double> values = v.elements(); values
					.hasMoreElements();) {
				double cur = values.nextElement().doubleValue();
				if (maxi == 0) {
					maxi = cur;
				} else if (cur > maxi)
					maxi = cur;
				if (mini == 0) {
					mini = cur;
				} else if (cur < mini)
					mini = cur;
				sum = sum + cur;
				n = n + 1;

			}

			if (n > 0) {
				Double max = new Double(maxi);
				Double min = new Double(mini);
				Double avg = new Double(sum / n);

				String eol = System.getProperty("line.separator");
				report.append(name + sep + max.toString() + sep
						+ min.toString() + sep + avg.toString() + eol);
			}
		}

		String[] fullreport = { report.toString() };

		System.out.println(report.toString());

		return fullreport;

	}

	@Override
	public void reset() {
		latencies = new Hashtable();
		deliveries = new Hashtable();
		textarea.setText("");

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (arg0.getActionCommand() == "reset") {
			reset();
		} else if (arg0.getActionCommand() == "generate") {
			textarea.setText(report()[0]);
		} else if (arg0.getActionCommand() == "monitor") {
			monitor = !monitor;
		}

	}

	protected Hashtable<String, Vector<Double>> latencies, deliveries;
	protected JFrame frame;
	protected JTabbedPane tabbedPane;
	protected JTextArea textarea;
	protected JPanel commands;
	protected boolean monitor = false;

}
