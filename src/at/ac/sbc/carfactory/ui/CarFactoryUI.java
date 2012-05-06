package at.ac.sbc.carfactory.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionListener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import at.ac.sbc.carfactory.ui.util.View;


import at.ac.sbc.carfactory.util.LogListener;

public class CarFactoryUI extends View implements LogListener, WindowListener {

	private static final long serialVersionUID = 986427844864093227L;
	private JTextArea loggerTextArea;
	private ProducerPanel producerPanel;
	//private ICarFactoryManager carFactoryManager;
	
	private JMenuItem createProducer;
	private JMenuItem closeApp;
	private JMenuItem showStatistics;

	// TODO: finish UI
	
	public CarFactoryUI() {
		//this.carFactoryManager = carFactoryManager;
		this.setTitle("Car-Factory");
		this.setSize(new Dimension(640, 480));
		
		this.addWindowListener(this);
		
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
		
		closeApp = new JMenuItem("Close App");
		// Alex: outsourced to Controller Class to achieve MVC Pattern
		//closeApp.addActionListener(this);
		
		createProducer = new JMenuItem("Create Producer");
		// Alex: outsourced to Controller Class to achieve MVC Pattern
		//createProducer.addActionListener(this);
		
		showStatistics = new JMenuItem("Show Statistics");
		// Alex: outsourced to Controller Class to achieve MVC Pattern
		//showStatistics.addActionListener(this);
		
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
	

//	public boolean assignWorkToProducer(long id, int numParts, CarPartEnum carPart) {
//		return this.carFactoryManager.assignWorkToProducer(numParts, carPart, id);
//	}

	@Override
	public void logMessageAdded(String message) {
		this.addLogMessage(message + "\n");
	}

	@Override
	public void windowActivated(WindowEvent e) { }

	@Override
	public void windowClosed(WindowEvent e) { }

	@Override
	public void windowClosing(WindowEvent e) {
		if (JOptionPane.OK_OPTION == getValidationDialogResult()){
			//TODO: close APP
			System.exit(0);
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) { }

	@Override
	public void windowDeiconified(WindowEvent e) { }

	@Override
	public void windowIconified(WindowEvent e) { }

	@Override
	public void windowOpened(WindowEvent e) { }

	
	public void addCreateProducerListener(ActionListener al) {
		createProducer.addActionListener(al);
    }
	
	public void addShowStatisticsListener(ActionListener al) {
		showStatistics.addActionListener(al);
    }
	
	public void addCloseAppListener(ActionListener al) {
		closeApp.addActionListener(al);
    }

	@Override
	public void closeView() {
		//close App
		System.exit(0);
	}

	@Override
	public void updateProducerPanel(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ProducerPanel getProducerPanel() {
		return this.producerPanel;
	}
    
}
