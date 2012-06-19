package at.ac.sbc.carfactory.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import at.ac.sbc.carfactory.domain.CarColor;

import at.ac.sbc.carfactory.domain.CarMotorType;

import at.ac.sbc.carfactory.domain.Order;

import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.ui.util.Model;
import at.ac.sbc.carfactory.ui.util.View;

//has reference to model (= CarFactoryManager) and view (UI)
public class Controller {
	private View view; // CarFactoryUI
	private Model model; // CarFactoryManager
	private Logger logger = Logger.getLogger(Controller.class);

	public Controller(Model model, View view) {
		this.model = model;
		this.view = view;

		this.view.addCreateProducerListener(new CreateProducerListener());
		this.view.addCloseAppListener(new CloseAppListener());
		this.view.addShowStatisticsListener(new ShowStatisticsListener());

		this.view.getProducerPanel().addCreateProducerBtListener(
				new CreateProducerBtListener());

		this.view.getOrderPanel().addCreateOrderBtnListener(
				new CreateOrderBtnListener());

	}

	// ////////////////////////////////////////inner class
	// CreateProducerListener
	/**
	 * When createProducer is pressed. If there was an error, tell the View to
	 * display it.
	 */
	class CreateProducerListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			logger.debug("createProducer1 Action");
			long id = model.createProducer();

			// TODO: show confirmation dialog?

			if (id != -1) {
				// update view
				view.getProducerPanel().addProducer(id);
				view.getProducerPanel().repaint();
			}
		}
	}// end inner class CreateProducerListener

	// ////////////////////////////////////////inner class
	// ShowStatisticsListener
	/**
	 * When showStatistics Button is pressed. If there was an error, tell the
	 * View to display it.
	 */
	class ShowStatisticsListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Perform action
		}
	}// end inner class ShowStatisticsListener

	// ////////////////////////////////////////inner class CloseAppListener
	/**
	 * When close App is pressed. If there was an error, tell the View to
	 * display it.
	 */
	class CloseAppListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			if (model.shutdown())
				logger.info("Model Shutdown successfully.");
			else
				logger.info("Model could not shutdown properly.");

			logger.debug("Closing View properly.");
			view.closeView();
			logger.debug("Closed View.");
		}
	}// end inner class CloseAppListener

	// ////////////////////////////////////////inner class ProducerPanel:
	// CreateProducerBtListener
	/**
	 * When ProducerPanel:createProducerBt is pressed. If there was an error,
	 * tell the View to display it.
	 */
	class CreateProducerBtListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			logger.debug("createProducer2 Action");
			long id = model.createProducer();

			// TODO: show confirmation dialog?

			if (id != -1) {
				// update view
				view.getProducerPanel().addProducer(id);
			}
		}

	}// end inner class ProducerPanel: CreateProducerBtListener

	// ////////////////////////////////////////inner class OrderPanel:
	// CreateORderBtnListener
	/**
	 * When ProducerPanel:createProducerBt is pressed. If there was an error,
	 * tell the View to display it.
	 */
	class CreateOrderBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			logger.debug("createOrder Action");

			CarColor carColor = (CarColor)view.getOrderPanel().getCarColorList().getSelectedItem();
			CarMotorType carMotorType = (CarMotorType)view.getOrderPanel().getCarMotorTypeList().getSelectedItem();
			Integer carAmount = (Integer)view.getOrderPanel().getCarCountList().getSelectedItem();

			Order order = model.createOrder(carAmount, carMotorType, carColor);

			// TODO: show confirmation dialog?

			if (order.getId() != -1) {
				// update view
				view.getOrderPanel().addOrder(order);
			}
		}

	}// end inner class OrderPanel: CreateOrderBntListener

	// assignWorkBt.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// if
	// (AssignWorkPanel.this.parent.assignWorkToProducer(AssignWorkPanel.this.producerID,
	// (Integer) numPartList.getSelectedItem(), (CarPartType)
	// carPartList.getSelectedItem()) == false) {
	// // TODO : showDialog
	// }
	// else {
	// AssignWorkPanel.this.dispose();
	// }
	// }
	// });

}
