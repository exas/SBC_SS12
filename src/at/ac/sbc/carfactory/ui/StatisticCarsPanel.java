package at.ac.sbc.carfactory.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

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
		c.insets = new Insets(10, 10, 10, 10); c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0.60; c.gridwidth = 1;
		this.add(tableScrollPane, c);

		carInfoTextArea = new JTextArea();
		this.carInfoTextArea.setEditable(false);
		JScrollPane carInfoTextAreaScrollPane = new JScrollPane(this.carInfoTextArea);

		c.anchor = GridBagConstraints.LINE_START; c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(10, 10, 10, 10); c.gridx = 0; c.gridy = 1; c.weightx = 1; c.weighty = 0.40; c.gridwidth = 1;
		this.add(carInfoTextAreaScrollPane, c);
	}

	private JTable initStatisticsTable() {
		if (table == null) {
			table = new JTable();
			tableModel = new DefaultTableModel(null, TableHeaders.statisticFinishedCars) {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					//all cells false
				    return false;
			    }
			};

			table.setModel(tableModel);

			table.setModel(tableModel);

			TableColumn col = table.getColumnModel().getColumn( 0 );
			col.setMaxWidth(40);
			col.setMinWidth(40);

			col = table.getColumnModel().getColumn( 1 );
			col.setMaxWidth(40);
			col.setMinWidth(40);

			col = table.getColumnModel().getColumn( 2 );
			col.setMaxWidth(40);
			col.setMinWidth(40);

			col = table.getColumnModel().getColumn( 4 );
			col.setMaxWidth(65);
			col.setMinWidth(65);

			col = table.getColumnModel().getColumn( 5 );
			col.setMaxWidth(50);
			col.setMinWidth(50);

			col = table.getColumnModel().getColumn( 6 );
			col.setMaxWidth(50);
			col.setMinWidth(50);

			col = table.getColumnModel().getColumn( 7 );
			col.setMaxWidth(50);
			col.setMinWidth(50);

			col = table.getColumnModel().getColumn( 10 );
			col.setMaxWidth(30);
			col.setMinWidth(30);

			col = table.getColumnModel().getColumn( 11 );
			col.setMaxWidth(30);
			col.setMinWidth(30);

			table.addMouseListener(new MouseAdapter() {
				private void maybeShowInfo(MouseEvent e) {
					if (e.isPopupTrigger() && table.isEnabled()) {
						Point p = new Point(e.getX(), e.getY());
						//int col = table.columnAtPoint(p);
						int row = table.rowAtPoint(p);
						table.setRowSelectionInterval(row, row);

						// translate table index to model index
						//int mcol = table.getColumn(table.getColumnName(col)).getModelIndex();

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
		Long carID = (Long)table.getValueAt(rowIndex, 0);
		Long bodyID = (Long)table.getValueAt(rowIndex, 1);
		Long motorID = (Long)table.getValueAt(rowIndex, 2);
		String tireIDs = (String)table.getValueAt(rowIndex, 3);
		Long assemblerID = (Long)table.getValueAt(rowIndex, 4);
		Long logisticianID = (Long)table.getValueAt(rowIndex, 5);
		Long orderId = (Long)table.getValueAt(rowIndex, 6);
		Boolean isDefect = (Boolean)table.getValueAt(rowIndex, 7);
		Long tester1_allParts = (Long)table.getValueAt(rowIndex, 10);
		Long tester2_defectParts = (Long)table.getValueAt(rowIndex, 11);

		this.carInfoTextArea.setText("");
		//this.carInfoTextArea.append("Car [" + carID + "]");

		String line1_carText = "";
		String line2_carDetails = "\n";
//		String line3_logisticianText = "\n";
		String line4_carBodyText =  "\n    ";
		String line5_carMotorText = "\n    ";
		String line6_carTiresText = "\n    ";

		line1_carText += "Car [" + carID + "]: { CarParts: ";
		line2_carDetails += "CarDetails: { OrderId<"+orderId+">, isDefect<"+isDefect+"> Assembler["+assemblerID+"], ";

		if((logisticianID != null) && (logisticianID.equals("") == false)) {
			line2_carDetails += "Logistician[" + logisticianID + "], ";
		}
		line2_carDetails += "Tester1_AllParts:["+tester1_allParts+"], Tester2_DefectParts["+tester2_defectParts+"] }";


		CarBody carBody = (CarBody)this.parent.getCarPart(bodyID, CarPartType.CAR_BODY);
		if(carBody != null) {
			line1_carText += "Body["+bodyID+"], ";
			line4_carBodyText += "Body["+bodyID+"]: { isDefect: <"+carBody.isDefect() +"> Producer["+carBody.getProducerId()+"], ";

			//this.carInfoTextArea.append("\tBody: " + bodyID + "\n");
			//this.carInfoTextArea.append("\t\tProducer: " + carBody.getProducerId() + "\n");
			if(carBody.isPainted() == true) {
				line4_carBodyText += "Painter["+carBody.getPainterWorkerId()+"], Color: " + carBody.getColor() + " }";
				//this.carInfoTextArea.append("\t\tColor: " + carBody.getColor() + "\n");
				//this.carInfoTextArea.append("\t\tPainter: " + carBody.getPainterWorkerId() + "\n");
			}
		}
		CarMotor carMotor = (CarMotor)this.parent.getCarPart(motorID, CarPartType.CAR_MOTOR);
		if(carMotor != null) {
			line1_carText += "Motor["+motorID+"], ";

			line5_carMotorText += "Motor["+motorID+"]: { isDefect: <"+carMotor.isDefect() +"> Producer["+carMotor.getProducerId()+"], ";
			line5_carMotorText += "Type: "+carMotor.getMotorType()+ " }";

			//this.carInfoTextArea.append("\tMotor: " + motorID + "\n");
			//this.carInfoTextArea.append("\t\tProducer: " + carMotor.getProducerId() + "\n");
			//this.carInfoTextArea.append("\t\tType: " + carMotor.getMotorType() + "\n");
		}

		//this.carInfoTextArea.append("\tTires: \n");
		line6_carTiresText += "CarTires: {";
		String[] tireIDsArr = tireIDs.split(",");
		for(int i = 0; i < tireIDsArr.length; i++) {
			CarTire carTire = (CarTire)this.parent.getCarPart(Long.parseLong(tireIDsArr[i].trim()), CarPartType.CAR_TIRE);
			if(carTire != null) {
				line1_carText += "T["+carTire.getId()+"], ";
				line6_carTiresText += " T["+carTire.getId() +"]: {isDefect: <"+carTire.isDefect() +"> Prod["+carTire.getProducerId()+"]}, ";

				//this.carInfoTextArea.append("\t\tTire: " + carTire.getId() + "\n");
				//this.carInfoTextArea.append("\t\t\tProducer: " + carTire.getProducerId() + "\n");
			}
		}
		//delete last , and space
		line6_carTiresText = line6_carTiresText.substring(0,line6_carTiresText.length()-2);
		line6_carTiresText += " }\n";

		line1_carText = line1_carText.substring(0,line1_carText.length()-2);
		line1_carText += " }";




		//this.carInfoTextArea.append("\tAssembler: " + assemblerID + "\n");
		//line2_assemblerText += "Assembler["+assemblerID+"]";

//		if((logisticianID != null) && (logisticianID.equals("") == false)) {
//			//this.carInfoTextArea.append("\tLogistician: " + logisticianID + "\n");
//			line3_logisticianText += "Logistician["+logisticianID+"]";
//		}

		this.carInfoTextArea.append(line1_carText);
		this.carInfoTextArea.append(line2_carDetails);
		//this.carInfoTextArea.append(line3_logisticianText);
		this.carInfoTextArea.append(line4_carBodyText);
		this.carInfoTextArea.append(line5_carMotorText);
		this.carInfoTextArea.append(line6_carTiresText);

	}

	public void carUpdate(Car car) {
		int row = this.findCar(car);
		Object[] temp = new Object[12];
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
			temp[9] = true;
		}

		temp[6] = car.getOrderId();
		temp[7] = car.isDefect();
		temp[8] = car.isTestingFinished();
		temp[10] = car.getTesterAllPartsAssembledWorkerId();
		temp[11] = car.getTesterIsDefectWorkerId();

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
