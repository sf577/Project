//Copyright (c) 2009 Luciano Ost
package lsi.noc.mapperscope;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import ptolemy.kernel.util.IllegalActionException;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Hashtable;

public class TableMapper extends JPanel {

	private Hashtable _lifelinesProducer;

	public TableMapper(Hashtable lifelinesProducer_)
			throws IllegalActionException {
		super(new GridLayout(1, 0));

		_lifelinesProducer = lifelinesProducer_;

		// System.out.println("mapping table" + _lifelinesProducer);

		JTable table = new JTable(new TableModel(_lifelinesProducer));
		table.setPreferredScrollableViewportSize(new Dimension(500, 200));
		table.setFillsViewportHeight(true);

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Add the scroll pane to this panel.
		add(scrollPane);
	}
}
