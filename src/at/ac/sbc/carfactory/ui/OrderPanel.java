package at.ac.sbc.carfactory.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import at.ac.sbc.carfactory.domain.Order;

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarMotorType;
import at.ac.sbc.carfactory.ui.util.TableHeaders;

public class OrderPanel extends JPanel {

	private static final long serialVersionUID = 2792120697879951769L;

	private JTable table = null;
	private DefaultTableModel tableModel;

	@SuppressWarnings("unused")
	private CarFactoryUI parent;

	private JButton createOrderBt;
	private JComboBox carMotorTypeList;
	private JComboBox carColorList;
	private JComboBox carCountList;

	public OrderPanel(CarFactoryUI parent) {
		this.parent = parent;
		this.initialize();
	}

	private void initialize() {
        this.setLayout(new GridBagLayout());
		this.initializeComponents();
	}

	private void initializeComponents() {
        // position elements
		GridBagConstraints c = new GridBagConstraints();

		this.initOrderTable();
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(table,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10,10,10,10);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0.8;
        c.gridwidth = 4;
        this.add(tableScrollPane, c);

        Integer[] array = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 50, 100, 200, 500, 1000 };

        carCountList = new JComboBox(array);
        carCountList.setSelectedIndex(0);

        CarMotorType[] carMotorTypes = new CarMotorType[CarMotorType.values().length];
		for (int i = 0; i < CarMotorType.values().length; i++) {
			carMotorTypes[i] = CarMotorType.values()[i];
		}
		carMotorTypeList = new JComboBox(carMotorTypes);
		carMotorTypeList.setSelectedIndex(0);

		CarColor[] carColor = new CarColor[CarColor.values().length];
		for (int i = 0; i < CarColor.values().length; i++) {
			carColor[i] = CarColor.values()[i];
		}
		carColorList = new JComboBox(carColor);
		carColorList.setSelectedIndex(0);

		createOrderBt = new JButton("Create Order!");

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10,10,10,10);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.8;
        c.weighty = 0;
        c.gridwidth = 1;
        this.add(carCountList, c);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.8;
        c.weighty = 0;
        c.gridwidth = 1;
        this.add(carMotorTypeList, c);
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0.8;
        c.weighty = 0;
        c.gridwidth = 1;
        this.add(carColorList, c);
        c.gridx = 3;
        c.gridy = 1;
        c.weightx = 0.8;
        c.weighty = 0;
        c.gridwidth = 1;
        this.add(createOrderBt, c);
	}

	private JTable initOrderTable() {
		if (table == null) {
			table = new JTable();
			tableModel = new DefaultTableModel(null, TableHeaders.orderHeaders);
			table.setModel(tableModel);
			//table.setModel(getMyTableModel());

			table.addMouseListener(new MouseAdapter() {
				private void maybeShowPopup(MouseEvent e) {
					if (e.isPopupTrigger() && table.isEnabled()) {
						Point p = new Point(e.getX(), e.getY());
						//int col = table.columnAtPoint(p);
						int row = table.rowAtPoint(p);
						table.setRowSelectionInterval(row, row);

						// translate table index to model index
						//int mcol = table.getColumn(table.getColumnName(col)).getModelIndex();

						if (row >= 0 && row < table.getRowCount()) {
							// create popup menu...
							JPopupMenu contextMenu = createContextMenu(row);

							// ... and show it
							if ((contextMenu != null) && (contextMenu.getComponentCount() > 0)) {
								contextMenu.show(table, p.x, p.y);
							}
						}
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {
					maybeShowPopup(e);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					maybeShowPopup(e);
				}
			});
		}

		this.table.getTableHeader().setDefaultRenderer(
				new DefaultTableCellRenderer() {
					private static final long serialVersionUID = -79265426L;

					@Override
					public Component getTableCellRendererComponent(
							 	JTable table, Object value, boolean isSelected,
							 	boolean hasFocus, int row, int column) {

						JTableHeader header = table.getTableHeader();
	                    setForeground(header.getForeground());
	                    setBackground(header.getBackground());
	                    setFont(header.getFont());

	                    setText(value == null ? "" : value.toString());
	                    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	                    setHorizontalAlignment(SwingConstants.CENTER);
	                    setHorizontalTextPosition(SwingConstants.LEFT);

	                    return this;
					}
				});

		// TODO: implement sorting method
		/* table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
			public void mouseClicked(MouseEvent evt) {
            	try {
            		getMyTableModel().sortByColumn(table.columnAtPoint(evt.getPoint()));
            	} catch (ArrayIndexOutOfBoundsException ex) {
            		// DO NOTHING
            	}
            }
        }); */
		return table;
	}

	private JPopupMenu createContextMenu(final int rowIndex) {
		JPopupMenu contextMenu = new JPopupMenu();

		JMenuItem orderDetailsMenuItem = new JMenuItem();
		orderDetailsMenuItem.setText("Order Details");
		orderDetailsMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long orderId = (Long)table.getValueAt(rowIndex, 0);
				new OrderDetailsPanel(orderId, OrderPanel.this.parent);
			}
		});

		contextMenu.add(orderDetailsMenuItem);

		return contextMenu;
	}

	public void addOrder(Order order) {
		Object[] row = new Object[4];
		row[0] = order.getId();
		row[1] = order.getCarAmount();
		row[2] = order.getCarMotorType();
		row[3] = order.getCarColor();
		this.tableModel.addRow(row);
		this.tableModel.fireTableDataChanged();
	}

//	public void removeOrder(long id) {
//		// TODO: remove producer
//		//this.tableModel.removeRow(row)
//		this.tableModel.fireTableDataChanged();
//	}

	public void addCreateOrderBtnListener(EventListener al) {
		this.createOrderBt.addActionListener((ActionListener) al);

	}

	public JComboBox getCarMotorTypeList() {
		return this.carMotorTypeList;
	}

	public JComboBox getCarColorList() {
		return this.carColorList;
	}

	public JComboBox getCarCountList() {
		return this.carCountList;
	}
}
