package at.ac.sbc.carfactory.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.ui.util.TableHeaders;

public class StatisticCarsPanel extends JPanel {

	private static final long serialVersionUID = 1131888264012626039L;
	private JTable table = null;
	private DefaultTableModel tableModel;
	private CarFactoryUI parent;

	public StatisticCarsPanel(CarFactoryUI parent) {
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
			tableModel = new DefaultTableModel(null, TableHeaders.statisticFinishedCars);
			table.setModel(tableModel);
		}
		return table;
	}

	public void carUpdate(Car car) {
		int row = this.findCar(car);
		Object[] temp = new Object[7];
		temp[0] = car.getId();
		temp[1] = car.getBody().getId();
		temp[2] = car.getMotor().getId();
		temp[3] = "";
		for(int i = 0; i < car.getTires().size(); i++) {
			if (i == (car.getTires().size() - 1)) {
				temp[3] = (String)temp[3] + car.getTires().get(i).getId();
			}
			temp[3] = (String)temp[3] + car.getTires().get(i).getId() + ", ";
		}
		
		temp[4] = car.getAssemblyWorkerId();
		temp[5] = car.getLogisticWorkerId();
		if(temp[5] != null) {
			temp[6] = true;
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

	private int findCar(Car car) {
		int nRow = tableModel.getRowCount();
		for (int i = 0; i < nRow; i++) {
			if ((Long) tableModel.getValueAt(i, 0) == car.getId()) {
				return i;
			}
		}
		return -1;
	}
}
