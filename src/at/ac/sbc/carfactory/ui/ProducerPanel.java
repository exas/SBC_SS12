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

import at.ac.sbc.carfactory.ui.util.TableHeaders;

public class ProducerPanel extends JPanel {

	private static final long serialVersionUID = 2792120697879951769L;

	private JTable table = null;
	private DefaultTableModel tableModel;
	private CarFactoryUI parent;
	private JButton createProducerBt;

	public ProducerPanel(CarFactoryUI parent) {
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

		this.initProducerTable();
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(table,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH; c.insets = new Insets(10,10,10,10);
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1.0; c.gridwidth = 1;
        this.add(tableScrollPane, c);

        createProducerBt = new JButton("Create Producer!");
        //Alex: added method addCreateProducerBtListener() so it is controlled by the Controller
//        createProducerBt.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				@SuppressWarnings("unused")
//				int a = 1;
//				a = 2;
//
//			}
//		});

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.NONE; c.insets = new Insets(10,10,10,10);
        c.gridx = 0; c.gridy = 1; c.weightx = 0.2; c.weighty = 0; c.gridwidth = 1;
        this.add(createProducerBt, c);
	}

	private JTable initProducerTable() {
		if (table == null) {
			table = new JTable();
			tableModel = new DefaultTableModel(null, TableHeaders.producerHeaders);
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

		JMenuItem deleteProducerMenuItem = new JMenuItem();
		deleteProducerMenuItem.setText("delete Producer");
		deleteProducerMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// getID
				table.getValueAt(rowIndex, 0);
				// TODO: call delete method
			}
		});

		JMenuItem assignWorkMenuItem = new JMenuItem();
		assignWorkMenuItem.setText("assign work");
		assignWorkMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long producerID = (Long)table.getValueAt(rowIndex, 0);
				new AssignWorkPanel(producerID, ProducerPanel.this.parent);
			}
		});

		contextMenu.add(deleteProducerMenuItem);
		contextMenu.add(assignWorkMenuItem);

		return contextMenu;
	}

	public void addProducer(long id) {
        JButton deleteProducerBt = new JButton("Delete");
        deleteProducerBt.setName(String.valueOf(id));
        deleteProducerBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: delete action
			}
		});
        JButton assignWorkBt = new JButton("assign work");
        assignWorkBt.setName(String.valueOf(id));

        assignWorkBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: assign work action
			}
		});

		Object[] temp = new Object[4];
		temp[0] = id;
		temp[1] = 0;
		temp[2] = deleteProducerBt;
		temp[3] = assignWorkBt;
		this.tableModel.addRow(temp);
		this.tableModel.fireTableDataChanged();
	}

//	public void removeProducer(long id) {
//		// TODO: remove producer
//		//this.tableModel.removeRow(row)
//		this.tableModel.fireTableDataChanged();
//	}

	public void addCreateProducerBtListener(EventListener al) {
		this.createProducerBt.addActionListener((ActionListener) al);
		//this.createProducerBt.addMouseListener((MouseListener) al);
    }



}
