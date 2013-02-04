//Copyright (c) 2009 Luciano Ost

package lsi.noc.mapperscope;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.text.TableView.TableRow;
import javax.swing.JTable;

import lsi.noc.application.Producer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.vergil.uml.Lifeline;

public class TableModel extends AbstractTableModel {

	private boolean DEBUG = false;
	private Hashtable _lifelines;
	protected List producers_;
	private String producer_name;
	protected Producer p;
	protected Lifeline l;
	protected int numberofElementes;

	/*
	 * private String[] columnNames = {"Producers", "Task Name", "Application",
	 * "# of interactions", "Message received from"};
	 */
	protected String[] columnNames = { "Producers", "Lifeline Name" };
	// "Lifeline Type"};

	// This should be fixed (have to be dinamic)
	public Object[][] table_data = new Object[36][36];

	// protected Object[][] table_data = new Object[numberofElementes =
	// _lifelines.size()][numberofElementes = _lifelines.size()];

	public TableModel(Hashtable _lifelinesProducer)
			throws IllegalActionException {
		_lifelines = _lifelinesProducer;

		Enumeration plataform_elements = _lifelines.keys();
		numberofElementes = _lifelines.size();
		// System.out.println("numberofElementes" + numberofElementes);
		int ind = 0;

		while (plataform_elements.hasMoreElements()) {

			l = (Lifeline) plataform_elements.nextElement();
			// System.out.println("Lifeline" + l.getUMLName() );

			p = (Producer) _lifelines.get(l);
			// System.out.println("Lifeline " + l.getUMLName() +
			// " mapped at Producer" + p.getAddressX()+ p.getAddressY());

			setValueAt(
					new String("Producer" + p.getAddressX() + p.getAddressY()),
					ind, 0);
			setValueAt(new String(l.getUMLName()), ind, 1);

			// System.out.println("Added ");
			// used to increase the in
			ind++;
		}
	}

	/**
	 * Gets all producers from the workspace
	 * 
	 * @return
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */

	protected List getproducers_() throws IllegalActionException,
			NameDuplicationException {

		Nameable container = getContainer();

		return ((CompositeEntity) container).entityList(Producer.class);

	}

	private Nameable getContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return table_data.length;
		// return numberofElementes = _lifelines.size();
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return table_data[row][col];
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column would
	 * contain text ("true"/"false"), rather than a check box.
	 */
	public Class getColumnClass(int c) {
		return (table_data[0][c]).getClass();
	}

	/*
	 * This method allows table's table_data can change.
	 */
	public void setValueAt(Object value, int row, int col) {
		if (DEBUG) {
			System.out.println("Setting value at " + row + "," + col + " to "
					+ value + " (an instance of " + value.getClass() + ")");
		}

		table_data[row][col] = value;
		fireTableCellUpdated(row, col);

		/*
		 * if (DEBUG) { System.out.println("New value of table_data:");
		 * printDebugData(); }
		 */
	}

}
