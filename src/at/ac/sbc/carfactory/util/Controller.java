package at.ac.sbc.carfactory.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.ui.util.Model;
import at.ac.sbc.carfactory.ui.util.View;


//has reference to model (= CarFactoryManager) and view (UI)
public class Controller {
	private View view; //CarFactoryUI
	private Model model;  //CarFactoryManager
	private Logger logger = Logger.getLogger(Controller.class);

	public Controller(Model model, View view) {
		this.model = model;
		this.view = view;
		
		this.view.addCreateProducerListener(new CreateProducerListener());
		this.view.addCloseAppListener(new CloseAppListener());
		this.view.addShowStatisticsListener(new ShowStatisticsListener());
		
		this.view.getProducerPanel().addCreateProducerBtListener(new CreateProducerBtListener());
		
	}
	
	//////////////////////////////////////////inner class CreateProducerListener
	/** When createProducer is pressed.
	* If there was an error, tell the View to display it.
	*/
	class CreateProducerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			logger.debug("createProducer1 Action");
			long id = model.createProducer();
			
			//TODO: show confirmation dialog?
			
			if (id != -1) {
				//update view
				view.getProducerPanel().addProducer(id);
				view.getProducerPanel().repaint();
			}
		}
	}//end inner class CreateProducerListener
	
	
	//////////////////////////////////////////inner class ShowStatisticsListener
	/** When showStatistics Button is pressed.
	* If there was an error, tell the View to display it.
	*/
	class ShowStatisticsListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//TODO Perform action
		}
	}//end inner class ShowStatisticsListener
	
	
	//////////////////////////////////////////inner class CloseAppListener
	/** When close App is pressed.
	* If there was an error, tell the View to display it.
	*/
	class CloseAppListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			logger.debug("Stopping Embedded JMS Server");
			//stop JMS Server
			JMSServer.getInstance().stop();
			
			logger.debug("Closing View");
			view.closeView();
		}
	}//end inner class CloseAppListener
	
	//////////////////////////////////////////inner class ProducerPanel: CreateProducerBtListener
	/** When ProducerPanel:createProducerBt is pressed.
	* If there was an error, tell the View to display it.
	*/
	class CreateProducerBtListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			logger.debug("createProducer2 Action");
			long id = model.createProducer();
			
			//TODO: show confirmation dialog?
			
			if (id != -1) {
				//update view
				view.getProducerPanel().addProducer(id);
			}
		}

		
	}//end inner class ProducerPanel: CreateProducerBtListener
	
	
//	assignWorkBt.addActionListener(new ActionListener() {
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			if (AssignWorkPanel.this.parent.assignWorkToProducer(AssignWorkPanel.this.producerID,
//					(Integer) numPartList.getSelectedItem(), (CarPartType) carPartList.getSelectedItem()) == false) {
//				// TODO : showDialog
//			}
//			else {
//				AssignWorkPanel.this.dispose();
//			}
//		}
//	});
	
}
