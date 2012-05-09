package at.ac.sbc.carfactory.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.ui.util.TableHeaders;

public class StatisticCarPartsPanel extends JPanel {

	private static final long serialVersionUID = -7357480927058456370L;
	private JTable table = null;
	private DefaultTableModel tableModel;
	private CarFactoryUI parent;

	public StatisticCarPartsPanel(CarFactoryUI parent) {
		this.parent = parent;
		this.initialize();
	}

	private void initialize() {
		this.setLayout(new GridBagLayout());
		this.initializeComponents();
	}

	private void initializeComponents() {
		GridBagConstraints c = new GridBagConstraints();

		this.initStatisticsTable();
		JScrollPane tableScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1.0;
		c.gridwidth = 1;
		this.add(tableScrollPane, c);
	}

	private JTable initStatisticsTable() {
		if (table == null) {
			table = new JTable();
			tableModel = new DefaultTableModel(null, TableHeaders.statisticCarParts);
			table.setModel(tableModel);
		}
		return table;
	}

	public void carPartUpdate(CarPart part, boolean finished) {
		int row = this.findCarPart(part);
		Object[] temp = new Object[4];
		temp[0] = part.getId();
		temp[1] = part.getCarPartType();
		temp[2] = part.getProducerId();
		temp[3] = null;
		temp[4] = null;
		try {
			if (Class.forName(CarBody.class.getName()).isInstance(part)) {
				if(((CarBody)part).isPainted() == true) {
					temp[3] = "Painted: " + ((CarBody)part).getPainterWorkerId() + " " + ((CarBody)part).getColor();
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(finished == true) {
			temp[4] = "finished";
		}
		
		if(row == -1) {
			this.tableModel.addRow(temp);
			this.tableModel.fireTableDataChanged();
		}
		else {
			for(int i = 0; i < this.tableModel.getColumnCount(); i++) {
				this.tableModel.setValueAt(temp[i], row, i);
			}
			this.tableModel.fireTableDataChanged();
		}
		

	}

	private int findCarPart(CarPart part) {
		int nRow = tableModel.getRowCount();
		//int nCol = tableModel.getColumnCount();
		//Object[][] tableData = new Object[nRow][nCol];
		for (int i = 0; i < nRow; i++) {
			if (((Long) tableModel.getValueAt(i, 0) == part.getId())
					&& (((CarPartType) tableModel.getValueAt(i, 1)).equals(part.getCarPartType()))) {
				return i;
			}
			/*
			 * for (int j = 0 ; j < nCol ; j++) { tableData[i][j] =
			 * tableModel.getValueAt(i,j); }
			 */
		}
		return -1;
	}
}
