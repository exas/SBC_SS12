package at.ac.sbc.carfactory.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import at.ac.sbc.carfactory.domain.CarPartEnum;

public class AssignWorkPanel extends JDialog {

	private static final long serialVersionUID = -3338193310548556725L;
	private long producerID;

	public AssignWorkPanel(long producerID) {
		this.producerID = producerID;
		this.initializeComponents();
	}
	
	private void initializeComponents() {
        // position elements
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		JLabel producerLabel = new JLabel("Producer: " + this.producerID);

		final JComboBox carPartList = new JComboBox(CarPartEnum.values());
		carPartList.setSelectedIndex(1);
		
		Integer[] array = {1,2,3,4,5,6,7,8,9};
		final JComboBox numPartList = new JComboBox(array);
		numPartList.setSelectedIndex(1);
		
		JButton assignWorkBt = new JButton("AssignWork");
		assignWorkBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Number of parts: " + numPartList.getSelectedItem());
				System.out.println("Parts: " + carPartList.getSelectedItem());
				// TODO: call assignWork for producer
				// TODO: close Dialog
			}
		});
		
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH; c.insets = new Insets(10,10,10,10);
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0.75; c.gridwidth = 1;
        this.add(producerLabel, c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1; c.weighty = 0.75; c.gridwidth = 1;
        this.add(numPartList, c);
        c.gridx = 2; c.gridy = 0; c.weightx = 1; c.weighty = 0.75; c.gridwidth = 1;
        this.add(carPartList, c);
        c.gridx = 3; c.gridy = 0; c.weightx = 1; c.weighty = 0.75; c.gridwidth = 1;
        this.add(assignWorkBt, c);
        
		this.setVisible(true);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.pack();
		this.validate();
	}
}
