package at.ac.sbc.carfactory.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import at.ac.sbc.carfactory.domain.CarMotorType;

import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.domain.CarTire;
import at.ac.sbc.carfactory.ui.util.TableHeaders;

public class StatisticCarPartsPanel extends JPanel {

	private static final long serialVersionUID = -7357480927058456370L;
	private JTable table = null;
	private DefaultTableModel tableModel;

	@SuppressWarnings("unused")
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
			tableModel = new DefaultTableModel(null, TableHeaders.statisticCarParts){

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					//all cells false
				    return false;
			    }
			};

			table.setModel(tableModel);

			TableColumn col = table.getColumnModel().getColumn( 0 );
			col.setMaxWidth(60);
			col.setMinWidth(60);

			col = table.getColumnModel().getColumn( 1 );
			col.setMaxWidth(100);
			col.setMinWidth(100);

			col = table.getColumnModel().getColumn( 2 );
			col.setMaxWidth(50);

			col = table.getColumnModel().getColumn( 4 );
			col.setMaxWidth(50);

			col = table.getColumnModel().getColumn( 5 );
			col.setMaxWidth(50);

			col = table.getColumnModel().getColumn( 6 );
			col.setMaxWidth(80);
		}
		return table;
	}

	public void carPartUpdate(CarPart part, boolean finished) {
		int row = this.findCarPart(part);
		Object[] temp = new Object[7];
		temp[0] = part.getId();
		temp[1] = part.getCarPartType();
		temp[2] = part.getProducerId();
		temp[3] = null;
		temp[4] = part.getOrderId();
		temp[5] = part.isDefect();
		temp[6] = null;
		try {
			if (Class.forName(CarBody.class.getName()).isInstance(part)) {
				if(((CarBody)part).isPainted() == true) {
					temp[3] = "Painted: " + ((CarBody)part).getPainterWorkerId() + " " + ((CarBody)part).getColor();
				}
			}

			if (Class.forName(CarMotor.class.getName()).isInstance(part)) {
				if( ((CarMotor)part).getMotorType() != null)
					temp[3] = "Motor_Type:" + ((CarMotor)part).getMotorType().toString();
				else
					temp[3] = "Motor_Type: n/a";
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(finished == true) {
			temp[6] = "finished";
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

	public CarPart getCarPart(long carPartId, CarPartType carPartType) {
		int carPartRow = this.findCarPart(carPartId, carPartType);
		if(carPartRow != -1) {
			CarPart carPart = null;
			switch(carPartType) {
				case CAR_MOTOR:
					carPart = new CarMotor();
					carPart.setId(carPartId);
					carPart.setCarPartType(carPartType);
					carPart.setProducerId((Long)table.getValueAt(carPartRow, 2));
					carPart.setOrderId((Long)table.getValueAt(carPartRow,4));
					carPart.setDefect((Boolean)table.getValueAt(carPartRow,5));
					String motorString = (String)table.getValueAt(carPartRow, 3);
					if((motorString != null) && (motorString.equals("") == false)) {
						String[] motorStringArr = motorString.split(":");

						((CarMotor)carPart).setMotorType(CarMotorType.valueOf(motorStringArr[1].trim()));
					}


					break;
				case CAR_BODY:
					carPart = new CarBody();
					carPart.setId(carPartId);
					carPart.setCarPartType(carPartType);
					carPart.setProducerId((Long)table.getValueAt(carPartRow, 2));
					String paintedString = (String)table.getValueAt(carPartRow, 3);
					if((paintedString != null) && (paintedString.equals("") == false)) {
						String[] painted = paintedString.split(" ");
						((CarBody)carPart).setPainterWorkerId(Long.valueOf(painted[1].trim()));
						((CarBody)carPart).setColor(CarColor.valueOf(painted[2].trim()));
					}
					carPart.setOrderId((Long)table.getValueAt(carPartRow,4));
					carPart.setDefect((Boolean)table.getValueAt(carPartRow,5));
					break;
				case CAR_TIRE:
					carPart = new CarTire();
					carPart.setId(carPartId);
					carPart.setCarPartType(carPartType);
					carPart.setProducerId((Long)table.getValueAt(carPartRow, 2));
					carPart.setOrderId((Long)table.getValueAt(carPartRow,4));
					carPart.setDefect((Boolean)table.getValueAt(carPartRow,5));
					break;
				default:
					//DO NOTHING
			}
			return carPart;
		}
		return null;
	}

	private int findCarPart(CarPart part) {
		return this.findCarPart(part.getId(), part.getCarPartType());
	}

	private int findCarPart(long carPartId, CarPartType carPartType) {
		int nRow = tableModel.getRowCount();
		//int nCol = tableModel.getColumnCount();
		//Object[][] tableData = new Object[nRow][nCol];
		for (int i = 0; i < nRow; i++) {
			if (((Long) tableModel.getValueAt(i, 0) == carPartId)
					&& (((CarPartType) tableModel.getValueAt(i, 1)).equals(carPartType))) {
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
