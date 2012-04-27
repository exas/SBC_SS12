package at.ac.sbc.carfactory.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import at.ac.sbc.carfactory.application.ICarFactoryManager;
import at.ac.sbc.carfactory.util.LogListener;

public class CarFactoryUI extends JFrame implements LogListener {

	private static final long serialVersionUID = 986427844864093227L;
	private JTextArea loggerTextArea;
	private ProducerPanel producerPanel;
	private ICarFactoryManager carFactoryManager;

	// TODO: finish UI
	
	public CarFactoryUI(ICarFactoryManager carFactoryManager) {
		this.carFactoryManager = carFactoryManager;
		this.setTitle("Car-Factory");
		this.setSize(new Dimension(640, 480));
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (JOptionPane.OK_OPTION == getValidationDialogResult()){
					//TODO: close APP
				}
			}
			@Override
			public void windowClosed(WindowEvent e) {
				//TODO: close APP
			}
		});
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(640, 480));
		this.setVisible(true);
		this.initalizeMenu();
		this.initializeComponents();
		// just in case swing doesn't do it itself --> validate GUI so that it is shown correctly
		this.validate();
	}
	
	private void initalizeMenu() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");		
		
		JMenuItem closeApp = new JMenuItem("Close App");
		closeApp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.OK_OPTION == getValidationDialogResult()) {
					// TODO: close App
				}
			}
		});
		
		JMenuItem createProducer = new JMenuItem("Create Producer");
		createProducer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CarFactoryUI.this.createProducer();
			}
		});
		
		JMenuItem showStatistics = new JMenuItem("Show Statistics");
		showStatistics.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: call show statistic panel
			}
		});
		
		fileMenu.add(createProducer);
		fileMenu.add(showStatistics);
		fileMenu.addSeparator();
		fileMenu.add(closeApp);
		menuBar.add(fileMenu);
		
		this.setJMenuBar(menuBar);
	}
	
	private void initializeComponents() {
        // position elements
		GridBagConstraints c = new GridBagConstraints();

		this.producerPanel = new ProducerPanel(this);
		
		this.loggerTextArea = new JTextArea();
		this.loggerTextArea.setEditable(false);
		JScrollPane textAreaScrollPane = new JScrollPane(this.loggerTextArea);
		
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH; c.insets = new Insets(10,10,10,10);
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0.75; c.gridwidth = 1;
        this.add(this.producerPanel, c);
        c.gridx = 0; c.gridy = 1; c.weightx = 1; c.weighty = 0.25;
        this.add(textAreaScrollPane, c);
	}
	
	private int getValidationDialogResult() {
		return (JOptionPane.showConfirmDialog(
					this,
					"Really Quit?",
					"GUI",
					JOptionPane.OK_CANCEL_OPTION));
	}
	
	private void addLogMessage(String message) {
		this.loggerTextArea.append(message);
		//this.loggerTextArea.repaint();
	}
	
	public long createProducer() {
		long id = this.carFactoryManager.createProducer();
		// TODO: show confirmation dialog 
		if(id != -1) {
			this.producerPanel.addProducer(id);
		}
		return id;
	}

	@Override
	public void logMessageAdded(String message) {
		this.addLogMessage(message + "\n");
	}
}
