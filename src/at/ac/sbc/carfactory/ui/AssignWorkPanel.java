package at.ac.sbc.carfactory.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import at.ac.sbc.carfactory.domain.CarPartType;

public class AssignWorkPanel extends JDialog {

	private static final long serialVersionUID = -3338193310548556725L;
	private long producerID;
	private CarFactoryUI parent;

	private JFormattedTextField textFieldErrorRate;
	private NumberFormat percentDisplayFormat;
	private NumberFormat percentEditFormat;

	public AssignWorkPanel(long producerID, CarFactoryUI parent) {
		this.producerID = producerID;
		this.parent = parent;
		this.initializeComponents();



	}

	private void initializeComponents() {

		// position elements
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		Locale locale = Locale.getDefault();

		percentDisplayFormat = NumberFormat.getPercentInstance(locale);
		percentDisplayFormat.setMinimumFractionDigits(2);
		percentDisplayFormat.setMaximumFractionDigits(2);
		percentDisplayFormat.setMaximumIntegerDigits(3);
		percentDisplayFormat.setMinimumIntegerDigits(1);

		percentEditFormat = NumberFormat.getNumberInstance();
		percentEditFormat.setMinimumFractionDigits(2);
		percentEditFormat.setMaximumFractionDigits(2);
		percentEditFormat.setMaximumIntegerDigits(3);
		percentEditFormat.setMinimumIntegerDigits(1);


		JLabel producerLabel = new JLabel("Producer: " + this.producerID);

		CarPartType[] carPartTypes = new CarPartType[3];
		for (int i = 0; i < CarPartType.values().length; i++) {
			if (CarPartType.values()[i].equals(CarPartType.CAR) == false) {
				carPartTypes[i] = CarPartType.values()[i];
			}
		}
		final JComboBox carPartList = new JComboBox(carPartTypes);
		carPartList.setSelectedIndex(0);

		Integer[] array = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 2000, 8000 };

		final JComboBox numPartList = new JComboBox(array);
		numPartList.setSelectedIndex(0);

		JButton assignWorkBt = new JButton("AssignWork");
		// TODO ?? really necessary? track dialogs via CarFactoryUI so we can
		// access them from outside (-> Controller)!

		NumberFormatter percentEditFormatter =
                new NumberFormatter(percentEditFormat) {
					private static final long serialVersionUID = 1L;
			@Override
			public String valueToString(Object o)
                  throws ParseException {
                Number number = (Number)o;
                if (number != null) {
                    double d = number.doubleValue() * 100.0;
                    number = new Double(d);
                }
                return super.valueToString(number);
            }
            @Override
            public Object stringToValue(String s)
                   throws ParseException {
                Number number = (Number)super.stringToValue(s);
                if (number != null) {

                	//limit to 100 %
                	if(number.doubleValue() >= 100)
                		number = new Double(100);

                	if(number.doubleValue() <= 0)
                		number = new Double(0);

                    double d = number.doubleValue() / 100.0;
                    number = new Double(d);
                }
                return number;
            }
        };

		textFieldErrorRate = new JFormattedTextField(
                new DefaultFormatterFactory(
                        new NumberFormatter(percentDisplayFormat),
                        new NumberFormatter(percentDisplayFormat),
                        percentEditFormatter));

		textFieldErrorRate.setValue(new Float(.15));
		textFieldErrorRate.setColumns(5);
		// textFieldErrorRate.addPropertyChangeListener("value", this);

		JLabel errorRateLabel = new JLabel("ErrRate %:");

		assignWorkBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (AssignWorkPanel.this.parent.assignWorkToProducer(
						AssignWorkPanel.this.producerID,
						(Integer) numPartList.getSelectedItem(),
						(Double) textFieldErrorRate.getValue(),
						(CarPartType) carPartList.getSelectedItem()) == false) {
					// TODO : showDialog
				} else {
					AssignWorkPanel.this.dispose();
				}
			}
		});

		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0.75;
		c.gridwidth = 1;
		this.add(producerLabel, c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0.75;
		c.gridwidth = 1;
		this.add(numPartList, c);
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0.75;
		c.gridwidth = 1;
		this.add(carPartList, c);
		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0.75;
		c.gridwidth = 1;
		this.add(errorRateLabel, c);
		c.gridx = 4;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0.75;
		c.gridwidth = 1;
		this.add(textFieldErrorRate, c);
		c.gridx = 5;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0.75;
		c.gridwidth = 1;
		this.add(assignWorkBt, c);

		this.setLocationRelativeTo(this.parent);
		this.setVisible(true);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.pack();
		this.validate();
	}
}
