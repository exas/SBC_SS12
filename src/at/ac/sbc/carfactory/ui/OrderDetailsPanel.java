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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarMotorType;
import at.ac.sbc.carfactory.ui.util.TableHeaders;

import at.ac.sbc.carfactory.domain.CarPartType;

public class OrderDetailsPanel extends JDialog {

	private static final long serialVersionUID = -3338193310548556725L;
	private long orderId;
	private CarFactoryUI parent;

	private JTable table = null;
	private DefaultTableModel tableModel;

	public OrderDetailsPanel(long orderId, CarFactoryUI parent) {
		this.orderId = orderId;
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

        this.setLocationRelativeTo(this.parent);
		this.setVisible(true);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.pack();
		this.validate();
	}

	private JTable initOrderTable() {
		if (table == null) {
			table = new JTable();
			tableModel = new DefaultTableModel(null, TableHeaders.orderHeaders);
			table.setModel(tableModel);
			//table.setModel(getMyTableModel());
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
		return table;
	}


}
