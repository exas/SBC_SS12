package at.ac.sbc.carfactory.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.domain.CarTire;
import at.ac.sbc.carfactory.ui.util.TableHeaders;

public class StatisticCarsPanel extends JPanel {

	private static final long serialVersionUID = 1131888264012626039L;
	private JTable table = null;
	private DefaultTableModel tableModel;
	private JTextArea carInfoTextArea;
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

		c.anchor = GridBagConstraints.LINE_START; c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(10, 10, 10, 10); c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0.7; c.gridwidth = 1;
		this.add(tableScrollPane, c);
		
		carInfoTextArea = new JTextArea();
		this.carInfoTextArea.setEditable(false);
		JScrollPane carInfoTextAreaScrollPane = new JScrollPane(this.carInfoTextArea);
		
		c.anchor = GridBagConstraints.LINE_START; c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(10, 10, 10, 10); c.gridx = 0; c.gridy = 1; c.weightx = 1; c.weighty = 0.3; c.gridwidth = 1;
		this.add(carInfoTextAreaScrollPane, c);
	}

	private JTable initStatisticsTable() {
		if (table == null) {
			table = new JTable();
			tableModel = new DefaultTableModel(null, TableHeaders.statisticFinishedCars);
			table.setModel(tableModel);
			
			table.addMouseListener(new MouseAdapter() {
				private void maybeShowInfo(MouseEvent e) {
					if (e.isPopupTrigger() && table.isEnabled()) {
						Point p = new Point(e.getX(), e.getY());
						int col = table.columnAtPoint(p);
						int row = table.rowAtPoint(p);
						table.setRowSelectionInterval(row, row);

						// translate table index to model index
						int mcol = table.getColumn(table.getColumnName(col)).getModelIndex();
			
						if (row >= 0 && row < table.getRowCount()) {
							// update info area
							updateCarInfoArea(row);
						}
					}
				}
	
				@Override
				public void mousePressed(MouseEvent e) {
					maybeShowInfo(e);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					maybeShowInfo(e);
				}
			});
		}
		return table;
	}
	
	public void updateCarInfoArea(int rowIndex) {
		int carID = (Integer)table.getValueAt(rowIndex, 0);
		int bodyID = (Integer)table.getValueAt(rowIndex, 1);
		int motorID = (Integer)table.getValueAt(rowIndex, 2);
		String tireIDs = (String)table.getValueAt(rowIndex, 3);
		int assemblerID = (Integer)table.getValueAt(rowIndex, 4);
		String logisticianID = (String)table.getValueAt(rowIndex, 5);
		
		this.carInfoTextArea.setText("");
		this.carInfoTextArea.append("Car: " + carID + "\n");
		
		CarBody carBody = (CarBody)this.parent.getCarPart(bodyID, CarPartType.CAR_BODY);
		if(carBody != null) {
			this.carInfoTextArea.append("\tBody: " + bodyID + "\n");
			this.carInfoTextArea.append("\t\tProducer: " + carBody.getProducerId() + "\n");
			if(carBody.isPainted() == true) {
				this.carInfoTextArea.append("\t\tColor: " + carBody.getColor() + "\n");
				this.carInfoTextArea.append("\t\tPainter: " + carBody.getPainterWorkerId() + "\n");
			}
		}
		CarMotor carMotor = (CarMotor)this.parent.getCarPart(motorID, CarPartType.CAR_MOTOR);
		if(carMotor != null) {
			this.carInfoTextArea.append("\tMotor: " + motorID + "\n");
			this.carInfoTextArea.append("\t\tProducer: " + carMotor.getProducerId() + "\n");
			this.carInfoTextArea.append("\t\tType: " + carMotor.getMotorType() + "\n");
		}
		
		this.carInfoTextArea.append("\tTires: \n");
		String[] tireIDsArr = tireIDs.split(",");
		for(int i = 0; i < tireIDsArr.length; i++) {
			CarTire carTire = (CarTire)this.parent.getCarPart(Long.parseLong(tireIDsArr[i]), CarPartType.CAR_TIRE);
			if(carTire != null) {
				this.carInfoTextArea.append("\t\tTire: " + tireIDsArr[i] + "\n");
				this.carInfoTextArea.append("\t\t\tProducer: " + carTire.getProducerId() + "\n");
			}
		}
		
		this.carInfoTextArea.append("\tAssembler: " + assemblerID + "\n");
		
		if((logisticianID != null) && (logisticianID.equals("") == false)) {
			this.carInfoTextArea.append("\tLogistician: " + logisticianID + "\n");
		}

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
			else {
				temp[3] = (String)temp[3] + car.getTires().get(i).getId() + ", ";
			}
		}
		
		temp[4] = car.getAssemblyWorkerId();
		temp[5] = car.getLogisticWorkerId();
		if(temp[5] != null) {
			temp[6] = true;
		}
		if(row == -1) {
			System.out.println("DIDN'T FIND CAR " + car.getId());
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
			if (((Long) tableModel.getValueAt(i, 0)).equals(car.getId())) {
				return i;
			}
		}
		return -1;
	}
}
