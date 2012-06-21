package at.ac.sbc.carfactory.ui;

import org.apache.log4j.Logger;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.ui.util.Model;
import at.ac.sbc.carfactory.ui.util.View;


import at.ac.sbc.carfactory.util.DomainListener;
import at.ac.sbc.carfactory.util.LogListener;

public class CarFactoryUI extends View implements DomainListener, LogListener, WindowListener {

	private static final long serialVersionUID = 986427844864093227L;
	private JTextArea loggerTextArea;
	private ProducerPanel producerPanel;
	private OrderPanel orderPanel;
	private StatisticCarPartsPanel statisticCarPartsPanel;
	private StatisticCarsPanel statisticCarsPanel;

	private JMenuItem createProducer;
	private JMenuItem closeApp;
	private JMenuItem showStatistics;

	private Logger logger = Logger.getLogger(CarFactoryUI.class);

	//Should be not in VIEW ? in controller ref.?
	//carFactoryManager
	private Model model;

	// TODO: finish UI

	public CarFactoryUI(Model model) {
		this.model = model;
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

		JTabbedPane tabbedPane = new JTabbedPane();

		this.producerPanel = new ProducerPanel(this);
		this.orderPanel = new OrderPanel(this);
		this.statisticCarPartsPanel = new StatisticCarPartsPanel(this);
		this.statisticCarsPanel = new StatisticCarsPanel(this);
		tabbedPane.add("Producers", this.producerPanel);
		tabbedPane.add("Orders", this.orderPanel);
		tabbedPane.add("Statistics Car-Parts", this.statisticCarPartsPanel);
		tabbedPane.add("Statistics Cars", this.statisticCarsPanel);


		this.loggerTextArea = new JTextArea();
		this.loggerTextArea.setEditable(false);
		JScrollPane textAreaScrollPane = new JScrollPane(this.loggerTextArea);

        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH; c.insets = new Insets(10,10,10,10);
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0.75; c.gridwidth = 1;
        this.add(tabbedPane, c);
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


	public boolean assignWorkToProducer(long id, int numParts, Double errorRate, CarPartType carPartType) {
		return this.model.assignWorkToProducer(numParts, errorRate, carPartType, id);
	}

	public CarPart getCarPart(long carPartID, CarPartType carPartType) {
		return this.statisticCarPartsPanel.getCarPart(carPartID, carPartType);
	}

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
			if (model.shutdown())
				logger.info("Model Shutdown successfully.");
			else
				logger.info("Model could not shutdown properly.");

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

	@Override
	public void addCreateProducerListener(ActionListener al) {
		createProducer.addActionListener(al);
    }


	@Override
	public void addShowStatisticsListener(ActionListener al) {
		showStatistics.addActionListener(al);
    }


	@Override
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

	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public void carPartUpdated(CarPart part, boolean finished) {
		this.statisticCarPartsPanel.carPartUpdate(part, finished);
	}

	@Override
	public void carUpdated(Car car) {
		this.statisticCarsPanel.carUpdate(car);
	}

	@Override
	public OrderPanel getOrderPanel() {
		return this.orderPanel;
	}

}
